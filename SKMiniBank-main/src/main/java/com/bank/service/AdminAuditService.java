package com.bank.service;

import com.bank.dao.AdminAuditDAO;
import com.bank.model.AdminAudit;
import javax.servlet.http.HttpServletRequest;

public class AdminAuditService {

    private static final AdminAuditDAO auditDAO = new AdminAuditDAO();

    public static void log(String adminId, String action, String module, String targetRecordId, String ipAddress, String result, String details) {
        try {
            AdminAudit audit = new AdminAudit(
                adminId != null ? adminId : "UNKNOWN",
                action,
                module,
                targetRecordId,
                ipAddress != null ? ipAddress : "0.0.0.0",
                result,
                details
            );
            auditDAO.logAction(audit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) return "0.0.0.0";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
