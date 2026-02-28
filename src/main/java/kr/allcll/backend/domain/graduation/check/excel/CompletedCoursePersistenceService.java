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
    public List<CompletedCourse> saveAllCompletedCourse(Long userId, List<CompletedCourseDto> completedCourses) {
        completedCourseRepository.deleteByUserId(userId);

        List<CompletedCourse> entities = completedCourses.stream()
            .map(dto -> dto.toEntity(userId))
            .toList();

        return completedCourseRepository.saveAll(entities);
    }
}
