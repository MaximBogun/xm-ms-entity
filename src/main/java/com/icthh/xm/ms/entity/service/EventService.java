package com.icthh.xm.ms.entity.service;

import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.entity.domain.Event;
import com.icthh.xm.ms.entity.repository.EventRepository;
import com.icthh.xm.ms.entity.repository.XmEntityRepository;
import com.icthh.xm.ms.entity.repository.search.EventSearchRepository;
import com.icthh.xm.ms.entity.repository.search.PermittedSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Event.
 */
@Service
@Transactional
@RequiredArgsConstructor
@LepService(group = "service.event")
public class EventService {

    private final EventRepository eventRepository;

    private final EventSearchRepository eventSearchRepository;

    private final PermittedRepository permittedRepository;

    private final PermittedSearchRepository permittedSearchRepository;

    private final XmEntityRepository xmEntityRepository;

    /**
     * Save a event.
     *
     * @param event the entity to save
     * @return the persisted entity
     */
    @LogicExtensionPoint("Save")
    public Event save(Event event) {
        if (event.getAssigned() != null) {
            event.setAssigned(xmEntityRepository.getOne(event.getAssigned().getId()));
        }
        Event result = eventRepository.save(event);
        eventSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the events.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    @FindWithPermission("EVENT.GET_LIST")
    @LogicExtensionPoint("FindAll")
    public List<Event> findAll(String privilegeKey) {
        return permittedRepository.findAll(Event.class, privilegeKey);
    }

    /**
     *  Get one event by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    @LogicExtensionPoint("FindOne")
    public Event findOne(Long id) {
        return eventRepository.findOne(id);
    }

    /**
     *  Delete the  event by id.
     *
     *  @param id the id of the entity
     */
    @LogicExtensionPoint("Delete")
    public void delete(Long id) {
        eventRepository.delete(id);
        eventSearchRepository.delete(id);
    }

    /**
     * Search for the event corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    @FindWithPermission("EVENT.SEARCH")
    @LogicExtensionPoint("Search")
    public List<Event> search(String query, String privilegeKey) {
        return permittedSearchRepository.search(query, Event.class, privilegeKey);
    }
}
