package kr.allcll.backend.admin.subject;

import java.util.List;
import kr.allcll.crawler.subject.CrawlerSubject;

public record SubjectSyncResult(
    List<CrawlerSubject> subjectsToAdd,
    List<CrawlerSubject> subjectsToDelete,
    List<CrawlerSubject> subjectsCanUpdate,
    List<SubjectDiffResult> updateDiffs
) {

    public boolean subjectsToAddIsExist() {
        return !subjectsToAdd.isEmpty();
    }

    public boolean subjectsToDeleteIsExist() {
        return !subjectsToDelete.isEmpty();
    }

    public boolean subjectsToUpdateIsExist() {
        return !subjectsCanUpdate.isEmpty();
    }
}
