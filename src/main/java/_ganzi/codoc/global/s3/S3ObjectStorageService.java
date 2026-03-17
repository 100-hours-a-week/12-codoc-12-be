package _ganzi.codoc.global.s3;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RequiredArgsConstructor
@Service
public class S3ObjectStorageService implements ObjectStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public String issueUploadUrl(String bucket, String key, String contentType, Duration expiration) {
        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .putObjectRequest(putObjectRequest)
                        .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public String issueDownloadUrl(String bucket, String key, Duration expiration) {
        GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(request)
                        .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void assertObjectExists(String bucket, String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (NoSuchKeyException exception) {
            throw new S3ObjectNotFoundException(exception);
        } catch (SdkException exception) {
            throw new S3ObjectStorageException(exception);
        }
    }
}
