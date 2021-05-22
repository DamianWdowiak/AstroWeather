package com.example.astroweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class CurrWeatherFragment extends Fragment {
    private static final String SAVED_JSON_FILENAME = "curr_weather_info.json";
    private static final String WEATHER_TIMESTAMP_FILENAME = "curr_weather_timestamp";
    private static final long ONE_HOUR = 1_000 * 60 * 60;
    private static final long ONE_MINUTE = 1_000 * 60;
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
            String unit = "metric".equals("metric") ? " \u2103" : " \u2109";
            if (isInternetAvailable()) {
                if (isDataUpdated()) {
                    try {
                        JSONObject savedData = loadJSONFromStorage();
                        updateFragmentFields(savedData, unit);
                    } catch (JSONException | IOException e) {
                        requestCurrWeatherData("lodz", "metric");
                    }
                } else {
                    requestCurrWeatherData("lodz", "metric");
                }
            } else {
                Toast.makeText(getActivity(), "Data may be outdated to refresh information connect to the Internet", Toast.LENGTH_LONG).show();

                try {
                    JSONObject savedData = loadJSONFromStorage();
                    updateFragmentFields(savedData, unit);
                } catch (JSONException | IOException e) {
                    Toast.makeText(getActivity(), "App needs internet connection", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(this, ONE_MINUTE);
                }
            }

            handler.postDelayed(this, REFRESH_RATE);
        }
    };
    private SharedViewModel viewModel;

    public CurrWeatherFragment() {
        // Required empty public constructor
    }

    private void saveTimestamp() {
        try (FileOutputStream outputStream = getContext().openFileOutput(WEATHER_TIMESTAMP_FILENAME, Context.MODE_PRIVATE)) {
            outputStream.write(LocalDateTime.now().toString().getBytes());
        } catch (Exception ignored) {
        }
    }

    private LocalDateTime loadTimestamp() throws IOException {
        FileInputStream fis = getContext().openFileInput(WEATHER_TIMESTAMP_FILENAME);
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = reader.readLine();
        while (line != null) {
            stringBuilder.append(line);
            line = reader.readLine();
        }

        return LocalDateTime.parse(stringBuilder.toString());
    }

    private boolean isDataUpdated() {
        try {
            LocalDateTime timestamp = loadTimestamp();
            if (LocalDateTime.now().isBefore(timestamp.plusHours(1))) {
                return true;
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private JSONObject loadJSONFromStorage() throws IOException, JSONException {
        FileInputStream fis = getContext().openFileInput(SAVED_JSON_FILENAME);
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = reader.readLine();
        while (line != null) {
            stringBuilder.append(line).append('\n');
            line = reader.readLine();
        }

        return new JSONObject(stringBuilder.toString());
    }

    private void saveJSONToStorage(JSONObject object) {
        try (FileOutputStream outputStream = getContext().openFileOutput(SAVED_JSON_FILENAME, Context.MODE_PRIVATE)) {
            outputStream.write(object.toString().getBytes());
        } catch (Exception ignored) {
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void requestCurrWeatherData(String location, String units) {
        String baseURL = "https://api.openweathermap.org/data/2.5/weather?";
        String apiKey = BuildConfig.OPEN_WEATHER_MAP_KEY;
        String url = baseURL + "q=" + location + "&units=" + units + "&appid=" + apiKey;
        String unit = units.equals("metric") ? " \u2103" : " \u2109";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        updateFragmentFields(response, unit);
                        saveJSONToStorage(response);
                        saveTimestamp();
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
        visibility.setText(response.getInt("visibility") / 1_000.0 + " km");
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