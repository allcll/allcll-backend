package kr.allcll.backend.domain.graduation.check.excel;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompletedCoursePersistenceService {

    private final CompletedCourseRepository completedCourseRepository;

    @Transactional
    public List<CompletedCourse> saveAllCompletedCourse(Long userId, List<CompletedCourseDto> parsedCourses) {
        completedCourseRepository.deleteByUserId(userId);

        List<CompletedCourse> completedCourses = parsedCourses.stream()
            .map(parsedCourse -> parsedCourse.toEntity(userId))
            .toList();

        return completedCourseRepository.saveAll(completedCourses);
    }

    public List<CompletedCourse> getCompletedCourses(Long userId) {
        return completedCourseRepository.findAllByUserId(userId);
    }
}
