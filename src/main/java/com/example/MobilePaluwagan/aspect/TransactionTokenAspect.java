package com.example.MobilePaluwagan.aspect;

import com.example.MobilePaluwagan.annotation.RequiresTransactionToken;
import com.example.MobilePaluwagan.entity.UserPrinciple;
import com.example.MobilePaluwagan.exception.InvalidTransactionTokenException;
import com.example.MobilePaluwagan.service.TransactionTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TransactionTokenAspect {

    private final TransactionTokenService tokenService;
    private final HttpServletRequest request;



    @Around("@annotation(com.example.MobilePaluwagan.annotation.RequiresTransactionToken)")
    public Object checkToken(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println(">>> TransactionTokenAspect triggered!");
        String token = request.getHeader("X-Transaction-Token");

        if (token == null || token.isBlank()) {
            throw new InvalidTransactionTokenException("Missing transaction token");
        }

        String tokenUserId = tokenService.validateAndConsume(token);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();

        String currentId = String.valueOf(user.userId());

        if (!tokenUserId.equals(currentId)){
            throw new InvalidTransactionTokenException("Token does not belong to this user.");
        }

        return joinPoint.proceed();
    }
}
