package com.icanstudioz.taxi.fragement;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.Server.Server;
import com.icanstudioz.taxi.acitivities.HomeActivity;
import com.icanstudioz.taxi.custom.Utils;
import com.icanstudioz.taxi.pojo.User;
import com.icanstudioz.taxi.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by android on 8/4/17.
 */

public class VehicleInformationFragment extends Fragment {

    ImageView vehicle_pic;
    TextInputEditText input_brand;
    TextInputEditText input_model;
    TextInputEditText input_year;
    TextInputEditText input_color;
    AppCompatButton btn_continue;
    private TextInputEditText input_vehicleno;

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_vehicle_detail, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String doc = bundle.getString("go");
            if (doc != null && doc.equals("doc")) {
                Toast.makeText(getActivity(), doc, Toast.LENGTH_SHORT).show();
                ((HomeActivity) getActivity()).changeFragment(new UploadDomentFragment(), "Upload Document");
            }
        }
        ((HomeActivity) getActivity()).fontToTitleBar("Add Vehicle Information");
        BindView();
        if (Utils.haveNetworkConnection(getActivity())) {
            getVehicleInfo();
        } else {
            try {
                User user = SessionManager.getUser();
                input_brand.setText(user.getBrand());
                input_model.setText(user.getModel());
                input_year.setText(user.getYear());
                input_color.setText(user.getColor());
                input_vehicleno.setText(user.getVehicle_no());


            } catch (Exception e) {
                Log.e("catch", e.toString());

            }

        }


        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (Utils.haveNetworkConnection(getActivity())) {

                        updateVehicleInfo(input_brand.getText().toString().trim(),
                                input_model.getText().toString().trim(),
                                input_year.getText().toString().trim(),
                                input_color.getText().toString().trim(), input_vehicleno.getText().toString().trim());
                    } else {
                        Toast.makeText(getActivity(), "network is not available", Toast.LENGTH_LONG).show();
                    }

                } else {
                    //do nothing here
                }
            }
        });
        return view;
    }

    public void BindView() {
        vehicle_pic = (ImageView) view.findViewById(R.id.vehicle_pic);
        input_brand = (TextInputEditText) view.findViewById(R.id.input_brand);
        input_model = (TextInputEditText) view.findViewById(R.id.input_model);
        input_year = (TextInputEditText) view.findViewById(R.id.input_year);
        input_color = (TextInputEditText) view.findViewById(R.id.input_color);
        btn_continue = (AppCompatButton) view.findViewById(R.id.btn_continue);
        input_vehicleno = (TextInputEditText) view.findViewById(R.id.input_vehicleno);
        overrideFonts(getActivity(), view);
    }

    public void getVehicleInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading.....");
        RequestParams params = new RequestParams();

        params.put("user_id", SessionManager.getUserId());
        User user = SessionManager.getUser();
        input_brand.setText(user.getBrand());
        input_model.setText(user.getModel());
        input_year.setText(user.getYear());
        input_color.setText(user.getColor());
        input_vehicleno.setText(user.getVehicle_no());

        Server.setHeader(user.getKey());
        Server.get(Server.GET_PROFILE, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.e("info", response.toString());

                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        Gson gson = new Gson();
                        User user = gson.fromJson(response.getJSONObject("data").toString(), User.class);

                        input_brand.setText(user.getBrand());
                        input_model.setText(user.getModel());
                        input_year.setText(user.getYear());
                        input_color.setText(user.getColor());
                        input_vehicleno.setText(user.getVehicle_no());
                       /* User user1=SessionManager.getUser();
                        user1.setBrand(user.getBrand());
                        user1.setModel(user.getModel());
                        user1.setYear(user.getYear());
                        user1.setColor(user.getColor());
                        user1.setVehicle_no(user.getVehicle_no());

                        SessionManager.setUser(gson.toJson(user1));*/

                    } else {

                        Toast.makeText(getActivity(), "error occurred", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {

                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

        });


    }

    public Boolean validate() {
        String message = "field is required";
        Boolean validate = true;
        if (input_brand.getText().toString().trim().equals("")) {
            input_brand.setError(message);
            validate = false;
        } else {
            input_brand.setError(null);
        }
        if (input_model.getText().toString().trim().equals("")) {
            input_model.setError(message);
            validate = false;
        } else {
            input_model.setError(null);
        }
        if (input_year.getText().toString().trim().equals("")) {
            input_year.setError(message);
            validate = false;
        } else {
            input_year.setError(null);
        }
        if (input_color.getText().toString().trim().equals("")) {
            input_color.setError(message);
            validate = false;
        } else {
            input_color.setError(null);
        }
        return validate;
    }


    public void updateVehicleInfo(String brand, String model, String year, String color, String vehicleno) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading.....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        RequestParams params = new RequestParams();

        Server.setHeader(SessionManager.getKEY());

        params.put("user_id", SessionManager.getUserId());
        params.put("brand", brand);
        params.put("model", model);
        params.put("year", year);
        params.put("color", color);
        params.put("vehicle_no", vehicleno);
        Server.post(Server.UPDATE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("info-updated", response.toString());
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        User user = SessionManager.getUser();
                        Gson gson = new Gson();
                        user.setBrand(brand);
                        user.setModel(model);
                        user.setYear(year);
                        user.setColor(color);
                        user.setVehicle_no(vehicleno);
                        SessionManager.setUser(gson.toJson(user));
                        Toast.makeText(getActivity(), "Information updated", Toast.LENGTH_LONG).show();
                        ((HomeActivity) getActivity()).changeFragment(new UploadDomentFragment(), "Upload Document");
                    } else {
                        Toast.makeText(getActivity(), "Failed to update information ", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {

                    Log.d("catch", e.toString());
                    Toast.makeText(getActivity(), "error occurred", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.cancel();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(getActivity(), "error occurred", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*Toast.makeText(context, "attach", Toast.LENGTH_SHORT).show();
        HomeActivity homeActivity=new HomeActivity();*/
    }

    private void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof AppCompatButton) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf"));
            } else if (v instanceof EditText) {
                Log.e("edittext", "called");
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf"));
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf"));
            }

        } catch (Exception e) {
            Log.d("catch", "font settting error  " + e.toString());
        }
    }

}
