package com.icanstudioz.taxi.acitivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.Server.Server;
import com.icanstudioz.taxi.custom.Utils;
import com.icanstudioz.taxi.fragement.AcceptedRequestFragment;
import com.icanstudioz.taxi.fragement.HomeFragment;
import com.icanstudioz.taxi.fragement.PaymentHistory;
import com.icanstudioz.taxi.fragement.ProfileFragment;
import com.icanstudioz.taxi.fragement.UploadDomentFragment;
import com.icanstudioz.taxi.fragement.VehicleInformationFragment;
import com.icanstudioz.taxi.pojo.PendingRequestPojo;
import com.icanstudioz.taxi.pojo.Tracking;
import com.icanstudioz.taxi.pojo.User;
import com.icanstudioz.taxi.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.thebrownarrow.permissionhelper.ActivityManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by android on 7/3/17.
 */

public class HomeActivity extends ActivityManagePermission implements NavigationView.OnNavigationItemSelectedListener, ProfileFragment.ProfileUpdateListener, ProfileFragment.UpdateListener, LocationEngineListener {
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    public Toolbar toolbar;
    private ImageView avatar;
    TextView is_online, username;
    Switch switchCompat;
    LinearLayout linearLayout;
    NavigationView navigationView;
    int[][] states = new int[][]{
            new int[]{-android.R.attr.state_checked},
            new int[]{android.R.attr.state_checked},
    };

    int[] thumbColors = new int[]{
            Color.RED,
            Color.GREEN,
    };
    String go = "";

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;


    PendingRequestPojo pojo;
    LocationEngine locationEngine;

