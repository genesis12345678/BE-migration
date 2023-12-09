package com.example.project3.repository;

import com.example.project3.entity.member.Member;
import com.example.project3.dto.response.member.UserDetailsMapping;
import com.example.project3.entity.member.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m.email as email, m.password as password, m.role as role " +
           "from Member m " +
           "where m.email = :email")
    Optional<UserDetailsMapping> findUserDetails(@Param("email") String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);

    Optional<Member> findByNickName(String nickName);

    @Query("select m.id from Member m where m.email = :email")
    Optional<Long> getIdMyEmail(@Param("email") String email);

    @Query("select m.email from Member m where m.refreshToken = :refreshToken")
    Optional<String> getEmailByRefreshToken(@Param("refreshToken") String refreshToken);

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.refreshToken = :newRefreshToken where m.refreshToken = :oldRefreshToken")
    void updateRefreshToken(@Param("oldRefreshToken") String oldRefreshToken, @Param("newRefreshToken") String newRefreshToken);

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.refreshToken = :refreshToken where m.email = :email")
    void setRefreshToken(@Param("email") String email, @Param("refreshToken") String refreshToken);
}