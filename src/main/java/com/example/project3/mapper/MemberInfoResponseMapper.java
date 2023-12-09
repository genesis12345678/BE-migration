package com.example.project3.mapper;

import com.example.project3.dto.response.member.MemberInfoResponse;
import com.example.project3.dto.response.member.SimplifiedPostResponse;
import com.example.project3.entity.member.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MemberInfoResponseMapper {

    MemberInfoResponseMapper INSTANCE = Mappers.getMapper(MemberInfoResponseMapper.class);

    @Mapping(target = "simplifiedPostResponseResponseList", source = "posts")
    @Mapping(target = "memberId", source = "member.id")
    MemberInfoResponse toMemberInfoResponse(Member member, List<SimplifiedPostResponse> posts);
}