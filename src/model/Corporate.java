package model;

public class Corporate extends Customer {
    private double discount = 0.8;

    public Corporate(String ID) {
        super(ID);
    }

    public Corporate(String name, String phoneNumber, Address address, String ID) {
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