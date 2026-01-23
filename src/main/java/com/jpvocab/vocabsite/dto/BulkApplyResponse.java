package com.jpvocab.vocabsite.dto;

import java.util.List;

public class BulkApplyResponse {
    private boolean success;
    private int updated;
    private List<BulkApplyFailure> failed;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public List<BulkApplyFailure> getFailed() {
        return failed;
    }

    public void setFailed(List<BulkApplyFailure> failed) {
        this.failed = failed;
    }
}
