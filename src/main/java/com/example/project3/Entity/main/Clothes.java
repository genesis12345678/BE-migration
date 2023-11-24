package com.example.project3.Entity.main;


import com.example.project3.Entity.member.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Clothes")
public class Clothes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clothes_id")
    private Long clothesId;

    @Column(name = "top")
    private String top;

    @Column(name = "bottom")
    private String bottom;

    @ManyToOne
    @JoinColumn(name = "weather_id", nullable = false)
    private Weather weather;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "clothes_img")
    private String clothesImg;
}

