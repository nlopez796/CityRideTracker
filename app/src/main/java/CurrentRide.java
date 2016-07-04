package capstone.project.nlopez.cityridetracker;

/**
 * Nuria Lopez
 * CSIS 4900 - Directed Project - City Ride Tracker
 * May 16th, 2016
 * Project to create an application that will capture location via GPS, and Travel Expenses for
 * Business Travelers.  User may be able to capture Amount Paid for Travel Expenses, Type of Ride,
 * and Date and Time of Ride completed as well.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

// Class CurrentRide - Where Ride connected to Service is set up

public class CurrentRide extends Activity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    // Variable references
    private static final String TAG = "CurrentRideActivity";
    protected static final int RESULT_SPEECH = 1;
    protected AutoCompleteTextView myLocationText;
    protected EditText amount;
    protected EditText date;
    protected EditText time;
    protected Spinner spinner;
    String spinnerData;

    List<String> list;
    TextView symbol;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.current_ride );

        // User fields
        myLocationText = (AutoCompleteTextView) findViewById( R.id.location );
        ImageButton btnSpeak = (ImageButton) findViewById( R.id.btnSpeak );
        amount = (EditText) findViewById( R.id.amount );
        symbol = (TextView) findViewById( R.id.symbol );
        date = (EditText) findViewById( R.id.date );
        time = (EditText) findViewById( R.id.time );
        spinner = (Spinner) findViewById( R.id.spinner );

        // Symbol changes depending on location
        Currency currency = Currency.getInstance( Locale.getDefault() );
        String symbolCurr = currency.getSymbol();
        symbol.setText( symbolCurr );

        // Changes font for title
        TextView layoutTitle = (TextView) findViewById( R.id.layout_title_1 );
        Typeface font = Typeface.createFromAsset( getAssets(), "Berlin.ttf" );
        assert layoutTitle != null;
        layoutTitle.setTypeface( font );
        layoutTitle.setText( "Save Current Ride" );

        // Set date and time EditTexts to be read as strings
        dateTimeChanger();

        // Set Spinner elements to be read as strings
        spinnerElements();
        spinner.setPrompt("Please select Ride Type: ");

        // Set EditTexts to be read only for certain fields
        setReadOnly();

        // Get Location for the AutoCompleteView
        locationMethod();

        // Button for Recording amount information via microphone
        btnSpeak.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH );

                intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US" );

                try {
                    startActivityForResult( intent, RESULT_SPEECH );
                    amount.setText( "" );
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText( getApplicationContext(),
                            "Oops! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT );
                    t.show();
                }
            }
        } );
    }

    public void dateTimeChanger() {
        // use SimpleDataFormat and "HH" for hour to get 24-hour clock
        date.setText( DateFormat.format( "MM/dd/yyy", new Date() ) );

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat( "hh:ss a", Locale.US );
        time.setText( sdf.format( c ) );
    }

    public void spinnerElements() {
        list = new ArrayList<>();
        list.add( "" );
        list.add( "Taxi" );
        list.add( "Uber" );
        list.add( "Bus" );
        list.add( "Train" );
        list.add( "Other" );

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>( this,
                android.R.layout.simple_spinner_item, list );
        dataAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

        spinner.setAdapter( dataAdapter );
        spinner.setOnItemSelectedListener( this );
    }

    public void setReadOnly() {
        myLocationText.setEnabled( false );
        date.setEnabled( false );
        time.setEnabled( false );
    }

    public void locationMethod() {
        LocationManager locationManager;
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        Criteria criteria = new Criteria();
        criteria.setAccuracy( Criteria.ACCURACY_FINE );
        criteria.setAltitudeRequired( false );
        criteria.setBearingRequired( false );
        criteria.setCostAllowed( true );
        criteria.setPowerRequirement( Criteria.POWER_LOW );

        // Get best provider whether it's GPS or Network
        String provider = locationManager.getBestProvider( criteria, true );

        // Get last known location first before you can update with a new location
        Location mLocation = locationManager.getLastKnownLocation( provider );
        updateWithNewLocation( mLocation );

        // Request location updates every minute or so... with 10 meters as your lowest distance
        if (ActivityCompat.checkSelfPermission( this,
                android.Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates( provider, (600000 * 10), 10, locationListener );
    }
    // Once there's an update on a different location, app checks if speed has been achieved,
    // if so.. check speed.

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation( location );
        }

        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation( null );
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // Update UI with a new location
    private void updateWithNewLocation(Location location) {
        String addressString = "No Address Found.";

        // Location element
        AtomicReference<String> latLongString = new AtomicReference<>();

        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            Geocoder gc = new Geocoder( this, Locale.getDefault() );
            try {
                // Get Location
                List<Address> addresses = gc.getFromLocation( lat, lng, 1 );
                StringBuilder sb = new StringBuilder();
                if (addresses.size() > 0) {
                    Address address = addresses.get( 0 );

                    // Full location address
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                        sb.append( address.getAddressLine( i ) ).append( ", " );
                    sb.append( address.getCountryName() );
                }
                addressString = sb.toString();
            } catch (IOException ignored) {
            }
        } else
            latLongString.set( "No Address Found." );
        myLocationText.setText( addressString );
    }

    // If you want to cancel, and go to Main Menu, just press the cancel button
    // it will ask if you are sure, and if so, it go back to the Main Menu.
    public void cancelRide(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( this );
        // Setting Dialog Title
        alertDialog.setTitle( "Confirm Cancel..." );
        // Setting Dialog Message
        alertDialog.setMessage( "Are you sure?" );
        // Setting Icon to Dialog
        alertDialog.setIcon( R.drawable.cancel_b );

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton( "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // invoke YES event - If you pressed yes, it will specify "Cancelling.."
                // and cancel Record Ride.
                Toast.makeText( getApplicationContext(), "Cancelling Current Ride info..",
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

    // Called when the user clicks the "Save" button
    public void saveRide(View view) {
        try {
            Log.i( TAG, " saveRide " );

            // Load ride object with fields from form
            Ride ride = new Ride( myLocationText.getText().toString(),
                    amount.getText().toString(),
                    date.getText().toString(),
                    time.getText().toString(),
                    spinnerData );

            // Validate fields before saving
            FieldValidator validator = new FieldValidator( ride );

            // If validation passed insert ride into database and return to main menu
            if (validator.isRideValid()) {
                // Database handle
                DatabaseHandler db = DatabaseHandler.getDBInstance( this );

                // Add ride to database
                boolean success = db.addRide( ride );

                // If add was successful
                if (success) {
                    Toast.makeText( this, "Current ride added", Toast.LENGTH_LONG ).show();
                }

                backToMainMenu();
            }
            // Ride validation problems
            else {
                // Show validation errors
                AlertDialog alert = new AlertDialog.Builder( this ).create();
                alert.setTitle( "New Ride Field Errors" );
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

    // Spinner to String
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerData = parent.getItemAtPosition( position ).toString();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onClick(View v) {
    }

    // Sets the amount EditText to receive voice to text information
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );

                    amount.setText( text.get( 0 ) );
                }
                break;
            }
        }
    }
}



