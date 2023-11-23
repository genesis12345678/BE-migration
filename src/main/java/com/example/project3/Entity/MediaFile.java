package com.example.project3.Entity;

import lombok.*;

import javax.persistence.*;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "file_url")
    private String fileUrl;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "file_type")
//    private FileType fileType;

    public MediaFile(String fileUrl, Post post) {
        this.fileUrl = fileUrl;
        this.post = post;
    }

    public MediaFile(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    public void setPost2(Post post) {
        this.post = post;
        post.getMediaFiles().add(this);
    }
}
