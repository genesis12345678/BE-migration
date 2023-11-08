package com.example.project3.config.jwt;

import com.example.project3.Entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private String secretKey;

    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);


    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(jwtProperties.getSecretKey().getBytes());
    }

    public String createAccessToken(Member member) {
       Date now = new Date();
       Collection<? extends GrantedAuthority> authorities = member.getAuthorities();

        return Jwts.builder()
               .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
               .setIssuedAt(now)
               .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_DURATION.toMillis()))
               .setSubject(member.getEmail())
               .claim("id",member.getId())
               .claim("authorities", authorities)
               .signWith(SignatureAlgorithm.HS256, secretKey)
               .compact();
    }

    public String createRefreshToken(Member member) {
       Date now = new Date();

       return Jwts.builder()
               .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
               .setIssuedAt(now)
               .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_DURATION.toMillis()))
               .setSubject(member.getEmail())
               .claim("id",member.getId())
               .signWith(SignatureAlgorithm.HS256, secretKey)
               .compact();
    }

    public boolean validToken(String token) {
        try{
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(
                new User(claims.getSubject(), "", authorities)
                ,token
                ,authorities);
    }

    public Long getMemberId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}