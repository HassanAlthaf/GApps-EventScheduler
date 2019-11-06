package com.dxc.hassanalthaf.eventscheduler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;

public class ViewEventActivity extends AppCompatActivity {
    private BarChart registrationVsAttendanceChart;

    public void initializeGraph(final String eventName, String formId) {
        String url = "<<GetStatisticsByForm.gs>>?formId=" + formId;

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest getGraphData = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("status")) {
                    try {
                        if (response.getInt("status") == 200) {
                            ArrayList<BarEntry> values = new ArrayList<>();

                            JSONObject data = new JSONObject(response.getString("data"));

                            float registrationCount = data.getInt("registrations");
                            float checkinsCount = data.getInt("checkins");

                            values.add(new BarEntry(1, new float[]{checkinsCount, registrationCount - checkinsCount}));

                            BarDataSet dataSet = new BarDataSet(values, " |  Statistics of " + eventName);
                            dataSet.setDrawIcons(false);
                            dataSet.setStackLabels(new String[]{"Check-ins", "Registrations"});
                            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                            BarData barData = new BarData(dataSet);

                            registrationVsAttendanceChart.setData(barData);
                            registrationVsAttendanceChart.setFitBars(true);
                            registrationVsAttendanceChart.invalidate();
                            registrationVsAttendanceChart.getDescription().setEnabled(false);
                            registrationVsAttendanceChart.getXAxis().setEnabled(false);

                            YAxis leftAxis = registrationVsAttendanceChart.getAxisLeft();
                            leftAxis.setGranularityEnabled(true);
                            leftAxis.setGranularity(1);

                            registrationVsAttendanceChart.getAxisRight().setEnabled(false);
                        } else {
                            Toast.makeText(getApplicationContext(), response.getString("error"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Log.e("API", ex.getMessage());
                        Toast.makeText(getApplicationContext(), "Fetching of graph data failed.", Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                Toast.makeText(getApplicationContext(), "Fetching of graph data failed.", Toast.LENGTH_LONG).show();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VEA-GRAPH", error.getMessage());
                Toast.makeText(getApplicationContext(), "We are encountering issues with our connection to the server. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

        getGraphData.setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 100,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getGraphData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        Intent intent = getIntent();

        getSupportActionBar().setTitle(intent.getStringExtra("eventName"));

        final String formId = intent.getStringExtra("formId");

        registrationVsAttendanceChart = (BarChart) findViewById(R.id.registrationVsAttendanceChart);
        registrationVsAttendanceChart.setMaxVisibleValueCount(1000);
        initializeGraph(intent.getStringExtra("eventName"), formId);

        Button deleteEventButton = findViewById(R.id.deleteEventButton);

        deleteEventButton.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(final View v) {

                new AlertDialog.Builder(ViewEventActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("Are you sure you want to delete this event? Note that this action is irreversible.")
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        Snackbar.make(v, "Event is being deleted...", Snackbar.LENGTH_INDEFINITE)
                                                .show();
                                        String url = "<DeleteEvent.gs>?formId=" + formId;

                                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                                        JsonObjectRequest deleteEventRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                if (response.has("status")) {
                                                    try {
                                                        if (response.getInt("status") == 200) {
                                                            Toast.makeText(getApplicationContext(), "Event has been successfully deleted.", Toast.LENGTH_LONG).show();

                                                            Intent intent = new Intent();
                                                            intent.putExtra("Deleted", "Y");

                                                            setResult(3, intent);
                                                            onBackPressed();
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "Deletion of event failed.", Toast.LENGTH_LONG).show();
                                                        }
                                                    } catch (Exception ex) {
                                                        Log.e("API", ex.getMessage());
                                                        Toast.makeText(getApplicationContext(), "Deletion of event failed.", Toast.LENGTH_LONG).show();
                                                    }

                                                    return;
                                                }

                                                Toast.makeText(getApplicationContext(), "Deletion of event failed.", Toast.LENGTH_LONG).show();
                                            }

                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e("VEA", error.getMessage());
                                                Toast.makeText(getApplicationContext(), "We are encountering issues with our connection to the server. Please try again.", Toast.LENGTH_LONG).show();
                                            }
                                        });


                                        deleteEventRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                300000,
                                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 100,
                                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                        queue.add(deleteEventRequest);
                                    }
                                })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


            }
        });
    }
}
