package ru.practicum.ewm.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.model.StateEvent;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.ewm.utils.Constants.FORMATTER_FOR_DATETIME;


@JsonTest
class EventDtoTest {

    @Autowired
    private JacksonTester<EventDto> json;

    @Test
    void testEventFullDto() throws Exception {
        EventDto eventFullDto = new EventDto();
        eventFullDto.setId(1L);
        eventFullDto.setAnnotation("Annotation new");

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(8L);
        categoryDto.setName("Name Category");
        eventFullDto.setCategory(categoryDto);

        eventFullDto.setConfirmedRequests(90L);
        eventFullDto.setCreatedOn(LocalDateTime.now());
        eventFullDto.setDescription("Description");
        eventFullDto.setEventDate(LocalDateTime.now().plusHours(10));

        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(55L);
        userShortDto.setName("Name User");
        eventFullDto.setInitiator(userShortDto);

        LocationDto locationDto = new LocationDto();
        locationDto.setId(4L);
        locationDto.setLat(45.89f);
        locationDto.setLon(33.81f);
        eventFullDto.setLocation(locationDto);

        eventFullDto.setPaid(true);
        eventFullDto.setParticipantLimit(34);
        eventFullDto.setPublishedOn(LocalDateTime.now().plusDays(10));
        eventFullDto.setRequestModeration(false);

        eventFullDto.setState(StateEvent.PENDING);
        eventFullDto.setTitle("Title");
        eventFullDto.setViews(2L);

        JsonContent<EventDto> result = json.write(eventFullDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.annotation");
        assertThat(result).hasJsonPath("$.category");
        assertThat(result).hasJsonPath("$.confirmedRequests");
        assertThat(result).hasJsonPath("$.createdOn");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.eventDate");
        assertThat(result).hasJsonPath("$.initiator");
        assertThat(result).hasJsonPath("$.location");
        assertThat(result).hasJsonPath("$.paid");
        assertThat(result).hasJsonPath("$.participantLimit");
        assertThat(result).hasJsonPath("$.publishedOn");
        assertThat(result).hasJsonPath("$.requestModeration");
        assertThat(result).hasJsonPath("$.state");
        assertThat(result).hasJsonPath("$.title");
        assertThat(result).hasJsonPath("$.views");

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(eventFullDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.annotation")
                .isEqualTo(eventFullDto.getAnnotation());
        assertThat(result).extractingJsonPathNumberValue("$.category.id")
                .isEqualTo(eventFullDto.getCategory().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.category.name")
                .isEqualTo(eventFullDto.getCategory().getName());
        assertThat(result).extractingJsonPathNumberValue("$.confirmedRequests")
                .isEqualTo(eventFullDto.getConfirmedRequests().intValue());
        assertThat(result).extractingJsonPathStringValue("$.createdOn")
                .isEqualTo(eventFullDto.getCreatedOn().format(FORMATTER_FOR_DATETIME));
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(eventFullDto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo(eventFullDto.getEventDate().format(FORMATTER_FOR_DATETIME));
        assertThat(result).extractingJsonPathNumberValue("$.initiator.id")
                .isEqualTo(eventFullDto.getInitiator().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.initiator.name")
                .isEqualTo(eventFullDto.getInitiator().getName());
        assertThat(result).extractingJsonPathNumberValue("$.location.id")
                .isEqualTo(eventFullDto.getLocation().getId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.location.lat")
                .isEqualTo(Double.parseDouble(eventFullDto.getLocation().getLat().toString()));
        assertThat(result).extractingJsonPathNumberValue("$.location.lon")
                .isEqualTo(Double.parseDouble(eventFullDto.getLocation().getLon().toString()));
        assertThat(result).extractingJsonPathBooleanValue("$.paid")
                .isEqualTo(eventFullDto.getPaid().booleanValue());
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit")
                .isEqualTo(eventFullDto.getParticipantLimit().intValue());
        assertThat(result).extractingJsonPathStringValue("$.publishedOn")
                .isEqualTo(eventFullDto.getPublishedOn().format(FORMATTER_FOR_DATETIME));
        assertThat(result).extractingJsonPathBooleanValue("$.requestModeration")
                .isEqualTo(eventFullDto.getRequestModeration().booleanValue());
        assertThat(result).extractingJsonPathStringValue("$.state")
                .isEqualTo(eventFullDto.getState().toString());
        assertThat(result).extractingJsonPathStringValue("$.title")
                .isEqualTo(eventFullDto.getTitle());
        assertThat(result).extractingJsonPathNumberValue("$.views")
                .isEqualTo(eventFullDto.getViews().intValue());
    }

}