package com.example.astroweather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private EditText editLatitude;
    private EditText editLongitude;
    private Button save;
    private Spinner refreshRate;

    private void bindViews() {
        editLatitude = (EditText) findViewById(R.id.editLatitude);
        editLongitude = (EditText) findViewById(R.id.editLongitude);
        save = (Button) findViewById(R.id.save);
        refreshRate = (Spinner) findViewById(R.id.refreshRate);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        bindViews();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.refreshRate, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refreshRate.setAdapter(adapter);

        Intent lastIntent = getIntent();
        String latitudeIntent = lastIntent.getStringExtra("latitude");
        String longitudeIntent = lastIntent.getStringExtra("longitude");
        String spinnerIntent = lastIntent.getStringExtra("spinner");
        if (latitudeIntent != null) {
            editLatitude.setText(latitudeIntent);
            editLongitude.setText(longitudeIntent);
            int spinnerPosition = adapter.getPosition(millisToMinutes(spinnerIntent));
            refreshRate.setSelection(spinnerPosition);
        }

        save.setOnClickListener(v -> {
            try {
                String latitudeString = editLatitude.getText().toString();
                String longitudeString = editLongitude.getText().toString();
                int spinnerString = Integer.parseInt(refreshRate.getSelectedItem().toString());
                double latitude = Double.parseDouble(latitudeString);
                double longitude = Double.parseDouble(longitudeString);

                if (validate(latitude, longitude)) {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("spinner", spinnerString);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validate(double latitude, double longitude) {
        return !(latitude < -90) && !(latitude > 90) && !(longitude < -180) && !(longitude > 180);
    }

    private CharSequence millisToMinutes(String millis){
        long millisLong = Long.parseLong(millis);
        return String.valueOf(millisLong / 60 / 1000);
    }
}