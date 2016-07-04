package capstone.project.nlopez.cityridetracker;

/**
 * Nuria Lopez
 * CSIS 4900 - Directed Project - City Ride Tracker
 * May 16th, 2016
 * Project to create an application that will capture location via GPS, and Travel Expenses for
 * Business Travelers.  User may be able to capture Amount Paid for Travel Expenses, Type of Ride,
 * and Date and Time of Ride completed as well.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

// Class EditRideActivity - assists in Editing Rides via the Ride History Menu

public class EditRideActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "EditRideActivity";

    protected AutoCompleteTextView myLocationText;
    protected EditText spinnerData;
    protected Spinner nameSpinner;
    private Ride crntRide;
    private int listViewID;
    public Ride listViewRide;
    protected EditText amount;
    protected EditText date;
    protected EditText time;
    TextView symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate( savedInstanceState );
            Log.i( TAG, " onCreate " );

            setContentView( R.layout.edit_ride );

            // Get layout variables
            myLocationText = (AutoCompleteTextView) findViewById( R.id.location );
            amount = (EditText) findViewById( R.id.amount );
            date = (EditText) findViewById( R.id.date );
            symbol = (TextView) findViewById( R.id.symbol );
            time = (EditText) findViewById( R.id.time );
            spinnerData = (EditText) findViewById( R.id.spinnerRide );
            nameSpinner = (Spinner) findViewById( R.id.spinnerLocations );

            // Change font for title
            TextView layoutTitle = (TextView) findViewById( R.id.layout_title_1 );
            Typeface font = Typeface.createFromAsset( getAssets(), "Berlin.ttf" );
            assert layoutTitle != null;
            layoutTitle.setTypeface( font );
            layoutTitle.setText( "Edit Ride" );

            nameSpinner.setOnItemSelectedListener( this );

            // Symbol changes depending on location
            Currency currency = Currency.getInstance( Locale.getDefault() );
            String symbolCurr = currency.getSymbol();
            symbol.setText( symbolCurr );

            loadSpinner();

            onClickUpdate( null );

        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Load the spinner with the ride list
    private void loadSpinner() {
        try {
            Log.i( TAG, " loadSpinner " );

            // database handle
            DatabaseHandler db = DatabaseHandler.getDBInstance( this );

            // Get list of rides
            ArrayList<Ride> ridesList = db.getAllRides();

            if (ridesList.size() == 0) {
                Toast.makeText( this, "No Rides Exist in Database", Toast.LENGTH_LONG ).show();
                backToMainMenu();
            }

            Intent intent = getIntent();
            listViewID = 0;

            // If this activity was triggered from the list rides activity
            // Get the ID of the active ride
            if (intent.getExtras() != null) {
                listViewID = intent.getExtras().getInt( "rideID" );
                listViewRide = db.getRide( listViewID );
            }

            ArrayAdapter<Ride> adapter = new ArrayAdapter<>( EditRideActivity.this,
                    android.R.layout.simple_spinner_item, ridesList );
            adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

            nameSpinner.setAdapter( adapter );
            nameSpinner.setWillNotDraw( false );
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Retrieves a single ride based on id for view
    private void viewRide(int id) {
        try {
            Log.i( TAG, " viewRides " );

            // database handle
            DatabaseHandler db = DatabaseHandler.getDBInstance( this );

            Ride ride = db.getRide( id );
            crntRide = ride;

            myLocationText.setText( ride.getLocation() );
            amount.setText( ride.getAmount() );
            date.setText( ride.getDate() );
            time.setText( ride.getTime() );
            spinnerData.setText( ride.getSpinnerData() );

        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        try {
            Log.i( TAG, " onItemSelected " );

            // If passed from the list activity
            if (listViewID > 0) {
                // Set the ride to edit fields
                viewRide( listViewID );

                // Set the spinner correctly
                for (int i = 0; i < nameSpinner.getCount(); i++) {
                    Ride spinnerData = (Ride) nameSpinner.getItemAtPosition( i );

                    if (spinnerData.getId() == listViewID) {
                        nameSpinner.setSelection( i );
                    }
                }

                // Reset the ID value to allow spinner to function normally in this view
                listViewID = 0;
            } else {
                Ride selectedRide = (Ride) parent.getItemAtPosition( position );
                int selectedRideId = selectedRide.getId();

                viewRide( selectedRideId );
            }
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Called when the user clicks the "Update" button
    public void onClickUpdate(View view) {
        try {
            Log.i( TAG, " onClickUpdate " );

            // Load ride object with fields from form
            Ride ride = new Ride( myLocationText.getText().toString(),
                    amount.getText().toString(),
                    date.getText().toString(),
                    time.getText().toString(),
                    spinnerData.getText().toString() );

            ride.setId( crntRide.getId() );

            // Validate fields before saving
            FieldValidator validator = new FieldValidator( ride );

            // If validation passed insert ride into database and return to main menu
            if (validator.isRideValid()) {
                // Database handle
                DatabaseHandler db = DatabaseHandler.getDBInstance( this );

                // Update ride in db
                boolean success = db.updateRide( ride );

                // If edit was successful
                if (success) {
                    Toast.makeText( this, "Ride: " + ride.getLocation() + " " +
                            "was Updated in Database", Toast.LENGTH_LONG ).show();
                }

                backToMainMenu();
            } else {
                // Show validation errors
                AlertDialog alert = new AlertDialog.Builder( this ).create();
                alert.setTitle( "Update Ride Errors" );
                alert.setMessage( validator.getValidationIssues() );
                alert.setCancelable( false );
                alert.setButton( DialogInterface.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        } );
                alert.show();
            }

        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // If you want to cancel, and go to Main Menu, just press the cancel button
    // it will ask if you are sure, and if so, it go back to the Main Menu.
    public void onClickCancel(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( this );
        // Setting Dialog Title
        alertDialog.setTitle( "No Changes Made..." );
        // Setting Dialog Message
        alertDialog.setMessage( "Are you sure you don't need to Edit Ride?" );
        // Setting Icon to Dialog
        alertDialog.setIcon( R.drawable.cancel_b );

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton( "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Invoke YES event - If you pressed yes, it will specify "Back to Main Menu.."
                // and cancel Record Ride.
                Toast.makeText( getApplicationContext(), "Back to Ride History Menu..",
                        Toast.LENGTH_SHORT ).show();
                // Return to previous activity by calling finish
                finish();
            }
        } );

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton( "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // invoke NO event - If you pressed No, it will specify "Please continue"
                // working on your ride.
                Toast.makeText( getApplicationContext(), "Please continue..",
                        Toast.LENGTH_SHORT ).show();
                dialog.cancel();
            }
        } );

        // Showing Alert Message
        alertDialog.show();
    }


    // Sends the user back to the main menu
    private void backToMainMenu() {
        try {
            Log.i( TAG, " backToMainMenu " );

            Intent intent = new Intent( this, MenuActivity.class );
            startActivity( intent );
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }
}
