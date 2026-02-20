package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Table(name = "user_login")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String password;
    @Column(name = "is_verified")
    private boolean isActive;
    @Column(name = "has_loan")
    private boolean hasLoan;
    @Column(name = "has_savings_account")
    private boolean hasSavingsAccount;

    @Transient
    private String verificationOtp;


    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;

    @OneToOne(mappedBy = "user")
    private UserInfo userInfo;

    @OneToOne(mappedBy = "user")
    private UserBank userBank;

    @OneToMany(mappedBy = "user")
    private List<UserSavings> userSavings;

}
