package kr.allcll.backend.admin.seat;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import kr.allcll.backend.admin.seat.dto.CrawledSubjectRemainingSeat;
import kr.allcll.crawler.common.properties.SjptProperties;
import org.springframework.stereotype.Component;

@Component
public class AllSeatBuffer {

    private final Queue<CrawledSubjectRemainingSeat> changedSubjectQueue;
    private final SjptProperties sjptProperties;

    public AllSeatBuffer(SjptProperties sjptProperties) {
        this.sjptProperties = sjptProperties;
        this.changedSubjectQueue = new ConcurrentLinkedQueue<>();
    }

    public void add(CrawledSubjectRemainingSeat changedSubject) {
        changedSubjectQueue.add(changedSubject);
    }

    public void addAll(List<CrawledSubjectRemainingSeat> changedSubjects) {
        changedSubjectQueue.addAll(changedSubjects);
    }

    public List<CrawledSubjectRemainingSeat> getAllAndFlush() {
        List<CrawledSubjectRemainingSeat> response = new ArrayList<>();
        CrawledSubjectRemainingSeat item;
        int cnt = 0;
        while (cnt < 100 && (item = changedSubjectQueue.poll()) != null) {
            response.add(item);
            cnt++;
        }
        return response;
    }
}
