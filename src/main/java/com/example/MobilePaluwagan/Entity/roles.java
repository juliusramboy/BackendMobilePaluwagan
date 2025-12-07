package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class roles {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_person") // for tagname
    @SequenceGenerator(name = "role_person", sequenceName = "role_machine", allocationSize = 1) // tagname and the machine
    @Column(name = "role_id")
    private Long id;
    @Column(name = "role_name")
    private String roleName;

}
