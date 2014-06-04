package com.navidroid.googleautocompletegeocoder;

import android.location.Address;

public interface OnAddressSelectedHandler {
	public void invoke(Address address);
}
