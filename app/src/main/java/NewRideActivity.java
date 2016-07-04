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
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

// Class NewRideActivity - Class that creates new ride when phone is off/dead, or out of GPS range.

public class NewRideActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "NewRideActivity";
    private static final String LOG_TAG = "RideGooglePlacesApi";
    protected static final int RESULT_SPEECH = 1;

    AutoCompleteTextView myLocationText;
    protected EditText amount;
    protected EditText date;
    protected EditText time;
    protected Spinner spinner;
    String spinnerData;
    TextView symbol;
    List<String> list;

    CharSequence attributions;
    Place place;

    protected GoogleApiClient mGoogleApiClient;
    protected PlaceArrayAdapter mPlaceArrayAdapter;

    // Southeast and Northeast coordinates for LatLngBounds
    private static final LatLngBounds BOUNDS_EASTERN_VIEW = new LatLngBounds(
            new LatLng( 25.454002, -80.477295 ), new LatLng( 29.823549, -81.290283 ) );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate( savedInstanceState );
            setContentView( R.layout.record_ride );
            Log.i( TAG, " onCreate " );

            // Get field references
            myLocationText = (AutoCompleteTextView) findViewById( R.id.location );
            symbol = (TextView) findViewById( R.id.symbol );
            amount = (EditText) findViewById( R.id.amount );
            date = (EditText) findViewById( R.id.date );
            time = (EditText) findViewById( R.id.time );
            spinner = (Spinner) findViewById( R.id.spinner );

            // Set date and time EditTexts to be read as strings
            dateTimeChanger();

            // Set Spinner elements to be read as strings
            spinnerElements();
            spinner.setPrompt("Please select Ride Type: ");

            // Get Location for the AutoCompleteView
            locationMethod();

            // Example taken from Google Places API
            mGoogleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( Places.GEO_DATA_API )
                    .build();

            myLocationText.setOnItemClickListener( mAutocompleteClickListener );
            mPlaceArrayAdapter = new PlaceArrayAdapter( this, android.R.layout.simple_list_item_1,
                    BOUNDS_EASTERN_VIEW, null );
            myLocationText.setAdapter( mPlaceArrayAdapter );


            // Symbol changes depending on location
            Currency currency = Currency.getInstance( Locale.getDefault() );
            String symbolCurr = currency.getSymbol();
            symbol.setText( symbolCurr );

            // Changes font for title
            TextView layoutTitle = (TextView) findViewById( R.id.layout_title_1 );
            Typeface font = Typeface.createFromAsset( getAssets(), "Berlin.ttf" );
            assert layoutTitle != null;
            layoutTitle.setTypeface( font );
            layoutTitle.setText( "Record a New Ride" );

            // Button used to get information via a Microphone for the Amount EditText
            ImageButton btnSpeak = (ImageButton) findViewById( R.id.btnSpeak );
            assert btnSpeak != null;
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

        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
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
        list.add( " " );
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

    // When location changes, update with GeoCode (String Address)
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
                List<Address> addresses = gc.getFromLocation( lat, lng, 1 );
                StringBuilder sb = new StringBuilder();
                if (addresses.size() > 0) {
                    Address address = addresses.get( 0 );

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

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter
                    .getItem( position );
            final String placeId = String.valueOf( item.placeId );
            Log.i( LOG_TAG, "Selected: " + item.description );
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById( mGoogleApiClient, placeId );
            placeResult.setResultCallback( mUpdatePlaceDetailsCallback );
            Log.i( LOG_TAG, "Fetching details for ID: " + item.placeId );
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e( LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString() );
                return;
            }
            // Selecting the first object buffer.
            place = places.get( 0 );
            attributions = places.getAttributions();

        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient( mGoogleApiClient );
        Log.i( LOG_TAG, "Google Places API connected." );

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e( LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode() );

        Toast.makeText( this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG ).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient( null );
        Log.e( LOG_TAG, "Google Places API connection suspended." );
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerData = parent.getItemAtPosition( position ).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // Connect Google Places API
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    // Disconnect Google Places API
    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    // Called when the user clicks the "Save" button
    public void saveRide(View view) {
        try {
            Log.i( TAG, " saveRide " );

            // Load ride object with fields from form
            Ride ride = new Ride( myLocationText.getText().toString(),
                    amount.getText().toString(),
                    date.getText().toString(),
                    time.getText().toString(), spinnerData );

            // Show confirmation screen
            AlertDialog confirm = new AlertDialog.Builder( this ).create();
            confirm.setTitle( "New Ride Details: " );
            confirm.setMessage( "Ride: " + ride.getLocation() + "\n" + "Amount Paid: $"
                    + ride.getAmount() + "\n" + "Date Paid: " + ride.getDate() + "\n"
                    + "Time Completed: " + ride.getTime() + "\n" + "Type of Ride: "
                    + ride.getSpinnerData() );
            confirm.setCancelable( true );
            confirm.setButton( DialogInterface.BUTTON_POSITIVE, "OK",
                    new
                            DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    backToMainMenu();
                                }
                            } );
            confirm.show();


            // Validate fields before saving
            FieldValidator validator = new FieldValidator( ride );

            // If validation passed insert ride into database and return to main menu
            if (validator.isRideValid()) {
                // Database handle
                DatabaseHandler db = DatabaseHandler.getDBInstance( this );

                // Add ride to db
                boolean success = db.addRide( ride );

                // If add was successful
                if (success) {

                    Toast.makeText( this, "New ride added!", Toast.LENGTH_LONG ).show();
                }
            }
            // Ride validation problems
            else {
                // Show validation errors
                AlertDialog alert = new AlertDialog.Builder( this ).create();
                alert.setTitle( "New Ride Field Errors" );
                alert.setMessage( validator.getValidationIssues() );
                alert.setCancelable( false );
                alert.setButton( DialogInterface.BUTTON_POSITIVE, "OK",
                        new
                                DialogInterface.OnClickListener() {
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


    // Called when the user clicks the "Cancel" button
    public void cancelRide(View view) {
        try {
            Log.i( TAG, " cancelRide " );
            // If you want to cancel, and go to Main Menu, just press the cancel button
            // it will ask if you are sure, and if so, it go back to the Main Menu.
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
                    // Invoke YES event - If you pressed yes, it will specify "Back to Main Menu.."
                    // and cancel Record Ride.
                    Toast.makeText( getApplicationContext(), "Back to Main Menu..",
                            Toast.LENGTH_SHORT ).show();
                    // Return to previous activity by calling finish
                    backToMainMenu();
                }
            } );

            // Setting Negative "NO" Button
            alertDialog.setNegativeButton( "NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Invoke NO event - If you pressed No, it will specify "Please continue"
                    // working on your ride.
                    Toast.makeText( getApplicationContext(), "Please continue..",
                            Toast.LENGTH_SHORT ).show();
                    dialog.cancel();
                }
            } );

            // Showing Alert Message
            alertDialog.show();
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

    // Shows the Amount EditText with the information that was recorded via the microphone
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
