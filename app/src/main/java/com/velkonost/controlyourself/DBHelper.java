package com.velkonost.controlyourself;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Velkonost
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TABLE_PREFIX = "controlyourself_";
    private static final String APPLICATIONS = "applications";
    private static final String META_DATA = "meta_data";
    private static final String DB_NAME = "control_yourself";

    private Context mContext;

    public DBHelper(Context context) {

        // конструктор суперкласса
        super(context, DB_NAME, null, 1);

        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("LOG_DB", "--- onCreate database ---");

        createApplicationsTable(db);
        createMetaDataTable(db);

        insertMetaCurrentDay(db);
    }

    void updateCurrentDay() {
        Date currentDay = Calendar.getInstance().getTime();
        Date previousDay = currentDay;

        Cursor cursor = this.getWritableDatabase()
                .rawQuery("select * from " + TABLE_PREFIX + META_DATA + " WHERE meta_key = ?", new String[]{"current_day"});

        if (cursor.moveToFirst()) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss zzzz yyyy", Locale.US);
            try {
                previousDay = dateFormat.parse(cursor.getString(cursor.getColumnIndex("meta_value")));
                if (currentDay.getTime() - previousDay.getTime() >= 86400000) {
                    String startOfCurrentDay = String.valueOf(getStartOfDay(currentDay));
                    ContentValues cvColumn = new ContentValues();
                    cvColumn.put("meta_value", startOfCurrentDay);

                    this.getWritableDatabase().update(TABLE_PREFIX + META_DATA, cvColumn, "meta_key = ?",
                            new String[]{"current_day"});
                    resetAllApplicationsWasteTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    private void resetAllApplicationsWasteTime() {
        ContentValues cvColumn = new ContentValues();
        cvColumn.put("waste_time", 0);
        this.getWritableDatabase().update(TABLE_PREFIX + APPLICATIONS, cvColumn, null, null);
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    private void insertMetaCurrentDay(SQLiteDatabase db) {
        if (db.query(TABLE_PREFIX + META_DATA,
                null,
                "meta_key = ?",
                new String[] {"current_day"},
                null, null, null).getCount() == 0) {
            ContentValues cvColumn = new ContentValues();
            cvColumn.put("meta_key", "current_day");
            cvColumn.put("meta_value", String.valueOf(getStartOfDay(Calendar.getInstance().getTime())));
            db.insert(TABLE_PREFIX + META_DATA, null, cvColumn);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    void updateApplicationTime(String applicationPackage, long maxTime) {
        ContentValues cvColumn = new ContentValues();
        cvColumn.put("application", applicationPackage);
        cvColumn.put("max_time", maxTime);
        cvColumn.put("waste_time", 0);
        if (this.getWritableDatabase().query(TABLE_PREFIX + APPLICATIONS,
                null,
                "application = ?",
                new String[] {applicationPackage},
                null, null, null).getCount() == 0) {
            this.getWritableDatabase().insert(TABLE_PREFIX + APPLICATIONS, null, cvColumn);
        } else {

            this.getWritableDatabase().update(TABLE_PREFIX + APPLICATIONS, cvColumn, "application = ?",
                    new String[]{applicationPackage});
        }
    }


    private void createMetaDataTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_PREFIX + META_DATA + " ("
                + "id integer primary key autoincrement,"
                + "meta_key text,"
                + "meta_value text"
                + ");");
    }

    private void createApplicationsTable(SQLiteDatabase db){
        db.execSQL("create table if not exists " + TABLE_PREFIX + APPLICATIONS + " ("
                + "id integer primary key autoincrement,"
                + "application text,"
                + "waste_time integer,"
                + "max_time integer"
                + ");");
    }

    void removeApplicationByPackageName(String packageName) {
        this.getWritableDatabase().delete(TABLE_PREFIX + APPLICATIONS, "application = ?", new String[]{packageName});
    }


    void updateApplicationByPackageName(String packageName) {
        Cursor c = this.getWritableDatabase().query(TABLE_PREFIX + APPLICATIONS,
                null,
                "application = ?",
                new String[] {packageName},
                null, null, null);

        if (c.moveToFirst()) {
            int wasteTimeIndex = c.getColumnIndex("waste_time");
            long wasteTime =  c.getLong(wasteTimeIndex) + 3;
            c.close();

            ContentValues cvColumn = new ContentValues();
            cvColumn.put("waste_time", wasteTime);

            this.getWritableDatabase().update(TABLE_PREFIX + APPLICATIONS, cvColumn, "application = ?",
                    new String[]{packageName});
        }
    }

    Cursor getAllApplications() {
        return this.getWritableDatabase().rawQuery("select * from " + TABLE_PREFIX + APPLICATIONS, null);
    }

    ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }

}
