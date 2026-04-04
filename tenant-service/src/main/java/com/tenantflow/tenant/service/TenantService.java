package com.tenantflow.tenant.service;

import com.tenantflow.tenant.model.Tenant;
import com.tenantflow.tenant.model.TenantStatus;

import java.util.List;

public interface TenantService {

    Tenant createTenant(Tenant tenant);

    Tenant getTenantById(Long id);

    Tenant getTenantByDomain(String domain);

    List<Tenant> getAllTenants();

    List<Tenant> getTenantsByStatus(TenantStatus status);

    Tenant updateTenant(Long id, Tenant tenant);

    void deleteTenant(Long id);
}
