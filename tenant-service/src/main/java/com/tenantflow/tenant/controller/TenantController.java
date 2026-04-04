package com.tenantflow.tenant.controller;

import com.tenantflow.tenant.model.Tenant;
import com.tenantflow.tenant.model.TenantStatus;
import com.tenantflow.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    // Constructor Injection — Module 1 DI in action!
    private final TenantService tenantService;

    // CREATE Tenant
    @PostMapping
    public ResponseEntity<Tenant> createTenant(
            @Valid @RequestBody Tenant tenant) {

        log.info("REST request to create tenant: {}",
                tenant.getEmail());

        Tenant createdTenant =
                tenantService.createTenant(tenant);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTenant);
    }

    // GET Tenant by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(
            @PathVariable Long id) {

        log.info("REST request to get tenant by id: {}", id);

        return ResponseEntity.ok(
                tenantService.getTenantById(id)
        );
    }

    // GET Tenant by Domain
    @GetMapping("/domain/{domain}")
    public ResponseEntity<Tenant> getTenantByDomain(
            @PathVariable String domain) {

        log.info("REST request to get tenant " +
                "by domain: {}", domain);

        return ResponseEntity.ok(
                tenantService.getTenantByDomain(domain)
        );
    }

    // GET All Tenants
    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {

        log.info("REST request to get all tenants");

        return ResponseEntity.ok(
                tenantService.getAllTenants()
        );
    }

    // GET Tenants by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Tenant>> getTenantsByStatus(
            @PathVariable TenantStatus status) {

        log.info("REST request to get tenants " +
                "by status: {}", status);

        return ResponseEntity.ok(
                tenantService.getTenantsByStatus(status)
        );
    }

    // UPDATE Tenant
    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody Tenant tenant) {

        log.info("REST request to update tenant: {}", id);

        return ResponseEntity.ok(
                tenantService.updateTenant(id, tenant)
        );
    }

    // DELETE Tenant
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable Long id) {

        log.info("REST request to delete tenant: {}", id);

        tenantService.deleteTenant(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
