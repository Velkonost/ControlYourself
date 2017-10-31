package com.velkonost.controlyourself;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gigamole.navigationtabstrip.NavigationTabStrip;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private NavigationTabStrip mNavigationTab;

    private ConfiguredApplicationsAdapter mConfiguredApplicationsAdapter;
    private InstalledApplicationsAdapter mInstalledApplicationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<Drawable> appsIconsList = new ArrayList<>();
        final ArrayList<String> appsTitlesList = new ArrayList<>();
        final ArrayList<String> appsPackagesNamesList = new ArrayList<>();

        setCheckLaunches();
        getInstalledApplicationsInfo(appsIconsList, appsTitlesList, appsPackagesNamesList);

        mViewPager = (ViewPager) findViewById(R.id.vp);
        mNavigationTab = (NavigationTabStrip) findViewById(R.id.nts_bottom);

        mViewPager.setOffscreenPageLimit(2);

        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                final View view = LayoutInflater.from(
                        getBaseContext()).inflate(R.layout.item_view_pager, null, false);

                final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv);

                if (position == 0) {
                    setRecyclerViewConfigurationGrid(recyclerView);
                    mInstalledApplicationsAdapter = new InstalledApplicationsAdapter(MainActivity.this, MainActivity.this,
                            appsIconsList, appsTitlesList, appsPackagesNamesList);

                    recyclerView.setAdapter(mInstalledApplicationsAdapter);

                } else if (position == 1) {
                    setRecyclerViewConfigurationList(recyclerView);
                    mConfiguredApplicationsAdapter = new ConfiguredApplicationsAdapter(MainActivity.this, MainActivity.this,
                            appsIconsList, appsTitlesList, appsPackagesNamesList);

                    recyclerView.setAdapter(mConfiguredApplicationsAdapter);
                }

                container.addView(view);
                return view;
            }
        });

        setNavigationTabConfiguration();

//        TextView tv =(TextView)findViewById(R.id.db);
//
//        tv.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                Intent dbmanager = new Intent(MainActivity.this,AndroidDatabaseManager.class);
//                startActivity(dbmanager);
//            }
//        });


    }


    private void setNavigationTabConfiguration() {
        mNavigationTab.setTabIndex(1, true);
        mNavigationTab.setTitles(getString(R.string.installed_apps_tab), getString(R.string.customized_apps_tab));
        mNavigationTab.setViewPager(mViewPager, 0);
    }

    public void addNewConfiguredApplication(
            Drawable applicationIcon,
            String applicationTitle,
            String applicationName,
            long maxTime
    ) {
       mConfiguredApplicationsAdapter.addNewApplication(applicationIcon, applicationTitle, applicationName, maxTime);
    }


    /**
     * Установка настроек RecyclerView
     */
    private void setRecyclerViewConfigurationGrid(RecyclerView recyclerView) {
        final GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 3);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.HORIZONTAL));
    }


    private void setRecyclerViewConfigurationList(RecyclerView recyclerView) {

        final GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.HORIZONTAL));

    }

    private void getInstalledApplicationsInfo(
            ArrayList<Drawable> appsIconsList,
            ArrayList<String> appsTitlesList,
            ArrayList<String> appsPackagesNamesList
    ) {

        final PackageManager pm = getPackageManager();

        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(packageInfo.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            appsIconsList.add(icon);
            appsTitlesList.add(pm.getApplicationLabel(packageInfo).toString());
            appsPackagesNamesList.add(packageInfo.packageName);
        }

        Log.i("KEKE", packages.toString());
    }


    private void setCheckLaunches() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentNotification = new Intent(this, BackgroundAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, pendingIntent); // Millisec * Second * Minute
//        am.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
    }

    @Override
    protected void onDestroy() {
        setCheckLaunches();
        super.onDestroy();
    }

    public void updateConfiguredApplication(int positionIndex, long applicationMaxTime) {
        mConfiguredApplicationsAdapter.updateConfiguredApplication(positionIndex, applicationMaxTime);
    }

    public void removeConfiguredApplicationByIndex(int positionIndex) {
        mConfiguredApplicationsAdapter.removeConfiguredApplicationByIndex(positionIndex);
        mInstalledApplicationsAdapter.removeConfiguredApplicationByIndex(positionIndex);
    }
}
