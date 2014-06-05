package com.navidroid.googleautocompletegeocoder;

import java.util.List;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

public interface GoogleGeocoderWrapper {
	
	public interface OnReverseGeocodeSuccess {
		void invoke(Address address);
	}
	
	public interface OnGeocodeSuccess {
		void invoke(List<Address> address);
	}
	
	public interface OnGeocodeFailure {
		void invoke(Exception e);
	}
	
	void reverseGeocode(LatLng location, OnReverseGeocodeSuccess onReverseGeocodeSuccess);
	
	void geocode(String search, GeocodeHeuristics heuristics, OnGeocodeSuccess onGeocodeSuccess, OnGeocodeFailure onGeocodeFailure);
	
}
