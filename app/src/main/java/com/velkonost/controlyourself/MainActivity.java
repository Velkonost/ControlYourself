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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private NavigationTabStrip mNavigationTab;

    private ConfiguredApplicationsAdapter mConfiguredApplicationsAdapter;
    private InstalledApplicationsAdapter mInstalledApplicationsAdapter;

    private MaterialSearchView searchView;
    private Toolbar toolbar;

    private String searchStr = "";

    private ArrayList<Drawable> appsIconsList;
    private ArrayList<String> appsTitlesList;
    private ArrayList<String> appsPackagesNamesList;

    private ArrayList<Drawable> filteredAppsIconsList;
    private ArrayList<String> filteredAppsTitlesList;
    private ArrayList<String> filteredAppsPackagesNamesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTypeface();

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        initSearch(searchView);


        initToolbar(MainActivity.this, toolbar); /** Инициализация */

        appsIconsList = new ArrayList<>();
        appsTitlesList = new ArrayList<>();
        appsPackagesNamesList = new ArrayList<>();

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

    }

    private void setTypeface() {
        FontsOverride.setDefaultFont(this, "DEFAULT");
        FontsOverride.setDefaultFont(this, "MONOSPACE");
        FontsOverride.setDefaultFont(this, "SERIF");
        FontsOverride.setDefaultFont(this, "SANS_SERIF");
    }

    private void initToolbar(AppCompatActivity activity, Toolbar toolbar) {

        toolbar.setTitle(" ");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return false;
            }
        });

        toolbar.inflateMenu(R.menu.search_menu);

        activity.setSupportActionBar(toolbar);
    }

    private void initSearch(final MaterialSearchView searchView) {
        searchView.setCursorDrawable(R.drawable.search_cursor);
        searchView.setEllipsize(true);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                searchStr = newText;

                filteredAppsIconsList = new ArrayList<>();
                filteredAppsPackagesNamesList = new ArrayList<>();
                filteredAppsTitlesList = new ArrayList<>();

                for (int i = 0; i < appsPackagesNamesList.size(); i++) {
                    if (appsTitlesList.get(i).toLowerCase().contains(searchStr.toLowerCase())
//                    || appsPackagesNamesList.get(i).toLowerCase().contains(searchStr.toLowerCase())
                            ) {
                        filteredAppsPackagesNamesList.add(appsPackagesNamesList.get(i));
                        filteredAppsTitlesList.add(appsTitlesList.get(i));
                        filteredAppsIconsList.add(appsIconsList.get(i));
                    }
                }

                mInstalledApplicationsAdapter.filterApplications(
                        filteredAppsIconsList,
                        filteredAppsTitlesList,
                        filteredAppsPackagesNamesList
                        );

                mConfiguredApplicationsAdapter.filterApplications(
                        filteredAppsIconsList,
                        filteredAppsTitlesList,
                        filteredAppsPackagesNamesList
                        );

                return true;
            }
        });
    }

    /**
     * При нажатии на кнопку "Назад" поиск закрывется.
     */
    @Override
    public void onBackPressed() {
       if (searchView.isSearchOpen())
            searchView.closeSearch();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        /**
         * Устанавливает меню для строки поиска.
         */
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        /**
         * Вешает слушателя для открытия строки по нажатию.
         */
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown () {
                searchView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onSearchViewClosed() {}
        });
        return true;
    }

    private void setNavigationTabConfiguration() {
        mNavigationTab.setTabIndex(1, true);
        mNavigationTab.setTitles(getString(R.string.installed_apps_tab), getString(R.string.customized_apps_tab));
        mNavigationTab.setViewPager(mViewPager, 0);
        mNavigationTab.setTitleSize(32);
//        mNavigationTab.setTypeface(
//                Typeface.createFromAsset(
//                        getAssets(),
//                        String.format(Locale.US, "fonts/%s", "helvetica_neue_ultralight.ttf")
//                )
//        );
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

            if (
                    !packageInfo.packageName.contains("htc")
                            && !packageInfo.packageName.contains("com.android")
                            && !packageInfo.packageName.contains("com.mediatek")
                            && !packageInfo.packageName.contains("com.google.android")
                            && !packageInfo.packageName.contains("com.longcheer")
                            && !packageInfo.packageName.contains("com.futuredial")
                            && !packageInfo.packageName.contains("com.fw")
                            && !packageInfo.packageName.contains("velkonost")
                            && !packageInfo.packageName.contains("com.shenqi")
                            && !packageInfo.packageName.contains("com.zui")
                            && !packageInfo.packageName.contains("com.qti")
                            && !packageInfo.packageName.contains("com.qualcomm")
                            && !packageInfo.packageName.contains("com.lenovo")
                            && !packageInfo.packageName.equals("android")
                            && !packageInfo.packageName.contains("itschool")
                    ) {
                appsIconsList.add(icon);
                appsTitlesList.add(pm.getApplicationLabel(packageInfo).toString());
                appsPackagesNamesList.add(packageInfo.packageName);
            }
        }
    }


    private void setCheckLaunches() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentNotification = new Intent(this, BackgroundAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 3000, pendingIntent);
    }

    @Override
    protected void onDestroy() {
        setCheckLaunches();
        super.onDestroy();
    }

    public void updateConfiguredApplication(int positionIndex, long applicationMaxTime) {
        mConfiguredApplicationsAdapter.updateConfiguredApplication(positionIndex, applicationMaxTime);
        mInstalledApplicationsAdapter.updateConfiguredApplication(positionIndex, applicationMaxTime);
    }

    public void removeConfiguredApplicationByIndex(int positionIndex) {
        mConfiguredApplicationsAdapter.removeConfiguredApplicationByIndex(positionIndex);
        mInstalledApplicationsAdapter.removeConfiguredApplicationByIndex(positionIndex);
    }
}
