package model;

public class ExpediteRateCalculator implements RateCalculator {
    @Override
    public double calculateRate(double weight) {
        if (weight < 20) return 60.0;
        else if (weight < 50) return 50.0;
        else if (weight < 100) return 40.0;
        else return 30.0;
    }
} 