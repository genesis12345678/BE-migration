package com.example.project3.repository;

import com.example.project3.Entity.member.Member;
import com.example.project3.Entity.member.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByRefreshToken(String refreshToken);

    Optional<Member> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    boolean existsByEmail(String email);


    Optional<Member> findByNickName(String nickName);
}