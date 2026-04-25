package com.tenantflow.billing.service;

import com.tenantflow.billing.dto.request.PlanUpgradeRequest;
import com.tenantflow.billing.dto.response.InvoiceResponse;
import com.tenantflow.billing.dto.response.SubscriptionResponse;
import java.util.List;
import java.util.UUID;

public interface BillingService {
    SubscriptionResponse getSubscription(UUID tenantId);
    SubscriptionResponse createSubscription(UUID tenantId, String plan);
    SubscriptionResponse upgradePlan(UUID tenantId, PlanUpgradeRequest request);
    InvoiceResponse generateInvoice(UUID tenantId);
    List<InvoiceResponse> getInvoices(UUID tenantId);
    InvoiceResponse getInvoice(UUID invoiceId);
}
