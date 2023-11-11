package com.example.project3.config.jwt;

import com.example.project3.Entity.member.Member;
import com.example.project3.repository.MemberRepository;
import com.example.project3.service.MemberDetailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private final MemberDetailService memberDetailService;
    private String secretKey;

    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);


    @PostConstruct
    protected void init() {
        log.info("TokenProvider init() 메소드 시작, secretKey 초기화 시작");
        secretKey = Base64.getEncoder().encodeToString(jwtProperties.getSecretKey().getBytes());
        log.info("TokenProvider secretKey 초기화 완료");
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
        log.info("TokenProvider getAuthentication 실행");
        Claims claims = getClaims(token);
        String email = claims.getSubject();
        UserDetails userDetails = memberDetailService.loadUserByUsername(email);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
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