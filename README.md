GoogleAutoCompleteGeocoder
==========================

Easily add an an auto complete geocoder to your Android app, as easy as adding an AutoCompleteTextView. Based on [this StackOverflow post](http://stackoverflow.com/questions/9142885/geocoder-autocomplete-in-android).

![Screenshot](http://i.imgur.com/2krRCup.png)

Usage
-----
GoogleAutoCompleteGeocoder extends the AutoCompleteTextView, so any attributes available on that class are also available here. The widget wraps the android.location.Geocoder object and performs gecoding look ups asynchronously. It is only available on devices with Google Play Services enabled, minimum SDK version 11.

Declared in Android XML:
```xml
<com.navidroid.googleautocompletegeocoder.GoogleAutoCompleteGeocoder
		android:hint="Search"
		android:layout_width="0dp"
		android:layout_weight="1"
		android:layout_height="wrap_content"
		android:ems="10"
		android:inputType="text" />
```

Event handling:
```java
autoCompleteGeocoder.setOnAddressSelectedHandler(new OnAddressSelectedHandler() {
    @Override
    public void invoke(Address address) {
        // Do something with the selected address
    }
});

autoCompleteGeocoder.setOnAddressSelectedHandler(new OnFoundSuggestionsHandler() {
    @Override
    public void invoke(List<Address> suggestions) {
        // Do something with the suggested addresses
});
```

Localization:
```java
autoCompleteGeocoder.setLocation(new LatLng(-43.526432, 172.595631)); // Results will be biased to this region
```
