package model;

public class Cargo {
    private String name;
    private String ID;
    private double length;
    private double width;
    private double height;
    private double actualWeight;
    private String type;

    public Cargo() {
    }

    public Cargo(String name, String ID, double length, double height, double width, double actualWeight, String type) {
        this.name = name;
        this.ID = ID;
        this.length = length;
        this.height = height;
        this.width = width;
        this.actualWeight = actualWeight;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getActualWeight() {
        return actualWeight;
    }

    public void setActualWeight(double actualWeight) {
        this.actualWeight = actualWeight;
    }

    public double calculateVolumeWeight() {
        return (length * width * height) / 6000.0;
    }

    public double getChargeableWeight() {
        double volumeWeight = calculateVolumeWeight();
        return Math.max(actualWeight, volumeWeight);
    }
}
