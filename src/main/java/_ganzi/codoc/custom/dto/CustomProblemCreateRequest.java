package _ganzi.codoc.custom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CustomProblemCreateRequest(@NotEmpty List<@Valid ImageItem> images) {

    public record ImageItem(@NotNull @Positive Integer order, @NotEmpty String fileKey) {}
}
