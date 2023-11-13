package com.example.project3.Entity.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    USER("USER"), GUEST("GUEST");
    private final String value;
}