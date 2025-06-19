package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OrderSummary {
    private final StringProperty orderNumber;
    private final StringProperty orderDate;
    private final StringProperty status;
    private final StringProperty freight;
    private final StringProperty sender;
    private final StringProperty recipient;
    private final StringProperty filePath;
    private final SimpleBooleanProperty selected;

    public OrderSummary(String orderNumber, String orderDate, String status, String freight, String sender, String recipient, String filePath) {
        this.orderNumber = new SimpleStringProperty(orderNumber);
        this.orderDate = new SimpleStringProperty(orderDate);
        this.status = new SimpleStringProperty(status);
        this.freight = new SimpleStringProperty(freight);
        this.sender = new SimpleStringProperty(sender);
        this.recipient = new SimpleStringProperty(recipient);
        this.filePath = new SimpleStringProperty(filePath);
        this.selected = new SimpleBooleanProperty(false);
    }

    public String getOrderNumber() {
        return orderNumber.get();
    }

    public StringProperty orderNumberProperty() {
        return orderNumber;
    }

    public String getOrderDate() {
        return orderDate.get();
    }

    public StringProperty orderDateProperty() {
        return orderDate;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getFreight() {
        return freight.get();
    }

    public StringProperty freightProperty() {
        return freight;
    }

    public String getSender() {
        return sender.get();
    }

    public StringProperty senderProperty() {
        return sender;
    }

    public String getRecipient() {
        return recipient.get();
    }

    public StringProperty recipientProperty() {
        return recipient;
    }

    public String getFilePath() {
        return filePath.get();
    }

    public StringProperty filePathProperty() {
        return filePath;
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
