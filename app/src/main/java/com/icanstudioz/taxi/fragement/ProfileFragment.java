package com.icanstudioz.taxi.fragement;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.Server.Server;
import com.icanstudioz.taxi.acitivities.HomeActivity;
import com.icanstudioz.taxi.custom.Utils;
import com.icanstudioz.taxi.pojo.User;
import com.icanstudioz.taxi.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thebrownarrow.permissionhelper.FragmentManagePermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;
import gun0912.tedbottompicker.TedBottomPicker;

/**
 * Created by android on 14/3/17.
 */
public class ProfileFragment extends FragmentManagePermission {
    private View view;
    private File imageFile;
    private ProfileUpdateListener profileUpdateListener;
    private UpdateListener listener;

    private EditText input_email, input_vehicle, input_name, input_paypalId, input_mobile;
    private AppCompatButton btn_update, btn_change;
    ImageView profile_pic;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.profile_fragment, container, false);

        ((HomeActivity) getActivity()).fontToTitleBar(getString(R.string.profile));
        bindView();

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    Utils.hideKeyboard(getActivity(), view);
                }
                if (Utils.haveNetworkConnection(getActivity())) {
                    Server.setHeader(SessionManager.getKEY());
                    if (validate()) {

                        UpdateUser();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
                }

            }
        });
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int MyVersion = Build.VERSION.SDK_INT;
                if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (!checkIfAlreadyhavePermission()) {
                        requestForSpecificPermission();
                    } else {
                        TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                    @Override
                                    public void onImageSelected(Uri uri) {
                                        // here is selected uri
                                        imageFile = new File(uri.getPath());
                                        String format = getMimeType(getActivity(), uri);
                                        upload_pic(format);

                                    }
                                }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                    @Override
                                    public void onError(String message) {
                                        Toast.makeText(getActivity(), getString(R.string.tryagian), Toast.LENGTH_LONG).show();
                                        Log.d(getTag(), message);
                                    }
                                })
                                .create();

                        tedBottomPicker.show(getActivity().getSupportFragmentManager());
                    }
                } else {
                    TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                            .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                @Override
                                public void onImageSelected(Uri uri) {

                                    imageFile = new File(uri.getPath());
                                    String format = getMimeType(getActivity(), uri);
                                    upload_pic(format);

                                }
                            }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                @Override
                                public void onError(String message) {
                                    Toast.makeText(getActivity(), getString(R.string.tryagian), Toast.LENGTH_LONG).show();
                                    Log.d(getTag(), message);
                                }
                            })
                            .create();

                    tedBottomPicker.show(getActivity().getSupportFragmentManager());
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            profileUpdateListener = (ProfileUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
        try {
            listener = (UpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public void upload_pic(String type) {
        progressBar.setVisibility(View.VISIBLE);
        RequestParams params = new RequestParams();
        if (imageFile != null) {
            try {

                if (type.equals("jpg")) {
                    params.put("avatar", imageFile, "image/jpeg");
                } else if (type.equals("jpeg")) {
                    params.put("avatar", imageFile, "image/jpeg");
                } else if (type.equals("png")) {
                    params.put("avatar", imageFile, "image/png");
                } else {
                    params.put("avatar", imageFile, "image/gif");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("catch", e.toString());
            }
        }
        Server.setHeader(SessionManager.getKEY());
        params.put("user_id", SessionManager.getUserId());

        Server.post(Server.UPDATE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("success", response.toString());
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        String url = response.getJSONObject("data").getString("avatar");

                        try {
                            Glide.with(getActivity()).load(url).apply(new RequestOptions().error(R.drawable.user_default)).into(profile_pic);
                        } catch (Exception e) {

                        }
                        User user = SessionManager.getUser();
                        user.setAvatar(url);
                        Gson gson = new Gson();
                        SessionManager.setUser(gson.toJson(user));
                        profileUpdateListener.update(url);
                        input_name.setText(user.getName());
                        input_email.setText(user.getEmail());
                        input_mobile.setText(user.getMobile());
                        input_vehicle.setText(user.getVehicle_info());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        if (response.has("data")) {
                            Toast.makeText(getActivity(), response.getString("data"), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

            }
        });

    }

    private void getUserInfoOnline() {
        RequestParams params = new RequestParams();
        params.put("user_id", SessionManager.getUserId());
        User user = SessionManager.getUser();

        Glide.with(getActivity()).load(user.getAvatar()).apply(new RequestOptions().error(R.drawable.user_default)).into(profile_pic);
        input_name.setText(user.getName());
        input_email.setText(user.getEmail());
        input_vehicle.setText(user.getVehicle_info());
        input_mobile.setText(user.getMobile());
        input_paypalId.setText(user.getPaypal_id());
        Server.setHeader(user.getKey());
        Server.get(Server.GET_PROFILE, params, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        User user1 = gson.fromJson(response.getJSONObject("data").toString(), User.class);

                        Glide.with(ProfileFragment.this).load(user1.getAvatar()).apply(new RequestOptions().error(R.drawable.user_default)).into(profile_pic);
                        input_name.setText(user1.getName());
                        input_email.setText(user1.getEmail());
                        input_vehicle.setText(user1.getVehicle_info());
                        input_mobile.setText(user1.getMobile());
                        input_paypalId.setText(user1.getPaypal_id());
                        user1.setKey(SessionManager.getKEY());

                        SessionManager.setUser(gson.toJson(user1));
                        profileUpdateListener.update(user1.getAvatar());
                        listener.name(user1.getName());


                    } else {
                        Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {

                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void getUserInfoOffline() {
        input_name.setText(SessionManager.getUser().getName());
        input_email.setText(SessionManager.getUser().getEmail());
        input_mobile.setText(SessionManager.getUser().getMobile());
        input_vehicle.setText(SessionManager.getUser().getVehicle_info());
        Glide.with(getActivity()).load(SessionManager.getUser().getAvatar()).apply(new RequestOptions().error(R.drawable.user_default)).into(profile_pic);
        input_paypalId.setText(SessionManager.getUser().getPaypal_id());
    }


    public Boolean validate() {
        Boolean value = true;

        if (input_name.getText().toString().trim().equals("")) {
            input_name.setError(getString(R.string.fiels_is_required));
            value = false;
        } else {
            input_name.setError(null);
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input_email.getText().toString().trim()).matches()) {
            input_email.setError(getString(R.string.email_invalid));
            value = false;
        } else {
            input_email.setError(null);
        }
        if (input_mobile.length() != 10) {
            input_mobile.setError(getString(R.string.mobile_invalid));
            value = false;
        } else {
            input_mobile.setError(null);
        }
        if (input_vehicle.getText().toString().trim().equals("")) {
            value = false;
            input_vehicle.setError(getString(R.string.fiels_is_required));
        } else {
            input_vehicle.setError(null);
        }
        return value;
    }

    public void bindView() {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        profile_pic = (ImageView) view.findViewById(R.id.profile_pic);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        input_email = (EditText) view.findViewById(R.id.input_email);
        input_vehicle = (EditText) view.findViewById(R.id.input_vehicle);
        input_name = (EditText) view.findViewById(R.id.input_name);
        // input_password = (EditText) view.findViewById(R.id.input_password);
        input_mobile = (EditText) view.findViewById(R.id.input_mobile);
        input_paypalId = (EditText) view.findViewById(R.id.input_paypal_id);
        btn_update = (AppCompatButton) view.findViewById(R.id.btn_update);
        btn_change = (AppCompatButton) view.findViewById(R.id.btn_change);


        MediumFont(input_vehicle);
        MediumFont(input_name);
        MediumFont(input_paypalId);
        MediumFont(input_email);
        MediumFont(input_mobile);
        BookFont(btn_update);
        BookFont(btn_change);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);

            }
        });

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.haveNetworkConnection(getActivity())) {
                    changepassword_dialog(getString(R.string.change_password));
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();

                }
            }
        });
        if (Utils.haveNetworkConnection(getActivity())) {
            getUserInfoOnline();
        } else {
            Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
            getUserInfoOffline();
        }
    }

    public void BookFont(AppCompatButton view1) {
        Typeface font1 = Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf");
        view1.setTypeface(font1);
    }

    public void MediumFont(EditText view) {
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf");
        view.setTypeface(font);
    }

    public void MediumFont(TextView view) {
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf");
        view.setTypeface(font);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                            .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                @Override
                                public void onImageSelected(Uri uri) {
                                    // here is selected uri
                                    profile_pic.setImageURI(uri);
                                }
                            }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                @Override
                                public void onError(String message) {
                                    Toast.makeText(getActivity(), getString(R.string.tryagian), Toast.LENGTH_SHORT).show();
                                    Log.d(getTag(), message);
                                }
                            })
                            .create();

                    tedBottomPicker.show(getActivity().getSupportFragmentManager());

                } else {

                }
            }
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    private boolean checkIfAlreadyhavePermission() {
        int fine = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        int read = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (fine == PackageManager.PERMISSION_GRANTED) {
            return true;

        }
        if (read == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (write == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void UpdateUser() {
        RequestParams params = new RequestParams();
        params.put("mobile", input_mobile.getText().toString().trim());
        params.put("name", input_name.getText().toString().trim());
        params.put("vehicle_info", input_vehicle.getText().toString().trim());
        params.put("paypal_id", input_paypalId.getText().toString().trim());
        Server.setHeader(SessionManager.getKEY());


        params.put("user_id", SessionManager.getUserId());
        Server.post(Server.UPDATE, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("success", response.toString());

                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        String name = input_name.getText().toString().trim();
                        String email = input_email.getText().toString().trim();
                        String mobile = input_mobile.getText().toString().trim();
                        String vehicle = input_vehicle.getText().toString().trim();
                        String paypal_id = input_paypalId.getText().toString().trim();

                        input_name.setText(name);
                        input_email.setText(email);
                        input_mobile.setText(mobile);
                        input_vehicle.setText(vehicle);
                        input_paypalId.setText(paypal_id);

                        User user = SessionManager.getUser();
                        Gson gson = new Gson();
                        user.setName(name);
                        user.setVehicle_info(vehicle);
                        user.setPaypal_id(paypal_id);

                        SessionManager.setUser(gson.toJson(user));

                        listener.name(user.getName());

                        Toast.makeText(getActivity(), getString(R.string.profile_updated), Toast.LENGTH_LONG).show();

                    } else {

                        Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    public void onResume() {
        super.onResume();

    }


    public interface ProfileUpdateListener {
        void update(String url);

    }

    public interface UpdateListener {
        void name(String name);

    }

    public void changepassword_dialog(String title) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.changepassword_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        TextView tle = (TextView) dialog.findViewById(R.id.title);
        final EditText password = (EditText) dialog.findViewById(R.id.input_Password);
        final EditText confirm_password = (EditText) dialog.findViewById(R.id.input_confirmPassword);
        AppCompatButton btn_change = (AppCompatButton) dialog.findViewById(R.id.change_password);
        overrideFonts(getActivity(), dialog.getCurrentFocus());
        MediumFont(tle);
        MediumFont(password);
        MediumFont(confirm_password);
        BookFont(btn_change);
        tle.setText(title);
        btn_change.setText(getString(R.string.change));


        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldpassword = password.getText().toString().trim();
                String confirmpassword = confirm_password.getText().toString().trim();
                if (password.getText().toString().trim().equals("")) {
                    password.setError(getString(R.string.password_required));
                } else if (!confirm_password.getText().toString().trim().equals("")) {

                    changepassword(dialog, SessionManager.getUserId(), oldpassword, confirmpassword);

                } else {

                    confirm_password.setError(getString(R.string.newpwd_required));
                }

            }
        });
        dialog.show();

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
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf"));
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf"));
            }

        } catch (Exception e) {
        }
    }

    public void changepassword(final Dialog dialog, String id, String oldpassword, String newpassword) {
        RequestParams params = new RequestParams();
        params.put("old_password", oldpassword);
        params.put("new_password", newpassword);
        params.put("user_id", id);
        Server.setHeader(SessionManager.getKEY());
        Server.post(Server.PASSWORD_RESET, params, new JsonHttpResponseHandler() {
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
                        dialog.cancel();
                        Toast.makeText(getActivity(), getString(R.string.password_updated), Toast.LENGTH_LONG).show();

                    } else {
                        String error = response.getString("data");
                        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }
}
