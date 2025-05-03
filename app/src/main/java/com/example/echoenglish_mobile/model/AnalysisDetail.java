package com.example.echoenglish_mobile.model;

import java.io.Serializable;

public class AnalysisDetail implements Serializable {
    private double pitch;
    private double intensity;

    private String stress_level;
    private double variation;

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
}