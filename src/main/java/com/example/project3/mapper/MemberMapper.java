package com.example.project3.mapper;

import com.example.project3.dto.request.SignupRequest;
import com.example.project3.entity.member.Member;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {

    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    @Mapping(target = "password", source = "request.password", qualifiedByName = "encodingPassword")
    @Mapping(target = "imageURL", source = "imageUrl")
    @Mapping(target = "name", source = "request.userName")
    @Mapping(target = "role", constant = "USER")
    Member toMemberEntity(SignupRequest request, String imageUrl);

    @Named("encodingPassword")
    static String encodingPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
}