package com.example.astroweather;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class CurrWeatherFragment extends Fragment {
    private static final long ONE_HOUR = 1_000 * 60 * 60;
    private static final long REFRESH_RATE = ONE_HOUR;
    private static final Handler handler = new Handler();
    private RequestQueue queue;
    private TextView visibility;
    private TextView temperature;
    private TextView humidity;
    private TextView windSpeed;
    private TextView pressure;
    private TextView description;
    private TextView locationName;
    private ImageView icon;
    public final Runnable updateData = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            requestCurrWeatherData("lodz", "metric");

            handler.postDelayed(this, REFRESH_RATE);
        }
    };
    private SharedViewModel viewModel;

    public CurrWeatherFragment() {
        // Required empty public constructor
    }

    private void requestCurrWeatherData(String location, String units) {
        String baseURL = "https://api.openweathermap.org/data/2.5/weather?";
        String apiKey = "";
        String url = baseURL + "q=" + location + "&units=" + units + "&appid=" + apiKey;
        String unit = units.equals("metric") ? " \u2103" : " \u2109";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        updateFragmentFields(response, unit);
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Response Error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getActivity(), "Couldn't obtain data from server!", Toast.LENGTH_SHORT).show());

        queue.add(jsonObjectRequest);
    }

    @SuppressLint("SetTextI18n")
    private void updateFragmentFields(JSONObject response, String unit) throws JSONException {
        description.setText(response.getJSONArray("weather").getJSONObject(0).getString("description"));
        temperature.setText(response.getJSONObject("main").getDouble("temp") + unit);
        humidity.setText(response.getJSONObject("main").getInt("humidity") + " %");
        pressure.setText(response.getJSONObject("main").getInt("pressure") + " hPa");
        visibility.setText(response.getInt("visibility") / 1_000 + " km");
        windSpeed.setText(response.getJSONObject("wind").getDouble("speed") + " km/h");
        locationName.setText(response.getJSONObject("sys").getString("country") + ", " + response.getString("name"));

        String iconCode = response.getJSONArray("weather").getJSONObject(0).getString("icon");

        String imgUrl = "https://openweathermap.org/img/wn/" + iconCode + "@4x.png";
        ImageRequest imageRequest = new ImageRequest(imgUrl, imageResponse -> icon.setImageBitmap(imageResponse),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                error -> Toast.makeText(getActivity(), "Couldn't obtain icon from server!", Toast.LENGTH_SHORT).show());

        queue.add(imageRequest);
    }

    private void bindViews(View rootView) {
        visibility = rootView.findViewById(R.id.visibility);
        temperature = rootView.findViewById(R.id.temperature);
        humidity = rootView.findViewById(R.id.humidity);
        windSpeed = rootView.findViewById(R.id.windSpeed);
        pressure = rootView.findViewById(R.id.pressure);
        description = rootView.findViewById(R.id.description);
        locationName = rootView.findViewById(R.id.locationName);
        icon = rootView.findViewById(R.id.icon);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        queue = Volley.newRequestQueue(requireActivity());
        SharedViewModelFactory sharedViewModelFactory = new SharedViewModelFactory();
        viewModel = new ViewModelProvider(requireActivity(), sharedViewModelFactory).get(SharedViewModel.class);

        handler.post(updateData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curr_weather, container, false);
    }
}