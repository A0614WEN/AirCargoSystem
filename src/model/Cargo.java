package model;

import javafx.beans.property.*;

public class Cargo {

    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty type;
    private final IntegerProperty quantity;
    private final DoubleProperty width;
    private final DoubleProperty length;
    private final DoubleProperty height;
    private final DoubleProperty weight;
    private final ReadOnlyDoubleWrapper volume;

    public Cargo(String id, String name, String type, int quantity, double width, double length, double height, double weight) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.width = new SimpleDoubleProperty(width);
        this.length = new SimpleDoubleProperty(length);
        this.height = new SimpleDoubleProperty(height);
        this.weight = new SimpleDoubleProperty(weight);
        this.volume = new ReadOnlyDoubleWrapper();

        // Bind volume to dimensions
        this.volume.bind(this.width.multiply(this.length).multiply(this.height));
    }

    // --- Property Getters ---
    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty typeProperty() { return type; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty widthProperty() { return width; }
    public DoubleProperty lengthProperty() { return length; }
    public DoubleProperty heightProperty() { return height; }
    public DoubleProperty weightProperty() { return weight; }
    public ReadOnlyDoubleProperty volumeProperty() { return volume.getReadOnlyProperty(); }

    // --- Standard Getters ---
    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getType() { return type.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getWidth() { return width.get(); }
    public double getLength() { return length.get(); }
    public double getHeight() { return height.get(); }
    public double getWeight() { return weight.get(); }
    public double getVolume() { return volume.get(); }

    // --- Standard Setters ---
    public void setId(String id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setType(String type) { this.type.set(type); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setWidth(double width) { this.width.set(width); }
    public void setLength(double length) { this.length.set(length); }
    public void setHeight(double height) { this.height.set(height); }
    public void setWeight(double weight) { this.weight.set(weight); }

    public double getChargeableWeight() {
        double volumeWeight = getVolume() / 6000.0;
        return Math.max(getWeight(), volumeWeight);
    }
}
