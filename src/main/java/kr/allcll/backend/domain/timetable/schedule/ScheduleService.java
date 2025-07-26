package kr.allcll.backend.domain.timetable.schedule;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.domain.timetable.TimeTable;
import kr.allcll.backend.domain.timetable.TimeTableRepository;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleCreateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleDeleteRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleResponse;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleUpdateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeSlotDto;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeTableDetailResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleService {

    private final TimeTableRepository timeTableRepository;
    private final SubjectRepository subjectRepository;
    private final OfficialScheduleRepository officialScheduleRepository;
    private final CustomScheduleRepository customScheduleRepository;

    @Transactional
    public ScheduleResponse addSchedule(
        Long timeTableId,
        ScheduleCreateRequest request,
        String token
    ) {
        TimeTable timeTable = getAuthorizedTimeTable(timeTableId, token);

        if (ScheduleType.fromValue(request.scheduleType()) == ScheduleType.OFFICIAL) {
            validateDuplicatedSubject(request, timeTable);
            Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND));

            OfficialSchedule schedule = new OfficialSchedule(timeTable, subject);
            officialScheduleRepository.save(schedule);

            return ScheduleResponse.fromOfficial(schedule, subject);

        } else {
            List<TimeSlotDto> normalizedTimeSlots = normalizeAndValidateTimeSlots(request.timeSlots());

            CustomSchedule schedule = new CustomSchedule(
                timeTable,
                request.subjectName(),
                request.professorName(),
                request.location(),
                normalizedTimeSlots
            );
            customScheduleRepository.save(schedule);
            return ScheduleResponse.fromCustom(schedule);
        }
    }

    public TimeTableDetailResponse getTimeTableWithSchedules(Long timeTableId, String token) {
        TimeTable timeTable = getAuthorizedTimeTable(timeTableId, token);

        List<ScheduleResponse> schedules = Stream.concat(
                officialScheduleRepository.findAllByTimeTableId(timeTableId).stream()
                    .map(schedule -> ScheduleResponse.fromOfficial(schedule, schedule.getSubject())),
                customScheduleRepository.findAllByTimeTableId(timeTableId).stream()
                    .map(ScheduleResponse::fromCustom)
            )
            .sorted(Comparator.comparing(ScheduleResponse::scheduleId))
            .collect(Collectors.toList());

        return TimeTableDetailResponse.from(timeTable, schedules);
    }

    @Transactional
    public ScheduleResponse updateSchedule(
        Long timetableId,
        Long scheduleId,
        ScheduleUpdateRequest request,
        String token
    ) {
        List<TimeSlotDto> normalizedTimeSlots = normalizeAndValidateTimeSlots(request.timeSlots());

        TimeTable timeTable = getAuthorizedTimeTable(timetableId, token);

        CustomSchedule schedule = customScheduleRepository
            .findByIdAndTimeTableId(scheduleId, timeTable.getId())
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.CUSTOM_SCHEDULE_NOT_FOUND));

        schedule.updateSchedule(
            request.subjectName(),
            request.professorName(),
            request.location(),
            normalizedTimeSlots
        );

        return ScheduleResponse.fromCustom(schedule);
    }

    @Transactional
    public void deleteSchedule(
        Long timeTableId,
        Long scheduleId,
        ScheduleDeleteRequest request,
        String token
    ) {
        TimeTable timeTable = getAuthorizedTimeTable(timeTableId, token);

        if (ScheduleType.fromValue(request.scheduleType()) == ScheduleType.OFFICIAL) {
            officialScheduleRepository.deleteByIdAndTimeTableId(scheduleId, timeTable.getId());
        } else {
            customScheduleRepository.deleteByIdAndTimeTableId(scheduleId, timeTable.getId());
        }
    }

    private TimeTable getAuthorizedTimeTable(Long timeTableId, String token) {
        TimeTable timeTable = timeTableRepository.findById(timeTableId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.TIMETABLE_NOT_FOUND));
        if (!token.equals(timeTable.getToken())) {
            throw new AllcllException(AllcllErrorCode.UNAUTHORIZED_ACCESS);
        }
        return timeTable;
    }

    private void validateDuplicatedSubject(ScheduleCreateRequest request, TimeTable timeTable) {
        if (officialScheduleRepository.existsByTimeTableIdAndSubjectId(timeTable.getId(), request.subjectId())) {
            throw new AllcllException(AllcllErrorCode.DUPLICATE_SCHEDULE);
        }
    }

    private List<TimeSlotDto> normalizeAndValidateTimeSlots(List<TimeSlotDto> timeSlots) {
        return timeSlots.stream().map(
                timeSlot -> {
                    String startTimeRequest = normalizeTimeFormat(timeSlot.startTime());
                    String endTimeRequest = normalizeTimeFormat(timeSlot.endTime());

                    LocalTime startTime = LocalTime.parse(startTimeRequest);
                    LocalTime endTime = LocalTime.parse(endTimeRequest);

                    if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
                        throw new AllcllException(AllcllErrorCode.INVALID_TIME);
                    }
                    return new TimeSlotDto(timeSlot.dayOfWeeks(), startTime.toString(), endTime.toString());
                })
            .collect(Collectors.toList());
    }

    private String normalizeTimeFormat(String time) {
        if (time == null || time.isBlank()) {
            return "00:00";
        }

        String[] parts = time.split(":");
        if (parts.length != 2) {
            return "00:00";
        }

        String hourPart = parts[0].equalsIgnoreCase("undefined") ? "00" : parts[0];
        String minutePart = parts[1].equalsIgnoreCase("undefined") ? "00" : parts[1];

        String hour = hourPart.length() == 1 ? "0" + hourPart : hourPart;
        String minute = minutePart.length() == 1 ? "0" + minutePart : minutePart;

        return hour + ":" + minute;
    }
}
