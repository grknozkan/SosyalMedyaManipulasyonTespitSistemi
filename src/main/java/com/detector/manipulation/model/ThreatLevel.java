package com.detector.manipulation.model;

public enum ThreatLevel {
    LOW("Düşük", "#10b981"),
    MEDIUM("Orta", "#f59e0b"),
    HIGH("Yüksek", "#f97316"),
    CRITICAL("Kritik", "#ef4444");

    private final String label;
    private final String color;

    ThreatLevel(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
