package model;

public abstract class Customer extends Person {
    private String ID;
    private double dicount;

    public Customer() {
    }

    public Customer(String ID) {
        this.ID = ID;
    }

    public Customer(String name, String phoneNumber, Address address, String ID) {
        super(name, phoneNumber, address);
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    abstract public double getDiscount();

    abstract public void setDiscount(double discount);
}
