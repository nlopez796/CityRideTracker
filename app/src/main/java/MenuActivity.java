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
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

// Class MenuActivity - Menu class (Shows CurrentRide, RecordRide, and RideHistory

public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.menu );

        TextView layoutTitle = (TextView) findViewById( R.id.layout_title );
        Typeface font = Typeface.createFromAsset( getAssets(), "Berlin.ttf" );
        layoutTitle.setTypeface( font );
        layoutTitle.setText( "Main Menu" );

        ImageButton btn1 = (ImageButton) findViewById( R.id.current_ride );
        ImageButton btn2 = (ImageButton) findViewById( R.id.ride_history );
        ImageButton btn3 = (ImageButton) findViewById( R.id.record_ride );

        // Location elements
        LocationManager locationManager;
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        } catch (Exception ignored) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
        } catch (Exception ignored) {
        }
        // If no GPS or Network, disable Current Ride, else continue with Home Screen.
        if (!gps_enabled && !network_enabled) {
            btn1.setEnabled( false );
            Toast.makeText( this, "GPS/Network is not enabled in your device."
                            + "\n Please go to your Settings and Enable GPS.",
                    Toast.LENGTH_LONG ).show();
        } else {

            btn1.setEnabled( true );
            btn2.setEnabled( true );
            btn3.setEnabled( true );

            // Go to Current Ride screen
            btn1.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity( new Intent( MenuActivity.this, CurrentRide.class ) );
                }
            } );
        }

        // Go to Ride History screen
        assert btn2 != null;
        btn2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent( MenuActivity.this, ListRidesActivity.class ) );
            }
        } );

        // Go to Record New Ride screen
        assert btn3 != null;
        btn3.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent( MenuActivity.this, NewRideActivity.class ) );
            }
        } );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i( "MenuActivity", " onOptionsItemSelected " );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected( item );

    }
}

