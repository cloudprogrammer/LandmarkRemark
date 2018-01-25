package net.ra1n_entertainment.landmarkremark;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SearchNotes extends AppCompatActivity {

    // Array of the notes found
    Map[] notes = {};

    // Our listView adapter
    ListItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_notes);

        ListView listView = findViewById(R.id.searchListView);

        // ListView item onClick listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the selected note then pass it back and finish this activity
                Map note = adapter.getItem(i);
                Intent intent = new Intent();
                intent.putExtra("note", (Serializable) note);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        final SearchView searchView = findViewById(R.id.searchSearchView);
        final CheckBox checkBox = findViewById(R.id.searchUsernameSearchCheckBox);

        searchView.setQueryHint("Search by username");

        // Search view query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchNotes(s, checkBox.isChecked());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        adapter = new ListItemAdapter();
        listView.setAdapter(adapter);

        // If checkbox is checked, search by username else search by description
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()) {
                    searchView.setQueryHint("Search by username");
                } else {
                    searchView.setQueryHint("Search by description");
                }
            }
        });

    }

    /**
     * Search backend notes
     * @param query - The string query
     * @param usernameSearch - A username search or description search
     */
    public void searchNotes(String query, boolean usernameSearch) {
        DataQueryBuilder dataQuery = DataQueryBuilder.create();
        dataQuery.setPageSize(25);
        // If usernameSearch is true set the query to search by username else by description
        if (usernameSearch) {
            dataQuery.setWhereClause("username = '" + query + "'");
        } else {
            dataQuery.setWhereClause("description LIKE '" + query + "%'");
        }

        // The call to backendless for the search
        Backendless.Data.of("Notes").find(dataQuery, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                // If the response is bigger than 0
                if (response.size() > 0) {
                    // Set the notes array to the returned notes and update the listView adapter
                    notes = response.toArray(new Map[response.size()]);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(SearchNotes.this, fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Adapter for listView
     */
    class ListItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return notes.length;
        }

        @Override
        public Map getItem(int i) {
            return notes[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = getLayoutInflater().inflate(R.layout.list_item_layout, null);

            TextView title = view.findViewById(R.id.noteItemTitle);
            TextView username = view.findViewById(R.id.noteItemUsername);
            TextView description = view.findViewById(R.id.noteItemDescription);
            TextView location = view.findViewById(R.id.noteItemLocation);

            title.setText(notes[i].get("title").toString());
            username.setText(notes[i].get("username").toString());
            description.setText(notes[i].get("description").toString());
            location.setText("Latitude: " + notes[i].get("latitude").toString() +
                    "\nLongitude: " + notes[i].get("longitude").toString());

            return view;
        }
    }

}
