// billing-service/src/main/java/com/tenantflow/billing/service/impl/BillingServiceImpl.java

package com.tenantflow.billing.service.impl;

import com.tenantflow.billing.dto.request.PlanUpgradeRequest;
import com.tenantflow.billing.dto.response.InvoiceResponse;
import com.tenantflow.billing.dto.response.SubscriptionResponse;
import com.tenantflow.billing.entity.Invoice;
import com.tenantflow.billing.entity.Subscription;
import com.tenantflow.billing.exception.ResourceNotFoundException;
import com.tenantflow.billing.repository.InvoiceRepository;
import com.tenantflow.billing.repository.SubscriptionRepository;
import com.tenantflow.billing.repository.UsageRecordRepository;
import com.tenantflow.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final UsageRecordRepository usageRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(UUID tenantId) {
        Subscription subscription = subscriptionRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found for tenant: " + tenantId));
        return mapToSubscriptionResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(UUID tenantId, String plan) {

        if (subscriptionRepository.existsByTenantId(tenantId)) {
            return getSubscription(tenantId);
        }

        Subscription.Plan subscriptionPlan = Subscription.Plan.valueOf(plan.toUpperCase());

        Subscription subscription = Subscription.builder()
                .tenantId(tenantId)
                .plan(subscriptionPlan)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .quotaLimit(subscriptionPlan.getQuotaLimit())
                .billingCycleStart(LocalDate.now())
                .billingCycleEnd(LocalDate.now().plusDays(30))
                .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription created: tenantId={} plan={}", tenantId, plan);

        return mapToSubscriptionResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse upgradePlan(UUID tenantId, PlanUpgradeRequest request) {

        Subscription subscription = subscriptionRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found for tenant: " + tenantId));

        Subscription.Plan newPlan = Subscription.Plan.valueOf(
                request.getPlan().toUpperCase());

        log.info("Upgrading plan: tenantId={} from={} to={}",
                tenantId, subscription.getPlan(), newPlan);

        subscription.setPlan(newPlan);
        subscription.setQuotaLimit(newPlan.getQuotaLimit());
        subscription = subscriptionRepository.save(subscription);

        return mapToSubscriptionResponse(subscription);
    }

    @Override
    @Transactional
    public InvoiceResponse generateInvoice(UUID tenantId) {

        Subscription subscription = subscriptionRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found for tenant: " + tenantId));

        LocalDate periodStart = subscription.getBillingCycleStart();
        LocalDate periodEnd = subscription.getBillingCycleEnd();

        // Count total API calls in billing period from usage_records
        // This is event sourcing — billing calculated from event stream!
        long totalCalls = usageRecordRepository
                .countByTenantIdAndDateRange(tenantId, periodStart, periodEnd);

        long amountCents = subscription.getPlan().getMonthlyPriceCents();

        String invoiceNumber = generateInvoiceNumber(tenantId);

        Invoice invoice = Invoice.builder()
                .tenantId(tenantId)
                .subscription(subscription)
                .invoiceNumber(invoiceNumber)
                .status(Invoice.InvoiceStatus.ISSUED)
                .plan(subscription.getPlan())
                .totalApiCalls(totalCalls)
                .amountCents(amountCents)
                .currency("USD")
                .billingPeriodStart(periodStart)
                .billingPeriodEnd(periodEnd)
                .issuedAt(LocalDateTime.now())
                .dueAt(LocalDateTime.now().plusDays(30))
                .build();

        invoice = invoiceRepository.save(invoice);
        log.info("Invoice generated: invoiceNumber={} tenantId={} amount={}",
                invoiceNumber, tenantId, amountCents);

        return mapToInvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoices(UUID tenantId) {
        return invoiceRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::mapToInvoiceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found: " + invoiceId));
        return mapToInvoiceResponse(invoice);
    }

    // ─── Private Helpers ───────────────────────────────────────

    private String generateInvoiceNumber(UUID tenantId) {
        // Format: INV-20240115-ABCD1234
        String datePart = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tenantPart = tenantId.toString()
                .substring(0, 8).toUpperCase();
        return "INV-" + datePart + "-" + tenantPart;
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .tenantId(s.getTenantId())
                .plan(s.getPlan().name())
                .status(s.getStatus().name())
                .quotaLimit(s.getQuotaLimit())
                .billingCycleStart(s.getBillingCycleStart())
                .billingCycleEnd(s.getBillingCycleEnd())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice i) {
        return InvoiceResponse.builder()
                .id(i.getId())
                .tenantId(i.getTenantId())
                .invoiceNumber(i.getInvoiceNumber())
                .status(i.getStatus().name())
                .plan(i.getPlan().name())
                .totalApiCalls(i.getTotalApiCalls())
                .amountCents(i.getAmountCents())
                .amountDollars(i.getAmountCents() / 100.0)
                .currency(i.getCurrency())
                .billingPeriodStart(i.getBillingPeriodStart())
                .billingPeriodEnd(i.getBillingPeriodEnd())
                .issuedAt(i.getIssuedAt())
                .dueAt(i.getDueAt())
                .paidAt(i.getPaidAt())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
