package com.syncengine.model;

public class EventStatistics {
    private long totalEvents;
    private long normalEvents;
    private long warningEvents;
    private long alertEvents;
    private long criticalEvents;
    private double averageValue;
    private double maxValue;
    private double minValue;
    
    public EventStatistics() {}
    
    public EventStatistics(long totalEvents, long normalEvents, long warningEvents, 
                          long alertEvents, long criticalEvents, double averageValue,
                          double maxValue, double minValue) {
        this.totalEvents = totalEvents;
        this.normalEvents = normalEvents;
        this.warningEvents = warningEvents;
        this.alertEvents = alertEvents;
        this.criticalEvents = criticalEvents;
        this.averageValue = averageValue;
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
    
    // Getters and Setters
    public long getTotalEvents() { return totalEvents; }
    public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }
    
    public long getNormalEvents() { return normalEvents; }
    public void setNormalEvents(long normalEvents) { this.normalEvents = normalEvents; }
    
    public long getWarningEvents() { return warningEvents; }
    public void setWarningEvents(long warningEvents) { this.warningEvents = warningEvents; }
    
    public long getAlertEvents() { return alertEvents; }
    public void setAlertEvents(long alertEvents) { this.alertEvents = alertEvents; }
    
    public long getCriticalEvents() { return criticalEvents; }
    public void setCriticalEvents(long criticalEvents) { this.criticalEvents = criticalEvents; }
    
    public double getAverageValue() { return averageValue; }
    public void setAverageValue(double averageValue) { this.averageValue = averageValue; }
    
    public double getMaxValue() { return maxValue; }
    public void setMaxValue(double maxValue) { this.maxValue = maxValue; }
    
    public double getMinValue() { return minValue; }
    public void setMinValue(double minValue) { this.minValue = minValue; }
}
