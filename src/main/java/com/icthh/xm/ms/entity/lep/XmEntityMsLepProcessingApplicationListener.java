package com.icthh.xm.ms.entity.lep;

import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_KEY_REPOSITORIES;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_KEY_SERVICES;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_KEY_TEMPLATES;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_PROFILE_EVENT_PRODUCER_SERVICE;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_REPOSITORY_XM_ENTITY;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_ATTACHMENT;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_LINK;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_LOCATION_SERVICE;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_MAIL_SERVICE;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_PROFILE;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_TAG_SERVICE;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_TENANT_CONFIG_SERICE;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_XM_ENTITY;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_SERVICE_XM_TENANT_LC;
import static com.icthh.xm.ms.entity.lep.LepXmEntityMsConstants.BINDING_SUB_KEY_TEMPLATE_REST;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.spring.SpringLepProcessingApplicationListener;
import com.icthh.xm.lep.api.ScopedContext;
import com.icthh.xm.ms.entity.repository.XmEntityRepository;
import com.icthh.xm.ms.entity.repository.kafka.ProfileEventProducer;
import com.icthh.xm.ms.entity.service.AttachmentService;
import com.icthh.xm.ms.entity.service.LinkService;
import com.icthh.xm.ms.entity.service.LocationService;
import com.icthh.xm.ms.entity.service.ProfileService;
import com.icthh.xm.ms.entity.service.TagService;
import com.icthh.xm.ms.entity.service.XmEntityService;
import com.icthh.xm.ms.entity.service.XmTenantLifecycleService;
import com.icthh.xm.ms.entity.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link XmEntityMsLepProcessingApplicationListener} class.
 */
@RequiredArgsConstructor
public class XmEntityMsLepProcessingApplicationListener extends SpringLepProcessingApplicationListener {

    private final XmEntityService xmEntityService;
    private final XmTenantLifecycleService xmTenantLifecycleService;
    private final XmEntityRepository xmEntityRepository;
    private final ProfileService profileService;
    private final LinkService linkService;
    private final MailService mailService;
    private final TenantConfigService tenantConfigService;
    private final AttachmentService attachmentService;
    private final RestTemplate restTemplate;
    private final LocationService locationService;
    private final TagService tagService;
    private final ProfileEventProducer profileEventProducer;

    @Override
    protected void bindExecutionContext(ScopedContext executionContext) {
        // services
        Map<String, Object> services = new HashMap<>();
        services.put(BINDING_SUB_KEY_SERVICE_PROFILE, profileService);
        services.put(BINDING_SUB_KEY_SERVICE_LINK, linkService);
        services.put(BINDING_SUB_KEY_SERVICE_XM_ENTITY, xmEntityService);
        services.put(BINDING_SUB_KEY_SERVICE_XM_TENANT_LC, xmTenantLifecycleService);
        services.put(BINDING_SUB_KEY_SERVICE_MAIL_SERVICE, mailService);
        services.put(BINDING_SUB_KEY_SERVICE_TENANT_CONFIG_SERICE, tenantConfigService);
        services.put(BINDING_SUB_KEY_SERVICE_ATTACHMENT, attachmentService);
        services.put(BINDING_SUB_KEY_SERVICE_LOCATION_SERVICE, locationService);
        services.put(BINDING_SUB_KEY_SERVICE_TAG_SERVICE, tagService);
        services.put(BINDING_SUB_KEY_PROFILE_EVENT_PRODUCER_SERVICE, profileEventProducer);

        executionContext.setValue(BINDING_KEY_SERVICES, services);

        // repositories
        Map<String, Object> repositories = new HashMap<>();
        repositories.put(BINDING_SUB_KEY_REPOSITORY_XM_ENTITY, xmEntityRepository);

        executionContext.setValue(BINDING_KEY_REPOSITORIES, repositories);

        // templates
        Map<String, Object> templates = new HashMap<>();
        templates.put(BINDING_SUB_KEY_TEMPLATE_REST, restTemplate);

        executionContext.setValue(BINDING_KEY_TEMPLATES, templates);
    }

}
