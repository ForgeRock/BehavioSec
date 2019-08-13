package com.behaviosec.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehavioSecReport {

    private boolean isBot = false;
    private boolean isTrained = false;
    private boolean ipChanged = false;
    private boolean isWhitelisted = false;
    private boolean isSessionCorrupted = false;
    private boolean deviceChanged = false;
    private boolean isDataCorrupted = false;
    private boolean isReplay = false;
    private boolean tabAnomaly = false;
    private boolean numpadAnomaly = false;
    private int ipSeverity = 0;
    private long startTimestamp = 0;
    private long endTimestamp = 0;
    private double score = 0.0;
    private double confidence = 0.0;
    private double uiScore = 0.0;
    private double uiConfidence = 0.0;
    private String userid = "";
    private String startDate= "";
    private String endDate = "";


    /**
     * data representation constructor
     */
    public BehavioSecReport() { }

    /**
     * auto generated setters and getters
     */
    public boolean isIsbot() {
        return isBot;
    }

    public void setIsbot(boolean isbot) {
        this.isBot = isbot;
    }

    public boolean isTrained() {
        return isTrained;
    }

    public void setTrained(boolean trained) {
        isTrained = trained;
    }

    public boolean isIpChanged() {
        return ipChanged;
    }

    public void setIpChanged(boolean ipChanged) {
        this.ipChanged = ipChanged;
    }

    public boolean isWhitelisted() {
        return isWhitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        isWhitelisted = whitelisted;
    }

    public boolean isSessionCorrupted() {
        return isSessionCorrupted;
    }

    public void setSessionCorrupted(boolean sessionCorrupted) {
        isSessionCorrupted = sessionCorrupted;
    }

    public boolean isDeviceChanged() {
        return deviceChanged;
    }

    public void setDeviceChanged(boolean deviceChanged) {
        this.deviceChanged = deviceChanged;
    }

    public boolean isDataCorrupted() {
        return isDataCorrupted;
    }

    public void setDataCorrupted(boolean dataCorrupted) {
        isDataCorrupted = dataCorrupted;
    }

    public boolean isReplay() {
        return isReplay;
    }

    public void setReplay(boolean replay) {
        isReplay = replay;
    }

    public boolean isTabAnomaly() {
        return tabAnomaly;
    }

    public void setTabAnomaly(boolean tabAnomaly) {
        this.tabAnomaly = tabAnomaly;
    }

    public boolean isNumpadAnomaly() {
        return numpadAnomaly;
    }

    public void setNumpadAnomaly(boolean numpadAnomaly) {
        this.numpadAnomaly = numpadAnomaly;
    }

    public int getIpSeverity() {
        return ipSeverity;
    }

    public void setIpSeverity(int ipSeverity) {
        this.ipSeverity = ipSeverity;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getUiScore() {
        return uiScore;
    }

    public void setUiScore(double uiScore) {
        this.uiScore = uiScore;
    }

    public double getUiConfidence() {
        return uiConfidence;
    }

    public void setUiConfidence(double uiConfidence) {
        this.uiConfidence = uiConfidence;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }





}
