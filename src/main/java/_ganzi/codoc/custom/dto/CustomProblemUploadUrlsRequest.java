package _ganzi.codoc.custom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CustomProblemUploadUrlsRequest(@NotEmpty List<@Valid ImageUploadRequest> images) {

    public record ImageUploadRequest(
            @NotNull @Positive Integer order, @NotEmpty String fileName, @NotEmpty String contentType) {}
}
