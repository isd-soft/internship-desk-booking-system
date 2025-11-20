package com.project.internship_desk_booking_system.entity;

import com.project.internship_desk_booking_system.enums.AuthProvider;
import com.project.internship_desk_booking_system.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(
            name = "user_seq",
            sequenceName = "user_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FavouriteDesks> favourites = new ArrayList<>();
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    public User(String email, String passwordHash) {
        this.role = Role.USER;
        this.email = email;
        this.passwordHash = passwordHash;
        this.authProvider = AuthProvider.LOCAL;
    }

    protected User() {
    }

    public static User ldapUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setAuthProvider(AuthProvider.LDAP);
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
