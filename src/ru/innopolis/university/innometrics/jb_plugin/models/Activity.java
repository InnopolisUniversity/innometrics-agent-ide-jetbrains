package ru.innopolis.university.innometrics.jb_plugin.models;

import java.util.ArrayList;
import java.util.List;

public class Activity {
    public List<Measurement> measurements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public void addMeasurement(Measurement measurement) {
        if (measurements == null) {
            measurements = new ArrayList<>();
        }
        measurements.add(measurement);
    }

    @Override
    public String toString() {
        return "Activity{" +
                "measurements=" + measurements +
                ", name='" + name + '\'' +
                '}';
    }
}
