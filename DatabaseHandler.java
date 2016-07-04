package capstone.project.nlopez.cityridetracker;

/**
 * Nuria Lopez
 * CSIS 4900 - Directed Project - City Ride Tracker
 * May 16th, 2016
 * Project to create an application that will capture location via GPS, and Travel Expenses for
 * Business Travelers.  User may be able to capture Amount Paid for Travel Expenses, Type of Ride,
 * and Date and Time of Ride completed as well.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

// Class DatabaseHandler - Class to assist in handling database commands

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHandler";

    // Database Version
    private static final int DATABASE_VERSION = 4;

    // Database Name
    public static final String DATABASE_NAME = "MyRidesDatabase";

    // Rides table name
    public static final String TABLE_RIDES = "myRides";

    // Rides Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_SPINNERDATA = "spinnerData";

    // Singleton of db handler class
    private static DatabaseHandler dbInstance = null;


    // Constructor
    protected DatabaseHandler(Context context) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    // Singleton pattern returning shared instance of class
    public static DatabaseHandler getDBInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DatabaseHandler( context );
        }

        return dbInstance;
    }

    // Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i( TAG, " onCreate " );

        try {
            String CREATE_RIDES_TABLE = "CREATE TABLE " + TABLE_RIDES +
                    "(" +
                    KEY_ID + " INTEGER PRIMARY KEY, " +
                    KEY_LOCATION + " TEXT, " +
                    KEY_AMOUNT + " TEXT, " +
                    KEY_DATE + " TEXT, " +
                    KEY_TIME + " TEXT, " +
                    KEY_SPINNERDATA + " TEXT " +
                    " )";

            db.execSQL( CREATE_RIDES_TABLE );
        } catch (Exception e) {
            Log.e( TAG, "*****Error: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i( TAG, " onUpgrade " );

        try {
            // Drop older table if existed
            db.execSQL( "DROP TABLE IF EXISTS " + TABLE_RIDES );

            // Create tables again
            onCreate( db );
        } catch (Exception e) {
            Log.e( TAG, "*****Error: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // Add new Ride
    public boolean addRide(Ride ride) {
        Log.i( TAG, " addRide " );

        boolean success = true;

        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put( KEY_LOCATION, ride.getLocation() );
            values.put( KEY_AMOUNT, ride.getAmount() );
            values.put( KEY_DATE, ride.getDate() );
            values.put( KEY_TIME, ride.getTime() );
            values.put( KEY_SPINNERDATA, ride.getSpinnerData() );

            // Insert row
            db.insert( TABLE_RIDES, null, values );

            db.close();
        } catch (Exception e) {
            success = false;

            Log.e( TAG, "*****Error: " + e.getMessage() );
            e.printStackTrace();
        }

        return success;
    }

    // Get single Ride
    public Ride getRide(int id) {
        Log.i( TAG, " getRide " );

        Ride ride = null;

        try {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query( TABLE_RIDES,
                    new String[]{
                            KEY_ID, KEY_LOCATION, KEY_AMOUNT, KEY_DATE, KEY_TIME, KEY_SPINNERDATA
                    },
                    KEY_ID + "=?", new String[]{String.valueOf( id )}, null, null, null, null );
            if (cursor != null) {
                cursor.moveToFirst();
            }

            assert cursor != null;
            ride = new Ride( Integer.parseInt( cursor.getString( 0 ) ), cursor.getString( 1 ),
                    cursor.getString( 2 ), cursor.getString( 3 ), cursor.getString( 4 ),
                    cursor.getString( 5 ) );

            cursor.close();
            db.close();
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }

        return ride;
    }

    // Getting all Rides
    public ArrayList<Ride> getAllRides() {
        Log.i( TAG, " getAllRides " );

        ArrayList<Ride> rideList = new ArrayList<>();

        try {
            // Select all query
            String selectAllQuery = "SELECT  * FROM " + TABLE_RIDES;

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery( selectAllQuery, null );

            // Looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Ride ride = new Ride();

                    ride.setId( Integer.parseInt( cursor.getString( 0 ) ) );
                    ride.setLocation( cursor.getString( 1 ) );
                    ride.setAmount( cursor.getString( 2 ) );
                    ride.setDate( cursor.getString( 3 ) );
                    ride.setTime( cursor.getString( 4 ) );
                    ride.setSpinnerData( cursor.getString( 5 ) );

                    // Adding Ride to list
                    rideList.add( ride );
                }
                while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }

        return rideList;
    }

    // Update single ride
    public boolean updateRide(Ride ride) {
        Log.i( TAG, " updateRide " );

        boolean success = true;

        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put( KEY_LOCATION, ride.getLocation() );
            values.put( KEY_AMOUNT, ride.getAmount() );
            values.put( KEY_DATE, ride.getDate() );
            values.put( KEY_TIME, ride.getTime() );
            values.put( KEY_SPINNERDATA, ride.getSpinnerData() );

            // Update row
            db.update( TABLE_RIDES, values, KEY_ID + " = ?", new String[]{String.valueOf( ride.getId() )} );

            db.close();
        } catch (Exception e) {
            success = false;
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }

        return success;
    }

    // Delete single ride by ride object
    public boolean deleteRide(Ride ride) {
        Log.i( TAG, " deleteRide " );

        boolean success = true;

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete( TABLE_RIDES, KEY_ID + " = ?", new String[]{String.valueOf( ride.getId() )} );

            db.close();
        } catch (Exception e) {
            success = false;

            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }

        return success;
    }

    // Get ride count
    public int getRideCount() {
        Log.i( TAG, " getRideCount " );

        int count = 0;

        try {
            String countQuery = "SELECT  * FROM " + TABLE_RIDES;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery( countQuery, null );
            count = cursor.getCount();

            cursor.close();
        } catch (Exception e) {
            Log.e( TAG, "** Error **: " + e.getMessage() );
            e.printStackTrace();
        }

        return count;
    }
}
