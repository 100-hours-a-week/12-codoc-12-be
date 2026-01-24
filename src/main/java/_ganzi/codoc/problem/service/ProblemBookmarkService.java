package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.domain.Bookmark;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.BookmarkRepository;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemBookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerBookmark(Long userId, Long problemId) {

        if (bookmarkRepository.existsByUserIdAndProblemId(userId, problemId)) {
            return;
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Problem problem =
                problemRepository.findById(problemId).orElseThrow(ProblemNotFoundException::new);

        bookmarkRepository.save(Bookmark.create(user, problem));
    }
}
