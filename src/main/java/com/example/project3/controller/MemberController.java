package com.example.project3.controller;

import com.example.project3.dto.request.SignupRequest;
import com.example.project3.dto.request.UpdateUserInfoRequest;
import com.example.project3.dto.response.MemberInfoResponse;
import com.example.project3.service.MemberService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Parameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "회원관리")
@ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "무효한 토큰으로 인증 불가능"),
        @ApiResponse(code = 404, message = "유효한 토큰이나 토큰 정보로 유저 조회 불가능"),
})
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @ApiOperation(value = "회원가입", notes = "multipart/form-data로 파일과 회원가입 JSON데이터를 요청한다.\n" +
                                    "request는 content-type을 \"applcation/json\"으로 명시해주어야 한다.\n" +
                                    "Swagger로는 요청하기 힘든 듯")
    @ApiResponses({
            @ApiResponse(code = 409, message = "Email already exists"),
            @ApiResponse(code = 200, message = "Signup Successful")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "회원가입 요청(application/json)", required = true,
                     dataType = "SignupRequest", paramType = "form", example =
            "{ " +
                    "  \"userName\" : \"\" ," +
                    "  \"email\" : \"\" ," +
                    "  \"password\" : \"\" ," +
                    "  \"address\" : \"\" ," +
                    "  \"nickName\" : \"\" ," +
                    "  \"message\" : \"\"" +
            " }")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestPart("request") SignupRequest request,
                                    @RequestPart(value = "file",required = false) MultipartFile file){
        log.info("회원가입 요청이 들어왔습니다.");

        log.info("userName = {}",request.getUserName());
        log.info("email = {}",request.getEmail());
        log.info("address = {}", request.getAddress());
        log.info("imageFile = {}", file.getOriginalFilename());
        log.info("nickName = {}", request.getNickName());
        log.info("message = {}", request.getMessage());

        return memberService.signup(request, file);
    }


    // TODO : 회원정보 조회에 인증이 필요할까
    @ApiOperation(value = "회원 정보 조회(토큰 필요)", notes = "기본적인 회원 정보와 등록했던 글 응답\n" +
                                                  "default 페이징사이즈 : 10")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "페이지 번호 (0부터 시작)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "페이지 크기")
    })
    @GetMapping("/user")
    public ResponseEntity<MemberInfoResponse> getMemberInfo(@ApiIgnore @AuthenticationPrincipal UserDetails userDetails,
                                                            @ApiIgnore @PageableDefault Pageable pageable) {
        log.info("회원정보 조회 요청이 들어왔습니다.");
        MemberInfoResponse userInfo = memberService.getMemberInfo(userDetails.getUsername(), pageable);
        return ResponseEntity.ok().body(userInfo);
    }


    @ApiOperation(value = "회원탈퇴(토큰 필요)", notes = "DB에서 회원 정보와 등록했던 글 영구적으로 삭제\n" +
                                             "사용한 액세스 토큰은 다시는 사용 불가능")
    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteAccount(@ApiIgnore @AuthenticationPrincipal UserDetails userDetails,
                                              HttpServletRequest request) {
        log.info("회원탈퇴 요청이 들어왔습니다.");
        log.info("email : {}", userDetails.getUsername());
        String accessToken = request.getHeader("Authorization");

        memberService.deleteAccount(userDetails.getUsername(), accessToken);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "로그아웃(토큰 필요)", notes = "로그아웃 시도한 액세스 토큰은 다시는 사용 불가능")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@ApiIgnore @AuthenticationPrincipal UserDetails userDetails ,
                                       HttpServletRequest request) {
        log.info("로그아웃 요청이 들어왔습니다.");
        String accessToken = request.getHeader("Authorization");

        memberService.logout(userDetails, accessToken);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "닉네임 중복 확인", notes = "회원가입 시도 시 사용가능한 닉네임인지 중복확인")
    @ApiResponses({
            @ApiResponse(code = 409, message = "닉네임 중복")
    })
    @PostMapping("/user/{nickName}")
    public ResponseEntity<Void> isDuplicatedNickname(@PathVariable String nickName) {
        log.info("닉네임 중복확인 요청이 들어왔습니다.");

        if (memberService.checkDuplicateNickname(nickName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        else return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "회원정보 수정(토큰 필요)", notes = "multipart/form-data로 파일과 회원정보 수정 JSON데이터를 요청한다.\n" +
                                                 "request는 content-type을 \"applcation/json\"으로 명시해주어야 한다.\n" +
                                                 "Swagger로는 요청하기 힘든 듯")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "회원정보 수정 요청(application/json)", required = true,
                    dataType = "UpdateUserInfoRequest", paramType = "form", example =
                    "{ " +
                     "  \"address\" : \"\" ," +
                     "  \"nickName\" : \"\" ," +
                     "  \"message\" : \"\"" +
                    " }")
    })
    @PatchMapping("/user")
    public ResponseEntity<Void> updateUserInfo(@ApiIgnore @AuthenticationPrincipal UserDetails userDetails,
                                               @RequestPart(value = "request",required = false) UpdateUserInfoRequest request,
                                               @RequestPart(value = "file",required = false) MultipartFile file) {
        log.info("회원정보 수정 요청이 들어왔습니다.");
        String email = userDetails.getUsername();
        memberService.updateUserInfo(email, request, file);

        return ResponseEntity.ok().build();
    }
}