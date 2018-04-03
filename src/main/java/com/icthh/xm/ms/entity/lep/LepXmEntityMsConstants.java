package com.icthh.xm.ms.entity.lep;

/**
 * The {@link LepXmEntityMsConstants} class.
 */
public final class LepXmEntityMsConstants {

    public static final String BINDING_KEY_SERVICES = "services";
    public static final String BINDING_SUB_KEY_SERVICE_XM_ENTITY = "xmEntity";
    public static final String BINDING_SUB_KEY_SERVICE_XM_TENANT_LC = "xmTenantLifeCycle";
    public static final String BINDING_SUB_KEY_SERVICE_PROFILE = "profileService";
    public static final String BINDING_SUB_KEY_SERVICE_LINK = "linkService";
    public static final String BINDING_SUB_KEY_SERVICE_ATTACHMENT = "attachmentService";
    public static final String BINDING_SUB_KEY_SERVICE_MAIL_SERVICE = "mailService";
    public static final String BINDING_SUB_KEY_SERVICE_TENANT_CONFIG_SERICE = "tenantConfigService";
    public static final String BINDING_SUB_KEY_SERVICE_LOCATION_SERVICE = "locationService";
    public static final String BINDING_SUB_KEY_SERVICE_TAG_SERVICE = "tagService";
    public static final String BINDING_SUB_KEY_PROFILE_EVENT_PRODUCER_SERVICE = "profileEventProducer";

    public static final String BINDING_KEY_REPOSITORIES = "repositories";
    public static final String BINDING_SUB_KEY_REPOSITORY_XM_ENTITY = "xmEntity";

    public static final String BINDING_KEY_TEMPLATES = "templates";
    public static final String BINDING_SUB_KEY_TEMPLATE_REST = "rest";

    private LepXmEntityMsConstants() {
        throw new UnsupportedOperationException("Prevent creation for constructor utils class");
    }

}