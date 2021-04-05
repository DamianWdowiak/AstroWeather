package com.example.astroweather;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;

import java.time.ZonedDateTime;

public class SunFragment extends Fragment {
    private AstroCalculator astroCalculator;

    private TextView dawn;
    private TextView sunrise;
    private TextView sunriseAzimuth;
    private TextView sunset;
    private TextView sunsetAzimuth;
    private TextView dusk;

    public SunFragment() {
        // Required empty public constructor
    }

    private void bindViews(View rootView) {
        dawn = rootView.findViewById(R.id.dawn);
        sunrise = rootView.findViewById(R.id.sunrise);
        sunriseAzimuth = rootView.findViewById(R.id.sunriseAzimuth);
        sunset = rootView.findViewById(R.id.sunset);
        sunsetAzimuth = rootView.findViewById(R.id.sunsetAzimuth);
        dusk = rootView.findViewById(R.id.dusk);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(getView());

        ZonedDateTime date = ZonedDateTime.now();
        astroCalculator = new AstroCalculator(new AstroDateTime(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                date.getHour(),
                date.getMinute(),
                date.getSecond(),
                date.getOffset().getTotalSeconds() % (60 * 60),
                false), new AstroCalculator.Location(51.759048, 19.458599));

        sunrise.setText(String.format("%d:%02d",
                astroCalculator.getSunInfo().getSunrise().getHour(),
                astroCalculator.getSunInfo().getSunrise().getMinute()));
        sunriseAzimuth.setText(String.format("%.02f", astroCalculator.getSunInfo().getAzimuthRise()));
        sunset.setText(String.format("%d:%02d",
                astroCalculator.getSunInfo().getSunset().getHour(),
                astroCalculator.getSunInfo().getSunset().getMinute()));
        sunsetAzimuth.setText(String.format("%.02f", astroCalculator.getSunInfo().getAzimuthSet()));
        dawn.setText(String.format("%d:%02d",
                astroCalculator.getSunInfo().getTwilightMorning().getHour(),
                astroCalculator.getSunInfo().getTwilightMorning().getMinute()));
        dusk.setText(String.format("%d:%02d",
                astroCalculator.getSunInfo().getTwilightEvening().getHour(),
                astroCalculator.getSunInfo().getTwilightEvening().getMinute()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sun, container, false);
    }
}