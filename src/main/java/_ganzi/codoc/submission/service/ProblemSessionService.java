package _ganzi.codoc.submission.service;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.submission.config.ProblemSessionProperties;
import _ganzi.codoc.submission.domain.ProblemSession;
import _ganzi.codoc.submission.enums.ProblemSessionStatus;
import _ganzi.codoc.submission.repository.ProblemSessionRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemSessionService {

    private final ProblemSessionRepository problemSessionRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final ProblemSessionProperties problemSessionProperties;

    @Transactional
    public ProblemSession resolveOrCreate(Long userId, Long problemId) {
        Instant now = Instant.now();
        ProblemSession activeSession =
                problemSessionRepository
                        .findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
                                userId, problemId, ProblemSessionStatus.ACTIVE)
                        .orElse(null);

        if (activeSession != null) {
            if (!activeSession.isExpired(now)) {
                return activeSession;
            }
            activeSession.markExpired();
            problemSessionRepository.save(activeSession);
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Problem problem =
                problemRepository.findById(problemId).orElseThrow(ProblemNotFoundException::new);

        Instant expiresAt = now.plus(problemSessionProperties.ttl());
        ProblemSession newSession = ProblemSession.create(user, problem, expiresAt);
        return problemSessionRepository.save(newSession);
    }

    @Transactional
    public ProblemSession requireActive(Long userId, Long problemId) {
        Instant now = Instant.now();
        ProblemSession activeSession =
                problemSessionRepository
                        .findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
                                userId, problemId, ProblemSessionStatus.ACTIVE)
                        .orElse(null);

        if (activeSession == null) {
            return null;
        }

        if (activeSession.isExpired(now)) {
            activeSession.markExpired();
            problemSessionRepository.save(activeSession);
            return null;
        }

        return activeSession;
    }
}
