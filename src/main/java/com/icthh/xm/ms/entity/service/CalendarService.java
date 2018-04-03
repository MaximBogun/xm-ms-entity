package com.icthh.xm.ms.entity.service;

import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.entity.domain.Calendar;
import com.icthh.xm.ms.entity.repository.CalendarRepository;
import com.icthh.xm.ms.entity.repository.search.CalendarSearchRepository;
import com.icthh.xm.ms.entity.repository.search.PermittedSearchRepository;
import com.icthh.xm.ms.entity.service.impl.StartUpdateDateGenerationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Calendar.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    private final CalendarSearchRepository calendarSearchRepository;

    private final PermittedRepository permittedRepository;

    private final PermittedSearchRepository permittedSearchRepository;

    private final StartUpdateDateGenerationStrategy startUpdateDateGenerationStrategy;

    /**
     * Save a calendar.
     *
     * @param calendar the entity to save
     * @return the persisted entity
     */
    public Calendar save(Calendar calendar) {

        startUpdateDateGenerationStrategy.preProcessStartDate(calendar,
                                                              calendar.getId(),
                                                              calendarRepository,
                                                              Calendar::setStartDate,
                                                              Calendar::getStartDate);
        Calendar result = calendarRepository.save(calendar);
        calendarSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the calendars.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    @FindWithPermission("CALENDAR.GET_LIST")
    public List<Calendar> findAll(String privilegeKey) {
        return permittedRepository.findAll(Calendar.class, privilegeKey);
    }

    /**
     *  Get one calendar by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Calendar findOne(Long id) {
        return calendarRepository.findOne(id);
    }

    /**
     *  Delete the  calendar by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        calendarRepository.delete(id);
        calendarSearchRepository.delete(id);
    }

    /**
     * Search for the calendar corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    @FindWithPermission("CALENDAR.SEARCH")
    public List<Calendar> search(String query, String privilegeKey) {
        return permittedSearchRepository.search(query, Calendar.class, privilegeKey);
    }
}