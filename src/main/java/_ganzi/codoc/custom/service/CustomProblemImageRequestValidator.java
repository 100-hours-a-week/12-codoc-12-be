package _ganzi.codoc.custom.service;

import _ganzi.codoc.custom.config.CustomProblemProperties;
import _ganzi.codoc.custom.dto.CustomProblemCreateRequest;
import _ganzi.codoc.custom.dto.CustomProblemUploadUrlsRequest;
import _ganzi.codoc.custom.exception.CustomProblemDuplicateImageOrderException;
import _ganzi.codoc.custom.exception.CustomProblemImageCountExceededException;
import _ganzi.codoc.custom.exception.CustomProblemInvalidImageContentTypeException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomProblemImageRequestValidator {

    private final CustomProblemProperties properties;

    public void validateUploadImages(List<CustomProblemUploadUrlsRequest.ImageUploadRequest> images) {
        validateImageCount(images.size());
        validateUniqueOrders(
                images.stream().map(CustomProblemUploadUrlsRequest.ImageUploadRequest::order).toList());
        validateContentTypes(
                images.stream()
                        .map(CustomProblemUploadUrlsRequest.ImageUploadRequest::contentType)
                        .toList());
    }

    public void validateCreateImages(List<CustomProblemCreateRequest.ImageItem> images) {
        validateImageCount(images.size());
        validateUniqueOrders(images.stream().map(CustomProblemCreateRequest.ImageItem::order).toList());
    }

    private void validateImageCount(int imageCount) {
        if (imageCount > properties.maxUploadImageCount()) {
            throw new CustomProblemImageCountExceededException();
        }
    }

    private void validateUniqueOrders(List<Integer> orders) {
        Set<Integer> uniqueOrders = new HashSet<>(orders);
        if (uniqueOrders.size() != orders.size()) {
            throw new CustomProblemDuplicateImageOrderException();
        }
    }

    private void validateContentTypes(List<String> contentTypes) {
        for (String contentType : contentTypes) {
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new CustomProblemInvalidImageContentTypeException();
            }
        }
    }
}
