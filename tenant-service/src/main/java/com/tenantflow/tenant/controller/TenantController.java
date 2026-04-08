// src/main/java/com/tenantflow/tenant/controller/TenantController.java

package com.tenantflow.tenant.controller;

import com.tenantflow.tenant.dto.request.TenantRequest;
import com.tenantflow.tenant.dto.request.UpdateTenantRequest;
import com.tenantflow.tenant.dto.response.PageResponse;
import com.tenantflow.tenant.dto.response.TenantResponse;
import com.tenantflow.tenant.dto.response.TenantStatsResponse;
import com.tenantflow.tenant.entity.SubscriptionPlan;
import com.tenantflow.tenant.entity.TenantStatus;
import com.tenantflow.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "APIs for managing tenants in TenantFlow platform")
public class TenantController {

    private final TenantService tenantService;

    // ─────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────

    @PostMapping
    @Operation(
            summary = "Create a new tenant",
            description = "Creates a new tenant with the specified plan and subdomain"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tenant created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Tenant already exists")
    })
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody TenantRequest request
    ) {
        log.info("POST /api/v1/tenants - Creating tenant: {}", request.name());
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(
            summary = "Get tenant by ID",
            description = "Retrieves a tenant by their unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant found"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<TenantResponse> getTenantById(
            @Parameter(description = "Tenant UUID")
            @PathVariable UUID id
    ) {
        log.debug("GET /api/v1/tenants/{}", id);
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @GetMapping("/subdomain/{subdomain}")
    @Operation(
            summary = "Get tenant by subdomain",
            description = "Retrieves a tenant by their unique subdomain"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant found"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<TenantResponse> getTenantBySubdomain(
            @Parameter(description = "Tenant subdomain e.g. acme-corp")
            @PathVariable String subdomain
    ) {
        log.debug("GET /api/v1/tenants/subdomain/{}", subdomain);
        return ResponseEntity.ok(tenantService.getTenantBySubdomain(subdomain));
    }

    @GetMapping
    @Operation(
            summary = "Get all tenants",
            description = "Returns paginated list of all tenants with optional filters"
    )
    public ResponseEntity<PageResponse<TenantResponse>> getAllTenants(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) TenantStatus status,

            @Parameter(description = "Filter by subscription plan")
            @RequestParam(required = false) SubscriptionPlan plan,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.debug("GET /api/v1/tenants - page: {}, size: {}, status: {}, plan: {}",
                page, size, status, plan);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Apply filters if provided
        if (status != null && plan != null) {
            return ResponseEntity.ok(
                    tenantService.getTenantsByStatus(status, pageable)
            );
        }

        if (status != null) {
            return ResponseEntity.ok(
                    tenantService.getTenantsByStatus(status, pageable)
            );
        }

        if (plan != null) {
            return ResponseEntity.ok(
                    tenantService.getTenantsByPlan(plan, pageable)
            );
        }

        return ResponseEntity.ok(tenantService.getAllTenants(pageable));
    }

    // ─────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(
            summary = "Get tenant statistics",
            description = "Returns platform-wide tenant statistics for dashboard"
    )
    public ResponseEntity<TenantStatsResponse> getTenantStats() {
        log.debug("GET /api/v1/tenants/stats");
        return ResponseEntity.ok(tenantService.getTenantStats());
    }

    // ─────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(
            summary = "Update tenant",
            description = "Updates tenant name, status or plan"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid state transition"),
            @ApiResponse(responseCode = "404", description = "Tenant not found"),
            @ApiResponse(responseCode = "409", description = "Name already taken")
    })
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        log.info("PUT /api/v1/tenants/{}", id);
        return ResponseEntity.ok(tenantService.updateTenant(id, request));
    }

    // ─────────────────────────────────────────
    // BUSINESS OPERATIONS
    // ─────────────────────────────────────────

    @PatchMapping("/{id}/suspend")
    @Operation(
            summary = "Suspend tenant",
            description = "Suspends an active tenant - blocks all API access"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant suspended successfully"),
            @ApiResponse(responseCode = "404", description = "Tenant not found"),
            @ApiResponse(responseCode = "422", description = "Tenant cannot be suspended in current state")
    })
    public ResponseEntity<TenantResponse> suspendTenant(
            @PathVariable UUID id
    ) {
        log.info("PATCH /api/v1/tenants/{}/suspend", id);
        return ResponseEntity.ok(tenantService.suspendTenant(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Activate tenant",
            description = "Activates an inactive or suspended tenant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant activated successfully"),
            @ApiResponse(responseCode = "404", description = "Tenant not found"),
            @ApiResponse(responseCode = "422", description = "Tenant cannot be activated in current state")
    })
    public ResponseEntity<TenantResponse> activateTenant(
            @PathVariable UUID id
    ) {
        log.info("PATCH /api/v1/tenants/{}/activate", id);
        return ResponseEntity.ok(tenantService.activateTenant(id));
    }

    @PatchMapping("/{id}/upgrade-plan")
    @Operation(
            summary = "Upgrade subscription plan",
            description = "Upgrades or downgrades tenant subscription plan"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan upgraded successfully"),
            @ApiResponse(responseCode = "404", description = "Tenant not found"),
            @ApiResponse(responseCode = "422", description = "Tenant is already on this plan")
    })
    public ResponseEntity<TenantResponse> upgradePlan(
            @PathVariable UUID id,
            @Parameter(description = "New subscription plan")
            @RequestParam SubscriptionPlan plan
    ) {
        log.info("PATCH /api/v1/tenants/{}/upgrade-plan?plan={}", id, plan);
        return ResponseEntity.ok(tenantService.upgradePlan(id, plan));
    }

    // ─────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete tenant",
            description = "Soft deletes a tenant - data is retained for audit purposes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tenant deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tenant not found"),
            @ApiResponse(responseCode = "422", description = "Tenant already deleted")
    })
    public ResponseEntity<Void> deleteTenant(
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/tenants/{}", id);
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}
