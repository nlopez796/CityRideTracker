package capstone.project.nlopez.cityridetracker;

/**
 * Nuria Lopez
 * CSIS 4900 - Directed Project - City Ride Tracker
 * May 16th, 2016
 * Project to create an application that will capture location via GPS, and Travel Expenses for
 * Business Travelers.  User may be able to capture Amount Paid for Travel Expenses, Type of Ride,
 * and Date and Time of Ride completed as well.
 */

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class SpeedManagerService extends Service implements IBaseGpsListener {

    private static final String TAG = "SpeedCheckerService";
    CountDownTimer timer;
    boolean timer_started = false;

    @Override
    public void onCreate() {
        Log.i( TAG, "Service onCreate" );

        LocationManager locationManager = (LocationManager) this.
                getSystemService( Context.LOCATION_SERVICE );
        if (ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this );
        SpeedManagerService.this.updateSpeed( null );
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i( TAG, "Service onStartCommand" );
        updateSpeed( null );
        return Service.START_STICKY;
    }

    public void updateSpeed(SpeedLocation location) {

        float nCurrentSpeed = (float) 3.57632;

        if (location != null)
            nCurrentSpeed = location.getSpeed();


        // In meters/second, if speed goes above 8 mph, then it will just log the speed.
        if (nCurrentSpeed >= 3.57632)
            Log.i( "Current Ride: ", "User is in a Vehicle. Speed is: " +
                    Math.round( nCurrentSpeed * 2.23694 / 3.6f ) + " mph. " );

        // However, if speed falls below 5 mph, then countdown timer
        // of 3 minutes will begin.

        if (nCurrentSpeed <= 2.23452)
            Log.i( "Current Ride: ", "Speed has decreased.  It is now: " +
                    Math.round( nCurrentSpeed * 2.23694 / 3.6f ) + " mph. " );
        final float finalNCurrentSpeed = nCurrentSpeed;
        timer = new CountDownTimer( 180000, 1000 ) {

            @Override
            public void onTick(long millisUntilFinished) {

                if (finalNCurrentSpeed > 3.57632)
                this.cancel();
                Log.i( "Current Ride", "Timer countdown: " + millisUntilFinished / 1000 + " seconds." );
            }

            // When countdown timer has finished, a Notification will pop up
            // asking user if they just paid for a ride.
            @Override
            public void onFinish() {
                Intent resultIntent = new Intent( SpeedManagerService.this, CurrentRide.class );
                NotificationCompat.Builder builder = new NotificationCompat
                        .Builder( SpeedManagerService.this );
                builder.setContentText( "Click to save Current Ride info" );
                builder.setSmallIcon( R.mipmap.ic_launcher );
                builder.setContentTitle( "Did you just pay for a Ride?" );
                builder.setContentIntent( PendingIntent.getActivity( SpeedManagerService.this, 0, resultIntent, 0 ) );
                NotificationManagerCompat.from( SpeedManagerService.this ).notify( 0,
                        builder.build() );
                cancel();
            }
        };
        timer.start();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            SpeedLocation myLocation = new SpeedLocation( location, true );
            this.updateSpeed( myLocation );
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        timer_started = false;
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }
}