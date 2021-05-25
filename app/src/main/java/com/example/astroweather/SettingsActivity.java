package com.example.astroweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsActivity extends AppCompatActivity {
    private static final String FAVOURITES_FILENAME = "favourites_cities";
    private final AtomicBoolean blocked = new AtomicBoolean(false);
    private EditText editLatitude;
    private EditText editLongitude;
    private Button save;
    private Button add;
    private Button delete;
    private Spinner refreshRate;
    private Spinner favourites;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch units;
    private RequestQueue queue;
    private EditText cityName;
    private ArrayList<String> cities;
    private ArrayAdapter<String> arrayAdapter;
    private boolean skipFirstAction = true;

    private void bindViews() {
        editLatitude = findViewById(R.id.editLatitude);
        editLongitude = findViewById(R.id.editLongitude);
        save = findViewById(R.id.save);
        add = findViewById(R.id.add);
        delete = findViewById(R.id.delete);
        refreshRate = findViewById(R.id.refreshRate);
        favourites = findViewById(R.id.favourites);
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

        if (savedInstanceState == null) {
            try {
                loadListFromStorage();
            } catch (Exception e) {
                cities = new ArrayList<>();
            }
        } else {
            cities = savedInstanceState.getStringArrayList("cities");
        }

        arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, cities);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        favourites.setAdapter(arrayAdapter);

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

        int index;
        if ((index = cities.indexOf(cityNameIntent)) != -1) {
            favourites.setSelection(index);
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
                    saveListToStorage();
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });

        favourites.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!skipFirstAction) {
                    cityName.setText(favourites.getItemAtPosition(position).toString());
                } else {
                    skipFirstAction = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        add.setOnClickListener(v -> {
            String city = cityName.getText().toString();
            if (!city.equals("") && !cities.contains(city)) {
                cities.add(city);
                favourites.setAdapter(arrayAdapter);
                favourites.setSelection(cities.size() - 1);
            }
        });

        delete.setOnClickListener(v -> {
            Object city = favourites.getSelectedItem();
            if (city != null) {
                cities.remove(city.toString());
                favourites.setAdapter(arrayAdapter);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("cities", cities);
    }

    private void saveListToStorage() {
        try (FileOutputStream outputStream = this.openFileOutput(FAVOURITES_FILENAME, Context.MODE_PRIVATE)) {
            for (String city : cities) {
                outputStream.write((city + "\n").getBytes());
            }
        } catch (Exception ignored) {
        }

    }

    private void loadListFromStorage() throws Exception {
        cities = new ArrayList<>();
        FileInputStream fis = this.openFileInput(FAVOURITES_FILENAME);
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);

        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = reader.readLine();
        while (line != null) {
            cities.add(line);
            line = reader.readLine();
        }
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
                        add.setEnabled(true);
                        delete.setEnabled(true);
                        blocked.set(false);
                    } catch (JSONException ignored) {
                    }
                },
                error -> {
                    save.setEnabled(false);
                    add.setEnabled(false);
                    delete.setEnabled(false);
                });

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
                        save.setEnabled(true);
                        add.setEnabled(true);
                        delete.setEnabled(true);
                        blocked.set(false);
                    } catch (JSONException ignored) {
                    }
                },
                error -> {
                    save.setEnabled(false);
                    add.setEnabled(false);
                    delete.setEnabled(false);
                });

        queue.add(jsonObjectRequest);
    }
}