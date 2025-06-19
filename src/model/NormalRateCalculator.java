package model;

public class NormalRateCalculator implements RateCalculator {
    @Override
    public double calculateRate(double weight) {
        if (weight < 20) return 35.0;
        else if (weight < 50) return 30.0;
        else if (weight < 100) return 25.0;
        else return 15.0;
    }
} 