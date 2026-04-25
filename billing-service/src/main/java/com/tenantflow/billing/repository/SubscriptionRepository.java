// billing-service/src/main/java/com/tenantflow/billing/repository/SubscriptionRepository.java

package com.tenantflow.billing.repository;

import com.tenantflow.billing.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByTenantId(UUID tenantId);

    boolean existsByTenantId(UUID tenantId);
}
