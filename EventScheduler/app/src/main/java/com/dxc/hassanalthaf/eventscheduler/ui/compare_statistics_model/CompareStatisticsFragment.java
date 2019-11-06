package com.dxc.hassanalthaf.eventscheduler.ui.compare_statistics_model;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dxc.hassanalthaf.eventscheduler.R;
import com.dxc.hassanalthaf.eventscheduler.StatisticsActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CompareStatisticsFragment extends Fragment {
    private CompareStatisticsViewModel mViewModel;
    private LinearLayout checkBoxContainer;
    private Button generateGraphButton;
    private ProgressBar progressLoader;

    public static CompareStatisticsFragment newInstance() {
        return new CompareStatisticsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.compare_statistics_fragment, container, false);

        checkBoxContainer = root.findViewById(R.id.checkBoxContainer);
        generateGraphButton = root.findViewById(R.id.generateGraphButton);
        progressLoader = root.findViewById(R.id.progressLoader);

        createCheckBoxes();

        generateGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> formIds = new ArrayList<>();

                for (int i = 0; i < checkBoxContainer.getChildCount(); i++) {
                    CheckBox box = (CheckBox)checkBoxContainer.getChildAt(i);

                    if (box.isChecked()) {
                        formIds.add(box.getTag().toString());
                    }
                }

                if (formIds.size() == 0) {
                    Toast.makeText(getContext(), "You need to select at least one Event.", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getContext(), StatisticsActivity.class);

                intent.putStringArrayListExtra("forms", formIds);

                startActivity(intent);
            }
        });

        return root;
    }

    private void createCheckBoxes() {
        String url = "<<GetAllForms.gs>>";

        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest getEventsRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressLoader.setVisibility(View.GONE);

                if (response.has("status")) {
                    try {
                        if (response.getInt("status") == 200) {
                            JSONArray arrayRows = null;

                            arrayRows = new JSONArray(response.getString("data"));

                            JSONObject[] rows = new JSONObject[arrayRows.length()];
                            String[] eventNames = new String[arrayRows.length()];

                            for (int i = 0; i < arrayRows.length(); i++) {
                                rows[i] = new JSONObject(arrayRows.getString(i));

                                CheckBox box = new CheckBox(getContext());
                                box.setText(rows[i].getString("title"));
                                box.setTag(rows[i].getString("formId"));

                                checkBoxContainer.addView(box);

                                //eventNames[i] = rows[i].getString("title");
                            }
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
                progressLoader.setVisibility(View.GONE);
                Toast.makeText(getContext(), "We are encountering issues with our connection to the server. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

        getEventsRequest.setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 100,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getEventsRequest);
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CompareStatisticsViewModel.class);
        // TODO: Use the ViewModel
    }
}
