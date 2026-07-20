package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.annotation.Idempotent;
import com.example.MobilePaluwagan.dto.Request.*;
import com.example.MobilePaluwagan.dto.Response.*;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.PaymongoPaymentRepository;
import com.example.MobilePaluwagan.repository.UserRepo;
import com.example.MobilePaluwagan.service.LoanPenaltyService;
import com.example.MobilePaluwagan.service.LoanService;
import com.example.MobilePaluwagan.service.PayMongoService;
import com.example.MobilePaluwagan.service.PayMongoWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor()
public class  LoanController {


    private final LoanService loanService;
    private final PayMongoService  payMongoService;
    private final LoanPenaltyService  loanPenaltyService;
    private final PayMongoWebhookService payMongoWebhookService;
    private final PaymongoPaymentRepository  paymongoPaymentRepository;
    private final UserRepo userRepo;


    @GetMapping("/loan/user-details")
    public ResponseEntity<ApiResponse<UserAllLoansResponse>> getLoanInfoFromUser(Authentication authentication){
        UserPrinciple loanInfo = (UserPrinciple) authentication.getPrincipal();
        Long userId = loanInfo.userId();

        ApiResponse<UserAllLoansResponse> userInfo = loanService.getAllTheInfo(userId);

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/verify/session")
    public ResponseEntity<?> session(Authentication request){
        if(request == null || !request.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not found");
        }

        String email = request.getName();
        User user = userRepo.findByEmail(email);

        Map<String, Object> info = new HashMap<>();
        info.put("userId", user.getId());
        info.put("email", user.getEmail());
        info.put("role", user.getRole().getRoleName());

        return ResponseEntity.ok(info);
    }



    @PostMapping("/loan/calculate-loan")
    public ResponseEntity<ApplyLoanResponse> calculateLoan(Authentication authentication, @RequestBody CalculateLoanRequest request){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        ApplyLoanResponse response = loanService.processLoanApplication(
                userId,
                request.getLoanAmount(),
                request.getStartDate(),
                request.getEndDate()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("loan/remit")
    public ResponseEntity<ApiResponse<UserDepositSavingsResponse>> userDepositSavings(Authentication authentication, @RequestBody UserDepositSavingsRequest request) {
        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        ApiResponse<UserDepositSavingsResponse> savings = loanService.userDeposit(
                userId,
                request.getAmountDeposit(),
                request.getDepositDate()
        );

        return ResponseEntity.ok(savings);
    }

    @PostMapping("/loan/apply-loan")
    public Long applyLoan(Authentication authentication, @RequestBody ApplyLoanRequest request){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        Long response = loanService.applyLoan(userId, request);

        return response;
    }




    @GetMapping("/loan/status/details")
    public Optional<LoanApplication> details(Authentication authentication){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        Optional<LoanApplication> userInfo = loanService.getDetails(userId);
        
        return userInfo;
    }

    @GetMapping("/loan/payment/filter")
    public ResponseEntity<paymentResponse> filterPayment(
            Authentication authentication,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        // Get the logged-in user's ID
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();  // Make sure your UserPrinciple has getUserId() method

        // Step 1: Build the filter object with userId
        PaymentFilterRequest filter = PaymentFilterRequest.builder()
                .userId(userId)
                .reference(reference)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .paymentMethod(paymentMethod)
                .build();

        // Step 2: Get filtered payments from service
        Page<LoanPayment> loanPayments = loanService.filterUserPayments(filter, page, size);

        // Step 3: Convert entities to DTOs
        List<PaymentInfo> paymentDTOs = loanPayments.getContent() // ← getContent()
                .stream()
                .map(loanService::convertToDTO)
                .collect(Collectors.toList());

        // Step 4: Create appropriate message
        String message = filter.hasFilters() ?
                "Successfully retrieved filtered payments" :
                "Successfully retrieved all payments";

        // Step 5: Build the response
        paymentResponse response = paymentResponse.builder()
                .success(true)
                .message(message)
                .filters(filter.hasFilters() ? filter : null)
                .payment(paymentDTOs)
                .currentPage(loanPayments.getNumber())
                .totalPages(loanPayments.getTotalPages())
                .totalElements(loanPayments.getTotalElements())
                .last(loanPayments.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-link")
    @Idempotent
    public ResponseEntity<ApiResponse<?>> createPaymentLink(@RequestBody PaymongoRequest request, Authentication authentication) {
        try {
            UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
            Long userId = userDetails.userId();
            String checkUrl = payMongoService.createPaymentLink(userId, request);


            return ResponseEntity.ok(new ApiResponse<>(true, "Payment link created", checkUrl));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // this is for testing
    @PostMapping("/simulate")
    public ResponseEntity<?> simulateWebhook(@RequestBody String payload) {
        try {
            payMongoWebhookService.processWebhookWithoutSignature(payload);
            return ResponseEntity.ok(Map.of("message", "Webhook simulated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/attach-method")
    public ResponseEntity<ApiResponse<?>> attachMethod(
            @RequestParam String intentId,
            @RequestParam String methodType) {
        try {
            Map<String, Object> result = payMongoService.attachPaymentMethod(intentId, methodType);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment method attached", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/payment/status/{intentId}")
    public ResponseEntity<ApiResponse<?>> checkPaymentStatus(@PathVariable String intentId) {
        PaymongoPayment payment = paymongoPaymentRepository
                .findByReferenceNumber(intentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return ResponseEntity.ok(new ApiResponse<>(true, "Payment status retrieved",
                Map.of(
                        "status", payment.getStatus(),
                        "expiresAt", payment.getExpiresAt(),
                        "amount", payment.getAmount()
                )
        ));
    }

    @PostMapping("/create-intent")
    @Idempotent
    public ResponseEntity<ApiResponse<?>> createIntent(@RequestBody PaymongoIntentRequest request, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse<>(false, "Not logged in", null));
            }
            UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
            Long userId = userDetails.userId();
            Map<String, Object> result = payMongoService.createPaymentIntent(userId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment intent created", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

}
