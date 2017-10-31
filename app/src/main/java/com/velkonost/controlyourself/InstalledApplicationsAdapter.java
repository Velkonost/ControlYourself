package com.velkonost.controlyourself;

import android.annotation.TargetApi;
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

class InstalledApplicationsAdapter extends RecyclerView.Adapter<InstalledApplicationsAdapter.ViewHolder> {

    private Context context;

    private ArrayList<Drawable> appsIconsList;
    private ArrayList<String> appsTitlesList;
    private ArrayList<String> appsPackagesNamesList;

    private ArrayList<String> packages;
    private ArrayList<Long> wasteTime;
    private ArrayList<Long> maxTime;

    private DBHelper mDBHelper;

    private MainActivity activity;


    InstalledApplicationsAdapter(
            MainActivity activity,
            Context context,
            ArrayList<Drawable> appsIconsList,
            ArrayList<String> appsTitlesList,
            ArrayList<String> appsPackagesNamesList
    ) {
        this.context = context;
        this.activity = activity;

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

    ///

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_installed_application, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        boolean alreadyConfigured = false;

        if (packages.contains(appsPackagesNamesList.get(position))) {
            alreadyConfigured = true;
        }

        holder.icon.setImageDrawable(appsIconsList.get(position));
        holder.title.setText(appsTitlesList.get(position));

        final boolean finalAlreadyConfigured = alreadyConfigured;
        holder.wrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!finalAlreadyConfigured) {
                    alertTimePickerNotConfigured(
                            appsIconsList.get(position),
                            appsTitlesList.get(position),
                            appsPackagesNamesList.get(position)
                    );
                } else {
                    alertTimePickerConfigured(appsPackagesNamesList.get(position), position);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return appsTitlesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout wrap;

        ImageView icon;
        TextView title;

        ViewHolder(final View itemView) {
            super(itemView);

            wrap = (RelativeLayout) itemView.findViewById(R.id.wrap);

            icon = (ImageView) itemView.findViewById(R.id.app_icon);
            title = (TextView) itemView.findViewById(R.id.app_title);
        }
    }

    /*
    ** Show AlertDialog with time picker.
    **/
    private void alertTimePickerNotConfigured(
            final Drawable applicationIcon,
            final String applicationTitle,
            final String applicationName
    ) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_select_time, null, false);

        // the time picker on the alert dialog, this is how to get the value
        final TimePicker myTimePicker = (TimePicker) view
                .findViewById(R.id.myTimePicker);


        myTimePicker.setIs24HourView(true);
        // the alert dialog
        new AlertDialog.Builder(context).setView(view)
                .setTitle(applicationTitle)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @TargetApi(11)
                    public void onClick(DialogInterface dialog, int id) {
                        long applicationMaxTime = myTimePicker.getCurrentHour() * 3600 + myTimePicker.getCurrentMinute() * 60;

                        mDBHelper.updateApplicationTime(applicationName, applicationMaxTime);
                        activity.addNewConfiguredApplication(applicationIcon, applicationTitle, applicationName, applicationMaxTime);

                        maxTime.add(applicationMaxTime);
                        wasteTime.add(0L);
                        packages.add(applicationName);
                        updateAdapter();

                        dialog.cancel();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    /*
** Show AlertDialog with time picker.
**/
    private void alertTimePickerConfigured(
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
        final AlertDialog alertDialog = initializeConfiguredAlertDialog(view, appsTitlesList.get(appsPackagesNamesList.indexOf(applicationName)));

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

                        activity.updateConfiguredApplication(packages.indexOf(applicationName), applicationMaxTime);
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
                            activity.removeConfiguredApplicationByIndex(packages.indexOf(applicationName));


                            removeConfiguredApplicationByIndex(packages.indexOf(applicationName));

                            allowRemoveApplication[0] = false;
                            alertDialog.cancel();
                        }
                    }
                });

            }
        });

        alertDialog.show();
    }

    private AlertDialog initializeConfiguredAlertDialog(View view, String applicationName) {
        return new AlertDialog.Builder(context)
                .setView(view)
                .setTitle(applicationName)
                .setPositiveButton(R.string.ok, null) //Set to null. We override the onclick
                .setNeutralButton(R.string.remove, null)
                .setNegativeButton(R.string.cancel, null)
                .create();
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
///