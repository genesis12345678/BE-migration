package com.example.project3.controller.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class JwtExceptionHandler {

    @ExceptionHandler(SignatureException.class)
    public String handleSignatureException() {
        return "토큰 서명이 유효하지 않습니다.";
    }

    @ExceptionHandler(MalformedJwtException.class)
    public String handleMalformedJwtException() {
        return "올바르지 않은 형식의 토큰입니다.";
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public String handleExpiredJwtException() {
        return "토큰이 만료되었습니다.";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(){
        return "토큰 필요";
    }
}