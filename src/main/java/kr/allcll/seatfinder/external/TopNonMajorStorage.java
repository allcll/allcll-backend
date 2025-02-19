package kr.allcll.seatfinder.external;

import java.util.ArrayList;
import java.util.List;
import kr.allcll.seatfinder.subject.Subject;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class TopNonMajorStorage {

    private final List<Subject> subjects;

    public TopNonMajorStorage() {
        this.subjects = new ArrayList<>();
    }

    public void addAll(List<Subject> subjects) {
        this.subjects.addAll(subjects);
    }
}
