package net.ra1n_entertainment.landmarkremark;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class NoteInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private View infoWindow = null;
    private LayoutInflater inflater = null;

    NoteInfoAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Inflate the view
        if (infoWindow == null) {
            infoWindow = inflater.inflate(R.layout.activity_note_info_adapter, null);
        }

        // Get elements
        TextView title = infoWindow.findViewById(R.id.noteInfoTitle);
        TextView description = infoWindow.findViewById(R.id.noteInfoDescription);

        // Set text
        title.setText(marker.getTitle());
        description.setText(marker.getSnippet());

        return(infoWindow);
    }
}
