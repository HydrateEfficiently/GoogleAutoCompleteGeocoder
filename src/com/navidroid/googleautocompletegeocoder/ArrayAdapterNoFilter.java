package com.navidroid.googleautocompletegeocoder;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

/*
 * Taken from http://stackoverflow.com/questions/9142885/geocoder-autocomplete-in-android
 */
public class ArrayAdapterNoFilter extends ArrayAdapter<String> {

    public ArrayAdapterNoFilter(Context context, int resource) {
        super(context, resource);
    }

    private static final NoFilter NO_FILTER = new NoFilter();

    @Override
    public Filter getFilter() {
        return NO_FILTER;
    }

    private static class NoFilter extends Filter {
        protected FilterResults performFiltering(CharSequence prefix) {
            return new FilterResults();
        }

        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Do nothing
        }
    }
}
