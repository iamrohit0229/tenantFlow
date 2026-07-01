package com.tenantflow.billing.controller;

import com.tenantflow.billing.dto.request.PlanUpgradeRequest;
import com.tenantflow.billing.dto.response.InvoiceResponse;
import com.tenantflow.billing.dto.response.SubscriptionResponse;
import com.tenantflow.billing.dto.response.UsageSummaryResponse;
import com.tenantflow.billing.service.BillingService;
import com.tenantflow.billing.service.UsageRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Billing, subscriptions and usage tracking APIs")
public class BillingController {

    private final BillingService billingService;
    private final UsageRecordService usageRecordService;

    // ─── Subscription APIs ─────────────────────────────────────

    @Operation(summary = "Get subscription for a tenant")
    @GetMapping("/subscriptions/{tenantId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @PathVariable UUID tenantId) {
        log.debug("Get subscription request: tenantId={}", tenantId);
        return ResponseEntity.ok(billingService.getSubscription(tenantId));
    }

    @Operation(summary = "Create subscription for a tenant")
    @PostMapping("/subscriptions/{tenantId}")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "FREE") String plan) {
        log.info("Create subscription request: tenantId={} plan={}", tenantId, plan);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(billingService.createSubscription(tenantId, plan));
    }

    @Operation(summary = "Upgrade or downgrade subscription plan")
    @PutMapping("/subscriptions/{tenantId}/plan")
    public ResponseEntity<SubscriptionResponse> upgradePlan(
            @PathVariable UUID tenantId,
            @Valid @RequestBody PlanUpgradeRequest request) {
        log.info("Plan upgrade request: tenantId={} newPlan={}", tenantId, request.getPlan());
        return ResponseEntity.ok(billingService.upgradePlan(tenantId, request));
    }

    // ─── Usage APIs ────────────────────────────────────────────

    @Operation(summary = "Get today's usage summary for a tenant")
    @GetMapping("/usage/{tenantId}/summary")
    public ResponseEntity<UsageSummaryResponse> getUsageSummary(
            @PathVariable UUID tenantId) {
        log.debug("Usage summary request: tenantId={}", tenantId);
        return ResponseEntity.ok(usageRecordService.getUsageSummary(tenantId));
    }

    // ─── Invoice APIs ──────────────────────────────────────────

    @Operation(summary = "Generate invoice for current billing period")
    @PostMapping("/invoices/{tenantId}/generate")
    public ResponseEntity<InvoiceResponse> generateInvoice(
            @PathVariable UUID tenantId) {
        log.info("Generate invoice request: tenantId={}", tenantId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(billingService.generateInvoice(tenantId));
    }

    @Operation(summary = "Get all invoices for a tenant")
    @GetMapping("/invoices/{tenantId}")
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @PathVariable UUID tenantId) {
        log.debug("Get invoices request: tenantId={}", tenantId);
        return ResponseEntity.ok(billingService.getInvoices(tenantId));
    }

    @Operation(summary = "Get a specific invoice by ID")
    @GetMapping("/invoices/detail/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @PathVariable UUID invoiceId) {
        log.debug("Get invoice request: invoiceId={}", invoiceId);
        return ResponseEntity.ok(billingService.getInvoice(invoiceId));
    }
}
