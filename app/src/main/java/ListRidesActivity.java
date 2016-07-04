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
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

// Class ListRidesActivity - class to list the history of rides created from the CurrentRide
// and Record Ride classes to the database.

public class ListRidesActivity extends ListActivity {

    public static final int MENU_VIEW = Menu.FIRST + 1;
    public static final int MENU_EDIT = Menu.FIRST + 2;
    public static final int MENU_DELETE = Menu.FIRST + 3;
    private static final String TAG = "ListRidesActivity";
    protected ArrayList<Ride> ridesList = null;

    Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate( savedInstanceState );
            Log.i( TAG, " onCreate " );

            thisContext = ListRidesActivity.this;

            initAdapter();
            registerForContextMenu( getListView() );

        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Create the ride list and set it to a stock simple list view
    private void initAdapter() {
        try {
            Log.i( TAG, " initAdapter " );

            // Database handle
            DatabaseHandler db = DatabaseHandler.getDBInstance( this );

            // Get list of rides
            ridesList = db.getAllRides();

            // If no users exist in database
            if (ridesList.size() == 0) {
                Toast.makeText( this, "No Rides Exist in Database!", Toast.LENGTH_LONG ).show();
                backToMainMenu();
            }

            ArrayAdapter<Ride> adapter = new ArrayAdapter<>( this, android.R.layout
                    .simple_list_item_1, ridesList );
            setListAdapter( adapter );
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Create context menu for each ride
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu
            .ContextMenuInfo menuInfo) {
        try {
            Log.i( TAG, " onCreateContextMenu " );

            menu.add( Menu.NONE, MENU_VIEW, Menu.NONE, "View Ride" );
            menu.add( Menu.NONE, MENU_EDIT, Menu.NONE, "Edit Ride" );
            menu.add( Menu.NONE, MENU_DELETE, Menu.NONE, "Delete Ride" );
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Perform associated task on context ride long click
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        try {
            Log.i( TAG, " onContextItemSelected " );

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)
                    item.getMenuInfo();

            switch (item.getItemId()) {
                // View ride
                case MENU_VIEW:

                    int viewRideID = ridesList.get( info.position ).getId();

                    // Call view activity with ride ID passed
                    Intent viewIntent = new Intent( this, ViewRideActivity.class );
                    viewIntent.putExtra( "rideID", viewRideID );
                    startActivity( viewIntent );

                    return (true);

                // Edit ride
                case MENU_EDIT:

                    int editRideID = ridesList.get( info.position ).getId();

                    // Call edit activity with ride ID passed
                    Intent editIntent = new Intent( this, EditRideActivity.class );
                    editIntent.putExtra( "rideID", editRideID );
                    startActivity( editIntent );

                    return (true);

                // Delete ride
                case MENU_DELETE:

                    int deleteRideID = ridesList.get( info.position ).getId();

                    // Database handle
                    final DatabaseHandler db = DatabaseHandler.getDBInstance( this );

                    // Get ride object from ID
                    final Ride ride = db.getRide( deleteRideID );


                    // Show delete confirmation dialog
                    AlertDialog alert = new AlertDialog.Builder( this ).create();
                    alert.setTitle( "Delete Confirmation..." );
                    alert.setMessage( "Are you sure you want to delete " + ride
                            .getLocation() + "?" );
                    alert.setCancelable( false );
                    alert.setButton( DialogInterface.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Delete ride from db
                                    boolean success = db.deleteRide( ride );

                                    // If delete was successful
                                    if (success) {
                                        Toast.makeText( thisContext, "Ride: " + ride.getLocation()
                                                        + " deleted from database",
                                                Toast.LENGTH_LONG ).show();
                                    }

                                    // Reload list to remove deleted ride
                                    initAdapter();

                                    dialog.dismiss();
                                }
                            } );
                    alert.setButton( DialogInterface.BUTTON_NEGATIVE, "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            } );

                    alert.show();

                    return (true);
            }
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }

        return (super.onContextItemSelected( item ));
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
    public void onBackPressed() {
        super.onBackPressed();
        startActivity( new Intent( this, MenuActivity.class ) );
    }
}
