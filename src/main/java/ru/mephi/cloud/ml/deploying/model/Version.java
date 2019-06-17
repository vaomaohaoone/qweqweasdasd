package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by kir on 06.04.19.
 */
@Data
@Entity(name = "Version")
@IdClass(Version.class)
public class Version implements Serializable {

   // @Id
   // @GeneratedValue(strategy = GenerationType.IDENTITY)
   // private Long id;
    @Id
    @ManyToOne
    @JoinColumn
    private Model model;

    @Id
    @ManyToOne
    @JoinColumn
    private Deployment deployment;

    @Column(name = "label")
    private String label;

    @Column(name = "port")
    private String port;

    protected Version(){}

    public Version(Deployment deployment, String label, String port) {
        this.deployment = deployment;
        this.label = label;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version that = (Version) o;
        return Objects.equals(deployment.getId(), that.deployment.getId()) &&
                Objects.equals(model.getName(), that.model.getName()) &&
                Objects.equals(label, that.label) &&
                Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, label);
    }
}
