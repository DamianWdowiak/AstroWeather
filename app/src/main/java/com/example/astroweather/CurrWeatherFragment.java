package com.example.astroweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class CurrWeatherFragment extends Fragment {
    public static final String DEFAULT_CITY_NAME = "lodz";
    public static final String METRIC = "metric";
    public static final String IMPERIAL = "imperial";
    private static final String SAVED_JSON_FILENAME = "curr_weather_info.json";
    private static final long ONE_HOUR = 1_000 * 60 * 60;
    private static final long ONE_MINUTE = 1_000 * 60;
    private static final long REFRESH_RATE = ONE_HOUR + ONE_MINUTE;
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
    private SharedViewModel viewModel;
    public final Runnable updateData = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            String units;
            String cityName;
            if (viewModel.getIsImperial().getValue() == null) {
                units = METRIC;
            } else {
                units = viewModel.getIsImperial().getValue() ? IMPERIAL : METRIC;
            }
            if (viewModel.getCityName().getValue() == null) {
                cityName = DEFAULT_CITY_NAME;
            } else {
                cityName = viewModel.getCityName().getValue();
            }


            if (isInternetAvailable()) {
                try {
                    fillViewWithSavedData();
                } catch (Exception e) {
                    requestCurrWeatherData(cityName, units);
                }
            } else {
                Toast.makeText(getActivity(), "Data may be outdated to refresh information connect to the Internet", Toast.LENGTH_LONG).show();

                try {
                    fillViewWithSavedData();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "App needs internet connection", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(this, ONE_MINUTE);
                }
            }

            handler.postDelayed(this, REFRESH_RATE);
        }
    };

    public CurrWeatherFragment() {
        // Required empty public constructor
    }

    private void fillViewWithSavedData() throws Exception {
        JSONObject savedData = loadJSONFromStorage();
        String units = savedData.getBoolean("is_imperial") ? IMPERIAL : METRIC;
        updateFragmentFields(savedData, units);
        updateViewModel(savedData, null);
    }

    private void updateViewModel(JSONObject savedData, String units) throws JSONException {
        viewModel.setLatitude(savedData.getJSONObject("coord").getDouble("lat"));
        viewModel.setLongitude(savedData.getJSONObject("coord").getDouble("lon"));
        if(units == null) {
            viewModel.setCityName(savedData.getString("name"));
            viewModel.setIsImperial(savedData.getBoolean("is_imperial"));
            viewModel.setRefreshRate(savedData.getLong("refresh_rate"));
        }
        else {
            viewModel.setIsImperial(units.equals(IMPERIAL));
            if(viewModel.getCityName().getValue() == null){
                viewModel.setCityName(savedData.getString("name"));
            }
        }
    }

    private JSONObject loadJSONFromStorage() throws Exception {
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

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());
        isDataUpdated(jsonObject);

        return jsonObject;
    }

    private void isDataUpdated(JSONObject jsonObject) throws Exception {
        LocalDateTime timestamp = LocalDateTime.parse(jsonObject.getString("timestamp"));
        if (timestamp.plusHours(1).isBefore(LocalDateTime.now())) {
            throw new Exception("Data outdated");
        }

        if (viewModel.getCityName().getValue() != null &&
                (!jsonObject.getString("name").equals(viewModel.getCityName().getValue()) ||
                        jsonObject.getBoolean("is_imperial") != viewModel.getIsImperial().getValue())) {
            throw new Exception("Data outdated");
        }
    }

    private void saveJSONToStorage(JSONObject object) {
        try (FileOutputStream outputStream = getContext().openFileOutput(SAVED_JSON_FILENAME, Context.MODE_PRIVATE)) {
            object.put("is_imperial", viewModel.getIsImperial().getValue());
            object.put("refresh_rate", viewModel.getRefreshRate().getValue());
            object.put("timestamp", LocalDateTime.now().toString());
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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        updateFragmentFields(response, units);
                        saveJSONToStorage(response);
                        updateViewModel(response, units);
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Response Error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getActivity(), "Couldn't obtain data from server!", Toast.LENGTH_SHORT).show();
                    try {
                        fillViewWithSavedData();
                    } catch (Exception ignored) {
                    }
                });

        queue.add(jsonObjectRequest);
    }

    @SuppressLint("SetTextI18n")
    private void updateFragmentFields(JSONObject response, String units) throws JSONException {
        String unit = units.equals(METRIC) ? " \u2103" : " \u2109";

        description.setText(response.getJSONArray("weather").getJSONObject(0).getString("description"));
        temperature.setText(response.getJSONObject("main").getDouble("temp") + unit);
        humidity.setText(response.getJSONObject("main").getInt("humidity") + " %");
        pressure.setText(response.getJSONObject("main").getInt("pressure") + " hPa");
        visibility.setText(response.getInt("visibility") / 1_000.0 + " km");
        windSpeed.setText(response.getJSONObject("wind").getDouble("speed") + " km/h");

        if(locationName != null) {
            locationName.setText(response.getJSONObject("sys").getString("country") + ", " + response.getString("name"));
        }

        if(icon != null) {
            String iconCode = response.getJSONArray("weather").getJSONObject(0).getString("icon");

            String imgUrl = "https://openweathermap.org/img/wn/" + iconCode + "@4x.png";
            ImageRequest imageRequest = new ImageRequest(imgUrl, imageResponse -> icon.setImageBitmap(imageResponse),
                    0, 0, ImageView.ScaleType.CENTER_CROP, null,
                    error -> Toast.makeText(getActivity(), "Couldn't obtain icon from server!", Toast.LENGTH_SHORT).show());

            queue.add(imageRequest);
        }
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
        viewModel.getRefreshNow().observe(this, (value) -> {
            String units = viewModel.getIsImperial().getValue() ? IMPERIAL : METRIC;
            String cityName = viewModel.getCityName().getValue();
            requestCurrWeatherData(cityName, units);
        });

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