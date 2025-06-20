package model;

import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderNumber;
    private java.time.LocalDate orderDate;
    private String customerType;
    private Customer customer;
    private Sender sender;
    private Recipient recipient;
    private Flight flight;
    private List<Cargo> cargoItems;
    private String paymentMethod;
    private String status;
    private double freight = 0;
    private String paymentStatus; // 新增支付状态
    private transient SimpleBooleanProperty selected = new SimpleBooleanProperty(false); // 用于TableView的复选框

    public Order() {
        this.sender = new Sender();
        this.recipient = new Recipient();
        this.cargoItems = new ArrayList<>();
        this.paymentStatus = "未支付"; // 默认值
    }

    public Order(String orderNumber, java.time.LocalDate orderDate, Customer customer, Sender sender, Recipient recipient
            , Flight flight, List<Cargo> cargoItems, String paymentMethod) {
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.customer = customer;
        this.sender = sender;
        this.recipient = recipient;
        this.flight = flight;
        this.cargoItems = cargoItems;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = "未支付"; // 默认设置为未支付
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

    public java.time.LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(java.time.LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public List<Cargo> getCargoItems() {
        return cargoItems;
    }

    public void setCargoItems(List<Cargo> cargoItems) {
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

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
