package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Set<Version> versions;

    protected Model() {
    }

    public Model(String name, Version ... versions) {
        this.name = name;
        for (Version version : versions) version.setModel(this);
        this.versions = Stream.of(versions).collect(Collectors.toSet());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Model model = (Model) o;

        if (id != model.id) return false;
        return name.equals(model.name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }
}
