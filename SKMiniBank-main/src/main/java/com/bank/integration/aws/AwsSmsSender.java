
package com.bank.integration.aws;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public final class AwsSmsSender {

    private AwsSmsSender() {
    }

    public static void send(String e164Number, String message) {

        if (e164Number == null
                || !e164Number.matches("\\+[1-9][0-9]{7,14}")) {
            throw new IllegalArgumentException(
                    "Mobile number must be in E.164 format"
            );
        }

        Region region = Region.of(
                AwsEmailSender.value("AWS_REGION", "ap-south-1")
        );

        try (SnsClient sns = SnsClient.builder()
                .region(region)
                .build()) {

            PublishRequest request = PublishRequest.builder()
                    .phoneNumber(e164Number)
                    .message(message)
                    .build();

            sns.publish(request);
        }
    }
}
