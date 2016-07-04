package capstone.project.nlopez.cityridetracker;

/**
 * Nuria Lopez
 * CSIS 4900 - Directed Project - City Ride Tracker
 * May 16th, 2016
 * Project to create an application that will capture location via GPS, and Travel Expenses for
 * Business Travelers.  User may be able to capture Amount Paid for Travel Expenses, Type of Ride,
 * and Date and Time of Ride completed as well.
 */

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

// Class FieldValidator - Error Validation class.  If information is not inputted, or missed,
// or typed/clicked incorrectly, error will be shown.

public class FieldValidator {
    private static final String TAG = "FieldValidator";

    private Ride ride;
    private ArrayList<String> validationErrorList;

    SimpleDateFormat dateFormat = new SimpleDateFormat( "MM/dd/yy", Locale.US );
    SimpleDateFormat timeFormat = new SimpleDateFormat( "hh:mm a", Locale.US );

    // Constructor
    public FieldValidator(Ride ride) {
        try {
            Log.i( TAG, " constructor " );

            this.ride = ride;

            validationErrorList = new ArrayList<>();

            validateRide();
        } catch (Exception e) {
            Log.e( TAG, "*****Error: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Boolean check if ride was valid
    public boolean isRideValid() {
        boolean validRide = true;

        try {
            Log.i( TAG, " isRideValid " );

            // If list has any elements, the validation failed
            if (validationErrorList.size() > 0) {
                validRide = false;
            }
        } catch (Exception e) {
            Log.e( TAG, "*****Error: " + e.getMessage() );
            e.printStackTrace();
        }

        return validRide;

    }

    // Error list of problems with Ride
    public String getValidationIssues() {
        String formattedValidationErrors = "";

        try {
            Log.i( TAG, " getValidationIssues " );

            // If no validation errors set to 'None'
            if (validationErrorList.size() == 0) {
                formattedValidationErrors = "None";
            } else {

                // Iterate through errors list
                for (Object error : validationErrorList) {
                    formattedValidationErrors = formattedValidationErrors + "-" + error + "\n";
                }
            }
        } catch (Exception e) {
            Log.e( TAG, "*****Error: " + e.getMessage() );
            e.printStackTrace();
        }

        return formattedValidationErrors;
    }

    // perform the validations and update the error message array list
    private void validateRide() {
        try {
            Log.i( TAG, " validateRide " );

            // get ride fields that need to be validated
            String location = ride.getLocation();
            String date = ride.getDate();
            String time = ride.getTime();
            String amount = ride.getAmount();

            // prevent double error checking on required fields
            boolean dateExists = false;
            AtomicBoolean emptyLocation = new AtomicBoolean( false );
            AtomicBoolean emptyAmount = new AtomicBoolean( false );

            // required location field missing
            if (location.isEmpty()) {
                emptyLocation.set( true );
                validationErrorList.add( "Location is a required field!" );
            }

            // Required Amount field missing
            if (amount.isEmpty()) {
                emptyAmount.set( true );
                validationErrorList.add( "Amount Paid is a required field!" );
            }

            // location of decimal if present
            int decimalLoc = amount.indexOf( "." );

            // decimal exists
            if (decimalLoc > -1) {
                if (amount.substring( decimalLoc + 1 ).length() != 2) {
                    validationErrorList.add( "Amount paid must be in whole dollar " +
                            "amounts and include 2 digit cents after decimal, if present" );
                }
            }

            // check date against global date format variable
            if (!date.isEmpty()) {
                // if date field is not proper length
                if (date.length() != 10) {
                    validationErrorList.add( "Date must contain 8 characters and " +
                            "follow mm/dd/yyyy format" );
                } else {
                    try {
                        dateFormat.setLenient( false );
                        dateFormat.parse( date );
                        dateExists = true;
                    } catch (ParseException e) {
                        validationErrorList.add( "Date must follow mm/dd/yyyy format" );
                    }
                }
            }

            // check time against global date format variable
            // time must exist if there is a valid date
            if ((!time.isEmpty()) || (dateExists)) {
                // if time field is not proper length
                if (time.length() != 8) {
                    validationErrorList.add( "Time must contain 8 characters and " +
                            "follow hh:mm format" );
                } else {
                    try {
                        timeFormat.setLenient( false );
                        timeFormat.parse( time );
                    } catch (ParseException e) {
                        validationErrorList.add( "Time must follow hh:mm format" );
                    }
                }
            }
        } catch (Exception e) {
            Log.e( TAG, "*****Error: " + e.getMessage() );
            e.printStackTrace();
        }
    }
}
