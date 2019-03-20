package com.icanstudioz.taxi.acitivities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.Server.Server;
import com.icanstudioz.taxi.custom.SetCustomFont;
import com.icanstudioz.taxi.custom.Utils;
import com.icanstudioz.taxi.pojo.User;
import com.icanstudioz.taxi.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thebrownarrow.permissionhelper.ActivityManagePermission;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends ActivityManagePermission {
    RelativeLayout relative_register;
    EditText input_email, input_password;
    AppCompatButton login;

    TextView as, txt_createaccount, forgot_password;
    String token;
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        bindView();
        applyfonts();

        relative_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();
                SessionManager.setGcmToken(token);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = getCurrentFocus();

                if (view != null) {
                    Utils.hideKeyboard(getApplicationContext(), view);
                }
                if (Utils.haveNetworkConnection(getApplicationContext())) {
                    if (validate()) {
                        login(input_email.getText().toString().trim(), input_password.getText().toString().trim());


                    } else {
                        // do nothing
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.network), Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    public void login(String email, String password) {

        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);
        params.put("utype", "1");
        params.put("gcm_token", token);
        Log.e("TOKEN",token);
        Server.post(Server.LOGIN, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        Utils utils = new Utils(LoginActivity.this);
                        utils.isAnonymouslyLoggedIn();

                        Gson gson = new Gson();
                        User user = gson.fromJson(response.getJSONObject("data").toString(), User.class);
                        SessionManager.setUser(gson.toJson(user));
                        SessionManager.setIsLogin();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);


                        if (user.getBrand() == null || user.getModel() == null || user.getYear() == null || user.getVehicle_no() == null || user.getColor() == null) {
                            intent.putExtra("go", "vehicle");
                            startActivity(intent);
                        } else if (user.getLicence() == null || user.getInsurance() == null || user.getPermit() == null || user.getRegisteration() == null) {
                            intent.putExtra("go","doc");
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        }

                        Toast.makeText(LoginActivity.this, getString(R.string.success), Toast.LENGTH_LONG).show();

                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, response.getString("data"), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {


                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }


    public void bindView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        as = (TextView) findViewById(R.id.as);
        txt_createaccount = (TextView) findViewById(R.id.txt_createaccount);
        input_email = (EditText) findViewById(R.id.input_email);
        input_password = (EditText) findViewById(R.id.input_password);
        relative_register = (RelativeLayout) findViewById(R.id.relative_register);
        login = (AppCompatButton) findViewById(R.id.login);
        forgot_password = (TextView) findViewById(R.id.txt_forgotpassword);
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.haveNetworkConnection(getApplicationContext())) {
                    changepassword_dialog();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.network), Toast.LENGTH_LONG).show();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    public Boolean validate() {
        Boolean value = true;
        if (input_email.getText().toString().equals("") && !android.util.Patterns.EMAIL_ADDRESS.matcher(input_email.getText().toString().trim()).matches()) {
            value = false;
            input_email.setError(getString(R.string.email_invalid));
        } else {
            input_email.setError(null);
        }

        if (input_password.getText().toString().trim().equals("")) {
            value = false;
            input_password.setError(getString(R.string.fiels_is_required));
        } else {
            input_password.setError(null);
        }
        return value;
    }

    public void applyfonts() {
        if (getCurrentFocus() != null) {
            SetCustomFont setCustomFont = new SetCustomFont();
            setCustomFont.overrideFonts(getApplicationContext(), getCurrentFocus());
        } else {
            Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Medium.otf");
            Typeface font1 = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
            input_email.setTypeface(font1);
            input_password.setTypeface(font1);
            login.setTypeface(font);
            txt_createaccount.setTypeface(font);
            forgot_password.setTypeface(font);
        }
    }

    public void resetPassword(String email, final Dialog dialog) {

        RequestParams params = new RequestParams();
        params.put("email", email);
        Server.post(Server.FORGOT_PASSWORD, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        String data = response.getString("data");
                        if (dialog != null) {
                            dialog.cancel();
                        }
                        Toast.makeText(LoginActivity.this, data, Toast.LENGTH_LONG).show();

                    } else {
                        String data = response.getString("data");
                        Toast.makeText(LoginActivity.this, data, Toast.LENGTH_LONG).show();


                    }
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(LoginActivity.this, getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);

            }
        });


    }

    public void changepassword_dialog() {
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_reset);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.message);
        final EditText email = (EditText) dialog.findViewById(R.id.input_email);

        AppCompatButton btn_change = (AppCompatButton) dialog.findViewById(R.id.btn_reset);
        AppCompatButton btn_cancel = (AppCompatButton) dialog.findViewById(R.id.btn_cancel);

        Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Medium.otf");
        Typeface font1 = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
        btn_change.setTypeface(font1);
        btn_cancel.setTypeface(font1);
        email.setTypeface(font);
        title.setTypeface(font);
        message.setTypeface(font);


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LoginActivity.this.getCurrentFocus();
                if (view != null) {
                    Utils.hideKeyboard(LoginActivity.this, view);
                }
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
                    // dialog.cancel();
                    resetPassword(email.getText().toString().trim(), dialog);

                } else {
                    email.setError(getString(R.string.email_invalid));
                    // Toast.makeText(LoginActivity.this, "email is invalid", Toast.LENGTH_LONG).show();
                }


            }
        });
        dialog.show();

    }
}
