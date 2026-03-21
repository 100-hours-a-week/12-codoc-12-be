package _ganzi.codoc.custom.infra;

import _ganzi.codoc.custom.config.CustomProblemProperties;
import _ganzi.codoc.custom.dto.CustomProblemUploadUrlsRequest;
import _ganzi.codoc.custom.dto.CustomProblemUploadUrlsResponse;
import _ganzi.codoc.custom.exception.CustomProblemImageSizeExceededException;
import _ganzi.codoc.custom.service.CustomProblemImageRequestValidator;
import _ganzi.codoc.global.config.StorageBucketProperties;
import _ganzi.codoc.global.s3.ObjectStorageService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomProblemStorageService {

    private final ObjectStorageService objectStorageService;
    private final StorageBucketProperties storageBucketProperties;
    private final CustomProblemProperties properties;
    private final CustomProblemImageRequestValidator imageRequestValidator;
    private final CustomProblemObjectKeyPolicy objectKeyPolicy;

    public CustomProblemUploadUrlsResponse issueUploadUrls(
            Long userId, CustomProblemUploadUrlsRequest request) {

        imageRequestValidator.validateUploadImages(request.images());

        List<CustomProblemUploadUrlsResponse.ImageUploadUrlItem> issuedUrls =
                new ArrayList<>(request.images().size());

        request
                .images()
                .forEach(
                        image -> {
                            String fileKey = objectKeyPolicy.generate(userId);
                            String uploadUrl =
                                    objectStorageService.issueUploadUrl(
                                            storageBucketProperties.images(),
                                            fileKey,
                                            image.contentType(),
                                            properties.uploadUrlExpiration());
                            issuedUrls.add(
                                    new CustomProblemUploadUrlsResponse.ImageUploadUrlItem(
                                            image.order(),
                                            fileKey,
                                            uploadUrl,
                                            Instant.now().plus(properties.uploadUrlExpiration())));
                        });

        return new CustomProblemUploadUrlsResponse(issuedUrls);
    }

    public void validateSourceImagesOwnedAndExisting(Long userId, List<String> fileKeys) {
        fileKeys.forEach(
                fileKey -> {
                    objectKeyPolicy.validateKeyNamespace(userId, fileKey);
                    long objectSize =
                            objectStorageService.getObjectSize(storageBucketProperties.images(), fileKey);
                    validateImageSize(objectSize);
                });
    }

    public List<String> createReadableUrls(List<String> fileKeys) {
        List<String> urls = new ArrayList<>(fileKeys.size());
        fileKeys.forEach(
                fileKey -> {
                    objectStorageService.assertObjectExists(storageBucketProperties.images(), fileKey);
                    urls.add(
                            objectStorageService.issueDownloadUrl(
                                    storageBucketProperties.images(), fileKey, properties.downloadUrlExpiration()));
                });
        return urls;
    }

    private void validateImageSize(long imageSizeBytes) {
        if (imageSizeBytes > properties.maxUploadImageSizeBytes()) {
            throw new CustomProblemImageSizeExceededException();
        }
    }
}
