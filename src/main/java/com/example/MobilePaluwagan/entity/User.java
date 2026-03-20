package com.example.MobilePaluwagan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @Column(name = "is_online")
    private boolean isOnline;

    @Transient
    private String verificationOtp;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<Token> tokens;

    @JsonIgnore
    @OneToOne(mappedBy = "user")
    @ToString.Exclude
    private UserInfo userInfo;

    @JsonIgnore
    @OneToOne(mappedBy = "user")
    @ToString.Exclude
    private UserBank userBank;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<UserSavings> userSavings;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<SavingsWithdrawApplication> savingsWithdrawApplications;

}
