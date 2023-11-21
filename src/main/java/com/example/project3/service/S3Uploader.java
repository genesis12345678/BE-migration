package com.example.project3.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    private final AmazonS3Client amazonS3Client;

    public String uploadProfileImage(MultipartFile file) throws IOException {
        if (isImageFile(file)) {
            return upload(file);
        } else {
            log.error("이미지 파일이 아닙니다.");
            throw new IllegalArgumentException("Unsupported file type");
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public List<String> upload(List<MultipartFile> files) {
        List<String> fileUrls = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            System.out.println("파일이 비어있습니다.");
            return fileUrls;
        }


        for (MultipartFile file : files) {

            try {

                String fileUrl = upload(file);

                fileUrls.add(fileUrl);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        return fileUrls;
    }


    private String upload(MultipartFile multipartFile) throws IOException {
        UUID uuid = UUID.randomUUID();
        String originName = multipartFile.getOriginalFilename();
        String fileName = uuid + "_" + originName + "/"+ multipartFile.getContentType();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        metadata.setContentLength(multipartFile.getSize());

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName,
                multipartFile.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        amazonS3Client.putObject(putObjectRequest);

        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }


    public String putS3(File uploadFile, String dirName, String originName) {
        UUID uuid = UUID.randomUUID();
        String fileName = dirName + "/" + uuid + "_" + originName;
        amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    public Optional<File> convert(MultipartFile file) {

        try {
            File convertedFile = File.createTempFile("temp", null);
            file.transferTo(convertedFile);
            return Optional.of(convertedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


}
