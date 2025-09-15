package com.vcampus.common.entity;

import java.io.Serializable;

public class RepairStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private int totalCount;
    private int pendingCount;
    private int processingCount;
    private int completedCount;
    private int cancelledCount;

    // Getter和Setter方法
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }

    public int getProcessingCount() { return processingCount; }
    public void setProcessingCount(int processingCount) { this.processingCount = processingCount; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public int getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }
}
