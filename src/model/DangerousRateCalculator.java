package model;

public class DangerousRateCalculator implements RateCalculator {
    @Override
    public double calculateRate(double weight) {
        if (weight < 20) return 80.0;
        else if (weight < 50) return 50.0;
        else if (weight < 100) return 30.0;
        else return 20.0;
    }
} 