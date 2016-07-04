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
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class ViewRideActivity extends Activity implements OnItemSelectedListener {
    private static final String TAG = "ViewRideActivity";

    protected AutoCompleteTextView myLocationText;
    protected EditText amount;
    protected EditText date;
    protected EditText time;
    TextView symbol;
    protected EditText spinnerData;
    protected Spinner locationSpinner;

    private int listViewID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate( savedInstanceState );
            Log.i( TAG, " onCreate " );

            setContentView( R.layout.view_ride );

            // Get field references
            myLocationText = (AutoCompleteTextView) findViewById( R.id.location );
            amount = (EditText) findViewById( R.id.amount );
            symbol = (TextView) findViewById( R.id.symbol );
            date = (EditText) findViewById( R.id.date );
            time = (EditText) findViewById( R.id.time );
            spinnerData = (EditText) findViewById( R.id.spinnerRide );

            // Changes font for title
            TextView layoutTitle = (TextView) findViewById( R.id.layout_title_1 );
            Typeface font = Typeface.createFromAsset( getAssets(), "Berlin.ttf" );
            assert layoutTitle != null;
            layoutTitle.setTypeface( font );
            layoutTitle.setText( "View a Ride" );

            locationSpinner = (Spinner) findViewById( R.id.spinnerLocations );
            locationSpinner.setOnItemSelectedListener( this );

            loadSpinner();

            // Symbol changes depending on location
            Currency currency = Currency.getInstance( Locale.getDefault() );
            String symbolCurr = currency.getSymbol();
            symbol.setText( symbolCurr );

            setReadOnly();

        } catch (Exception e) {
            Log.e( TAG, "** Error **" + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Load the spinner with the ride locations
    private void loadSpinner() {
        try {
            Log.i( TAG, " loadSpinner " );

            // database handle
            DatabaseHandler db = DatabaseHandler.getDBInstance( this );

            // Get list of rides
            ArrayList<Ride> ridesList = db.getAllRides();

            if (ridesList.size() == 0) {
                Toast.makeText( this, "No rides exist in database", Toast.LENGTH_LONG ).show();
                backToMainMenu();
            }

            Intent intent = getIntent();
            listViewID = 0;

            // If this activity was triggered from the list rides activity
            // get the ID of the active ride
            if (intent.getExtras() != null) {
                listViewID = intent.getExtras().getInt( "rideID" );
            }

            ArrayAdapter<Ride> adapter = new ArrayAdapter<>( ViewRideActivity.this,
                    android.R.layout.simple_spinner_item, ridesList );
            adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

            locationSpinner.setAdapter( adapter );
            locationSpinner.setWillNotDraw( false );
        } catch (Exception e) {
            Log.e( TAG, "** Error **" + e.getMessage() );
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

    // This event fires when the spinner changes and the position in the list
    // is sent to the view ride method for retrieval.
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        try {
            Log.i( TAG, " onItemSelected " );

            // If passed from the list activity
            if (listViewID > 0) {
                // set the ride to view fields
                viewRide( listViewID );

                // Set the spinner correctly
                for (int i = 0; i < locationSpinner.getCount(); i++) {
                    Ride spinnerRide = (Ride) locationSpinner.getItemAtPosition( i );

                    if (spinnerRide.getId() == listViewID) {
                        locationSpinner.setSelection( i );
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
            Log.e( TAG, "** Error **" + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Called when the user clicks the "Cancel" button
    public void onClickCancel(View view) {
        try {
            Log.i( TAG, " onClickCancel " );

            backToHistoryMenu();
        } catch (Exception e) {
            Log.e( TAG, "** Error **" + e.getMessage() );
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

    // Sends the user back to the ride history menu
    private void backToHistoryMenu() {
        try {
            Log.i( TAG, " backToHistoryMenu " );

            Intent intent = new Intent( this, ListRidesActivity.class );
            startActivity( intent );
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public void setReadOnly() {
        myLocationText.setEnabled( false );
        amount.setEnabled( false );
        date.setEnabled( false );
        time.setEnabled( false );
        spinnerData.setEnabled( false );
    }
}

