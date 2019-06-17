package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kir on 06.04.19.
 */
@Data
@Entity(name = "Deployment")
@Table(name = "deployment")
public class Deployment implements Serializable {
    private static final long serialVersionUID = -3009157732242241606L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "replicas")
    private Integer replicas;

    @Column(name = "cpu")
    private Double cpu;

    @Column(name = "ram")
    private String ram;

    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL)
    private Set<Version> versions = new HashSet<>();

    protected Deployment() {
    }

    public Deployment(Integer replicas, double cpu, String ram) {
        this.replicas = replicas;
        this.cpu = cpu;
        this.ram = ram;
    }
}
