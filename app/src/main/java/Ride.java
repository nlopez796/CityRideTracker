package capstone.project.nlopez.cityridetracker;

/**
 * Nuria Lopez
 * CSIS 4900 - Directed Project - City Ride Tracker
 * May 16th, 2016
 * Project to create an application that will capture location via GPS, and Travel Expenses for
 * Business Travelers.  User may be able to capture Amount Paid for Travel Expenses, Type of Ride,
 * and Date and Time of Ride completed as well.
 */

public class Ride {
    protected int id;
    protected String location;
    protected String amount;
    protected String date;
    protected String time;
    protected String spinnerData;

    // Empty constructor
    public Ride() {

    }

    // Constructor
    public Ride(int id, String location, String amount, String date, String time, String spinnerData) {
        this.id = id;
        this.location = location;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.spinnerData = spinnerData;

    }

    // Constructor
    public Ride(String location, String amount, String date, String time, String spinnerData) {
        this.location = location;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.spinnerData = spinnerData;

    }

    // Overwrite toString so that Ride #, Location and Date are returned from Ride object
    public String toString() {
        return "Ride # " + id + " - " + date + " - " + location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSpinnerData() {
        return spinnerData;
    }

    public void setSpinnerData(String spinnerData) {
        this.spinnerData = spinnerData;
    }
}