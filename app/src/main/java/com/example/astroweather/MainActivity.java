package com.example.astroweather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


public class MainActivity extends FragmentActivity {
    private static final int NUM_PAGES = 2;
    private static final int NO_DATA_FROM_INTENT = -200;
    private static final long DELAY_15_SECONDS = 1000 * 15;
    private static final double DEFAULT_LATITUDE = 51.7833;
    private static final double DEFAULT_LONGITUDE = 19.4667;

    @SuppressLint("SimpleDateFormat")
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TextView latitude;
    private TextView longitude;
    private SharedViewModel viewModel;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);

        SharedViewModelFactory sharedViewModelFactory = new SharedViewModelFactory();
        viewModel = new ViewModelProvider(this, sharedViewModelFactory).get(SharedViewModel.class);

        if (viewModel.getRefreshRate().getValue() == null) {
            viewModel.setRefreshRate(DELAY_15_SECONDS);
            viewModel.setLatitude(DEFAULT_LATITUDE);
            viewModel.setLongitude(DEFAULT_LONGITUDE);
        }

        Intent intent = getIntent();
        double latitudeIntent = intent.getDoubleExtra("latitude", NO_DATA_FROM_INTENT);
        double longitudeIntent = intent.getDoubleExtra("longitude", NO_DATA_FROM_INTENT);
        int spinnerIntent = intent.getIntExtra("spinner", NO_DATA_FROM_INTENT);
        if (latitudeIntent != NO_DATA_FROM_INTENT) {
            viewModel.setLatitude(latitudeIntent);
            viewModel.setLongitude(longitudeIntent);
            viewModel.setRefreshRate(secondsToMillis(spinnerIntent));
        }

        latitude.setText(String.format("%.5f", viewModel.getLatitude().getValue()));
        longitude.setText(String.format("%.5f", viewModel.getLongitude().getValue()));

        viewPager = findViewById(R.id.view_pager);
        if (viewPager != null) {
            pagerAdapter = new ScreenSlidePagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setPageTransformer(new ZoomOutPageTransformer());
        }
    }

    private long minutesToMillis(int minutes) {
        return minutes * 1000L * 60L;
    }

    private long secondsToMillis(int seconds) {
        return seconds * 1000L;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("latitude", viewModel.getLatitude().getValue().toString());
            intent.putExtra("longitude", viewModel.getLongitude().getValue().toString());
            intent.putExtra("spinner", viewModel.getRefreshRate().getValue().toString());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new SunFragment();
            } else {
                return new MoonFragment();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}