package com.dxc.hassanalthaf.eventscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dxc.hassanalthaf.eventscheduler.graphs.ComparisonGraphIndexAxisValueFormatter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {

    private BarChart registrationVsAttendanceChart;
    private ProgressBar progressLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        progressLoader = findViewById(R.id.progressLoader);

        ArrayList<String> forms = getIntent().getStringArrayListExtra("forms");

        getSupportActionBar().setTitle("Compare Statistics");

        registrationVsAttendanceChart = findViewById(R.id.registrationVsAttendanceChart);

        String json = null;

        try {
            json = new JSONArray(forms.toArray()).toString();
        } catch (Exception ex) {
            Log.e("Test", ex.getMessage());
        }


        String url = "<GET STATISTICS BY FORM>?forms=" + json;

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest getGraphData = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressLoader.setVisibility(View.GONE);

                if (response.has("status")) {
                try {
                    if (response.getInt("status") == 200) {
                        ArrayList<BarEntry> values = new ArrayList<>();

                        JSONArray data = new JSONArray(response.getString("data"));

                        ArrayList<String> descriptions = new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject row = data.getJSONObject(i);

                            float registrationCount = row.getInt("registrations");
                            float checkinsCount = row.getInt("checkins");

                            descriptions.add(row.getString("title"));

                            values.add(new BarEntry(i + 1, new float[]{checkinsCount, registrationCount - checkinsCount}));
                        }

                        BarDataSet dataSet = new BarDataSet(values, " |  Combined Events Statistics");
                        dataSet.setDrawIcons(false);
                        dataSet.setStackLabels(new String[]{"Check-ins", "Registrations"});
                        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                        BarData barData = new BarData(dataSet);

                        registrationVsAttendanceChart.setData(barData);
                        registrationVsAttendanceChart.setFitBars(true);
                        registrationVsAttendanceChart.invalidate();
                        registrationVsAttendanceChart.getDescription().setEnabled(false);

                        XAxis xAxis = registrationVsAttendanceChart.getXAxis();
                        xAxis.setGranularityEnabled(true);
                        xAxis.setGranularity(1);
                        xAxis.setValueFormatter(new ComparisonGraphIndexAxisValueFormatter(descriptions));

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
                progressLoader.setVisibility(View.GONE);
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
}
