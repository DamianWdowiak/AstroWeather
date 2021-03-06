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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class LongTermForecastFragment extends Fragment {
    public static final String METRIC = "metric";
    public static final String IMPERIAL = "imperial";
    public static final String DEFAULT_CITY_NAME = "lodz";
    private static final String SAVED_JSON_FILENAME = "forecast_weather_info.json";
    private static final long ONE_HOUR = 1_000 * 60 * 60;
    private static final long ONE_MINUTE = 1_000 * 60;
    private static final long REFRESH_RATE = ONE_HOUR;
    private static final Handler handler = new Handler();
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dayOfTheWeekFormatter = new SimpleDateFormat("EE");
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat textToDateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private RequestQueue queue;
    private TextView locationName;
    private TextView date1;
    private TextView temperature1;
    private TextView date2;
    private TextView temperature2;
    private TextView date3;
    private TextView temperature3;
    private TextView date4;
    private TextView temperature4;
    private TextView date5;
    private TextView temperature5;
    private ImageView icon1;
    private ImageView icon2;
    private ImageView icon3;
    private ImageView icon4;
    private ImageView icon5;
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
                    requestForecastData(cityName, units);
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

    public LongTermForecastFragment() {
        // Required empty public constructor
    }

    private void fillViewWithSavedData() throws Exception {
        JSONObject savedData = loadJSONFromStorage();
        String units = savedData.getBoolean("is_imperial") ? IMPERIAL : METRIC;
        updateFragmentFields(savedData, units);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    private void requestForecastData(String location, String units) {
        String baseURL = "https://api.openweathermap.org/data/2.5/forecast?";
        String apiKey = BuildConfig.OPEN_WEATHER_MAP_KEY;
        String url = baseURL + "q=" + location + "&units=" + units + "&appid=" + apiKey;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        updateFragmentFields(response, units);
                        saveJSONToStorage(response);
                    } catch (JSONException | ParseException e) {
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

    private void isDataUpdated(JSONObject jsonObject) throws Exception {
        LocalDateTime timestamp = LocalDateTime.parse(jsonObject.getString("timestamp"));
        if (timestamp.plusHours(1).isBefore(LocalDateTime.now())) {
            throw new Exception("Data outdated");
        }

        if (viewModel.getCityName().getValue() != null &&
                (!jsonObject.getJSONObject("city").getString("name").equals(viewModel.getCityName().getValue()) ||
                        jsonObject.getBoolean("is_imperial") != viewModel.getIsImperial().getValue())) {
            throw new Exception("Data outdated");
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

    private void saveJSONToStorage(JSONObject object) {
        try (FileOutputStream outputStream = getContext().openFileOutput(SAVED_JSON_FILENAME, Context.MODE_PRIVATE)) {
            object.put("is_imperial", viewModel.getIsImperial().getValue());
            object.put("timestamp", LocalDateTime.now().toString());
            outputStream.write(object.toString().getBytes());
        } catch (Exception ignored) {
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateFragmentFields(JSONObject response, String units) throws JSONException, ParseException {
        String unit = units.equals("metric") ? " \u2103" : " \u2109";

        temperature1.setText(getTemperature(response, 0, "temp") + unit);
        temperature2.setText(getTemperature(response, 8, "temp") + unit);
        temperature3.setText(getTemperature(response, 16, "temp") + unit);
        temperature4.setText(getTemperature(response, 24, "temp") + unit);
        temperature5.setText(getTemperature(response, 32, "temp") + unit);

        date1.setText(dayOfTheWeekFormatter.format(textToDateFormatter.parse(getStringDate(response, 0))));
        date2.setText(dayOfTheWeekFormatter.format(textToDateFormatter.parse(getStringDate(response, 8))));
        date3.setText(dayOfTheWeekFormatter.format(textToDateFormatter.parse(getStringDate(response, 16))));
        date4.setText(dayOfTheWeekFormatter.format(textToDateFormatter.parse(getStringDate(response, 24))));
        date5.setText(dayOfTheWeekFormatter.format(textToDateFormatter.parse(getStringDate(response, 32))));

        if(locationName != null) {
            locationName.setText(response.getJSONObject("city").getString("country") +
                    ", " + response.getJSONObject("city").getString("name"));
        }

        setWeatherIcons(response);
    }

    private void setWeatherIcons(JSONObject response) throws JSONException {
        String iconCode1 = getIconCode(response, 0);
        String iconCode2 = getIconCode(response, 8);
        String iconCode3 = getIconCode(response, 16);
        String iconCode4 = getIconCode(response, 24);
        String iconCode5 = getIconCode(response, 32);

        String imgUrl = "https://openweathermap.org/img/wn/" + iconCode1 + "@4x.png";
        ImageRequest imageRequest1 = new ImageRequest(imgUrl, imageResponse -> icon1.setImageBitmap(imageResponse),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                error -> Toast.makeText(getActivity(), "Couldn't obtain icon from server!", Toast.LENGTH_SHORT).show());

        imgUrl = "https://openweathermap.org/img/wn/" + iconCode2 + "@4x.png";
        ImageRequest imageRequest2 = new ImageRequest(imgUrl, imageResponse -> icon2.setImageBitmap(imageResponse),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                error -> Toast.makeText(getActivity(), "Couldn't obtain icon from server!", Toast.LENGTH_SHORT).show());

        imgUrl = "https://openweathermap.org/img/wn/" + iconCode3 + "@4x.png";
        ImageRequest imageRequest3 = new ImageRequest(imgUrl, imageResponse -> icon3.setImageBitmap(imageResponse),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                error -> Toast.makeText(getActivity(), "Couldn't obtain icon from server!", Toast.LENGTH_SHORT).show());

        imgUrl = "https://openweathermap.org/img/wn/" + iconCode4 + "@4x.png";
        ImageRequest imageRequest4 = new ImageRequest(imgUrl, imageResponse -> icon4.setImageBitmap(imageResponse),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                error -> Toast.makeText(getActivity(), "Couldn't obtain icon from server!", Toast.LENGTH_SHORT).show());

        imgUrl = "https://openweathermap.org/img/wn/" + iconCode5 + "@4x.png";
        ImageRequest imageRequest5 = new ImageRequest(imgUrl, imageResponse -> icon5.setImageBitmap(imageResponse),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                error -> Toast.makeText(getActivity(), "Couldn't obtain icon from server!", Toast.LENGTH_SHORT).show());

        queue.add(imageRequest1);
        queue.add(imageRequest2);
        queue.add(imageRequest3);
        queue.add(imageRequest4);
        queue.add(imageRequest5);
    }

    private String getStringDate(JSONObject response, int i) throws JSONException {
        return response.getJSONArray("list").getJSONObject(i).getString("dt_txt");
    }

    private String getIconCode(JSONObject response, int i) throws JSONException {
        return response.getJSONArray("list").getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon");
    }

    private double getTemperature(JSONObject response, int i, String mode) throws JSONException {
        return response.getJSONArray("list").getJSONObject(i).getJSONObject("main").getDouble(mode);
    }

    private void bindViews(View rootView) {
        icon2 = rootView.findViewById(R.id.icon2);
        icon3 = rootView.findViewById(R.id.icon3);
        icon4 = rootView.findViewById(R.id.icon4);
        icon5 = rootView.findViewById(R.id.icon5);
        locationName = rootView.findViewById(R.id.locationName);
        date1 = rootView.findViewById(R.id.date1);
        icon1 = rootView.findViewById(R.id.icon1);
        temperature1 = rootView.findViewById(R.id.temperature1);
        date2 = rootView.findViewById(R.id.date2);
        temperature2 = rootView.findViewById(R.id.temperature2);
        date3 = rootView.findViewById(R.id.date3);
        temperature3 = rootView.findViewById(R.id.temperature3);
        date4 = rootView.findViewById(R.id.date4);
        temperature4 = rootView.findViewById(R.id.temperature4);
        date5 = rootView.findViewById(R.id.date5);
        temperature5 = rootView.findViewById(R.id.temperature5);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        queue = Volley.newRequestQueue(requireActivity());
        SharedViewModelFactory sharedViewModelFactory = new SharedViewModelFactory();
        viewModel = new ViewModelProvider(requireActivity(), sharedViewModelFactory).get(SharedViewModel.class);
        viewModel.getRefreshNow().observe(this, (value) -> {
            String units = viewModel.getIsImperial().getValue() ? "imperial" : "metric";
            String cityName = viewModel.getCityName().getValue();
            requestForecastData(cityName, units);
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
        return inflater.inflate(R.layout.fragment_long_term_forecast, container, false);
    }
}