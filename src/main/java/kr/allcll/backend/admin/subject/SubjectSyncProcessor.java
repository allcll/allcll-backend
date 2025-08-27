package kr.allcll.backend.admin.subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import kr.allcll.crawler.subject.CrawlerSubject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubjectSyncProcessor {

    public static SubjectSyncResult process(List<CrawlerSubject> newCrawlerSubjects,
        List<kr.allcll.crawler.subject.CrawlerSubject> existingCrawlerSubjects) {

        Set<String> newSubjectsKeySet = newCrawlerSubjects.stream()
            .map(SubjectSyncProcessor::generateSubjectKey)
            .collect(Collectors.toSet());

        Map<String, kr.allcll.crawler.subject.CrawlerSubject> existingSubjectsWithKey = existingCrawlerSubjects.stream()
            .collect(Collectors.toMap(
                SubjectSyncProcessor::generateSubjectKey,
                subject -> subject
            ));

        List<kr.allcll.crawler.subject.CrawlerSubject> subjectsToAdd = newCrawlerSubjects.stream()
            .filter(subject -> !existingSubjectsWithKey.containsKey(generateSubjectKey(subject)))
            .toList();

        List<kr.allcll.crawler.subject.CrawlerSubject> subjectsToDelete = existingCrawlerSubjects.stream()
            .filter(subject -> !newSubjectsKeySet.contains(generateSubjectKey(subject)))
            .toList();

        List<SubjectDiffResult> updateDiffs = new ArrayList<>();
        List<CrawlerSubject> subjectsToUpdate = newCrawlerSubjects.stream()
            .filter(newSubject -> {
                String key = generateSubjectKey(newSubject);
                kr.allcll.crawler.subject.CrawlerSubject existingSubject = existingSubjectsWithKey.get(key);
                if (existingSubject == null) {
                    return false;
                }
                boolean shouldUpdate =
                    (existingSubject.isDeleted() == true && newSubject.isDeleted() == false)
                        || hasDifferent(existingSubject, newSubject);
                if (shouldUpdate) {
                    String lesnEmpDiff = formatDiff(existingSubject.getLesnEmp(), newSubject.getLesnEmp());
                    String lesnTimeDiff = formatDiff(existingSubject.getLesnTime(), newSubject.getLesnTime());
                    String lesnRoomDiff = formatDiff(existingSubject.getLesnRoom(), newSubject.getLesnRoom());
                    String isDeletedDiff = formatDiffBoolean(existingSubject.isDeleted(), newSubject.isDeleted());

                    updateDiffs.add(new SubjectDiffResult(
                        existingSubject.getId(),
                        newSubject.getCuriNo(),
                        newSubject.getCuriNm(),
                        lesnRoomDiff,
                        lesnTimeDiff,
                        lesnEmpDiff,
                        isDeletedDiff
                    ));
                }
                return shouldUpdate;
            })
            .toList();

        return new SubjectSyncResult(subjectsToAdd, subjectsToDelete, subjectsToUpdate, updateDiffs);
    }

    private static String generateSubjectKey(kr.allcll.crawler.subject.CrawlerSubject crawlerSubject) {
        return crawlerSubject.getCuriNo() + "|" +
            crawlerSubject.getDeptCd() + "|" +
            crawlerSubject.getClassName() + "|" +
            crawlerSubject.getSmtCd();
    }

    private static boolean hasDifferent(kr.allcll.crawler.subject.CrawlerSubject oldValue, CrawlerSubject newValue) {
        return !safeEquals(oldValue.getLesnRoom(), newValue.getLesnRoom())
            || !safeEquals(oldValue.getLesnTime(), newValue.getLesnTime())
            || !safeEquals(oldValue.getLesnEmp(), newValue.getLesnEmp());
    }

    private static boolean safeEquals(String oldValue, String newValue) {
        String aa = (oldValue == null) ? "" : oldValue.trim();
        String bb = (newValue == null) ? "" : newValue.trim();
        return aa.equals(bb);
    }

    private static String formatDiff(String oldValue, String newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return "•";
        }
        String oldSafe = (oldValue == null || oldValue.isBlank()) ? "-" : oldValue;
        String newSafe = (newValue == null || newValue.isBlank()) ? "-" : newValue;
        return oldSafe + " → " + newSafe;
    }

    private static String formatDiffBoolean(Boolean oldValue, Boolean newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return "•";
        }
        String oldSafe = oldValue == null ? "-" : String.valueOf(oldValue);
        String newSafe = (newValue == null) ? "-" : String.valueOf(newValue);
        return oldSafe + " → " + newSafe;
    }
}
