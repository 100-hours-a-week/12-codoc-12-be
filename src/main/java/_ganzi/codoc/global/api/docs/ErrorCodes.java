package _ganzi.codoc.global.api.docs;

import _ganzi.codoc.global.exception.GlobalErrorCode;
import _ganzi.codoc.problem.exception.ProblemErrorCode;
import _ganzi.codoc.submission.exception.SubmissionErrorCode;
import _ganzi.codoc.user.exception.QuestErrorCode;
import _ganzi.codoc.user.exception.UserErrorCode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorCodes {

    GlobalErrorCode[] global() default {};

    SubmissionErrorCode[] submission() default {};

    ProblemErrorCode[] problem() default {};

    UserErrorCode[] user() default {};

    QuestErrorCode[] quest() default {};
}
