package capstone.project.nlopez.cityridetracker;

/**
 * Nuria Lopez
 * CSIS 4900 - Directed Project - City Ride Tracker
 * May 16th, 2016
 * Project to create an application that will capture location via GPS, and Travel Expenses for
 * Business Travelers.  User may be able to capture Amount Paid for Travel Expenses, Type of Ride,
 * and Date and Time of Ride completed as well.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

// Class DeleteRideActivity - Assists in deleting Rides from the Database

public class DeleteRideActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "DeleteRideActivity";

    protected AutoCompleteTextView myLocationText;
    protected EditText spinnerString;
    protected EditText amount;
    protected EditText date;
    protected EditText time;
    protected Spinner spinner;

    private Spinner nameSpinner;

    private Ride crntRide;
    private Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate( savedInstanceState );
            Log.i( TAG, " onCreate " );

            setContentView( R.layout.delete_ride );

            // Changes font for title
            TextView layoutTitle = (TextView) findViewById( R.id.layout_title_1 );
            Typeface font = Typeface.createFromAsset( getAssets(), "Berlin.ttf" );
            assert layoutTitle != null;
            layoutTitle.setTypeface( font );
            layoutTitle.setText( "Delete Ride" );


            // get field references
            myLocationText = (AutoCompleteTextView) findViewById( R.id.location );
            amount = (EditText) findViewById( R.id.amount );
            date = (EditText) findViewById( R.id.date );
            time = (EditText) findViewById( R.id.time );
            spinner = (Spinner) findViewById( R.id.spinner );

            nameSpinner.setOnItemSelectedListener( this );

            thisContext = DeleteRideActivity.this;

            loadSpinner();
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // load the spinner with the ride names
    private void loadSpinner() {
        try {
            Log.i( TAG, " loadSpinner " );

            // database handle
            DatabaseHandler db = DatabaseHandler.getDBInstance( this );

            // get list of rides
            ArrayList<Ride> ridesList = db.getAllRides();

            if (ridesList.size() == 0) {
                Toast.makeText( this, "No Rides Exist in Database", Toast.LENGTH_LONG ).show();
                backToMainMenu();
            }

            ArrayAdapter<Ride> adapter = new ArrayAdapter<>( DeleteRideActivity.this,
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

            // Database handle
            DatabaseHandler db = DatabaseHandler.getDBInstance( this );

            Ride ride = db.getRide( id );
            crntRide = ride;

            myLocationText.setText( ride.getLocation() );
            amount.setText( ride.getAmount() );
            date.setText( ride.getDate() );
            time.setText( ride.getTime() );
            spinnerString.setText( ride.getSpinnerData() );
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // This event fires when the spinner changes and the position in the list (plus one)
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        try {
            Log.i( TAG, " onItemSelected " );

            Ride selectedRide = (Ride) parent.getItemAtPosition( position );
            int selectedRideId = selectedRide.getId();

            viewRide( selectedRideId );
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Called when the user clicks the "Delete" button
    public void onClickDelete(View view) {
        try {
            Log.i( TAG, " onClickDelete " );

            // Database handle
            final DatabaseHandler db = DatabaseHandler.getDBInstance( this );

            // Show delete confirmation dialog
            AlertDialog alert = new AlertDialog.Builder( this ).create();
            alert.setTitle( "Delete Confirmation" );
            alert.setMessage( "Are you sure you want to delete " + crntRide.getLocation() + "?" );
            alert.setCancelable( false );
            alert.setButton( DialogInterface.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.i( TAG, "** crnt ride id: " + crntRide.getId() );

                    // Delete ride from db
                    boolean success = db.deleteRide( crntRide );

                    // If delete was successful
                    if (success) {
                        Toast.makeText( thisContext, "Ride: " + crntRide.getLocation() +
                                " Deleted from Database",
                                Toast.LENGTH_LONG ).show();
                    }

                    dialog.dismiss();

                    backToMainMenu();
                }
            } );
            alert.setButton( DialogInterface.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            } );

            alert.show();
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Called when the user clicks the "Cancel" button
    public void onClickCancel(View view) {
        try {
            Log.i( TAG, " onClickCancel " );

            backToMainMenu();
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
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
