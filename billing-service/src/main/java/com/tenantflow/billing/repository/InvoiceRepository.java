// billing-service/src/main/java/com/tenantflow/billing/repository/InvoiceRepository.java

package com.tenantflow.billing.repository;

import com.tenantflow.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByTenantIdAndStatus(UUID tenantId, Invoice.InvoiceStatus status);
}
