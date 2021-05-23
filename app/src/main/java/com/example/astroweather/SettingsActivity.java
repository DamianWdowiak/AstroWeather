package com.example.astroweather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsActivity extends AppCompatActivity {
    private EditText editLatitude;
    private EditText editLongitude;
    private Button save;
    private Spinner refreshRate;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch units;
    private RequestQueue queue;
    private EditText cityName;
    private final AtomicBoolean blocked = new AtomicBoolean(false);

    private void bindViews() {
        editLatitude = findViewById(R.id.editLatitude);
        editLongitude = findViewById(R.id.editLongitude);
        save = findViewById(R.id.save);
        refreshRate = findViewById(R.id.refreshRate);
        units = findViewById(R.id.units);
        cityName = findViewById(R.id.cityName);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        bindViews();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.refreshRate, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        refreshRate.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);

        Intent lastIntent = getIntent();
        String latitudeIntent = lastIntent.getStringExtra("latitude");
        String longitudeIntent = lastIntent.getStringExtra("longitude");
        String spinnerIntent = lastIntent.getStringExtra("spinner");
        String cityNameIntent = lastIntent.getStringExtra("cityName");
        boolean unitsIntent = lastIntent.getBooleanExtra("units", false);
        if (latitudeIntent != null) {
            editLatitude.setText(latitudeIntent);
            editLongitude.setText(longitudeIntent);
            cityName.setText(cityNameIntent);
            int spinnerPosition = adapter.getPosition(millisToSeconds(spinnerIntent));
            refreshRate.setSelection(spinnerPosition);
            units.setChecked(unitsIntent);
        }

        save.setOnClickListener(v -> {
            try {
                String latitudeString = editLatitude.getText().toString();
                String longitudeString = editLongitude.getText().toString();
                String cityNameString = cityName.getText().toString();
                int spinnerString = Integer.parseInt(refreshRate.getSelectedItem().toString());
                double latitude = Double.parseDouble(latitudeString);
                double longitude = Double.parseDouble(longitudeString);

                if (validate(latitude, longitude)) {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("cityName", cityNameString);
                    intent.putExtra("spinner", spinnerString);
                    intent.putExtra("units", units.isChecked());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });

        editLatitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!blocked.get()) {
                    findCityByCoord(editLatitude.getText().toString(), editLongitude.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editLongitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!blocked.get()) {
                    findCityByCoord(editLatitude.getText().toString(), editLongitude.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cityName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!blocked.get()) {
                    findCityByName(cityName.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean validate(double latitude, double longitude) {
        return !(latitude < -90) && !(latitude > 90) && !(longitude < -180) && !(longitude > 180);
    }

    private CharSequence millisToMinutes(String millis) {
        long millisLong = Long.parseLong(millis);
        return String.valueOf(millisLong / 60 / 1000);
    }

    private CharSequence millisToSeconds(String millis) {
        long millisLong = Long.parseLong(millis);
        return String.valueOf(millisLong / 1000);
    }

    private void findCityByName(String cityName) {
        String baseURL = "https://api.openweathermap.org/data/2.5/weather?";
        String apiKey = BuildConfig.OPEN_WEATHER_MAP_KEY;
        String url = baseURL + "q=" + cityName + "&appid=" + apiKey;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        blocked.set(true);
                        editLatitude.setText(response.getJSONObject("coord").getString("lat"));
                        editLongitude.setText(response.getJSONObject("coord").getString("lon"));
                        save.setEnabled(true);
                        blocked.set(false);
                    } catch (JSONException ignored) {
                    }
                },
                error -> save.setEnabled(false));

        queue.add(jsonObjectRequest);
    }

    private void findCityByCoord(String lat, String lon) {
        String baseURL = "https://api.openweathermap.org/data/2.5/weather?";
        String apiKey = BuildConfig.OPEN_WEATHER_MAP_KEY;
        String url = baseURL + "lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        blocked.set(true);
                        cityName.setText(response.getString("name"));
                        blocked.set(false);
                    } catch (JSONException ignored) {
                    }
                    save.setEnabled(true);
                },
                error -> save.setEnabled(false));

        queue.add(jsonObjectRequest);
    }
}