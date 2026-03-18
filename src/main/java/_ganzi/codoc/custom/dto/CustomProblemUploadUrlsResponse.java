package _ganzi.codoc.custom.dto;

import java.time.Instant;
import java.util.List;

public record CustomProblemUploadUrlsResponse(List<ImageUploadUrlItem> images) {

    public record ImageUploadUrlItem(
            Integer order, String fileKey, String uploadUrl, Instant expiresAt) {}
}
