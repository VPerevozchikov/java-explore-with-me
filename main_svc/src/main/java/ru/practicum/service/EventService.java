package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryMapper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.dto.participationRequest.UpdateRequestState;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.*;
import ru.practicum.model.*;
import ru.practicum.repository.EventJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class EventService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventJpaRepository eventJpaRepository;

    private final CategoryService categoryService;
    private final UserService userService;
    private final ParticipationService participationService;
    private final EntityManager entityManager;

    @Autowired
    public EventService(EventJpaRepository eventJpaRepository, CategoryService categoryService, UserService userService,
                        ParticipationService participationService, EntityManager entityManager) {
        this.eventJpaRepository = eventJpaRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.participationService = participationService;
        this.entityManager = entityManager;
    }

    public EventFullDto createEvent(NewEventDto newEventDto, int userId) {
        EventFullDto eventFullDto = new EventFullDto();
        LocalDateTime newEventDateTime = LocalDateTime.parse(newEventDto.getEventDate(), TIME_FORMAT);
        if (HOURS.between(LocalDateTime.now(), newEventDateTime) < 2) {
            throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
        }
        Category category = CategoryMapper.toCategory(categoryService.getCategoryById(newEventDto.getCategory()));
        User user = UserMapper.toUser(userService.getUserById(userId));

        Event event = EventMapper.toEvent(newEventDto, category, user);
        Event savedEvent = eventJpaRepository.save(event);

        eventFullDto = EventMapper.toFullDto(savedEvent, 0);
        return eventFullDto;
    }

    public List<EventShortDto> getEventsByCategory(int catId) {
        if (catId <= 0) {
            throw new BadParameterException("Id категории должен быть >0");
        }
        List<Event> events = eventJpaRepository.findByCategoryId(catId);
        if (events.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream().map(Event::getId).collect(Collectors.toList()));

        return events.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public List<EventShortDto> getAllByUser(int userId, int from, int size) {
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (from < 0 || size < 1) {
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Event> events = eventJpaRepository.getAllByUser(userId, page);
        if (events == null || events.isEmpty()) {
            return new ArrayList<EventShortDto>();
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream().map(Event::getId).collect(Collectors.toList()));

        return events.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public EventFullDto getByUserAndId(int userId, int eventId) {
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (eventId < 0) {
            throw new BadParameterException("Id события должен быть больше 0");
        }

        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId);
        if (event == null) {
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId()));

        return EventMapper.toFullDto(event, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    public EventFullDto getEventById(int eventId) {
        if (eventId < 0) {
            throw new BadParameterException("Id события должен быть больше 0");
        }
        Optional<Event> eventOptional = eventJpaRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new ElementNotFoundException("События с id=" + eventId + " не найдено");
        }
        Event event = eventOptional.get();
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId()));
        return EventMapper.toFullDto(event, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    public EventFullDto getEventByIdWithStats(int eventId, HttpServletRequest request) {
        EventFullDto eventDto = this.getEventById(eventId);
        if (eventDto.getState() != EventState.PUBLISHED) {
            throw new ElementNotFoundException("Событие с id=" + eventId + " не опубликовано");
        }
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-event-service");
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
        endpointHitDto.setUri(request.getRequestURI());

        StatsClient.postHit(endpointHitDto);

        return eventDto;
    }

    public EventFullDto patchEvent(int userId, int eventId, UpdateEventUserRequest updateRequest) {
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (eventId < 0) {
            throw new BadParameterException("Id соытия должен быть больше 0");
        }

        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId);
        if (event == null) {
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new DataConflictException("Нельзя обновлять событие в состоянии 'Опубликовано'");
        }

        String annotation = updateRequest.getAnnotation();
        if (!(annotation == null || annotation.isBlank())) {
            event.setAnnotation(annotation);
        }
        Integer categoryId = updateRequest.getCategory();
        if (categoryId != null && categoryId > 0) {
            CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
            if (categoryDto != null) {
                event.setCategory(CategoryMapper.toCategory(categoryDto));
            }
        }
        String newDateString = updateRequest.getEventDate();
        if (!(newDateString == null || newDateString.isBlank())) {
            LocalDateTime newDate = LocalDateTime.parse(newDateString, TIME_FORMAT);
            if (HOURS.between(LocalDateTime.now(), newDate) < 2) {
                throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
            }
            event.setEventDate(newDate);
        }
        Location location = updateRequest.getLocation();
        if (location != null) {
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        String stateString = updateRequest.getStateAction();
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionUser.valueOf(stateString)) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }
        String title = updateRequest.getTitle();
        if (!(title == null || title.isBlank())) {
            event.setTitle(title);
        }

        eventJpaRepository.save(event);
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId()));
        Optional<Event> eventOptional = eventJpaRepository.findById(event.getId());

        return EventMapper.toFullDto(eventOptional.get(), idViewsMap.getOrDefault(event.getId(), 0L));
    }

    public EventFullDto patchAdminEvent(int eventId, UpdateEventAdminRequest adminRequest) {
        if (eventId < 0) {
            throw new BadParameterException("Id события должен быть больше 0");
        }

        Optional<Event> eventOptional = eventJpaRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new ElementNotFoundException("События с id=" + eventId + " не найдено");
        }
        Event event = eventOptional.get();

        String annotation = adminRequest.getAnnotation();
        if (!(annotation == null || annotation.isBlank())) {
            event.setAnnotation(annotation);
        }
        int categoryId = adminRequest.getCategory();
        if (categoryId > 0) {
            CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
            if (categoryDto != null) {
                event.setCategory(CategoryMapper.toCategory(categoryDto));
            }
        }
        String description = adminRequest.getDescription();
        if (!(description == null || description.isBlank())) {
            event.setDescription(description);
        }

        String newDateString = adminRequest.getEventDate();
        if (!(newDateString == null || newDateString.isBlank())) {
            LocalDateTime newDate = LocalDateTime.parse(newDateString, TIME_FORMAT);
            if (HOURS.between(LocalDateTime.now(), newDate) < 2) {
                throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
            }
            event.setEventDate(newDate);
        }
        Location location = adminRequest.getLocation();
        if (location != null) {
            event.setLocation(location);
        }
        if (adminRequest.getPaid() != null) {
            event.setPaid(adminRequest.getPaid());
        }
        if (adminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(adminRequest.getParticipantLimit());
        }
        if (adminRequest.getRequestModeration() != null) {
            event.setRequestModeration(adminRequest.getRequestModeration());
        }

        String stateString = adminRequest.getStateAction();
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionAdmin.valueOf(stateString)) {
                case PUBLISH_EVENT:
                    if (HOURS.between(LocalDateTime.now(), event.getEventDate()) < 1) {
                        throw new CreateConditionException("Начало события должно быть минимум на один час позже момента публикации");
                    }
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new DataConflictException("Попытка опубликовать событие с id=" + event.getId() + ", которое уже опубликоано.");
                    }
                    if (event.getState() == EventState.CANCELED) {
                        throw new DataConflictException("Попытка опубликовать событие с id=" + event.getId() + ", которое уже отменено.");
                    }
                    event.setState(EventState.PUBLISHED);
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new DataConflictException("Попытка отменить событие с id=" + event.getId() + ", которое уже опубликоано.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        String title = adminRequest.getTitle();
        if (!(title == null || title.isBlank())) {
            event.setTitle(title);
        }

        eventJpaRepository.save(event);
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId()));
        Optional<Event> updatedEventOptional = eventJpaRepository.findById(event.getId());

        return EventMapper.toFullDto(updatedEventOptional.get(), idViewsMap.getOrDefault(event.getId(), 0L));
    }

    public List<ParticipationRequestDto> getParticipationInfo(int userId, int eventId) {
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (eventId < 0) {
            throw new BadParameterException("Id соытия должен быть больше 0");
        }

        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId);
        if (event == null) {
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        List<ParticipationRequestDto> partDtos = participationService.getALlRequestsEventId(event.getId());
        if (partDtos.isEmpty()) {
            return new ArrayList<>();
        }

        return partDtos;
    }

    public EventRequestStatusUpdateResult updateStatus(int userId, int eventId, EventRequestStatusUpdateRequest updateRequest) {
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (eventId < 0) {
            throw new BadParameterException("Id соытия должен быть больше 0");
        }

        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId);
        if (event == null) {
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult();

        List<ParticipationRequestDto> requests = participationService.getALlRequestsEventId(eventId);
        int confirmedRequestsAmount = event.getConfirmedRequests();
        int limit = event.getParticipantLimit();
        boolean limitAchieved = false;

        if (updateRequest.getStatus() == UpdateRequestState.REJECTED) {
            for (int id : updateRequest.getRequestIds()) {
                ParticipationRequestDto prDto = requests.stream().filter(pr -> pr.getId() == id).findFirst().orElseThrow();
                if (prDto.getStatus().equals(RequestStatus.PENDING.name())) {
                    prDto.setStatus(RequestStatus.REJECTED.toString());
                    participationService.update(prDto, event);
                    updateResult.getRejectedRequests().add(prDto);
                } else {
                    throw new CreateConditionException("Нельзя отклонить уже обработанную заявку id=" + id);
                }
            }
            return updateResult;
        } else {
            if ((limit == 0 || !event.isRequestModeration())) {
                for (int id : updateRequest.getRequestIds()) {
                    ParticipationRequestDto prDto = requests.stream().filter(pr -> pr.getId() == id).findFirst().orElseThrow();
                    if (prDto.getStatus().equals(RequestStatus.PENDING.name())) {
                        prDto.setStatus(RequestStatus.CONFIRMED.toString());
                        confirmedRequestsAmount++;
                        event.setConfirmedRequests(confirmedRequestsAmount);
                        eventJpaRepository.save(event);
                        participationService.update(prDto, event);
                        updateResult.getConfirmedRequests().add(prDto);
                    } else {
                        throw new CreateConditionException("Нельзя подтвердить уже обработанную заявку id=" + id);
                    }
                }
                return updateResult;
            } else {
                for (int id : updateRequest.getRequestIds()) {
                    limitAchieved = confirmedRequestsAmount >= limit;
                    ParticipationRequestDto prDto = requests.stream().filter(pr -> pr.getId() == id).findFirst().orElseThrow();
                    if (prDto.getStatus().equals(RequestStatus.PENDING.name())) {
                        if (limitAchieved) {
                            prDto.setStatus(RequestStatus.REJECTED.toString());
                            participationService.update(prDto, event);
                            updateResult.getRejectedRequests().add(prDto);
                        } else {
                            prDto.setStatus(RequestStatus.CONFIRMED.toString());
                            confirmedRequestsAmount++;
                            event.setConfirmedRequests(confirmedRequestsAmount);
                            eventJpaRepository.save(event);
                            participationService.update(prDto, event);
                            updateResult.getConfirmedRequests().add(prDto);
                        }
                    } else {
                        throw new CreateConditionException("Нельзя подтвердить уже обработанную заявку id=" + id);
                    }
                }
            }
        }

        if (limitAchieved) {
            throw new CreateConditionException("Превышен лимит на кол-во участников. Лимит = " + limit + ", кол-во подтвержденных заявок =" + confirmedRequestsAmount);
        }
        return updateResult;
    }

    public List<EventFullDto> searchEvents(List<Integer> users, List<String> states, List<Integer> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        if (from < 0 || size < 1) {
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        criteriaQuery = criteriaQuery.select(eventRoot);

        List<Event> resultEvents = null;
        Predicate complexPredicate = null;
        if (rangeStart != null && rangeEnd != null) {
            Predicate predicateForDateTime
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), rangeStart, rangeEnd);
            complexPredicate = predicateForDateTime;
        }
        if (users != null && !users.isEmpty()) {
            Predicate predicateForUsersId
                    = eventRoot.get("initiator").get("id").in(users);
            if (complexPredicate == null) {
                complexPredicate = predicateForUsersId;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForUsersId);
            }
        }
        if (categories != null && !categories.isEmpty()) {
            Predicate predicateForCategoryId
                    = eventRoot.get("category").get("id").in(categories);
            if (complexPredicate == null) {
                complexPredicate = predicateForCategoryId;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForCategoryId);
            }
        }
        if (states != null && !states.isEmpty()) {
            Predicate predicateForStates
                    = eventRoot.get("state").as(String.class).in(states);
            if (complexPredicate == null) {
                complexPredicate = predicateForStates;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForStates);
            }
        }
        if (complexPredicate != null) {
            criteriaQuery.where(complexPredicate);
        }
        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        resultEvents = typedQuery.getResultList();

        if (resultEvents == null || resultEvents.isEmpty()) {
            return new ArrayList<EventFullDto>();
        }

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(resultEvents.stream().map(Event::getId).collect(Collectors.toList()));

        return resultEvents.stream()
                .map(e -> EventMapper.toFullDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public List<EventShortDto> searchEventsWithStats(String text,
                                                     List<Integer> categories,
                                                     Boolean paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable,
                                                     String sort,
                                                     int from,
                                                     int size,
                                                     HttpServletRequest request) {

        if (from < 0 || size < 1) {
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        criteriaQuery.select(eventRoot);

        List<Event> resultEvents = null;
        Predicate complexPredicate = null;
        if (rangeStart != null && rangeEnd != null) {
            complexPredicate
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), rangeStart, rangeEnd);
        } else {
            complexPredicate
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), LocalDateTime.now(), LocalDateTime.of(9999, 1, 1, 1, 1, 1));
        }
        if (text != null && !text.isBlank()) {
            String decodeText = URLDecoder.decode(text, StandardCharsets.UTF_8);

            Expression<String> annotationLowerCase = criteriaBuilder.lower(eventRoot.get("annotation"));
            Expression<String> descriptionLowerCase = criteriaBuilder.lower(eventRoot.get("description"));
            Predicate predicateForAnnotation
                    = criteriaBuilder.like(annotationLowerCase, "%" + decodeText.toLowerCase() + "%");
            Predicate predicateForDescription
                    = criteriaBuilder.like(descriptionLowerCase, "%" + decodeText.toLowerCase() + "%");
            Predicate predicateForText = criteriaBuilder.or(predicateForAnnotation, predicateForDescription);
            if (complexPredicate == null) {
                complexPredicate = predicateForText;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForText); //прикрепили к общему предикату по AND
            }
        }
        if (categories != null && !categories.isEmpty()) {
            if (categories.stream().anyMatch(c -> c <= 0)) {
                throw new BadParameterException("Id категории должен быть > 0");
            }
            Predicate predicateForCategoryId
                    = eventRoot.get("category").get("id").in(categories);
            if (complexPredicate == null) {
                complexPredicate = predicateForCategoryId;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForCategoryId);
            }
        }
        if (paid != null) {
            Predicate predicateForPaid
                    = criteriaBuilder.equal(eventRoot.get("paid"), paid);
            if (complexPredicate == null) {
                complexPredicate = predicateForPaid;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForPaid);
            }
        }
        if (onlyAvailable != null) {
            Predicate predicateForOnlyAvailable
                    = criteriaBuilder.lt(eventRoot.get("confirmedRequests"), eventRoot.get("participantLimit"));
            if (complexPredicate == null) {
                complexPredicate = predicateForOnlyAvailable;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForOnlyAvailable);
            }
        }

        Predicate predicateForPublished
                = criteriaBuilder.equal(eventRoot.get("state"), EventState.PUBLISHED);
        if (complexPredicate == null) {
            complexPredicate = predicateForPublished;
        } else {
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForPublished);
        }

        if (complexPredicate != null) {
            criteriaQuery.where(complexPredicate);
        }

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        resultEvents = typedQuery.getResultList();

        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-event-service");
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
        endpointHitDto.setUri(request.getRequestURI());

        StatsClient.postHit(endpointHitDto);

        if (resultEvents == null || resultEvents.isEmpty()) {
            return new ArrayList<EventShortDto>();
        }

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(resultEvents.stream().map(Event::getId).collect(Collectors.toList()));

        return resultEvents.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public Set<EventFullDto> getEventsByIdSet(Set<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Event> eventList = eventJpaRepository.findByIdIn(eventIds);

        if (eventList == null || eventList.isEmpty()) {
            return new HashSet<EventFullDto>();
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(eventList.stream().map(Event::getId).collect(Collectors.toList()));
        return eventList.stream()
                .map(e -> EventMapper.toFullDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toSet());
    }

    public Set<Event> getEventsByIds(Set<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Event> eventList = eventJpaRepository.findEventsWIthUsersByIdSet(eventIds);
        return new HashSet<>(eventList);
    }
}