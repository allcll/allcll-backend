package kr.allcll.backend.admin.basket;

import java.util.List;
import kr.allcll.crawler.basket.CrawlerBasket;
import kr.allcll.crawler.basket.CrawlerBasketRepository;
import kr.allcll.crawler.client.BasketClient;
import kr.allcll.crawler.client.payload.BasketPayload;
import kr.allcll.crawler.common.entity.CrawlerSemester;
import kr.allcll.crawler.credential.Credential;
import kr.allcll.crawler.credential.Credentials;
import kr.allcll.crawler.subject.CrawlerSubject;
import kr.allcll.crawler.subject.CrawlerSubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBasketService {

    private final Credentials credentials;
    private final BasketClient basketClient;
    private final CrawlerBasketRepository crawlerBasketRepository;
    private final CrawlerSubjectRepository crawlerSubjectRepository;

    public void fetchAndSaveBaskets(String userId) {
        Credential credential = credentials.findByUserId(userId);
        List<CrawlerSubject> crawlerSubjects = crawlerSubjectRepository.findAllBySemesterAt(CrawlerSemester.now());
        for (CrawlerSubject crawlerSubject : crawlerSubjects) {
            List<CrawlerBasket> notDuplicatedCrawlerBaskets = fetchBaskets(crawlerSubject, credential);
            crawlerBasketRepository.saveAll(notDuplicatedCrawlerBaskets);
            wait(1000);
        }
    }

    private List<CrawlerBasket> fetchBaskets(CrawlerSubject crawlerSubject, Credential credential) {
        BasketPayload requestPayload = BasketPayload.from(crawlerSubject);
        List<CrawlerBasket> crawlerBaskets = basketClient.execute(credential, requestPayload).toBaskets(crawlerSubject);
        return filterDuplicatedBaskets(crawlerBaskets);
    }

    private List<CrawlerBasket> filterDuplicatedBaskets(List<CrawlerBasket> crawlerBaskets) {
        return crawlerBaskets.stream()
            .filter(this::doesNotDuplicatedBasket)
            .toList();
    }

    /*
    중복된 Basket 기준은 다음과 같다.
    1. 한 과목에는 여러 개의 Basket이 존재할 수 있다. (subjectId)
    2. 위 조건을 만족하는 여러 개의 Basket 중에서는 학생 학과 코드로 구분한다. (studentDeptCd)
    3. 위 두 조건을 만족해도 여러 학기에 걸쳐 중복되는 Basket이 존재할 수 있다. 이것은 학기로 구분한다. (semesterAt)
    4. 하지만 3번 조건은 무시해도 된다. subjectId에 이미 semesterAt이 포함되어 있기 때문이다.
     */
    private boolean doesNotDuplicatedBasket(CrawlerBasket crawlerBasket) {
        return !crawlerBasketRepository.existsBySubjectIdAndStudentDeptCd(
            crawlerBasket.getSubjectId(),
            crawlerBasket.getStudentDeptCd()
        );
    }

    private void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Thread sleep error", e);
        }
    }
}
