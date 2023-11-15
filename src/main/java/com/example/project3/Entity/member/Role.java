package com.example.project3.Entity.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    USER("ROLE_USER"), GUEST("ROLE_GUEST");
    private final String value;
}