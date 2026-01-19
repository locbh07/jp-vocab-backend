package com.jpvocab.vocabsite.model;

public class QuizPlanResponse {
    private int batchSize;
    private int plannedSessions;
    private int completedSessionsToday;
    private int remainingSessionsToday;
    private QuizPlanNextSession nextSession;

    public int getBatchSize() {
        return batchSize;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    public int getPlannedSessions() {
        return plannedSessions;
    }
    public void setPlannedSessions(int plannedSessions) {
        this.plannedSessions = plannedSessions;
    }
    public int getCompletedSessionsToday() {
        return completedSessionsToday;
    }
    public void setCompletedSessionsToday(int completedSessionsToday) {
        this.completedSessionsToday = completedSessionsToday;
    }
    public int getRemainingSessionsToday() {
        return remainingSessionsToday;
    }
    public void setRemainingSessionsToday(int remainingSessionsToday) {
        this.remainingSessionsToday = remainingSessionsToday;
    }
    public QuizPlanNextSession getNextSession() {
        return nextSession;
    }
    public void setNextSession(QuizPlanNextSession nextSession) {
        this.nextSession = nextSession;
    }
}
