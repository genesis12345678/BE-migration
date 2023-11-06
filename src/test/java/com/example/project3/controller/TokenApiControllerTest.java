package com.example.project3.controller;

import com.example.project3.Entity.Member;
import com.example.project3.Entity.RefreshToken;
import com.example.project3.config.jwt.JwtProperties;
import com.example.project3.dto.request.CreateAccessTokenRequest;
import com.example.project3.jwt.JwtFactory;
import com.example.project3.repository.MemberRepository;
import com.example.project3.repository.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Locale;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class TokenApiControllerTest {

    private final static Faker faker = new Faker(new Locale("ko"));

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void setMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        memberRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @DisplayName("새로운 액세스 토큰 발급")
    @Test
    void createNewAccessToken() throws Exception {
        // given
        final String url = "/api/token";

        String username = faker.name().lastName() + faker.name().firstName();
        String email = faker.internet().emailAddress();
        String address = faker.address().fullAddress();
        String imageURL = faker.internet().avatar();
        String nickName = faker.name().prefix() + faker.name().firstName();
        String phoneNumber = "010" + faker.numerify("########");
        String gender = faker.options().option("MALE", "FEMALE");

        Member testMember = Member.builder()
                .name(username)
                .email(email)
                .address(address)
                .imageURL(imageURL)
                .nickName(nickName)
                .phoneNumber(phoneNumber)
                .gender(gender)
                .password("testPassword13@")
                .build();

        memberRepository.save(testMember);

        Member member = memberRepository.findByEmail(email).orElseThrow(
                ()->new IllegalArgumentException("Unexpected"));

        String refreshToken = JwtFactory
                .builder()
                .claims(Map.of("id", member.getId()))
                .build()
                .createToken(jwtProperties);

        refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken));

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToken);

        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}

