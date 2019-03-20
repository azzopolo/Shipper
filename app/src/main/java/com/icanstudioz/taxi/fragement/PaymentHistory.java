package com.icanstudioz.taxi.fragement;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.Server.Server;
import com.icanstudioz.taxi.acitivities.HomeActivity;
import com.icanstudioz.taxi.adapter.PaymentAdapter;
import com.icanstudioz.taxi.custom.Utils;
import com.icanstudioz.taxi.pojo.PendingRequestPojo;
import com.icanstudioz.taxi.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by android on 15/4/17.
 */

public class PaymentHistory extends Fragment {
    View view;
    TextView txt_error;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.payement_history, container, false);
        txt_error = (TextView) view.findViewById(R.id.txt_error);
        ((HomeActivity) getActivity()).fontToTitleBar(getString(R.string.payment_history));
        if (Utils.haveNetworkConnection(getActivity())) {
            getPaymentHistory();
        } else {
            Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();

        }
        return view;
    }

    private void getPaymentHistory() {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading......");
        progressDialog.show();
        RequestParams params = new RequestParams();
        params.put("driver_id", SessionManager.getUserId());
        Server.setHeader(SessionManager.getKEY());
        Server.get(Server.PAYMENT_HISTORY, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        Gson gson = new GsonBuilder().create();
                        List<PendingRequestPojo> list = gson.fromJson(response.getJSONArray("data").toString(), new TypeToken<List<PendingRequestPojo>>() {
                        }.getType());
                        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        PaymentAdapter paymentAdapter = new PaymentAdapter(list);
                        recyclerView.setAdapter(paymentAdapter);
                        paymentAdapter.notifyDataSetChanged();
                        progressDialog.cancel();

                    } else {
                        progressDialog.cancel();
                        txt_error.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    progressDialog.cancel();
                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progressDialog.cancel();
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
            }
        });


    }


}
