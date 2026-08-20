package kr.allcll.backend.admin.seat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import kr.allcll.crawler.subject.CrawlerSubject;
import org.springframework.stereotype.Component;

@Component
public class TargetSubjectStorage {

    private final Queue<CrawlerSubject> generalSubjectsQueue;
    private final Queue<CrawlerSubject> firstPriorityQueue;
    private final Queue<CrawlerSubject> secondPriorityQueue;
    private final Queue<CrawlerSubject> thirdPriorityQueue;

    public TargetSubjectStorage() {
        this.generalSubjectsQueue = new ConcurrentLinkedQueue<>();
        this.firstPriorityQueue = new ConcurrentLinkedQueue<>();
        this.secondPriorityQueue = new ConcurrentLinkedQueue<>();
        this.thirdPriorityQueue = new ConcurrentLinkedQueue<>();
    }

    public void addGeneralSubjects(List<CrawlerSubject> crawlerSubjects) {
        generalSubjectsQueue.clear();
        generalSubjectsQueue.addAll(crawlerSubjects);
    }

    public void addPinSubjects(Map<CrawlerSubject, Integer> subjects) {
        firstPriorityQueue.clear();
        secondPriorityQueue.clear();
        thirdPriorityQueue.clear();
        for (Map.Entry<CrawlerSubject, Integer> entry : subjects.entrySet()) {
            CrawlerSubject crawlerSubject = entry.getKey();
            int priority = entry.getValue();
            switch (priority) {
                case 1:
                    firstPriorityQueue.add(crawlerSubject);
                    break;
                case 2:
                    secondPriorityQueue.add(crawlerSubject);
                    break;
                case 3:
                    thirdPriorityQueue.add(crawlerSubject);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid priority: " + priority);
            }
        }
    }

    public CrawlerSubject getNextGeneralTarget() {
        CrawlerSubject crawlerSubject = generalSubjectsQueue.poll();
        if (crawlerSubject != null) {
            generalSubjectsQueue.add(crawlerSubject);
        }
        return crawlerSubject;
    }

    public CrawlerSubject getNextPinTarget() {
        Priority priority = Priority.getRandomPriority(
            firstPriorityQueue.size(),
            secondPriorityQueue.size(),
            thirdPriorityQueue.size()
        );

        if (priority == null) {
            return null; // 모든 큐가 비어있을 경우
        }

        CrawlerSubject crawlerSubject = null;
        switch (priority) {
            case FIRST:
                crawlerSubject = firstPriorityQueue.poll();
                if (crawlerSubject != null) {
                    firstPriorityQueue.add(crawlerSubject);
                }
                break;
            case SECOND:
                crawlerSubject = secondPriorityQueue.poll();
                if (crawlerSubject != null) {
                    secondPriorityQueue.add(crawlerSubject);
                }
                break;
            case THIRD:
                crawlerSubject = thirdPriorityQueue.poll();
                if (crawlerSubject != null) {
                    thirdPriorityQueue.add(crawlerSubject);
                }
                break;
        }
        return crawlerSubject;
    }

    public List<CrawlerSubject> getTargetSubjects() {
        List<CrawlerSubject> crawlerSubjects = new LinkedList<>();
        crawlerSubjects.addAll(firstPriorityQueue);
        crawlerSubjects.addAll(secondPriorityQueue);
        crawlerSubjects.addAll(thirdPriorityQueue);
        return crawlerSubjects;
    }

    public List<CrawlerSubject> getTargetGeneralSubjects() {
        return new LinkedList<>(generalSubjectsQueue);
    }

    private enum Priority {
        FIRST(50),
        SECOND(30),
        THIRD(20);

        private static final Random random = new Random();
        private final int baseWeight;

        Priority(int baseWeight) {
            this.baseWeight = baseWeight;
        }

        /**
         * 각 우선순위의 큐 사이즈에 따라 가중치를 산출한다. - 큐에 값이 하나이면 기본 가중치 그대로 사용 - 그 이상이면, 추가 요소마다 기본 가중치의 1/10씩 추가 (dampening factor
         * = 10)
         */
        private static int getAdjustedWeight(int queueSize, int baseWeight) {
            if (queueSize <= 0) {
                return 0;
            }
            // 큐에 1개 있을 때는 기본 가중치 그대로, 그 외에는 (queueSize - 1) * (baseWeight / 10) 만큼 추가
            return baseWeight + (int) ((queueSize - 1) * (baseWeight / 10.0));
        }

        /**
         * 각 큐의 사이즈와 기본 가중치를 반영하여 우선순위를 랜덤하게 선택한다.
         */
        public static Priority getRandomPriority(
            int firstQueueSize,
            int secondQueueSize,
            int thirdQueueSize
        ) {
            int firstWeight = getAdjustedWeight(firstQueueSize, FIRST.baseWeight);
            int secondWeight = getAdjustedWeight(secondQueueSize, SECOND.baseWeight);
            int thirdWeight = getAdjustedWeight(thirdQueueSize, THIRD.baseWeight);

            int totalWeight = firstWeight + secondWeight + thirdWeight;
            if (totalWeight == 0) {
                return null; // 모든 큐가 비어있을 경우
            }

            int randomWeight = random.nextInt(totalWeight);
            if (randomWeight < firstWeight) {
                return FIRST;
            } else if (randomWeight < firstWeight + secondWeight) {
                return SECOND;
            } else {
                return THIRD;
            }
        }
    }
}
