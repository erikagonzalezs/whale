package com.example.taller3.utils;

import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class LocationManager {
    public final static String geocoderSearch(LatLng latLng, Geocoder geocoder)
    {
        String addressName = " ";
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addresses!=null && !addresses.isEmpty())
            {
                Address addressResult = addresses.get(0);
                addressName= addressResult.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressName;
    }
    public final static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

}
