package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kir on 06.04.19.
 */

@Data
@Entity(name = "Model")
@Table(name = "model")
public class Model {
    private static final long serialVersionUID = -3009157732242241606L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "name")
    private String name;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "model_client",
            joinColumns = {@JoinColumn(name = "model_id")},
            inverseJoinColumns = {@JoinColumn(name = "client_id")})
    private Set<Client> clients = new HashSet<>();

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL)
    private Set<Version> versions = new HashSet<>();

    protected Model() {
    }

    public Model(String name) {
        this.name = name;
    }
}
