package model;

import java.util.ArrayList;

public class Order {
    private String orderNumber;
    private String orderDate;
    private Customer customer;
    private Sender sender;
    private Recipient recipient;
    private Flight flight;
    private ArrayList<Cargo> cargoItems;
    private Payment paymentMethod;
    private String status;
    private double freight = 0;

    public Order(String orderNumber, String orderDate, Customer customer, Sender sender, Recipient recipient
            , Flight flight, ArrayList<Cargo> cargoItems, Payment paymentMethod) {
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.customer = customer;
        this.sender = sender;
        this.recipient = recipient;
        this.flight = flight;
        this.cargoItems = cargoItems;
        this.paymentMethod = paymentMethod;
    }

    public void addCargo(Cargo cargo) {
        cargoItems.add(cargo);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public ArrayList<Cargo> getCargoItems() {
        return cargoItems;
    }

    public void setCargoItems(ArrayList<Cargo> cargoItems) {
        this.cargoItems = cargoItems;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public Payment getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Payment paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getFreight() {
        return freight;
    }

    public void setFreight(double freight) {
        this.freight = freight;
    }
}
