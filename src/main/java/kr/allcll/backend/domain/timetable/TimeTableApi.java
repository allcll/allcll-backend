package kr.allcll.backend.domain.timetable;

import kr.allcll.backend.domain.timetable.dto.TimeTableResponse;
import kr.allcll.backend.domain.timetable.dto.TimeTablesResponse;
import kr.allcll.backend.support.web.ThreadLocalHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TimeTableApi {

    private final TimeTableService timeTableService;

    @GetMapping("/api/timetables")
    public ResponseEntity<TimeTablesResponse> getTimetables() {
        TimeTablesResponse timeTablesResponse = timeTableService.getTimetables(ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.ok(timeTablesResponse);
    }

    @PostMapping("/api/timetable")
    public ResponseEntity<Void> createTimeTable(@RequestBody Map<String, String> request) {
        String timetableName = request.get("timetableName");
        String semester = request.get("semester");

        timeTableService.createTimeTable(ThreadLocalHolder.SHARED_TOKEN.get(), timetableName, semester);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/timetables/{timetableId}")
    public ResponseEntity updateTimeTable(
            @PathVariable Long timetableId,
            @RequestBody Map<String, String> request
    ) {
        String newTitle = request.get("title");
        return ResponseEntity.ok(timeTableService.updateTimeTable(timetableId, newTitle, ThreadLocalHolder.SHARED_TOKEN.get()));
    }

    @DeleteMapping("/api/timetables/{timetableId}")
    public ResponseEntity<Void> deleteTimeTable(@PathVariable Long timetableId) {
        timeTableService.deleteTimeTable(timetableId, ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.noContent().build();
    }
}
