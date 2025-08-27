package kr.allcll.backend.admin.seat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.allcll.backend.admin.seat.dto.ChangeSubjectsResponse;
import kr.allcll.crawler.common.exception.CrawlerAllcllException;
import kr.allcll.crawler.subject.CrawlerSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 교양 과목의 상태를 관리한다.
 */
@Slf4j
@Component
public class LiveBoards {

    private static final int MAX_SIZE = 20;
    private final Map<CrawlerSubject, Integer> liveBoardSubjects; //subject, 여석 수

    public LiveBoards() {
        this.liveBoardSubjects = new ConcurrentHashMap<>();
    }

    public List<ChangeSubjectsResponse> checkStatus(CrawlerSubject crawlerSubject, Integer remainSeat) {
        if (canOnlyIn(crawlerSubject, remainSeat)) {
            liveBoardSubjects.put(crawlerSubject, remainSeat);
            return List.of(ChangeSubjectsResponse.of(crawlerSubject.getId(), ChangeStatus.IN, remainSeat));
        }
        CrawlerSubject maxCrawlerSubject = findMaxRemainSeatSubject();
        Integer maxSubjectRemainSeat = liveBoardSubjects.get(maxCrawlerSubject);
        if (canInAndOut(crawlerSubject, maxCrawlerSubject, remainSeat)) {
            liveBoardSubjects.put(crawlerSubject, remainSeat);
            liveBoardSubjects.remove(maxCrawlerSubject);
            return List.of(
                ChangeSubjectsResponse.of(crawlerSubject.getId(), ChangeStatus.IN, remainSeat),
                ChangeSubjectsResponse.of(maxCrawlerSubject.getId(), ChangeStatus.OUT, maxSubjectRemainSeat)
            );
        }
        if (canOut(crawlerSubject, remainSeat)) {
            liveBoardSubjects.remove(crawlerSubject);
            return List.of(ChangeSubjectsResponse.of(crawlerSubject.getId(), ChangeStatus.OUT, remainSeat));
        }
        if (canUpdate(crawlerSubject, remainSeat)) {
            liveBoardSubjects.put(crawlerSubject, remainSeat);
            return List.of(ChangeSubjectsResponse.of(crawlerSubject.getId(), ChangeStatus.UPDATE, remainSeat));
        }
        return Collections.emptyList();
    }

    private boolean canUpdate(CrawlerSubject crawlerSubject, Integer remainSeat) {
        return isExistAtLiveBoard(crawlerSubject) && isRemainSeatNotEmpty(remainSeat);
    }

    private boolean canInAndOut(CrawlerSubject crawlerSubject, CrawlerSubject maxRemainSeatCrawlerSubject,
        Integer compareRemainSeat) {
        return isNotExistAtLiveBoard(crawlerSubject)
            && isMoreThanMaxSize()
            && liveBoardSubjects.get(maxRemainSeatCrawlerSubject) > compareRemainSeat;
    }

    /*
    추가할 수 있는 조건: 전광판 과목이 20개보다 적거나, 전광판 꼴찌보다 새로운 과목의 여석 값이 작은 경우. 또한 0개인 경우 IN될 수 없다.
    교양과목인 것은 전제되어있다.
     */
    public boolean canOnlyIn(CrawlerSubject crawlerSubject, Integer compareRemainSeat) {
        return isNotExistAtLiveBoard(crawlerSubject) && isFewerThanMaxSize() && isRemainSeatNotEmpty(compareRemainSeat);
    }

    public boolean canOut(CrawlerSubject crawlerSubject, Integer compareRemainSeat) {
        return isExistAtLiveBoard(crawlerSubject) && isRemainSeatBecomeEmpty(compareRemainSeat);
    }

    private CrawlerSubject findMaxRemainSeatSubject() {
        return liveBoardSubjects.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElseThrow(() -> new CrawlerAllcllException("MAP_EMPTY", "전광판이 비어있습니다."))
            .getKey();
    }

    private boolean isRemainSeatNotEmpty(Integer remainSeat) {
        return !isRemainSeatBecomeEmpty(remainSeat);
    }

    private boolean isRemainSeatBecomeEmpty(Integer remainSeat) {
        return remainSeat.equals(0);
    }

    private boolean isMoreThanMaxSize() {
        return !isFewerThanMaxSize();
    }

    private boolean isFewerThanMaxSize() {
        return liveBoardSubjects.size() < MAX_SIZE;
    }

    private boolean isNotExistAtLiveBoard(CrawlerSubject crawlerSubject) {
        return !isExistAtLiveBoard(crawlerSubject);
    }

    private boolean isExistAtLiveBoard(CrawlerSubject crawlerSubject) {
        return liveBoardSubjects.containsKey(crawlerSubject);
    }
}
