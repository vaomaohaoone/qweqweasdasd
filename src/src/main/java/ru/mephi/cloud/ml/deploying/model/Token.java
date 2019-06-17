package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * Created by kir on 06.04.19.
 */
@Data
@Entity
@Table(name = "token")
public class Token {
    private static final long serialVersionUID = -3009157732242241606L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Type(type = "integer")
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Client client;

    @Column(name = "refresh_token")
    private String refresh_token;

    @Column(name = "access_token")
    private String access_token;

    protected Token() {
    }

    public Token(String refresh_token, String access_token) {
        this.refresh_token = refresh_token;
        this.access_token = access_token;
    }
}
