package com.example.astroweather;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends FragmentActivity {
    private static final int NUM_PAGES = 2;
    private static final int DELAY_ONE_SECOND = 1000;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    private final Handler handler = new Handler();
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TextView clock;
    private final Runnable blink = new Runnable() {
        @Override
        public void run() {
            Date date = Calendar.getInstance().getTime();
            clock.setText(df.format(date.getTime()));
            handler.postDelayed(this, DELAY_ONE_SECOND);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        clock = findViewById(R.id.clock);
        Date date = Calendar.getInstance().getTime();
        clock.setText(df.format(date.getTime()));

        handler.post(blink);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(blink);
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