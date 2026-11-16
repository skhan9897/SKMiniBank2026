
package com.bank.integration.aws;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

public final class AwsEmailSender {

    private AwsEmailSender() {
    }

    public static void send(
            String to,
            String subject,
            String text) {

        if (to == null || !to.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        String from = required("AWS_SES_FROM_EMAIL");

        Region region = Region.of(
                value("AWS_REGION", "ap-south-1")
        );

        try (SesClient ses = SesClient.builder()
                .region(region)
                .build()) {

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(from)
                    .destination(
                            Destination.builder()
                                    .toAddresses(to)
                                    .build()
                    )
                    .message(
                            Message.builder()
                                    .subject(
                                            Content.builder()
                                                    .data(subject)
                                                    .charset("UTF-8")
                                                    .build()
                                    )
                                    .body(
                                            Body.builder()
                                                    .text(
                                                            Content.builder()
                                                                    .data(text)
                                                                    .charset("UTF-8")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            ses.sendEmail(request);
        }
    }

    static String value(String key, String fallback) {
        String value = System.getenv(key);

        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    static String required(String key) {
        String value = System.getenv(key);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    key + " is not configured"
            );
        }

        return value.trim();
    }
}
