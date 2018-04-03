package com.icthh.xm.ms.entity.service.impl;

import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_AUTH_CONTEXT;
import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_TENANT_CONTEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.commons.security.XmAuthenticationContext;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.entity.EntityApp;
import com.icthh.xm.ms.entity.config.LepConfiguration;
import com.icthh.xm.ms.entity.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.entity.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.entity.domain.Attachment;
import com.icthh.xm.ms.entity.domain.Link;
import com.icthh.xm.ms.entity.domain.Profile;
import com.icthh.xm.ms.entity.domain.XmEntity;
import com.icthh.xm.ms.entity.domain.ext.IdOrKey;
import com.icthh.xm.ms.entity.repository.LinkRepository;
import com.icthh.xm.ms.entity.repository.XmEntityRepository;
import com.icthh.xm.ms.entity.repository.search.XmEntityPermittedSearchRepository;
import com.icthh.xm.ms.entity.repository.search.XmEntitySearchRepository;
import com.icthh.xm.ms.entity.service.*;
import com.icthh.xm.ms.entity.util.XmHttpEntityUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.net.URI;
import java.util.Date;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    EntityApp.class,
    SecurityBeanOverrideConfiguration.class,
    WebappTenantOverrideConfiguration.class,
    LepConfiguration.class
})
public class EntityServiceImplIntTest {

    private XmEntityServiceImpl xmEntityService;

    @Autowired
    private XmEntityRepository xmEntityRepository;

    @Autowired
    private XmEntitySpecService xmEntitySpecService;

    @Autowired
    private XmEntitySearchRepository xmEntitySearchRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private LinkService linkService;

    @Autowired
    private LifecycleLepStrategyFactory lifecycleService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private LepManager lepManager;

    @Autowired
    private XmEntityPermittedSearchRepository permittedSearchRepository;

    @Autowired
    private StartUpdateDateGenerationStrategy startUpdateDateGenerationStrategy;

    @Mock
    private ProfileService profileService;

    @Mock
    private StorageService storageService;

    @Mock
    private XmAuthenticationContextHolder authContextHolder;

    @Mock
    private XmAuthenticationContext context;

    private Profile self;

    private static final String TEST_LINK_KEY = "TEST.LINK";

    private static final String TARGET_TYPE_KEY = "ACCOUNT.USER";

    @BeforeTransaction
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
        MockitoAnnotations.initMocks(this);
        when(authContextHolder.getContext()).thenReturn(context);
        when(context.getRequiredUserKey()).thenReturn("userKey");

        XmEntity sourceEntity = xmEntityRepository.save(createEntity(1l, TARGET_TYPE_KEY));
        self = new Profile();
        self.setXmentity(sourceEntity);
        when(profileService.getSelfProfile()).thenReturn(self);

        xmEntityService = new XmEntityServiceImpl(
            xmEntitySpecService,
            xmEntityRepository,
            xmEntitySearchRepository,
            lifecycleService,
            null,
            profileService,
            linkService,
            storageService,
            attachmentService,
            permittedSearchRepository,
            startUpdateDateGenerationStrategy,
            authContextHolder);
        xmEntityService.setSelf(xmEntityService);

