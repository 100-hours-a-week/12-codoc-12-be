package _ganzi.codoc.custom.domain;

import _ganzi.codoc.custom.enums.CustomProblemStatus;
import _ganzi.codoc.global.converter.StringListConverter;
import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "custom_problem")
@Entity
public class CustomProblem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomProblemStatus status;

    @Convert(converter = StringListConverter.class)
    @Column(name = "source_file_keys", nullable = false, columnDefinition = "json")
    private List<String> sourceFileKeys;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private CustomProblem(Long userId, CustomProblemStatus status, List<String> sourceFileKeys) {
        this.userId = userId;
        this.status = status;
        this.sourceFileKeys = sourceFileKeys;
    }

    public static CustomProblem createProcessing(Long userId, List<String> sourceFileKeys) {
        return new CustomProblem(userId, CustomProblemStatus.PROCESSING, sourceFileKeys);
    }

    public void complete(String title, String content) {
        if (isDeleted() || getStatus() != CustomProblemStatus.PROCESSING) {
            return;
        }

        this.status = CustomProblemStatus.COMPLETED;
        this.title = title;
        this.content = content;
    }

    public void fail() {
        if (isDeleted() || getStatus() != CustomProblemStatus.PROCESSING) {
            return;
        }

        this.status = CustomProblemStatus.FAILED;
    }

    public void markDeleted(Instant deletedAt) {
        this.isDeleted = true;
        this.deletedAt = deletedAt;
    }
}
