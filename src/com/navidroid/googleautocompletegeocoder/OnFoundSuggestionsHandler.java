package com.navidroid.googleautocompletegeocoder;

import java.util.List;
import android.location.Address;

public interface OnFoundSuggestionsHandler {
	public void invoke(List<Address> suggestions);
}
