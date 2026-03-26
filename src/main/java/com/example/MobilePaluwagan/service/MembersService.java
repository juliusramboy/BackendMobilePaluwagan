package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.dto.Request.ProfileUpdateRequest;
import com.example.MobilePaluwagan.dto.Request.RegisterRequest;
import com.example.MobilePaluwagan.dto.Response.*;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MembersService {


    private final UserInfoRepo userInfoRepo;
    private final RoleRepo roleRepo;
    private final UserRepo userRepo;
    private final UserBankRepo userBankRepo;
    private final LoanPaymentRepo loanPaymentRepo;
    private final UserSavingsRepo userSavingsRepo;
    private final ProfileService profileService;
    private final DueDateScheduleRepository dueDateScheduleRepo;
    private final LedgerRepo ledgerRepo;
    private final NotificationRepository notificationRepo;
    private final LoanApplicationRepo loanApplicationRepo;
    private final UserLoanRepo userLoanRepo;
    private final SseController sseController;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository  tokenRepository;


    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Page<MembersFilterProjection> filterMembers(MembersFilterResponse filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // ← Convert empty string to null
        String fullName  = (filter.getFullName() != null && !filter.getFullName().isEmpty())
                ? filter.getFullName() : null;
        String role = (filter.getRole() != null && !filter.getRole().isEmpty())
                ? filter.getRole() : null;

        return userInfoRepo.filterMembers(fullName, role, pageable);
    }


    public ResponseEntity<Long> adminRegister(RegisterRequest register) {

        Role defaultRole = roleRepo.findById(1)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));


        String rawPassword = register.getPassword();
        String hashedPassword = encoder.encode(rawPassword);
        User user = new User();
        user.setEmail(register.getEmail());
        user.setPassword(hashedPassword);
        user.setRole(defaultRole);
        user.setActive(true);

        User userdataWithId = userRepo.save(user);


        UserVerification userVerification = new UserVerification();
        userVerification.setUserId(userdataWithId.getId());

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userdataWithId.getId());
        userInfo.setFirstName(register.getFirstName());
        userInfo.setMiddleName(register.getMiddleName());
        userInfo.setLastName(register.getLastName());
        userInfo.setSuffix(register.getSuffix());
        userInfo.setPhoneNumber(register.getPhoneNumber());
        userInfo.setVerifiedDate(LocalDate.now());

        userInfoRepo.save(userInfo);

        UserBank userBank = new UserBank();
        userBank.setUserId(userdataWithId.getId());
        userBank.setAccountBalance(BigDecimal.valueOf(0L));

        userBankRepo.save(userBank);

        return new ResponseEntity<>(userdataWithId.getId(), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteMember(Long userId) {

        Optional<Loan> userLoan = userLoanRepo.findByUserId(userId);

        if (userLoan.isEmpty()) {
            User user = userRepo.findById(userId).orElseThrow();
            user.setUserBank(null);
            user.setUserSavings(null);
            user.setUserInfo(null);
            user.setSavingsWithdrawApplications(null);
            user.setTokens(null);
            userRepo.save(user);

            // 2. Now safe to delete in order
            ledgerRepo.deleteByUserId(userId);
            notificationRepo.deleteByUserId(userId);
            loanPaymentRepo.deleteByUserId(userId);
            userLoanRepo.deleteByUserId(userId);
            loanApplicationRepo.deleteByUserId(userId);
            userSavingsRepo.deleteByUserId(userId);
            userBankRepo.deleteByUserId(userId);
            userInfoRepo.deleteByUserId(userId);

            tokenRepository.deleteByUserId(userId);

            // 4. Root last
            userRepo.deleteById(userId);

            return ResponseEntity.ok("Successfully Deleted Member");
        }


        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The user has a loan; you can't delete a user that has an active loan."
        );
    }

    public AdminMemberListResponse getAdminUserProfile(Long userId, int page, int size) {
        UserProfileResponse info = profileService.userAllInfo(userId);
        Page<Ledger> ledgerPayments = profileService.getAllPayments(userId, page, size);

        return AdminMemberListResponse.builder()
                .info(info)
                .allPayments(ledgerPayments)
                .build();
    }

    @Transactional
    public ApiResponse<String> updateProfile(Long userId, ProfileUpdateRequest request) {


        UserInfo info = userInfoRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));



        if (request.getFirstName() != null) {
            info.setFirstName(request.getFirstName());
        }

        if (request.getMiddleName() != null) {
            info.setMiddleName(request.getMiddleName());
        }

        if (request.getLastName() != null) {
            info.setLastName(request.getLastName());
        }

        if (request.getSuffix() != null) {
            info.setSuffix(request.getSuffix());
        }

        if (request.getGender() != null) {
            info.setGender(request.getGender());
        }

        if (request.getAddress() != null) {
            info.setAddress(request.getAddress());
        }

        if (request.getBirthDay() != null) {
            info.setBirthDay(request.getBirthDay());
        }

        if (request.getPhoneNumber() != null) {
            info.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getEmail() != null){
            user.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && request.getOldPassword() != null){

            if (passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
                    String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

                    user.setPassword(encodedNewPassword);

                    userRepo.save(user);
                } else {
                    return new ApiResponse<>(
                            false,
                            "New password cannot be null or blank",
                            null
                    );
                }
            } else {
                return new ApiResponse<>(
                        false,
                        "Old password does not match",
                        null
                );
            }

        }

        UserInfo savedUser = userInfoRepo.save(info);

        sseController.notifyUpdate();
        return new ApiResponse<>(
                true,
                "Successfully updated profile",
                "Profile updated"
        );
    }
}
