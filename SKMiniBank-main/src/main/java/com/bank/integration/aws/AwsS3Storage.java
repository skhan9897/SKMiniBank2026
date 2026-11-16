package com.bank.integration.aws;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

/** Private document storage. Bucket policy should block public access and enforce encryption. */
public final class AwsS3Storage {
    private AwsS3Storage() { }
    public static void upload(Path file, String objectKey, String contentType) {
        String bucket = AwsEmailSender.required("AWS_S3_BUCKET");
        try (S3Client s3 = S3Client.builder()
                .region(Region.of(AwsEmailSender.value("AWS_REGION", "ap-south-1"))).build()) {
            s3.putObject(PutObjectRequest.builder().bucket(bucket).key(objectKey)
                    .contentType(contentType).serverSideEncryption("AES256").build(), RequestBody.fromFile(file));
        }
    }
}
