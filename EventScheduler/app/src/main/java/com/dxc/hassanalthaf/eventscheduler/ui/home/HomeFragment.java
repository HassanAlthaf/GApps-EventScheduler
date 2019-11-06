package com.dxc.hassanalthaf.eventscheduler.ui.home;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dxc.hassanalthaf.eventscheduler.QRScannerActivity;
import com.dxc.hassanalthaf.eventscheduler.R;
import com.dxc.hassanalthaf.eventscheduler.ViewEventActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private EditText eventName;
    private Button createEventButton;

    private ListView eventsList;

    private ProgressBar progressLoader;
    private ProgressBar listLoader;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        eventName = (EditText) root.findViewById(R.id.eventName);
        createEventButton = (Button) root.findViewById(R.id.createEventButton);
        progressLoader = (ProgressBar) root.findViewById(R.id.progressLoader);
        listLoader = (ProgressBar) root.findViewById(R.id.listLoader);
        eventsList = (ListView) root.findViewById(R.id.eventsList);
        prepareEventsTable();

        FloatingActionButton qrScannerButton = root.findViewById(R.id.qrScannerButton);

        qrScannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), QRScannerActivity.class);

                startActivityForResult(intent, 2);
            }
        });

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = eventName.getText().toString();

                if (title.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in the Event Name.", Toast.LENGTH_LONG).show();
                    return;
                }

                progressLoader.setVisibility(View.VISIBLE);

                String url = "<EventScheduler.gs Published Link>?title=" + title;

                RequestQueue queue = Volley.newRequestQueue(getContext());

                JsonObjectRequest createEventRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    progressLoader.setVisibility(View.GONE);

                    if (response.has("status")) {
                        try {
                            if (response.getInt("status") == 200) {
                                Toast.makeText(getContext(), "Event has been successfully created. Check your email.", Toast.LENGTH_LONG).show();
                                prepareEventsTable();
                            } else {
                                Toast.makeText(getContext(), "Creation of event failed. (RESP1)", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            Log.e("API", ex.getMessage());
                            Toast.makeText(getContext(), "Creation of event failed. (JSON)", Toast.LENGTH_LONG).show();
                        }

                        return;
                    }

                    Toast.makeText(getContext(), "Creation of event failed. (RESP2)", Toast.LENGTH_LONG).show();
                }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressLoader.setVisibility(View.GONE);

                        Toast.makeText(getContext(), "We are encountering issues with our connection to the server. Please try again.", Toast.LENGTH_LONG).show();
                        //if (error == null || error.networkResponse == null) {}
                    }
                });

                createEventRequest.setRetryPolicy(new DefaultRetryPolicy(
                        300000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 100,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(createEventRequest);
            }
        });

        return root;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (data != null) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 2 && data.hasExtra("Result")) {
                try {
                    JSONObject dataObject = new JSONObject(data.getStringExtra("Result"));
                    this.handleQrCodeContents(dataObject);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }

            if (requestCode == 3 && data.hasExtra("Deleted")) {
                this.prepareEventsTable();
            }
        }
    }

    private void handleQrCodeContents(JSONObject contents) {
        if (
                !contents.has("formId") ||
                !contents.has("eventName") ||
                !contents.has("employeeId") ||
                !contents.has("checkinsSpreadsheetId")
        ) {
            Toast.makeText(getContext(), "Invalid QR Code.", Toast.LENGTH_LONG).show();

            return;
        }

        final String formId;
        final String employeeId;
        final String spreadsheetId;

        try {
            formId = contents.getString("formId");
            employeeId = contents.getString("employeeId");
            spreadsheetId = contents.getString("checkinsSpreadsheetId");
        } catch (Exception ex) {
            Toast.makeText(getContext(), "Invalid QR Code.", Toast.LENGTH_LONG).show();

            return;
        }

        progressLoader.setVisibility(View.VISIBLE);

        String url = "<CheckIns.gs URL>";

        RequestQueue queue = Volley.newRequestQueue(getContext());

        StringRequest checkinRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String responseText) {
                        progressLoader.setVisibility(View.GONE);
                        JSONObject response = null;

                        try {
                            response = new JSONObject(responseText);
                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "Invalid registration. Check-in failed.", Toast.LENGTH_LONG);
                        }

                        if (response.has("status")) {
                            try {
                                if (response.getInt("status") == 200) {
                                    Toast.makeText(getContext(), new JSONObject(response.getString("data")).getString("name") + " has been successfully checked-in!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), response.getString("error"), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception ex) {
                                Log.e("API", ex.getMessage());
                                Toast.makeText(getContext(), "Invalid registration. Check-in failed.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressLoader.setVisibility(View.GONE);

                        Toast.makeText(getContext(), "Server seems to be down. Try again later.", Toast.LENGTH_LONG).show();

                        Log.e("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("formId", formId);
                params.put("spreadsheetId", spreadsheetId);
                params.put("employeeId", employeeId);

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();

                params.put("dateTime", formatter.format(date));

                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                Location location = null;

                try {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } catch (SecurityException exception) {
                    Toast.makeText(getContext(), "Necessary permissions are not enabled. Restart the application.", Toast.LENGTH_LONG).show();
                }

                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));

                return params;
            }
        };

        checkinRequest.setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 100,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(checkinRequest);
    }


    class EventsListAdapter extends ArrayAdapter<String> {
        private Context context;
        private JSONObject[] rows;

        public EventsListAdapter(Context context, JSONObject[] rows, String[] eventNames) {
            super(context, R.layout.event_row, R.id.eventName, eventNames);

            this.context = context;
            this.rows = rows;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.event_row, parent, false);

            TextView eventName = row.findViewById(R.id.eventName);

            String title = "Failed Loading Title...";
            String formId = "";

            try {
                title = rows[position].getString("title");
                formId = rows[position].getString("formId");
            } catch (JSONException ex) {
                Log.e("HomeFragment", ex.getMessage());
            }

            eventName.setText(title);

            row.setTag(formId);

            return super.getView(position, row, parent);
        }
    }

    public void populateEventsTable(final JSONObject[] rows, final String[] eventNames)
    {
        EventsListAdapter adapter = new EventsListAdapter(getContext(), rows, eventNames);
        eventsList.setAdapter(adapter);

        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ViewEventActivity.class);

                intent.putExtra("eventName", eventNames[position]);
                intent.putExtra("formId", view.getTag().toString());

                startActivityForResult(intent, 3);
            }
        });
    }

    public void prepareEventsTable()
    {
        eventsList.setAdapter(null);
        listLoader.setVisibility(View.VISIBLE);
        String url = "<GetAllForms.gs>";

        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest getEventsRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                listLoader.setVisibility(View.GONE);

                if (response.has("status")) {
                    try {
                        if (response.getInt("status") == 200) {
                            JSONArray arrayRows = null;

                            arrayRows = new JSONArray(response.getString("data"));

                            JSONObject[] rows = new JSONObject[arrayRows.length()];
                            String[] eventNames = new String[arrayRows.length()];

                            for (int i = 0; i < arrayRows.length(); i++) {
                                rows[i] = new JSONObject(arrayRows.getString(i));

                                eventNames[i] = rows[i].getString("title");
                            }

                            populateEventsTable(rows, eventNames);
                        } else {
                            Toast.makeText(getContext(), "Failed to fetch Events List.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Log.e("API", ex.getMessage());
                        Toast.makeText(getContext(), "Failed to fetch Events List.", Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                Toast.makeText(getContext(), "Failed to fetch Events List.", Toast.LENGTH_LONG).show();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                listLoader.setVisibility(View.GONE);

                Toast.makeText(getContext(), "We are encountering issues with our connection to the server. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

        getEventsRequest.setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 100,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getEventsRequest);
    }
}