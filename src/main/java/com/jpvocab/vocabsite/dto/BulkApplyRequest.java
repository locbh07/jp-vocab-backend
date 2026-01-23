package com.jpvocab.vocabsite.dto;

import java.util.List;

public class BulkApplyRequest {
    private List<BulkApplyItem> updates;

    public List<BulkApplyItem> getUpdates() {
        return updates;
    }

    public void setUpdates(List<BulkApplyItem> updates) {
        this.updates = updates;
    }
}
