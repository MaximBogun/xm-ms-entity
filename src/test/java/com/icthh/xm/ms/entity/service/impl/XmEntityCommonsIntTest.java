package com.icthh.xm.ms.entity.service.impl;

import static com.google.common.collect.ImmutableMap.of;
import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_AUTH_CONTEXT;
import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_TENANT_CONTEXT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.icthh.xm.commons.lep.XmLepScriptConfigServerResourceLoader;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.ms.entity.EntityApp;
import com.icthh.xm.ms.entity.config.LepConfiguration;
import com.icthh.xm.ms.entity.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.entity.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.entity.service.FunctionExecutorService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    EntityApp.class,
    SecurityBeanOverrideConfiguration.class,
    WebappTenantOverrideConfiguration.class,
    LepConfiguration.class
})
public class XmEntityCommonsIntTest {

    @Autowired
    private FunctionExecutorService functionService;


    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private XmAuthenticationContextHolder authContextHolder;

    @Autowired
    private LepManager lepManager;

    @Autowired
    private XmLepScriptConfigServerResourceLoader leps;

    @BeforeTransaction
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
    }

    @SneakyThrows
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        lepManager.beginThreadContext(ctx -> {
            ctx.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
            ctx.setValue(THREAD_CONTEXT_KEY_AUTH_CONTEXT, authContextHolder.getContext());
        });

    }

    void initLeps() {
        leps.onRefresh("/config/tenants/RESINTTEST/entity/lep/function/Function$$NAME$$tenant.groovy", loadFile("config/testlep/Function$$NAME$$tenant.groovy"));
        leps.onRefresh("/config/tenants/RESINTTEST/entity/lep/commons/xm/path/to/commons/Commons$$trololo$$around.groovy", loadFile("config/testlep/Commons$$trololo$$around.groovy"));
    }

    @SneakyThrows
    public static String loadFile(String path) {
        InputStream cfgInputStream = new ClassPathResource(path).getInputStream();
        return IOUtils.toString(cfgInputStream, UTF_8);
    }

    @After
    @Override
    public void finalize() {
        lepManager.endThreadContext();
        tenantContextHolder.getPrivilegedContext().destroyCurrentContext();
    }


    @Test
    @Transactional
    public void testCommons() {
        initLeps();
        Map<String, Object> name = functionService.execute("NAME", of());
        log.info("RESULT {}", name);
        assertThat(name.get("result")).isEqualTo("RESULT [1, 2, 5] | COMMON_ARGUMENT");
    }



}