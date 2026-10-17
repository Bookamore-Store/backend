package com.bookamore.backend.entity;

import com.bookamore.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column() // password can be null if user logged in via 3d-party provider
    private String password;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "entity_id", referencedColumnName = "id", insertable = false, updatable = false)
    @SQLRestriction("entity_type = 'USER'")
    @OrderBy("id ASC")
    @BatchSize(size = 10)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthProvider> authProviders = new ArrayList<>();

    @Column(name = "password_reset_code")
    private String passwordResetCode;

    @Column(name = "password_reset_code_expiration_time")
    private LocalDateTime passwordResetCodeExpirationTime;
}
