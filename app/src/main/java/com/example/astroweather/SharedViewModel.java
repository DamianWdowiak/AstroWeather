package com.example.astroweather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Double> longitude = new MutableLiveData<>();
    private final MutableLiveData<Double> latitude = new MutableLiveData<>();
    private final MutableLiveData<Long> refreshRate = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isImperial = new MutableLiveData<>();
    private final MutableLiveData<Boolean> refreshNow = new MutableLiveData<>();
    private final MutableLiveData<String> cityName = new MutableLiveData<>();

    public LiveData<Double> getLongitude() {
        return longitude;
    }

    public void setLongitude(double value) {
        longitude.setValue(value);
    }

    public LiveData<Double> getLatitude() {
        return latitude;
    }

    public void setLatitude(double value) {
        latitude.setValue(value);
    }

    public LiveData<Long> getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(long value) {
        refreshRate.setValue(value);
    }

    public LiveData<Boolean> getIsImperial() {
        return isImperial;
    }

    public void setIsImperial(boolean value) {
        isImperial.setValue(value);
    }

    public LiveData<Boolean> getRefreshNow() {
        return refreshNow;
    }

    public void setRefreshNow(boolean value) {
        refreshNow.setValue(value);
    }

    public LiveData<String> getCityName() {
        return cityName;
    }

    public void setCityName(String value) {
        cityName.setValue(value);
    }
}
