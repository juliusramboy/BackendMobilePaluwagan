package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_person") // for tagname
    @SequenceGenerator(name = "role_person", sequenceName = "role_machine", allocationSize = 1) // tagname and the machine
    @Column(name = "role_id")
    private Long id;
    @Column(name = "role_name")
    private String roleName;

    @Override
    public String getAuthority() {
        return roleName;
    }
}
