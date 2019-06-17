package ru.mephi.cloud.ml.deploying.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mephi.cloud.ml.deploying.config.ConfigSwarm;
import ru.mephi.cloud.ml.deploying.model.*;
import ru.mephi.cloud.ml.deploying.repository.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by kir on 06.04.19.
 */
@Service
public class ApiServiceImpl implements ApiService {
    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private ModelRepo modelRepo;

    @Autowired
    private ImageContainerRepo imageContainerRepo;

    @Autowired
    private DeploymentRepo deploymentRepo;

    @Autowired
    private VersionRepo versionRepo;

    @Autowired
    private ConfigSwarm configSwarm;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void run(String name, String tag, String replicas, String ram, String cpu, String username) throws InterruptedException, IOException {
        List<ImageContainer> imageContainers = imageContainerRepo.findByNameAndTag(name, tag);
        if (!imageContainers.isEmpty()) {
            String port = imageContainers.get(0).getPort();
            pullAndRun(name, tag, port, replicas, ram, cpu);
            Model model = new Model(name);
            List<Client> clients = clientRepo.findByUsername(username);
            if (!clients.isEmpty()) {
                Client client = clients.get(0);
                model.getClients().add(client);
                client.getModels().add(model);
                modelRepo.save(new Model(name));
                Deployment deployment = new Deployment(Integer.valueOf(replicas), Double.valueOf(cpu), Integer.valueOf(ram));
                deployment.getVersions().add(new Version(model, tag, port));
                deploymentRepo.save(deployment);
            }
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void build(String username, String role, String port, String name, MultipartFile zip) throws InterruptedException, IOException {
        List<ImageContainer> imageContainers = imageContainerRepo.findByName(name);
        String tag = "";
        if (imageContainers.isEmpty()) {
            tag = name + ".1";
        } else {
            tag = name + "." + String.valueOf(imageContainers.size() + 1);
        }
        pushImage(username, role, port, zip, name, tag);
        ImageContainer imageContainer = new ImageContainer(name, tag, port);
        imageContainer.setClient(clientRepo.findByUsername(username).get(0));
        imageContainerRepo.save(imageContainer);
    }

    private void pullAndRun(String name, String tag, String port, String replicas, String ram, String cpu) throws IOException, InterruptedException {
        // подключение к manager ноде кластера и pull образа из репозитория
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(configSwarm.getFingerprint());
        ssh.connect(configSwarm.getManagerNode());
        ssh.authPassword(configSwarm.getUsernameSwarm(), configSwarm.getPasswordSwarm());
        Session session = ssh.startSession();
        Session.Command cmd = session.exec("docker service create -p " + port + ":" + port + " --replicas " + replicas + " --limit-cpu " + cpu + " --limit-memory " + ram + " --name " + name + " " + configSwarm.getDockerHubRepository() + name + ":" + tag);
        System.out.println(net.schmizz.sshj.common.IOUtils.readFully(cmd.getInputStream()).toString());
        session.close();
        ssh.disconnect();
    }


    private void pushImage(String username, String role, String port, MultipartFile zip, String name, String tag) throws IOException, InterruptedException {
        String pathZip = configSwarm.getZipDirectory() + "/" + zip.getOriginalFilename();
        // Распаковка файлов в склад директорию
        zip.transferTo(new File(pathZip));
        String sklad_directory = configSwarm.getSkladDirectory();
        FileUtils.forceMkdir(new File(sklad_directory + name + "_" + tag));
        unpackingZip(sklad_directory + name + "_" + tag, pathZip);
        // Дополнение директории файлами app.py и Dockerfile
        File source2 = new File(configSwarm.getDockerFile());
        File dest1 = new File(sklad_directory + name + "_" + tag + "/app.py");
        File dest2 = new File(sklad_directory + name + "_" + tag + "/Dockerfile");
        buildAppFile(dest1, username, role, port);
        Files.copy(source2.toPath(), dest2.toPath());
        // Запаковка в tar архив директории
        File[] files = new File(sklad_directory + name + "_" + tag).listFiles();
        try {
            compress(sklad_directory + name + "_" + tag + ".tar", files);
        } catch (IOException er4) {
            er4.printStackTrace();
            System.err.println("No such file or directory");
        }
        //Инициализация клиента
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryEmail(configSwarm.getRegistryEmail())
                .withRegistryPassword(configSwarm.getRegistryPassword())
                .withRegistryUsername(configSwarm.getRegistryUsername())
                .build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        try {
            //Создание потока с tar файла
            File initialFile = new File(sklad_directory + name + "_" + tag + ".tar");
            InputStream targetStream = new FileInputStream(initialFile);
            // билд из потока
            BuildImageResultCallback a = dockerClient.buildImageCmd().withTarInputStream(targetStream).exec(new BuildImageResultCallback());
            String image_id = a.awaitImageId();
            // Push в репозиторий
            dockerClient.tagImageCmd(image_id, configSwarm.getDockerHubRepository() + name, tag).exec();
            dockerClient.pushImageCmd(configSwarm.getDockerHubRepository() + name + ":" + tag).withTag(tag).exec(new PushImageResultCallback()).awaitCompletion(1000, TimeUnit.SECONDS);
        } catch (IOException er5) {
            er5.printStackTrace();
            System.err.println("No such tar file");
        }

    }

    private static void compress(String name, File[] files) throws IOException {
        try (TarArchiveOutputStream out = getTarArchiveOutputStream(name)) {
            for (File file : files) {
                addToArchiveCompression(out, file);
            }
        }
    }

    private static TarArchiveOutputStream getTarArchiveOutputStream(String name) throws IOException {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(new FileOutputStream(name));
        // TAR has an 8 gig file limit by default, this gets around that
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        // TAR originally didn't support long file names, so enable the support for it
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        taos.setAddPaxHeadersForNonAsciiNames(true);
        return taos;
    }

    private static void addToArchiveCompression(TarArchiveOutputStream out, File file) throws IOException {
        String entry = "." + File.separator + file.getName();
        if (file.isFile()) {
            out.putArchiveEntry(new TarArchiveEntry(file, entry));
            try (FileInputStream in = new FileInputStream(file)) {
                IOUtils.copy(in, out);
            }
            out.closeArchiveEntry();
        } else {
            System.out.println(file.getName() + " is not supported");
        }
    }

    private static void unpackingZip(String path_docker, String path_zip) {
        // Распаковка файлов в директорию path_docker
        byte[] buffer = new byte[2048];
        Path outDir = Paths.get(path_docker);

        try (FileInputStream fis = new FileInputStream(path_zip);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipInputStream stream = new ZipInputStream(bis)) {

            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {

                Path filePath = outDir.resolve(entry.getName());

                try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                     BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {

                    int len;
                    while ((len = stream.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                } catch (IOException er) {
                    er.printStackTrace();
                    System.err.println("No such file or directory");
                }
            }
        } catch (IOException er2) {
            er2.printStackTrace();
            System.err.println("No such file or directory");
        }

    }

    private void buildAppFile(File dest, String user, String role, String port) throws FileNotFoundException, IOException {
        File file = new File(configSwarm.getAppFile());
        Scanner scanner = new Scanner(file).useDelimiter("\n");
        List<String> buff = new LinkedList<String>();
        FileWriter writer = new FileWriter(dest);
        for (int i = 0; i < 9; i++) {
            writer.write(scanner.next() + "\n");
        }
        writer.write("app.config['SECRET_KEY'] = " + "'TimoFeevKey1'" + "\n");
        writer.write("app.config['user'] = '" + user + "'" + "\n");
        writer.write("app.config['role'] = '" + role + "'" + "\n");
        while (scanner.hasNext()) {
            writer.write(scanner.next() + "\n");
        }
        writer.write("    app.run(debug=True, host=\"0.0.0.0\", port=" + port + ")\n");
        writer.close();
    }


    public List<String> busyPorts() throws IOException {
        // подключение к manager ноде кластера и pull образа из репозитория
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(configSwarm.getFingerprint());
        ssh.connect(configSwarm.getManagerNode());
        ssh.authPassword(configSwarm.getUsernameSwarm(), configSwarm.getPasswordSwarm());
        Session session = ssh.startSession();
        String a = "docker service ls --format \"{{.Name}}{{.Ports}}\"";
        Session.Command cmd = session.exec(a);
        final String res = "";
        Map<String, String> cur = convert(cmd.getInputStream());
        List<String> result = new LinkedList<String>();
        for (Map.Entry entry : cur.entrySet()) {
            result.add(String.valueOf(entry.getValue()));
        }
        session.close();
        ssh.disconnect();
        Iterable<ImageContainer> rep = imageContainerRepo.findAll();
        Iterator<ImageContainer> iter = rep.iterator();
        while (iter.hasNext()) {
            String pr = iter.next().getPort();
            if (!res.contains(pr))
                result.add(pr);
        }
        return result;
    }


    public Map<String, String> getModels(String username) {
        List<Client> clients = clientRepo.findByUsername(username);
        Map<String, String> result = new HashMap<>();
        if (!clients.isEmpty()) {
            Client client = clients.get(0);
            List<Model> models = modelRepo.findByClient(client);
            if (!models.isEmpty()) {
                for (Model model : models) {
                    List<Version> versions = versionRepo.findByModel(model);
                    for (Version version : versions) {
                        result.put(version.getLabel(), "http://" + configSwarm.getManagerNode() + ":" + version.getPort());
                    }
                }
            }
            return result;
        } else
            return result;
    }


    private Map<String, String> convert(InputStream is) {
        Scanner scanner = new Scanner(is).useDelimiter("\n");
        Map<String, String> result = new HashMap<>();
        String regex = "(^[0-9]+)";
        Pattern pattern = Pattern.compile(regex);
        while (scanner.hasNext()) {
            String item = scanner.next();
            String[] items = item.split("\\*:");
            String key = items[0];
            String val = items[1];
            Matcher matcher = pattern.matcher(val);
            if (matcher.find())
                result.put(key, matcher.group(0));
        }
        return result;
    }
}
