package model;

public class Individual extends Customer {
    private double discount = 0.9;

    public Individual(String ID) {
        super(ID);
    }

    public Individual(String name, String phoneNumber, Address address, String ID) {
        super(name, phoneNumber, address, ID);
    }

    @Override
    public double getDiscount() {
        return discount;
    }

    @Override
    public void setDiscount(double discount) {
        this.discount = discount;
    }
} 