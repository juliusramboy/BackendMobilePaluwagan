package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "role_name")
    private String roleName;

    @Override
    public String getAuthority() {
        return roleName;
    }
}