        lepManager.beginThreadContext(ctx -> {
            ctx.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
            ctx.setValue(THREAD_CONTEXT_KEY_AUTH_CONTEXT, authContextHolder.getContext());
        });
    }

    @After
    public void afterTest() {
        tenantContextHolder.getPrivilegedContext().destroyCurrentContext();
    }

    @Test
    @Transactional
    public void testGetLinkTargetsSelfKey() throws Exception {
        XmEntity targetEntity = xmEntityRepository.save(createEntity(2l, TARGET_TYPE_KEY));
        Link link = createSelfLink(targetEntity);
        linkRepository.save(link);

        List<Link> result = xmEntityService.getLinkTargets(IdOrKey.SELF, TARGET_TYPE_KEY);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(BigInteger.ONE.intValue());
    }

    @Test
    @Transactional
    public void testGetLinkSourcesSelfKey() throws Exception {
        XmEntity targetEntity = xmEntityRepository.save(createEntity(2l, "ACCOUNT.USER"));
        Link link = createSelfLink(targetEntity);
        linkRepository.save(link);

        List<Link> result = xmEntityService.getLinkSources(IdOrKey.of(targetEntity.getId()), TEST_LINK_KEY);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(BigInteger.ONE.intValue());
    }

    @Test
    @Transactional
    public void testGetLinkTargetsByEntityId() throws Exception {
        XmEntity sourceEntity = xmEntityRepository.save(createEntity(null, "ACCOUNT"));
        XmEntity targetEntity = xmEntityRepository.save(createEntity(2l, TARGET_TYPE_KEY));
        Link link = createLink(sourceEntity, targetEntity);
        linkRepository.save(link);

        List<Link> targetsLinks = xmEntityService.getLinkTargets(IdOrKey.of(sourceEntity.getId()),
            TARGET_TYPE_KEY);

        assertThat(targetsLinks).isNotEmpty();
        assertThat(targetsLinks.size()).isEqualTo(BigInteger.ONE.intValue());
    }

    @Test
    @Transactional
    public void testGetLinkSourcesByEntityId() throws Exception {
        XmEntity sourceEntity = xmEntityRepository.save(createEntity(null, "ACCOUNT"));
        XmEntity targetEntity = xmEntityRepository.save(createEntity(2l, "ACCOUNT.USER"));
        Link link = createLink(sourceEntity, targetEntity);
        linkRepository.save(link);

        List<Link> sourcesLinks = xmEntityService
            .getLinkSources(IdOrKey.of(targetEntity.getId()), TEST_LINK_KEY);

        assertThat(sourcesLinks).isNotNull();
        assertThat(sourcesLinks.size()).isEqualTo(BigInteger.ONE.intValue());
    }

    @Test(expected = EntityNotFoundException.class)
    @Transactional
    public void testFailGetLinkTargetsByKey() throws Exception {
        xmEntityService.getLinkTargets(IdOrKey.of("some key"), "ANY");
    }

    @Test(expected = EntityNotFoundException.class)
    @Transactional
    public void testFailGetLinkSourcesByKey() throws Exception {
        xmEntityService.getLinkSources(IdOrKey.of("some key"), "ANY");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void saveSelfLinkTarget() throws Exception {
        when(storageService.store(Mockito.any(MultipartFile.class), Mockito.any())).thenReturn("test.txt");
        int databaseSizeBeforeCreate = linkRepository.findAll().size();

        XmEntity targetEntity = xmEntityRepository.save(createEntity(2l, TARGET_TYPE_KEY));
        Link link = createSelfLink(targetEntity);

        // Create link with attachment
        MockMultipartFile file =
            new MockMultipartFile("file", "test.txt", "text/plain", "TEST".getBytes());

        xmEntityService.saveLinkTarget(IdOrKey.SELF, link, file);

        // Validate the link in the database
        List<Link> linkList = linkRepository.findAll();
        assertThat(linkList).hasSize(databaseSizeBeforeCreate + BigInteger.ONE.intValue());

        //validate attachment in database
        List<Attachment> attachments = attachmentService.findAll(null);
        assertThat(attachments).hasSize(BigInteger.ONE.intValue());
        Attachment attachment = attachments.stream().findFirst().get();
        assertThat(attachment.getContentUrl()).contains("test.txt");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void addFileAttachment() {
        when(storageService.store(Mockito.any(MultipartFile.class), Mockito.any())).thenReturn("test.txt");

        XmEntity targetEntity = xmEntityRepository.save(createEntity(2l, TARGET_TYPE_KEY));
        MockMultipartFile file =
            new MockMultipartFile("file", "test.txt", "text/plain", "TEST".getBytes());

        xmEntityService.addFileAttachment(targetEntity, file);

        XmEntity foundEntity = xmEntityService.findOne(IdOrKey.of(targetEntity.getId()));

        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getAttachments()).hasSize(BigInteger.ONE.intValue());
        assertThat(foundEntity.getAttachments().stream().findFirst().get().getContentUrl()).isEqualTo("test.txt");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void updateAvatar() throws Exception {
        when(storageService.store(Mockito.any(HttpEntity.class), Mockito.any())).thenReturn("test.txt");
        MockMultipartFile file =
            new MockMultipartFile("file", "test.jpg", "image/jpg", "TEST".getBytes());
        HttpEntity<Resource> avatarEntity = XmHttpEntityUtils.buildAvatarHttpEntity(file);

        URI uri = xmEntityService.updateAvatar(IdOrKey.SELF, avatarEntity);

        assertThat(uri != null);

        XmEntity storedEntity = xmEntityService.findOne(IdOrKey.SELF);

        assertThat(storedEntity.getAvatarUrl() != null);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void deleteSelfLinkTarget() {
        XmEntity targetEntity = xmEntityRepository.save(createEntity(2l, TARGET_TYPE_KEY));

        Link link = createLink(self.getXmentity(), targetEntity);
        linkRepository.save(link);

        xmEntityService.deleteLinkTarget(IdOrKey.SELF, link.getId().toString());
        assertThat(linkRepository.findAll()).hasSize(BigInteger.ZERO.intValue());

    }

    @Test(expected = BusinessException.class)
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void deleteForeignLinkTarget() {
        XmEntity sourceEntity = xmEntityRepository.save(createEntity(25l, "ACCOUNT.USER"));
        XmEntity targetEntity = xmEntityRepository.save(createEntity(2l, TARGET_TYPE_KEY));

        Link link = createLink(sourceEntity, targetEntity);
        linkRepository.save(link);

        xmEntityService.deleteLinkTarget(IdOrKey.SELF, link.getId().toString());
    }

    private XmEntity createEntity(Long id, String typeKey) {
        XmEntity entity = new XmEntity();
        entity.setId(id);
        entity.setName("Name");
        entity.setTypeKey(typeKey);
        entity.setStartDate(new Date().toInstant());
        entity.setUpdateDate(new Date().toInstant());
        entity.setKey("KEY-" + id);
        entity.setStateKey("STATE1");
        entity.setData(ImmutableMap.<String, Object>builder()
            .put("AAAAAAAAAA", "BBBBBBBBBB").build());
        return entity;
    }

    private Link createSelfLink(XmEntity target) {
        return createLink(self.getXmentity(), target);
    }

    private IdOrKey createSelfIdOrKey() {
        return IdOrKey.of(self.getXmentity().getId());
    }

    private Link createLink(XmEntity source, XmEntity target) {
        Link link = new Link();
        link.setSource(source);
        link.setTarget(target);
        link.setStartDate(new Date().toInstant());
        link.setTypeKey(TEST_LINK_KEY);

        return link;
    }
}