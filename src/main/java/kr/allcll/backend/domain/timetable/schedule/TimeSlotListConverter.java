package kr.allcll.backend.domain.timetable.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.List;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeSlotDto;

@Converter
public class TimeSlotListConverter implements AttributeConverter<List<TimeSlotDto>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<TimeSlotDto> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert TimeSlot list to JSON", e);
        }
    }

    @Override
    public List<TimeSlotDto> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<List<TimeSlotDto>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
