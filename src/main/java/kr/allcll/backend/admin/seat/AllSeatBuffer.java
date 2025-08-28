package kr.allcll.backend.admin.seat;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import kr.allcll.backend.admin.seat.dto.ChangeSubjectsResponse;
import kr.allcll.crawler.common.properties.SjptProperties;
import org.springframework.stereotype.Component;

/**
 * deprecated : 변경감지로 정책 변경에 따라 해당 클래스를 사용하지 않습니다.
 */

@Component
public class AllSeatBuffer {

    private final Queue<ChangeSubjectsResponse> changedSubjectQueue;
    private final SjptProperties sjptProperties;

    public AllSeatBuffer(SjptProperties sjptProperties) {
        this.sjptProperties = sjptProperties;
        this.changedSubjectQueue = new ConcurrentLinkedQueue<>();
    }

    public void add(ChangeSubjectsResponse changedSubject) {
        changedSubjectQueue.add(changedSubject);
    }

    public void addAll(List<ChangeSubjectsResponse> changedSubjects) {
        changedSubjectQueue.addAll(changedSubjects);
    }

    public List<ChangeSubjectsResponse> getAllAndFlush() {
        List<ChangeSubjectsResponse> response = new ArrayList<>();
        ChangeSubjectsResponse item;
        int cnt = 0;
        while (cnt < 100 && (item = changedSubjectQueue.poll()) != null) {
            response.add(item);
            cnt++;
        }
        return response;
    }
}
