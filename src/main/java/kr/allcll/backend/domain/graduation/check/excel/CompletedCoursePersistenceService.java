package kr.allcll.backend.domain.graduation.check.excel;

import java.util.List;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompletedCoursePersistenceService {

    private final UserRepository userRepository;
    private final CompletedCourseRepository completedCourseRepository;

    @Transactional
    public List<CompletedCourse> saveAllCompletedCourse(
        Long userId,
        List<CompletedCourseDto> parsedCourses
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));

        completedCourseRepository.deleteByUserId(userId);
        List<CompletedCourse> completedCourses = parsedCourses.stream()
            .map(parsedCourse -> parsedCourse.toEntity(user.getId(), user.getAdmissionYear()))
            .toList();
        return completedCourseRepository.saveAll(completedCourses);
    }

    public List<CompletedCourse> getCompletedCourses(Long userId) {
        return completedCourseRepository.findAllByUserId(userId);
    }
}
