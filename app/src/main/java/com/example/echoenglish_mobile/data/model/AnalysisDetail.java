package com.example.echoenglish_mobile.data.model;

public class AnalysisDetail {
    private double pitch;
    private double intensity;

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public String getStress_level() {
        return stress_level;
    }

    public void setStress_level(String stress_level) {
        this.stress_level = stress_level;
    }

    public double getVariation() {
        return variation;
    }

    public void setVariation(double variation) {
        this.variation = variation;
    }

    private String stress_level;
    private double variation;
}