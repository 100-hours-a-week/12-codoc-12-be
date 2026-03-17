package _ganzi.codoc.global.s3;

public class S3ObjectStorageException extends RuntimeException {

    public S3ObjectStorageException(Throwable cause) {
        super("Failed to access S3 object", cause);
    }
}
