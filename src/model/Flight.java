package model;

public class Flight {
    private String flightNumber;
    private String departureCity;
    private String arrivalCity;
    private String flightDate;
    private double maxWeight;

    public Flight() {
    }

    public Flight(String flightNumber, String departureCity, String flightDate, String arrivalCity, double maxWeight) {
        this.flightNumber = flightNumber;
        this.departureCity = departureCity;
        this.flightDate = flightDate;
        this.arrivalCity = arrivalCity;
        this.maxWeight = maxWeight;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public String getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(String flightDate) {
        this.flightDate = flightDate;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }
}
