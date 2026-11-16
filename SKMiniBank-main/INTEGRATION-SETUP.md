# SK Mini Bank integration setup (preserves existing UI)

## Included integration foundation
- Kafka producer uses `KAFKA_BOOTSTRAP_SERVERS` and `KAFKA_TRANSACTION_TOPIC`; it does not block banking requests when Kafka is unavailable.
- AWS SDK v2 clients: SES email, SNS SMS, and private S3 document upload. Runtime credentials use the AWS default credential chain (IAM role recommended).
- Email OTP endpoint: `POST /api/otp/email` with `email`; verify with `action=verify`, `email`, and `otp`.
- Mobile OTP endpoint: `POST /api/otp/mobile` with `mobile` (10-digit Indian mobile or E.164); verify with `action=verify`, `mobile`, and `otp`.
- OTPs are generated with `SecureRandom`, stored as SHA-256 hashes in the HTTP session, expire after five minutes, allow at most five verification attempts, and are never returned by the API.
- Aadhaar adapter contract is present but intentionally fails closed. A real provider-specific signed API adapter cannot be completed or enabled without a contract/onboarding with an authorized UIDAI-compliant provider. Do not use unofficial OTP endpoints.

## Configure
1. Copy `.env.example` to `.env` and set the existing database URL/user/password. Never commit `.env`.
2. Verify the SES sender identity and request production access if the AWS account is in the SES sandbox.
3. Enable SNS SMS in the selected AWS region and complete any required origination/registration steps for India.
4. Create a private S3 bucket with Block Public Access enabled and encryption at rest. Grant the app IAM role only the required `s3:PutObject` permission on that bucket.
5. On EC2/ECS, attach an IAM role with least-privilege SES/SNS/S3 permissions. Avoid static AWS keys.
6. Run `docker compose up --build -d` and inspect `docker compose logs -f app kafka`.

## Important limitations / required next wiring
- The new OTP endpoints are integration endpoints; existing login/registration UI flows still need to call them and gate account actions on successful verification. Existing legacy mock OTP routes must be removed from those flows before production use.
- Existing login and UPI-PIN APIs still contain hardcoded OTP `9897`; do not expose this build as a real-money production bank until those bypasses are replaced with verified challenge checks and security review is complete.
- Existing Kafka producer call sites were not found in the source audit. Transaction DAO/service code must call `KafkaService.logTransaction` after a successful database commit (prefer an outbox pattern for financial-grade reliability).
- S3 upload utility is available, but existing KYC/document upload flows have not yet been switched to S3.
- Aadhaar OTP is NOT live. It requires an authorized provider agreement, API documentation, consent flow, signing/encryption requirements, and provider credentials. Do not store full Aadhaar numbers unnecessarily; mask/tokenize them and restrict access.
- AWS SES/SNS/S3 need real AWS account configuration and permissions. No AWS resources are created by this source change.