    String[] permissions = {PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        if (SessionManager.isLoggedIn()) {
            BindView();
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("action")) {
                String action = intent.getStringExtra("action");
                AcceptedRequestFragment commonRequestFragment = new AcceptedRequestFragment();
                Bundle b = new Bundle();
                b.putString("status", action);
                commonRequestFragment.setArguments(b);
                changeFragment(commonRequestFragment, getString(R.string.requests));

            } else {
                if (intent != null && intent.hasExtra("go")) {
                    go = intent.getStringExtra("go");
                    if (!go.equals("") && go.equals("vehicle")) {
                        changeFragment(new VehicleInformationFragment(), getString(R.string.add_vehicleinfo));
                    } else if (!go.equals("") && go.equals("doc")) {
                        changeFragment(new UploadDomentFragment(), getString(R.string.upload_doc));
                    }
                } else {
                    navigationView.setCheckedItem(R.id.home);
                    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.home));
                }
            }

        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }


        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("KEY", SessionManager.getKEY() + "......");
                if (Utils.haveNetworkConnection(getApplicationContext())) {
                    DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getThumbDrawable()), new ColorStateList(states, thumbColors));

                    if (isChecked) {
                        is_online(SessionManager.getUserId(), "1", false);
                    } else {
                        is_online(SessionManager.getUserId(), "0", false);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_not_available), Toast.LENGTH_LONG).show();

                }
            }
        });
        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }

        callLocationEngine();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askCompactPermissions(permissions, new PermissionResult() {
                @Override
                public void permissionGranted() {
                    // getLocation();
                    setListener();
                }

                @Override
                public void permissionDenied() {

                }

                @Override
                public void permissionForeverDenied() {
                    openSettingsApp(getApplicationContext());
                }
            });

        } else {

            setListener();
            // getLocation();
        }


    }

    private void setupDrawer() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void drawer_close() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        AcceptedRequestFragment acceptedRequestFragment;
        Bundle bundle;
        switch (item.getItemId()) {
            case R.id.home:
                changeFragment(new HomeFragment(), getString(R.string.home));
                break;
            case R.id.pending_requests:
                acceptedRequestFragment = new AcceptedRequestFragment();
                bundle = new Bundle();
                bundle.putString("status", "PENDING");
                acceptedRequestFragment.setArguments(bundle);
                changeFragment(acceptedRequestFragment, getString(R.string.requests));
                break;
            case R.id.accepted_requests:
                acceptedRequestFragment = new AcceptedRequestFragment();
                bundle = new Bundle();
                bundle.putString("status", "ACCEPTED");
                acceptedRequestFragment.setArguments(bundle);
                changeFragment(acceptedRequestFragment, getString(R.string.requests));
                break;
            case R.id.completed_rides:
                acceptedRequestFragment = new AcceptedRequestFragment();
                bundle = new Bundle();
                bundle.putString("status", "COMPLETED");
                acceptedRequestFragment.setArguments(bundle);
                changeFragment(acceptedRequestFragment, getString(R.string.requests));
                break;
            case R.id.cancelled:
                acceptedRequestFragment = new AcceptedRequestFragment();
                bundle = new Bundle();
                bundle.putString("status", "CANCELLED");
                acceptedRequestFragment.setArguments(bundle);
                changeFragment(acceptedRequestFragment, getString(R.string.requests));
                break;
            case R.id.vehicle_information:
                changeFragment(new VehicleInformationFragment(), getString(R.string.vehicle_info));
                break;
            case R.id.payment_detail:
                changeFragment(new PaymentHistory(), getString(R.string.payment_history));
                break;
            case R.id.profile:
                changeFragment(new ProfileFragment(), getString(R.string.profile));
                break;
            case R.id.logout:
                is_online(SessionManager.getUserId(), "0", true);

                break;
            default:
                break;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
    }

    public void changeFragment(final Fragment fragment, final String fragmenttag) {
        try {
            drawer_close();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().addToBackStack(null);
            fragmentTransaction.replace(R.id.frame, fragment, fragmenttag);
            fragmentTransaction.commit();
        } catch (Exception e) {
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null && pojo != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            setStatus(pojo, "", false);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationEngine != null) {
            locationEngine.activate();
        }
    }

    private void callLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.setFastestInterval(5);
        locationEngine.setInterval(10);
        locationEngine.setSmallestDisplacement(5);
        locationEngine.activate();


    }

    @SuppressLint("ParcelCreator")
    public class CustomTypefaceSpan extends TypefaceSpan {

        private final Typeface newType;

        public CustomTypefaceSpan(String family, Typeface type) {
            super(family);
            newType = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            applyCustomTypeFace(ds, newType);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            applyCustomTypeFace(paint, newType);
        }

        private void applyCustomTypeFace(Paint paint, Typeface tf) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            if (old == null) {
                oldStyle = 0;
            } else {
                oldStyle = old.getStyle();
            }

            int fake = oldStyle & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }

            paint.setTypeface(tf);
        }
    }


    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Medium.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    public void fontToTitleBar(String title) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
        title = "<font color='#000000'>" + title + "</font>";
        SpannableString s = new SpannableString(title);
        s.setSpan(font, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            toolbar.setTitle(Html.fromHtml(String.valueOf(s), Html.FROM_HTML_MODE_LEGACY));
        } else {
            toolbar.setTitle((Html.fromHtml(String.valueOf(s))));
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawer_close();
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }

    }


    public void initViews() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        switchCompat = (Switch) navigationView.getHeaderView(0).findViewById(R.id.online);
        avatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile);
        linearLayout = (LinearLayout) navigationView.getHeaderView(0).findViewById(R.id.linear);
        is_online = (TextView) navigationView.getHeaderView(0).findViewById(R.id.is_online);
        username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_name);
        TextView v = (TextView) navigationView.getHeaderView(0).findViewById(R.id.version);

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            v.setText("V ".concat(version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void BindView() {
        initViews();

        setupDrawer();
        Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
        username.setTypeface(font);
        is_online.setTypeface(font);
        toolbar.setTitle("");
        if (Utils.haveNetworkConnection(getApplicationContext())) {
            getInfo();

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.network_not_available), Toast.LENGTH_LONG).show();

            Glide.with(HomeActivity.this).load(SessionManager.getAvatar()).apply(new RequestOptions().error(R.drawable.user_default)).into(avatar);
            username.setText(SessionManager.getName());
            switchCompat.setChecked(false);
            DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getThumbDrawable()), new ColorStateList(states, thumbColors));

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (pojo != null && locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
        }


    }

    @Override
    public void update(String url) {
        if (!url.equals("")) {
            Glide.with(getApplicationContext()).load(url).apply(new RequestOptions().error(R.drawable.user_default)).into(avatar);
        }
    }

    @Override
    public void name(String name) {
        if (!name.equals("")) {
            username.setText(name);
        }
    }

    public void is_online(String user_id, String status, Boolean what) {
        RequestParams params = new RequestParams();
        params.put("user_id", user_id);
        params.put("is_online", status);
        Server.setHeader(SessionManager.getKEY());
        Server.post(Server.UPDATE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        String status = response.getJSONObject("data").getString("is_online");

                        if (what) {
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                            SessionManager.logoutUser(HomeActivity.this);
                            finish();
                        } else {
                            if (status.equals("1")) {
                                SessionManager.setStatus("true");
                                is_online.setText(getResources().getString(R.string.online));
                                switchCompat.setChecked(true);
                            } else {
                                SessionManager.setStatus("false");
                                is_online.setText(getResources().getString(R.string.offline));
                                switchCompat.setChecked(false);
                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {

                    Toast.makeText(getApplicationContext(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Log.e("FAIl", throwable.toString() + ".." + errorResponse);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e("FAIl", throwable.toString() + ".." + errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("FAIl", throwable.toString() + ".." + responseString);
            }

            @Override
            public void onFinish() {
                super.onFinish();

            }
        });

    }

    public void getInfo() {
        RequestParams params = new RequestParams();
        params.put("user_id", SessionManager.getUserId());
        username.setText(SessionManager.getName());
        Glide.with(getApplicationContext()).load(SessionManager.getAvatar()).apply(new RequestOptions().error(R.drawable.user_default)).into(avatar);

        Server.setHeader(SessionManager.getKEY());
        Server.get(Server.GET_PROFILE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("success", response.toString());
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        Gson gson = new Gson();
                        User user = gson.fromJson(response.getJSONObject("data").toString(), User.class);
                        user.setKey(SessionManager.getKEY());
                        SessionManager.setUser(gson.toJson(user));

                        username.setText(user.getName());
                        Glide.with(getApplicationContext()).load(user.getAvatar()).apply(new RequestOptions().error(R.drawable.user_default)).into(avatar);
                        if (response.getJSONObject("data").getString("is_online").equalsIgnoreCase("1")) {
                            switchCompat.setChecked(true);
                            is_online.setText(getResources().getString(R.string.online));
                            SessionManager.setStatus("true");
                            DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getThumbDrawable()), new ColorStateList(states, thumbColors));
                        } else {
                            switchCompat.setChecked(true);
                            DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getThumbDrawable()), new ColorStateList(states, thumbColors));
                        }


                    } else {

                    }
                } catch (JSONException e) {

                }
            }
        });
    }

    public void setPojo(PendingRequestPojo pojo) {
        this.pojo = pojo;
    }

    public void setStatus(PendingRequestPojo pojo, String status, boolean what) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Tracking/" + pojo.getRide_id());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if (dataSnapshot != null && dataSnapshot.hasChildren()) {
                        Tracking tracking = dataSnapshot.getValue(Tracking.class);
                        tracking.setClient_id(pojo.getUser_id());
                        tracking.setDriver_id(pojo.getDriver_id());
                        tracking.setRide_id(pojo.getRide_id());
                        tracking.setDriver_latitude(latitude);
                        tracking.setDriver_longitude(longitude);
                        if (what) {
                            tracking.setStatus(status);
                        }
                        reference.setValue(tracking);
                    } else {
                        Tracking tracking1 = new Tracking();
                        tracking1.setClient_id(pojo.getUser_id());
                        tracking1.setDriver_id(pojo.getDriver_id());
                        tracking1.setRide_id(pojo.getRide_id());
                        tracking1.setDriver_latitude(latitude);
                        tracking1.setDriver_longitude(longitude);
                        if (what) {
                            tracking1.setStatus(status);
                        }
                        reference.setValue(tracking1);
                    }
                } catch (Exception e) {

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setListener() {
        locationEngine.addLocationEngineListener(this);
    }


}
