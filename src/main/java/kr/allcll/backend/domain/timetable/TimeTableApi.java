package kr.allcll.backend.domain.timetable;

import kr.allcll.backend.domain.timetable.dto.TimeTableCreateRequest;
import kr.allcll.backend.domain.timetable.dto.TimeTableResponse;
import kr.allcll.backend.domain.timetable.dto.TimeTableUpdateRequest;
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

@RestController
@RequiredArgsConstructor
public class TimeTableApi {

    private final TimeTableService timeTableService;

    @PostMapping("/api/timetable")
    public ResponseEntity<Void> createTimeTable(@RequestBody TimeTableCreateRequest request) {
        timeTableService.createTimeTable(ThreadLocalHolder.SHARED_TOKEN.get(), request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/timetables/{timeTableId}")
    public ResponseEntity<TimeTableResponse> updateTimeTable(
            @PathVariable Long timeTableId,
            @RequestBody TimeTableUpdateRequest request
    ) {
        TimeTableResponse response = timeTableService.updateTimeTable(
                timeTableId,
                request.title(),
                ThreadLocalHolder.SHARED_TOKEN.get()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/timetables/{timeTableId}")
    public ResponseEntity<Void> deleteTimeTable(@PathVariable Long timeTableId) {
        timeTableService.deleteTimeTable(timeTableId, ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/timetables")
    public ResponseEntity<TimeTablesResponse> getTimeTables() {
        TimeTablesResponse timeTablesResponse = timeTableService.getTimetables(ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.ok(timeTablesResponse);
    }
}
