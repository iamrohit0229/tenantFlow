package com.tenantflow.tenant.interceptor;

import com.tenantflow.tenant.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TenantSchemaInterceptor implements HandlerInterceptor {

    // X-Tenant-ID header set by API Gateway
    // after extracting tenantId from JWT
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null && !tenantId.isBlank()) {
            TenantContext.setTenantId(tenantId);
            log.debug("Tenant context set from header: {}", tenantId);
        }
        return true;
    }

    // CRITICAL: Always clear ThreadLocal after request
    // Memory leak if not cleared!
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
