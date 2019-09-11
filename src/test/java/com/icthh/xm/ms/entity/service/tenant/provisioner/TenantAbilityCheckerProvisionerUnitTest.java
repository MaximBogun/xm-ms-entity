package com.icthh.xm.ms.entity.service.tenant.provisioner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.gen.model.Tenant;
import com.icthh.xm.commons.tenant.TenantContext;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantKey;
import com.icthh.xm.ms.entity.AbstractUnitTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

public class TenantAbilityCheckerProvisionerUnitTest extends AbstractUnitTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private TenantContextHolder tenantContextHolder;

    @Mock
    private TenantContext tenantContext;

    private TenantAbilityCheckerProvisioner provisioner;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        provisioner = new TenantAbilityCheckerProvisioner(tenantContextHolder);
        when(tenantContextHolder.getContext()).thenReturn(tenantContext);
    }

    @Test
    public void createTenant() {

        when(tenantContext.getTenantKey()).thenReturn(Optional.of(new TenantKey("XM")));
        provisioner.createTenant(new Tenant().tenantKey("NEWTENANT"));
        verify(tenantContext).getTenantKey();

    }

    @Test
    public void createTenantNotAllowed() {

        when(tenantContext.getTenantKey()).thenReturn(Optional.of(new TenantKey("NONXM")));
        expectedEx.expect(BusinessException.class);
        expectedEx.expectMessage("Only 'XM' tenant allow to create new tenant");

        provisioner.createTenant(new Tenant().tenantKey("NEWTENANT"));
        verify(tenantContext).getTenantKey();

    }

    @Test
    public void manageTenant() {

        when(tenantContext.getTenantKey()).thenReturn(Optional.of(new TenantKey("XM")));
        provisioner.manageTenant("NEWTENANT", "ACTIVE");
        verify(tenantContext).getTenantKey();
    }

    @Test
    public void manageTenantNotAllowed() {

        when(tenantContext.getTenantKey()).thenReturn(Optional.of(new TenantKey("NONXM")));
        expectedEx.expect(BusinessException.class);
        expectedEx.expectMessage("Only 'XM' tenant allow to manage tenant");

        provisioner.manageTenant("NEWTENANT", "ACTIVE");
        verify(tenantContext).getTenantKey();

    }

    @Test
    public void deleteTenant() {
        when(tenantContext.getTenantKey()).thenReturn(Optional.of(new TenantKey("XM")));
        provisioner.deleteTenant("NEWTENANT");
        verify(tenantContext).getTenantKey();
    }

    @Test
    public void deleteTenantNotAllowed() {
        when(tenantContext.getTenantKey()).thenReturn(Optional.of(new TenantKey("NONXM")));
        expectedEx.expect(BusinessException.class);
        expectedEx.expectMessage("Only 'XM' tenant allow to delete tenant");

        provisioner.deleteTenant("NEWTENANT");
        verify(tenantContext).getTenantKey();
    }

}
