package com.bank.model;

import java.sql.Timestamp;

public class AdminAudit {

    private int auditId;
    private String adminId;
    private String action;
    private String module;
    private String targetRecordId;
    private String ipAddress;
    private String result;
    private String details;
    private Timestamp createdAt;

    public AdminAudit() {
    }

    public AdminAudit(String adminId, String action, String module, String targetRecordId, String ipAddress, String result, String details) {
        this.adminId = adminId;
        this.action = action;
        this.module = module;
        this.targetRecordId = targetRecordId;
        this.ipAddress = ipAddress;
        this.result = result;
        this.details = details;
    }

    public int getAuditId() {
        return auditId;
    }

    public void setAuditId(int auditId) {
        this.auditId = auditId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getTargetRecordId() {
        return targetRecordId;
    }

    public void setTargetRecordId(String targetRecordId) {
        this.targetRecordId = targetRecordId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
