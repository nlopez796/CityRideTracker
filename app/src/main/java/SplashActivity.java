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
import android.os.Bundle;

// Class SplashActivity - Starts app and starts service running in background
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.splash );

        // Starts service running in background
        Intent serviceIntent = new Intent( this, SpeedManagerService.class );
        startService( serviceIntent );

        // Timer set to spend 3 seconds on splash screen then off to main menu
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep( 3000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent( SplashActivity.this, MenuActivity.class );
                    startActivity( intent );
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}

