package kr.allcll.backend.admin.subject;

import java.util.List;
import kr.allcll.crawler.common.entity.CrawlerSemester;
import kr.allcll.crawler.common.exception.CrawlerAllcllException;
import kr.allcll.crawler.subject.CrawlerSubject;
import kr.allcll.crawler.subject.CrawlerSubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSubjectService {

    private final SubjectFetcher subjectFetcher;
    private final CrawlerSubjectRepository crawlerSubjectRepository;

    @Transactional
    public SubjectSyncResult syncSubjects(String userId, String year, String semesterCode) {
        List<CrawlerSubject> allCrawlerSubjects = subjectFetcher.fetchSubjects(userId, year, semesterCode);
        log.info("[SubjectService] 현재 학사 정보 전체 과목 수: {}", allCrawlerSubjects.size());
        List<CrawlerSubject> existingCrawlerSubjects = crawlerSubjectRepository
            .findAllBySemesterAtIncludingDeleted(CrawlerSemester.now());
        log.info("[SubjectService] 기존 과목 수: {}", existingCrawlerSubjects.size());
        SubjectSyncResult syncResult = SubjectSyncProcessor.process(allCrawlerSubjects, existingCrawlerSubjects);
        saveAddedSubjects(syncResult);
        updateChangedSubjects(syncResult);
        deleteRemovedSubjects(syncResult);

        return syncResult;
    }

    private void deleteRemovedSubjects(SubjectSyncResult syncResult) {
        if (syncResult.subjectsToDeleteIsExist()) {
            List<CrawlerSubject> subjectsToDelete = syncResult.subjectsToDelete();
            log.info("[SubjectService] 삭제된 과목 수: {}", subjectsToDelete.size());
            softDeleteSubjects(subjectsToDelete);
        }
    }

    private void saveAddedSubjects(SubjectSyncResult syncResult) {
        if (syncResult.subjectsToAddIsExist()) {
            List<CrawlerSubject> subjectsToAdd = syncResult.subjectsToAdd();
            log.info("[SubjectService] 새로 추가된 과목 수: {}", subjectsToAdd.size());
            crawlerSubjectRepository.saveAll(subjectsToAdd);
        }
    }

    private void updateChangedSubjects(SubjectSyncResult syncResult) {
        if (syncResult.subjectsToUpdateIsExist()) {
            List<CrawlerSubject> subjectsToUpdate = syncResult.subjectsCanUpdate();
            log.info("[SubjectService] 변경된 과목 수: {}", subjectsToUpdate.size());
            for (CrawlerSubject updatedSubject : subjectsToUpdate) {
                CrawlerSubject existingSubject = crawlerSubjectRepository.findByLogicalKeyIncludingDeleted(
                    updatedSubject.getCuriNo(),
                    updatedSubject.getDeptCd(),
                    updatedSubject.getClassName(),
                    updatedSubject.getSmtCd(),
                    CrawlerSemester.now()
                ).orElseThrow(() -> new CrawlerAllcllException("SUBJECT_NOT_FOUND", "과목이 존재하지 않습니다."));
                existingSubject.updateFrom(updatedSubject);
            }
        }
    }

    private void softDeleteSubjects(List<CrawlerSubject> crawlerSubjects) {
        crawlerSubjects.forEach(CrawlerSubject::delete);
        crawlerSubjectRepository.saveAll(crawlerSubjects);
    }
}
