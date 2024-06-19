package com.phonecompany.billing.model;

import java.time.LocalDateTime;

public class Log {

    private String number;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean artificial;

    public Log(String number, LocalDateTime startTime, LocalDateTime endTime) {
        this.number = number;
        this.startTime = startTime;
        this.endTime = endTime;
        this.artificial = false;
    }

    public Log(String number, LocalDateTime startTime, LocalDateTime endTime, boolean artificial) {
        this.number = number;
        this.startTime = startTime;
        this.endTime = endTime;
        this.artificial = artificial;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isArtificial() {
        return artificial;
    }

    public void setArtificial(boolean artificial) {
        this.artificial = artificial;
    }
}
