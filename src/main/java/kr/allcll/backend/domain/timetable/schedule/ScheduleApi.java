package kr.allcll.backend.domain.timetable.schedule;

import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleCreateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleResponse;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleUpdateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeTableDetailResponse;
import kr.allcll.backend.support.web.ThreadLocalHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class ScheduleApi {

    private final ScheduleService scheduleService;

    @PostMapping("/api/timetables/{timeTableId}/schedules")
    public ResponseEntity<ScheduleResponse> addSchedule(
        @PathVariable(name = "timeTableId") Long timeTableId,
        @RequestBody ScheduleCreateRequest scheduleRequest
    ) {
        ScheduleResponse response = scheduleService.addSchedule(
            timeTableId,
            scheduleRequest,
            ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/timetables/{timeTableId}/schedules")
    public ResponseEntity<TimeTableDetailResponse> getTimeTableWithSchedules(
        @PathVariable(name = "timeTableId") Long timeTableId
    ) {
        TimeTableDetailResponse response = scheduleService.getTimeTableWithSchedules(timeTableId,
            ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/timetables/{timeTableId}/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
        @PathVariable(name = "timeTableId") Long timeTableId,
        @PathVariable(name = "scheduleId") Long scheduleId,
        @RequestBody ScheduleUpdateRequest updateRequest
    ) {
        ScheduleResponse updated = scheduleService.updateSchedule(
            timeTableId,
            scheduleId,
            updateRequest,
            ThreadLocalHolder.SHARED_TOKEN.get()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/timetables/{timeTableId}/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
        @PathVariable(name = "timeTableId") Long timeTableId,
        @PathVariable(name = "scheduleId") Long scheduleId
    ) {
        scheduleService.deleteSchedule(
            timeTableId,
            scheduleId,
            ThreadLocalHolder.SHARED_TOKEN.get()
        );
        return ResponseEntity.noContent().build();
    }
}
