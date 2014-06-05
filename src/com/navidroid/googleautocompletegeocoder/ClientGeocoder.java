package com.navidroid.googleautocompletegeocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

public class ClientGeocoder implements GoogleGeocoderWrapper {
	
	private Geocoder geocoder;

	public ClientGeocoder(Context context) {
		geocoder = new Geocoder(context);
	}
	
	public void reverseGeocode(LatLng location, final OnReverseGeocodeSuccess onReverseGeocodeSuccess) {
		AsyncTask<LatLng, Void, Address> asyncGeocodeTask = new AsyncTask<LatLng, Void, Address>() {
			@Override
			protected Address doInBackground(LatLng... params) {
				LatLng location = params[0];
				try {
					List<Address> result = geocoder.getFromLocation(location.latitude, location.longitude, 1);
					if (result != null && result.size() > 0) {
						return result.get(0);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Address result) {
				onReverseGeocodeSuccess.invoke(result);
			}
		};
		
		AsyncTaskExecutor.execute(asyncGeocodeTask, location);
	}
	
	public void geocode(String search, final GeocodeHeuristics heuristics, final OnGeocodeSuccess onGeocodeSuccess, final OnGeocodeFailure onGeocodeFailure) {
		AsyncTask<String, Void, GeocodeResult> asyncGeocodeTask = new AsyncTask<String, Void, GeocodeResult>() {
			@Override
			protected GeocodeResult doInBackground(String... params) {
				GeocodeResult result = new GeocodeResult();
				String search = params[0];
				try {
					LatLngBounds bounds = heuristics.bounds;
					result.addresses = bounds == null ? getFromLocationName(search, 5) : getFromLocationName(search, 5, bounds);
				} catch (IOException e) {
					result.e = e;
				}
				return result;
			}
			
			@Override
			protected void onPostExecute(GeocodeResult result) {
				if (result.addresses != null) {
					onGeocodeSuccess.invoke(result.addresses);
				} else {
					onGeocodeFailure.invoke(result.e);
				}
			}
		};
		
		AsyncTaskExecutor.execute(asyncGeocodeTask, search);
	}
	
	private List<Address> getFromLocationName(String search, int maxResults) throws IOException {
		return geocoder.getFromLocationName(search, maxResults);
	}
	
	private List<Address> getFromLocationName(String search, int maxResults, LatLngBounds bounds) throws IOException {
		return geocoder.getFromLocationName(
			search,
			maxResults,
			bounds.southwest.latitude,
			bounds.southwest.longitude,
			bounds.northeast.latitude,
			bounds.northeast.longitude);
	}
	
}
