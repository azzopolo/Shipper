package com.icanstudioz.taxi.fragement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.Server.Server;
import com.icanstudioz.taxi.acitivities.HomeActivity;
import com.icanstudioz.taxi.custom.GPSTracker;
import com.icanstudioz.taxi.custom.LocationService;
import com.icanstudioz.taxi.pojo.PendingRequestPojo;
import com.icanstudioz.taxi.pojo.Tracking;
import com.icanstudioz.taxi.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.thebrownarrow.permissionhelper.FragmentManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by android on 14/3/17.
 */

public class AcceptedDetailFragment extends FragmentManagePermission {
    AppCompatButton trackRide, complete, cancel, approve, accept;
    TextView title, drivername, mobilenumber, pickup_location, drop_location, fare, payment_status;
    String request = "";
    String permissions[] = {PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};

    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    private View view;
    private String pickup = "";
    private String drop = "";
    private String driver = "";
    private String basefare = "";
    private SwipeRefreshLayout swipeRefreshLayout;
    private String mobile = "";
    private String ride_id = "";
    private String paymnt_status = "";
    private String paymnt_mode = "";
    LinearLayout linearChat;
    TableRow mobilenumber_row;
    private String user_id;
    PendingRequestPojo pojo;

    GPSTracker gpsTracker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.accepted_detail_fragmnet, container, false);
        ((HomeActivity) getActivity()).fontToTitleBar(getString(R.string.passanger_info));
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BindView();

       /* trackRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pojo.getPikup_location().equals("") && !pojo.getDrop_locatoin().equals("")) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", pojo);
                    MapView mapView = new MapView();
                    mapView.setArguments(bundle);
                    ((HomeActivity) getActivity()).changeFragment(mapView, getString(R.string.track_ride));
                } else {
                    Toast.makeText(getActivity(), getString(R.string.invalid_location), Toast.LENGTH_LONG).show();
                }
            }
        });*/

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogCreate(getString(R.string.ride_completion), getString(R.string.ride_complete_msg), "COMPLETED");
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogCreate(getString(R.string.ride_cancellation), getString(R.string.ride_cancel_msg), "CANCELLED");
            }
        });
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogCreate(getString(R.string.ride_acceptance), getString(R.string.ride_accept_msg), "ACCEPTED");

            }
        });
    }

    public void AlertDialogCreate(String title, String message, final String status) {
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.mipmap.ic_warning_white_24dp);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, Color.RED);
        new AlertDialog.Builder(getActivity())
                .setIcon(drawable)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getString(R.string.ccancel), null)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        SendStatus(ride_id, status);

                    }
                })
                .setNegativeButton(getString(R.string.ccancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    public void BindView() {
        gpsTracker = new GPSTracker(getActivity());
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        linearChat = (LinearLayout) view.findViewById(R.id.linear_chat);
        accept = (AppCompatButton) view.findViewById(R.id.btn_accept);
        complete = (AppCompatButton) view.findViewById(R.id.btn_complete);
        approve = (AppCompatButton) view.findViewById(R.id.btn_approve);
        cancel = (AppCompatButton) view.findViewById(R.id.btn_cancel);
        trackRide = (AppCompatButton) view.findViewById(R.id.btn_trackride);
        title = (TextView) view.findViewById(R.id.title);
        drivername = (TextView) view.findViewById(R.id.txt_drivername);
        mobilenumber = (TextView) view.findViewById(R.id.txt_mobilenumber);
        payment_status = (TextView) view.findViewById(R.id.txt_paymentstatus);
        pickup_location = (TextView) view.findViewById(R.id.txt_pickuplocation);
        drop_location = (TextView) view.findViewById(R.id.txt_droplocation);
        fare = (TextView) view.findViewById(R.id.txt_basefare);
        mobilenumber_row = view.findViewById(R.id.mobilenumber_row);
        pickup_location.setSelected(true);
        drop_location.setSelected(true);
        Bundle bundle = getArguments();
        if (bundle != null) {
            pojo = (PendingRequestPojo) bundle.getSerializable("data");
            title.setText(getString(R.string.taxi));
            pickup = pojo.getPickup_adress();
            drop = pojo.getDrop_address();
            driver = pojo.getUser_name();
            basefare = pojo.getAmount();
            ride_id = pojo.getRide_id();
            user_id = pojo.getUser_id();
            mobile = pojo.getUser_mobile();
            paymnt_status = pojo.getPayment_status();
            paymnt_mode = pojo.getPayment_mode();
            if (pickup != null) {
                pickup_location.setText(pickup);
            }
            if (drop != null) {
                drop_location.setText(drop);
            }
            if (driver != null) {
                drivername.setText(driver);
            }
            if (fare != null) {
                fare.setText(basefare + " " + SessionManager.getUnit());
            }
            if (mobile != null) {
                mobilenumber.setText(mobile);
            }
            if (paymnt_mode == null) {
                paymnt_mode = "";
            }
            if (ride_id != null) {

            } else {
                ride_id = "";
            }
            request = pojo.getStatus();

            if (!request.equals("") && request.equalsIgnoreCase("PENDING")) {
                cancel.setVisibility(View.VISIBLE);
                accept.setVisibility(View.VISIBLE);
            }
            if (request != null && !request.equals("") && request.equalsIgnoreCase("CANCELLED")) {
                trackRide.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                accept.setVisibility(View.GONE);
                approve.setVisibility(View.GONE);
                complete.setVisibility(View.GONE);
                payment_status.setText(pojo.getPayment_status());
            }
            if (request != null && !request.equals("") && request.equalsIgnoreCase("COMPLETED")) {
                trackRide.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                accept.setVisibility(View.GONE);
                approve.setVisibility(View.GONE);
                complete.setVisibility(View.GONE);
                payment_status.setText(pojo.getPayment_status());
            }
        }

        if (!request.equals("") && request.equalsIgnoreCase("ACCEPTED")) {
            isStarted();
            if (paymnt_mode.equals("OFFLINE") && !paymnt_status.equals("PAID")) {
                payment_status.setText(R.string.coh_driver);
                approve.setVisibility(View.VISIBLE);
                complete.setVisibility(View.GONE);
                approve.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        approvePaymet();
                    }
                });
            } else {
                approve.setVisibility(View.GONE);
                payment_status.setText(paymnt_status);
                complete.setVisibility(View.VISIBLE);
            }

            trackRide.setVisibility(View.VISIBLE);

            if (pojo.getPayment_status().equals("") && pojo.getPayment_mode().equals("")) {
                payment_status.setText(R.string.unpaid);
                complete.setVisibility(View.GONE);
                cancel.setVisibility(View.VISIBLE);
            }
            if (paymnt_mode.equals("OFFLINE") && paymnt_status.equals("PAID")) {
                payment_status.setText(R.string.payment_receive_from_customer);
            }



       /* linearChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putString("name", driver);
                b.putString("id", ride_id);
                b.putString("user_id", user_id);
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setArguments(b);
                ((HomeActivity) getActivity()).changeFragment(chatFragment, "Messages");
            }
        });*/


        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        mobilenumber_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCompactPermission(PermissionUtils.Manifest_CALL_PHONE, new PermissionResult() {
                    @Override
                    public void permissionGranted() {
                        if (mobile != null && !mobile.equals("")) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + mobile));
                            startActivity(callIntent);
                        }
                    }

                    @Override
                    public void permissionDenied() {

                    }

                    @Override
                    public void permissionForeverDenied() {

                    }
                });
            }
        });
    }

    private void approvePaymet() {
        RequestParams params = new RequestParams();
        params.put("ride_id", ride_id);
        params.put("payment_status", "PAID");
        Server.setHeader(SessionManager.getKEY());
        Server.setContentType();
        Server.post(Server.APPROVE_PAYMENT, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                approve.setVisibility(View.GONE);
                payment_status.setText("PAID");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void SendStatus(String ride_id, final String status) {
        RequestParams params = new RequestParams();
        params.put("ride_id", ride_id);
        params.put("status", status);
        Server.setHeader(SessionManager.getKEY());
        Server.setContentType();
        Server.post(Server.STATUS_CHANGE, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    AcceptedRequestFragment acceptedRequestFragment = new AcceptedRequestFragment();
                    Bundle bundle;
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        if (status.equalsIgnoreCase("COMPLETED")) {
                            bundle = new Bundle();
                            bundle.putString("status", "COMPLETED");
                            acceptedRequestFragment.setArguments(bundle);
                            ((HomeActivity) getActivity()).changeFragment(acceptedRequestFragment, getString(R.string.requests));
                            Toast.makeText(getActivity(), getString(R.string.ride_reuest_completed), Toast.LENGTH_LONG).show();
                        } else if (status.equalsIgnoreCase("ACCEPTED")) {
                            startService();

                            bundle = new Bundle();
                            bundle.putString("status", "ACCEPTED");
                            acceptedRequestFragment.setArguments(bundle);
                            ((HomeActivity) getActivity()).setPojo(pojo);

                            ((HomeActivity) getActivity()).setStatus(pojo, "accepted", true);
                            ((HomeActivity) getActivity()).changeFragment(acceptedRequestFragment, getString(R.string.requests));
                            Toast.makeText(getActivity(), getString(R.string.ride_reuest_accepted), Toast.LENGTH_LONG).show();
                        } else {
                            bundle = new Bundle();
                            bundle.putString("status", "CANCELLED");
                            acceptedRequestFragment.setArguments(bundle);
                            ((HomeActivity) getActivity()).changeFragment(acceptedRequestFragment, getString(R.string.requests));
                            Toast.makeText(getActivity(), getString(R.string.ride_reuest_cancelled), Toast.LENGTH_LONG).show();
                        }

                    } else {
                        String data = response.getJSONObject("data").toString();
                        Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();
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

    public void startService() {
        Intent myIntent = new Intent(getActivity(), LocationService.class);
        pendingIntent = PendingIntent.getService(getActivity(), 0, myIntent, 0);
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 60); // first time
        long frequency = 60 * 1000; // in ms
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);
    }

    public Boolean GPSEnable() {
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        if (gpsTracker.canGetLocation()) {
            return true;

        } else {
            gpsTracker.showSettingsAlert();
            return false;
        }


    }

    void isStarted() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Tracking/" + pojo.getRide_id());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.hasChildren()) {
                    Tracking tracking = dataSnapshot.getValue(Tracking.class);
                    if (tracking.getStatus().equalsIgnoreCase("accepted")) {
                        trackRide.setText(getString(R.string.Pick_Customer));

                        trackRide.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    askCompactPermissions(permissions, new PermissionResult() {
                                        @Override
                                        public void permissionGranted() {
                                            gotoMap();
                                        }

                                        @Override
                                        public void permissionDenied() {

                                        }

                                        @Override
                                        public void permissionForeverDenied() {

                                        }
                                    });
                                } else {
                                    gotoMap();

                                }
                            }
                        });

                    } else if (tracking.getStatus().equalsIgnoreCase("started")) {
                        trackRide.setText(getString(R.string.track_ride));

                        trackRide.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                askCompactPermissions(permissions, new PermissionResult() {
                                    @Override
                                    public void permissionGranted() {
                                        launchNavigation();
                                    }

                                    @Override
                                    public void permissionDenied() {

                                    }

                                    @Override
                                    public void permissionForeverDenied() {

                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void launchNavigation() {


        if (GPSEnable()) {

            try {
                String[] latlong = pojo.getPikup_location().split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);
                String[] latlong1 = pojo.getDrop_locatoin().split(",");
                double latitude1 = Double.parseDouble(latlong1[0]);
                double longitude1 = Double.parseDouble(latlong1[1]);


// Create a NavigationViewOptions object to package everything together
                Point origin = Point.fromLngLat(longitude, latitude);
                Point destination = Point.fromLngLat(longitude1, latitude1);

                fetchRoute(origin, destination);
           /*     NavigationLauncherOptions.Builder navigationLauncherOptions = NavigationLauncherOptions.builder();
                navigationLauncherOptions.origin(origin);
                navigationLauncherOptions.destination(destination);
                navigationLauncherOptions.shouldSimulateRoute(false);
                navigationLauncherOptions.enableOffRouteDetection(true);
                navigationLauncherOptions.snapToRoute(true);
                                *//*NavigationLauncher.startNavigation(getActivity(), o, d,
                                        null, false);*//*
                NavigationLauncher.startNavigation(getActivity(), navigationLauncherOptions.build());
           */
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.toString() + " ", Toast.LENGTH_SHORT).show();
            }
        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    private void fetchRoute(Point origin, Point destination) {
        NavigationRoute.builder(getActivity())
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        DirectionsRoute directionsRoute = response.body().routes().get(0);
                        startNavigation(directionsRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });
    }

    private void startNavigation(DirectionsRoute directionsRoute) {
        NavigationLauncherOptions.Builder navigationLauncherOptions = NavigationLauncherOptions.builder();
        navigationLauncherOptions.shouldSimulateRoute(false);
        navigationLauncherOptions.enableOffRouteDetection(true);
        navigationLauncherOptions.snapToRoute(true);
        navigationLauncherOptions.directionsRoute(directionsRoute);
        NavigationLauncher.startNavigation(getActivity(), navigationLauncherOptions.build());


    }

    private void gotoMap() {
        if (GPSEnable()) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("data", pojo);
            MapView mapView = new MapView();
            mapView.setArguments(bundle);
            ((HomeActivity) getActivity()).changeFragment(mapView, "MapView");
        } else {
            gpsTracker.showSettingsAlert();
        }
    }


}
