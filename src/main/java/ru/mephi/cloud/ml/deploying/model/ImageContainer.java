package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by kir on 06.04.19.
 */
@Data
@Entity
public class ImageContainer implements Serializable {
    private static final long serialVersionUID = -3009157732242241606L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id_repo;

    @Column(name = "name")
    private String name;

    @Column(name = "tag")
    private String tag;

    @Column(name = "port")
    private String port;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Client client;

    protected ImageContainer() {
    }

    public ImageContainer(String name, String tag, String port) {
        this.name = name;
        this.tag = tag;
        this.port = port;
    }

}
