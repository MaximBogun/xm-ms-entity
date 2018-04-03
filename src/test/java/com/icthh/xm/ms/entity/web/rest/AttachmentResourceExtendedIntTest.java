package com.icthh.xm.ms.entity.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.icthh.xm.commons.exceptions.spring.web.ExceptionTranslator;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.entity.EntityApp;
import com.icthh.xm.ms.entity.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.entity.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.entity.domain.Attachment;
import com.icthh.xm.ms.entity.repository.AttachmentRepository;
import com.icthh.xm.ms.entity.repository.search.AttachmentSearchRepository;
import com.icthh.xm.ms.entity.repository.search.PermittedSearchRepository;
import com.icthh.xm.ms.entity.service.AttachmentService;
import com.icthh.xm.ms.entity.service.impl.StartUpdateDateGenerationStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import java.time.Instant;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Extended Test class for the AttachmentResource REST controller.
 *
 * @see AttachmentResource
 */
@RunWith(SpringRunner.class)
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    EntityApp.class,
    SecurityBeanOverrideConfiguration.class,
    WebappTenantOverrideConfiguration.class
})
public class AttachmentResourceExtendedIntTest {

    private static final Instant MOCKED_START_DATE = Instant.ofEpochMilli(42L);

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private AttachmentResource attachmentResource;

    @Autowired
    private AttachmentSearchRepository attachmentSearchRepository;

    @Autowired
    private PermittedRepository permittedRepository;

    @Autowired
    private PermittedSearchRepository permittedSearchRepository;

    @Spy
    private StartUpdateDateGenerationStrategy startUpdateDateGenerationStrategy;

    private AttachmentService attachmentService;

    private MockMvc restAttachmentMockMvc;

    private Attachment attachment;

    @BeforeTransaction
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(startUpdateDateGenerationStrategy.generateStartDate()).thenReturn(MOCKED_START_DATE);

        attachmentService = new AttachmentService(
            attachmentRepository,
            attachmentSearchRepository,
            permittedRepository,
            permittedSearchRepository,
            startUpdateDateGenerationStrategy);

        AttachmentResource attachmentResourceMock = new AttachmentResource(attachmentService, attachmentResource);
        this.restAttachmentMockMvc = MockMvcBuilders.standaloneSetup(attachmentResourceMock)
                                                    .setCustomArgumentResolvers(pageableArgumentResolver)
                                                    .setControllerAdvice(exceptionTranslator)
                                                    .setValidator(validator)
                                                    .setMessageConverters(jacksonMessageConverter).build();

        attachment = AttachmentResourceIntTest.createEntity(em);

    }

    @After
    @Override
    public void finalize() {
        tenantContextHolder.getPrivilegedContext().destroyCurrentContext();
    }

    @Test
    @Transactional
    public void checkStartDateIsNotRequired() throws Exception {
        int databaseSizeBeforeTest = attachmentRepository.findAll().size();
        // set the field null
        attachment.setStartDate(null);

        // Create the Attachment.

        restAttachmentMockMvc.perform(post("/api/attachments")
                                          .contentType(TestUtil.APPLICATION_JSON_UTF8)
                                          .content(TestUtil.convertObjectToJsonBytes(attachment)))
                             .andExpect(status().isCreated())
                             .andExpect(jsonPath("$.startDate").value(MOCKED_START_DATE.toString()))
        ;

        List<Attachment> attachmentList = attachmentRepository.findAll();
        assertThat(attachmentList).hasSize(databaseSizeBeforeTest + 1);

        Attachment testAttachment = attachmentList.get(attachmentList.size() - 1);
        assertThat(testAttachment.getStartDate()).isEqualTo(MOCKED_START_DATE);

        // Validate the Attachment in Elasticsearch
        Attachment attachmentEs = attachmentSearchRepository.findOne(testAttachment.getId());
        assertThat(attachmentEs).isEqualToComparingFieldByField(testAttachment);
    }

    @Test
    @Transactional
    public void createAttachmentWithEntryDate() throws Exception {

        int databaseSizeBeforeCreate = attachmentRepository.findAll().size();

        // Create the Attachment
        restAttachmentMockMvc.perform(post("/api/attachments")
                                          .contentType(TestUtil.APPLICATION_JSON_UTF8)
                                          .content(TestUtil.convertObjectToJsonBytes(attachment)))
                             .andExpect(status().isCreated())
                             .andExpect(jsonPath("$.startDate").value(MOCKED_START_DATE.toString()));

        // Validate the Attachment in the database
        List<Attachment> voteList = attachmentRepository.findAll();
        assertThat(voteList).hasSize(databaseSizeBeforeCreate + 1);

        Attachment testAttachment = voteList.get(voteList.size() - 1);
        assertThat(testAttachment.getStartDate()).isEqualTo(MOCKED_START_DATE);

        // Validate the Attachment in Elasticsearch
        Attachment attachmentEs = attachmentSearchRepository.findOne(testAttachment.getId());
        assertThat(attachmentEs).isEqualToComparingFieldByField(testAttachment);
    }

    @Test
    @Transactional
    public void checkStartDateIsRequiredInDb() throws Exception {

        Attachment att = attachmentService.save(attachment);

        // set the field null
        when(startUpdateDateGenerationStrategy.generateStartDate()).thenReturn(null);

        att.setStartDate(null);

        attachmentService.save(att);

        try {
            attachmentRepository.flush();
            fail("DataIntegrityViolationException exception was expected!");
        } catch (DataIntegrityViolationException e) {
            assertThat(e.getMostSpecificCause().getMessage())
                .containsIgnoringCase("NULL not allowed for column \"START_DATE\"");
        }

    }

}