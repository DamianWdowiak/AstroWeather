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

public class MoonFragment extends Fragment {
    private AstroCalculator astroCalculator;

    private TextView moonrise;
    private TextView moonset;
    private TextView newMoon;
    private TextView fullMoon;
    private TextView illumination;
    private TextView lunarDay;

    public MoonFragment() {
        // Required empty public constructor
    }

    private void bindViews(View rootView) {
        moonrise = rootView.findViewById(R.id.moonrise);
        moonset = rootView.findViewById(R.id.moonset);
        newMoon = rootView.findViewById(R.id.newMoon);
        fullMoon = rootView.findViewById(R.id.fullMoon);
        illumination = rootView.findViewById(R.id.illumination);
        lunarDay = rootView.findViewById(R.id.lunarDay);
    }

    @SuppressLint("DefaultLocale")
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

        moonrise.setText(String.format("%d:%02d",
                astroCalculator.getMoonInfo().getMoonrise().getHour(),
                astroCalculator.getMoonInfo().getMoonrise().getMinute()));
        moonset.setText(String.format("%d:%02d",
                astroCalculator.getMoonInfo().getMoonset().getHour(),
                astroCalculator.getMoonInfo().getMoonset().getMinute()));
        newMoon.setText(String.format("%02d-%02d-%04d",
                astroCalculator.getMoonInfo().getNextNewMoon().getDay(),
                astroCalculator.getMoonInfo().getNextNewMoon().getMonth(),
                astroCalculator.getMoonInfo().getNextNewMoon().getYear()));
        fullMoon.setText(String.format("%02d-%02d-%04d",
                astroCalculator.getMoonInfo().getNextFullMoon().getDay(),
                astroCalculator.getMoonInfo().getNextFullMoon().getMonth(),
                astroCalculator.getMoonInfo().getNextFullMoon().getYear()));
        illumination.setText(String.format("%.02f%%", astroCalculator.getMoonInfo().getIllumination()));
        lunarDay.setText(String.format("%f", astroCalculator.getMoonInfo().getAge()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moon, container, false);
    }
}