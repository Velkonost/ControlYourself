package com.velkonost.controlyourself;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * @author Velkonost
 */

public class ConfiguredApplicationsAdapter extends RecyclerView.Adapter<ConfiguredApplicationsAdapter.ViewHolder> {

    private Context context;

    private ArrayList<Drawable> appsIconsList;
    private ArrayList<String> appsTitlesList;
    private ArrayList<String> appsPackagesNamesList;

    private ArrayList<String> packages;
    private ArrayList<Long> wasteTime;
    private ArrayList<Long> maxTime;

    private DBHelper mDBHelper;

    private MainActivity activity;

    ConfiguredApplicationsAdapter(
            MainActivity activity,
            Context context,
            ArrayList<Drawable> appsIconsList,
            ArrayList<String> appsTitlesList,
            ArrayList<String> appsPackagesNamesList
    ) {

        this.activity = activity;
        this.context = context;

        this.appsIconsList = appsIconsList;
        this.appsTitlesList = appsTitlesList;
        this.appsPackagesNamesList = appsPackagesNamesList;

        packages = new ArrayList<>();
        wasteTime = new ArrayList<>();
        maxTime = new ArrayList<>();

        mDBHelper = new DBHelper(context);

        Cursor cursor = mDBHelper.getAllApplications();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                packages.add(cursor.getString(cursor.getColumnIndex("application")));
                wasteTime.add(cursor.getLong(cursor.getColumnIndex("waste_time")));
                maxTime.add(cursor.getLong(cursor.getColumnIndex("max_time")));

                cursor.moveToNext();
            }
        }
    }

    void addNewApplication(
            Drawable appIcon,
            String appTitle,
            String appPackageName,
            long appMaxTime
    ) {
        appsIconsList.add(appIcon);
        appsTitlesList.add(appTitle);
        appsPackagesNamesList.add(appPackageName);
        packages.add(appPackageName);
        wasteTime.add(0L);
        maxTime.add(appMaxTime);

        notifyDataSetChanged();
    }


    @Override
    public ConfiguredApplicationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_configured_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConfiguredApplicationsAdapter.ViewHolder holder, final int position) {

        final int index = appsPackagesNamesList.indexOf(packages.get(position));
        holder.icon.setImageDrawable(appsIconsList.get(index));
        holder.title.setText(appsTitlesList.get(index));

        String applicationWasteTime = formatApplicationWasteTime(wasteTime.get(position));
        String applicationMaxTime = formatApplicationMaxTime(maxTime.get(position));

        holder.wasteTime.setText(applicationWasteTime);
        holder.maxTime.setText(applicationMaxTime);

        holder.wrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertTimePicker(appsPackagesNamesList.get(index), position);
            }
        });
    }

    private String formatApplicationWasteTime(long applicationWasteTime) {
        long wasteHours = applicationWasteTime / 3600;
        long wasteMinutes = (applicationWasteTime - wasteHours * 3600) / 60;
        return wasteHours + context.getString(R.string.h) + wasteMinutes + context.getString(R.string.m);
    }

    private String formatApplicationMaxTime(long applicationMaxTime) {
        long maxHours = applicationMaxTime / 3600;
        long maxMinutes = (applicationMaxTime - maxHours * 3600) / 60;
        return maxHours + context.getString(R.string.h) + maxMinutes + context.getString(R.string.m);
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout wrap;

        ImageView icon;

        TextView title;
        TextView wasteTime;
        TextView maxTime;

        ViewHolder(final View itemView) {
            super(itemView);

            wrap = (RelativeLayout) itemView.findViewById(R.id.wrap);

            icon = (ImageView) itemView.findViewById(R.id.app_icon);
            title = (TextView) itemView.findViewById(R.id.app_title);
            wasteTime = (TextView) itemView.findViewById(R.id.waste_time);
            maxTime = (TextView) itemView.findViewById(R.id.max_time);
        }
    }

    /*
    ** Show AlertDialog with time picker.
    **/
    private void alertTimePicker(
            final String applicationName,
            final int positionIndex
    ) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_select_time, null, false);

        // the time picker on the alert dialog, this is how to get the value
        final TimePicker myTimePicker = (TimePicker) view
                .findViewById(R.id.myTimePicker);

        myTimePicker.setIs24HourView(true);
        final boolean[] allowRemoveApplication = {false};
        final AlertDialog alertDialog = initializeAlertDialog(view, appsTitlesList.get(appsPackagesNamesList.indexOf(applicationName)));

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeBtn = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button neutralBtn = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);

                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        allowRemoveApplication[0] = false;
                        long applicationMaxTime = myTimePicker.getCurrentHour() * 3600 + myTimePicker.getCurrentMinute() * 60;

                        updateConfiguredApplication(positionIndex, applicationMaxTime);
                        mDBHelper.updateApplicationTime(applicationName, applicationMaxTime);

                        alertDialog.cancel();
                    }
                });

                negativeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allowRemoveApplication[0] = false;
                        alertDialog.cancel();
                    }
                });

                neutralBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!allowRemoveApplication[0]) {
                            allowRemoveApplication[0] = true;
                            Toast.makeText(context, context.getString(R.string.tap_to_remove), Toast.LENGTH_SHORT).show();
                        } else {
                            mDBHelper.removeApplicationByPackageName(applicationName);
                            activity.removeConfiguredApplicationByIndex(positionIndex);

                            allowRemoveApplication[0] = false;
                            alertDialog.cancel();
                        }
                    }
                });

            }
        });

        alertDialog.show();
    }

    private AlertDialog initializeAlertDialog(View view, String applicationName) {
        return new AlertDialog.Builder(context)
                .setView(view)
                .setTitle(applicationName)
                .setPositiveButton(R.string.ok, null) //Set to null. We override the onclick
                .setNeutralButton(R.string.remove, null)
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    void updateConfiguredApplication(int index, long applicationMaxTime) {
        maxTime.set(index, applicationMaxTime);
        wasteTime.set(index, 0L);

        updateAdapter();
    }

    void removeConfiguredApplicationByIndex(int index) {
        maxTime.remove(index);
        wasteTime.remove(index);
        packages.remove(index);

        updateAdapter();
    }

    private void updateAdapter() {
        notifyDataSetChanged();
    }


}