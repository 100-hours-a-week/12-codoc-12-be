package _ganzi.codoc.global.s3;

import java.time.Duration;

public interface ObjectStorageService {

    String issueUploadUrl(String bucket, String key, String contentType, Duration expiration);

    String issueDownloadUrl(String bucket, String key, Duration expiration);

    long getObjectSize(String bucket, String key);

    void assertObjectExists(String bucket, String key);
}
