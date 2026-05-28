package com.CompraVenta.Backend.Audit.aspect;

import com.CompraVenta.Backend.Audit.annotation.Auditable;
import com.CompraVenta.Backend.Audit.entity.AudLog;
import com.CompraVenta.Backend.Audit.repository.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;


@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final ObjectMapper objectMapper;
    private final AuditRepository auditRepository;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String employeeId = extractEmployeeId();
        String ipAddress = extractIpAddress();
        String beforeValue = null;
        String afterValue = null;
        String errorMessage = null;
        Object result = null;
        try {
           if(joinPoint.getArgs().length > 0 ){
               beforeValue = serializeSafely(joinPoint.getArgs()[0]);
           }
           result = joinPoint.proceed();
           if(result!=null){
               afterValue =  serializeSafely(result);
           }
        }catch (Throwable ex){
            errorMessage = ex.getMessage();
            throw ex;
        }finally {
            persisAduitLog(
                    auditable.operation(),
                    auditable.entity(),
                    employeeId,
                    ipAddress,
                    beforeValue,
                    afterValue,
                    errorMessage
            );
        }
        return result;

    }
    private void persisAduitLog(
            String operation, String entityType, String employeeId, String ipAddress,
            String beforeValue, String afterValue, String errorMessage
    ){
        try{
            AudLog log =  AudLog.builder()
                    .operation(operation)
                    .entityType(entityType)
                    .employeeId(employeeId)
                    .ipAddress(ipAddress)
                    .beforeValue(beforeValue)
                    .afterValue(afterValue)
                    .errorMessage(errorMessage)
                    .timestamp(Instant.now())
                    .build();
            auditRepository.save(log);
        }catch (Exception e){
            log.error("Error persisting audit log for operation{}: {}",operation,e.getMessage());
        }
    }
    private String extractEmployeeId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "ANONYMOUS";

    }
    private String extractIpAddress(){
        try{
            ServletRequestAttributes requestAttributes =
                        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        }catch (IllegalStateException ex){
            return "UNKNOWN";
        }
    }
    private String  serializeSafely(Object object){
        try {
            return objectMapper.writeValueAsString(object);

        }catch (Exception ex){
            return object.toString();
        }
    }
}
