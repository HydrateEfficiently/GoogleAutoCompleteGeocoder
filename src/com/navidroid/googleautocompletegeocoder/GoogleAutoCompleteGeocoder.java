package com.navidroid.googleautocompletegeocoder;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.navidroid.googleautocompletegeocoder.GoogleGeocoderWrapper.OnGeocodeFailure;
import com.navidroid.googleautocompletegeocoder.GoogleGeocoderWrapper.OnGeocodeSuccess;
import com.navidroid.googleautocompletegeocoder.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;

public class GoogleAutoCompleteGeocoder extends AutoCompleteTextView {
	
	private final static int BOUNDS_INFLATION_FACTOR_DEGREES = 2;
	private final static int AUTO_COMPLETE_DELAY_MS = 500;
	private final static int MAX_SUGGESTIONS = 5;
	private final static int DEFAULT_MINIMUM_CHARACTERS = 3;
	
	private String apiKey;
	private boolean useClientSide = true;
	private long textLastEnteredTime;
	private Handler handler;
	private List<Address> currentAddresses;
	private ArrayAdapterNoFilter autoCompleteAdapter;
	private OnAddressSelectedHandler onAddressSelectedHandler;
	private OnFoundSuggestionsHandler onFoundSuggestionsHandler;
	private GoogleGeocoderWrapper geocoder;
	private GeocodeHeuristics heuristics;
	
	private OnGeocodeSuccess onGeocodeSuccess = new OnGeocodeSuccess() {
		@Override
		public void invoke(List<Address> addresses) {
			updateAutoComplete(addresses);
		}
	};
	
	private OnGeocodeFailure onGeocodeFailure = new OnGeocodeFailure() {
		@Override
		public void invoke(Exception e) {
			Log.e("GoogleAutoCompleteGeocoder", e.getMessage(), e);
		}
	};

	public GoogleAutoCompleteGeocoder(Context context, AttributeSet attrs) throws Exception {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GoogleAutocompleteGeocoder);
		final int customAttrCount = a.getIndexCount();
		for (int i = 0; i < customAttrCount; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
				case R.styleable.GoogleAutocompleteGeocoder_adapterResourceId:
					// TODO: implement me!
					break;
				case R.styleable.GoogleAutocompleteGeocoder_apiKey:
					apiKey = a.getString(i);
					break;
				case R.styleable.GoogleAutocompleteGeocoder_useClientSide:
					useClientSide = a.getBoolean(i, true);
					break;
				case R.styleable.GoogleAutocompleteGeocoder_maxResults:
					// TODO: implement me!
					break;
				case R.styleable.GoogleAutocompleteGeocoder_minCharacters:
					// TODO: implement me!
					break;
			}
		}
		
		if (!useClientSide && apiKey == null) {
			throw new Exception("Cannot use server-side geocoding without an API key");
		}
		
		if (useClientSide && !Geocoder.isPresent()) {
			throw new Exception("Client-side geocoding service is not present on this device.");
		}
		
		if (useClientSide) {
			geocoder = new ClientGeocoder(context);
		}
		
		setupTextWatcher();
		setupOnItemSelectedListener();
		setThreshold(DEFAULT_MINIMUM_CHARACTERS);
		autoCompleteAdapter = new ArrayAdapterNoFilter(context, R.layout.autocomplete_textview);
		setAdapter(autoCompleteAdapter);
		heuristics = new GeocodeHeuristics();
		handler = new Handler();
	}
	
	private void setupTextWatcher() {
		addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String search = s.toString();
				textLastEnteredTime = System.currentTimeMillis();
				if (shouldTryGeocode(search)) {
					final String searchRef = new String(search);
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (textLastEnteredTime + AUTO_COMPLETE_DELAY_MS < System.currentTimeMillis()) {
								geocoder.geocode(searchRef, heuristics, onGeocodeSuccess, onGeocodeFailure);
							}
						}
					}, AUTO_COMPLETE_DELAY_MS);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable text) {
				for (int i = 0; i < text.length(); i++) {
					int lastIndex = i - 1;
					Character currentCharacter = text.charAt(i);
					if ((lastIndex < 0 || ((Character)(text.charAt(lastIndex))).equals(' ')) && Character.isLowerCase(currentCharacter)) {
						text.replace(i, i + 1, Character.toUpperCase(currentCharacter) + "");
					}
				}
			}
		});
	}
	
	private void setupOnItemSelectedListener() {
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				assert currentAddresses != null;
				Address selected = currentAddresses.get(position);
				if (onAddressSelectedHandler != null) {
					onAddressSelectedHandler.invoke(selected);
				}
			}
		});
	}
	
	private boolean shouldTryGeocode(String search) {
		String[] addressParts = search.split(" ");
		if (addressParts.length == 0) {
			return false;
		} else if (addressParts.length == 1) {
			return addressParts[0].length() > 0 && !Character.isDigit(addressParts[0].charAt(0));
		} else {
			return true;
		}
	}
	
	private void updateAutoComplete(List<Address> suggestions) {
		autoCompleteAdapter.clear();
		currentAddresses = suggestions;
		
		for (Address address : suggestions) {
			String formattedAddress = formatAddress(address);
			autoCompleteAdapter.add(formattedAddress);
		}
		
		if (onFoundSuggestionsHandler != null && suggestions.size() > 0) {
			onFoundSuggestionsHandler.invoke(suggestions);
		}
		
		autoCompleteAdapter.notifyDataSetChanged();
	}
	
	private String formatAddress(Address address) {
		String formattedAddress = "";
		int lineCount = address.getMaxAddressLineIndex();
		for (int i = 0; i < lineCount; i++) {
			formattedAddress += address.getAddressLine(i);
			if (i < lineCount - 1) {
				formattedAddress += ", ";
			}
		}
		return formattedAddress;
	}
	
	public void setLocation(LatLng location) {
		// TODO: make this smarter!
		LatLng negativeBound = new LatLng(location.latitude - BOUNDS_INFLATION_FACTOR_DEGREES, location.longitude - BOUNDS_INFLATION_FACTOR_DEGREES);
		LatLng positiveBound = new LatLng(location.latitude + BOUNDS_INFLATION_FACTOR_DEGREES, location.longitude + BOUNDS_INFLATION_FACTOR_DEGREES);
		heuristics.bounds = new LatLngBounds(negativeBound, positiveBound);
	}
	
	public void setOnAddressSelectedHandler(OnAddressSelectedHandler handler) {
		onAddressSelectedHandler = handler;
	}
	
	public void setOnFoundSuggestionsHandler(OnFoundSuggestionsHandler handler) {
		onFoundSuggestionsHandler = handler;
	}
}
