package com.icanstudioz.taxi.fragement;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.acitivities.HomeActivity;
import com.icanstudioz.taxi.custom.GPSTracker;
import com.icanstudioz.taxi.custom.Utils;
import com.icanstudioz.taxi.pojo.PendingRequestPojo;
import com.icanstudioz.taxi.pojo.Tracking;
import com.thebrownarrow.permissionhelper.FragmentManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by android on 10/3/17.
 */

public class MapView extends FragmentManagePermission implements OnMapReadyCallback, DirectionCallback, GoogleMap.OnMapLoadedCallback, Animation.AnimationListener, LocationEngineListener {
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};
    GoogleMap myMap;
    com.google.android.gms.maps.MapView mMapView;
    private View view;
    PendingRequestPojo pojo;

    private LatLng origin = new LatLng(21.7051, 72.9959);
    private LatLng destination = new LatLng(23.0225, 72.5714);
    private LatLng camera = new LatLng(21.7051, 72.9959);
    private String pickup = "";
    private String drop = "";
    private String serverKey = "AIzaSyAlBu8MsC7jxJ68rpRR722Ojl_HQiWpnhQ";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Double currentLatitude;
    private Double currentLongitude;
    private Marker marker;
    LocationEngine locationEngine;
    TextView startRide;
    private Location location;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.mapview, container, false);
        ((HomeActivity) getActivity()).fontToTitleBar("Track Ride");
        mMapView = (com.google.android.gms.maps.MapView) view.findViewById(R.id.mapview);
        startRide = (TextView) view.findViewById(R.id.txt_start);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            pojo = (PendingRequestPojo) bundle.getSerializable("data");
            String[] latlong = pojo.getPikup_location().split(",");
            origin = new LatLng(Double.parseDouble(latlong[0]), Double.parseDouble(latlong[1]));
            String[] latlong1 = pojo.getDrop_locatoin().split(",");
            destination = new LatLng(Double.parseDouble(latlong1[0]), Double.parseDouble(latlong1[1]));
            pickup = pojo.getPickup_adress();
            drop = pojo.getDrop_address();

        }
        callLocationEngine();

        if (!Utils.haveNetworkConnection(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
        }

        startRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeStatus();

            }
        });


        return view;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        try {
            if (direction.isOK()) {
                ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                myMap.addPolyline(DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.RED));
            }
        } catch (Exception e) {

        }
        myMap.addMarker(new MarkerOptions().position(origin).snippet(pickup).title(getString(R.string.pick_up_location)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        myMap.addMarker(new MarkerOptions().snippet(drop).position(destination).title(getString(R.string.drop_up_location)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        //setCurrentLocation(origin.latitude,origin.longitude);
    }

    @Override
    public void onDirectionFailure(Throwable t) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;
        myMap.setMaxZoomPreference(80);
        requestDirection();
        //  setCurrentLocation(origin, destination);
        // googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 25));
        if (!GPSEnable()) {
            //tunonGps();
        }
    }

    public void requestDirection() {

        try {
            Snackbar.make(view, getString(R.string.direct_requesting), Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {

        }
        GoogleDirection.withServerKey(getString(R.string.google_android_map_api_key))
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
        }

    }

    private void setListener() {
        locationEngine.addLocationEngineListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        if (locationEngine != null) {
            locationEngine.activate();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public Boolean GPSEnable() {
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        if (gpsTracker.canGetLocation()) {
            return true;

        } else {
            return false;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

   /* public void getCurrentlOcation() {

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
    }*/

/*    public void tunonGps() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(5 * 1000);
            mLocationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            getCurrentlOcation();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and checkky the result in onActivityResult().
                                status.startResolutionForResult(getActivity(), 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }

    }*/


    public void setCurrentLocation(final Double lat, final Double log) {
        try {
            if (marker != null) {
                marker.setPosition(new LatLng(lat, log));
            } else {
                marker = myMap.addMarker(new MarkerOptions().position(new LatLng(lat, log)).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
            }
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, log), 15);
            myMap.animateCamera(cameraUpdate);
            marker.showInfoWindow();
        } catch (Exception e) {

        }
    }

  /*  @SuppressWarnings({"MissingPermission"})
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            if (mGoogleApiClient.isConnected()) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (location == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                } else {

                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();

                    if (!currentLatitude.equals(0.0) && !currentLongitude.equals(0.0)) {
                        setCurrentLocation(currentLatitude, currentLongitude);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.couldnt_get_location), Toast.LENGTH_LONG).show();
                    }


                }
            }

        } catch (Exception e) {

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("SUSPEND", String.valueOf(i));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {

        }
    }*/

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        //   Toast.makeText(getActivity(), locationEngine.getLastLocation() + "", Toast.LENGTH_SHORT).show();
        locationEngine.requestLocationUpdates();
        if (location != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            if (!currentLatitude.equals(0.0) && !currentLongitude.equals(0.0)) {
                setCurrentLocation(currentLatitude, currentLongitude);
            } else {
                Toast.makeText(getActivity(), getString(R.string.couldnt_get_location), Toast.LENGTH_LONG).show();
            }
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            if (!currentLatitude.equals(0.0) && !currentLongitude.equals(0.0)) {
                if (marker != null) {
                    marker.setPosition(new LatLng(currentLatitude, currentLongitude));
                } else {
                    marker = myMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
                }
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 15);
                myMap.animateCamera(cameraUpdate);
            } else {
                Toast.makeText(getActivity(), getString(R.string.couldnt_get_location), Toast.LENGTH_LONG).show();
            }
        }
        DecimalFormat dtime = new DecimalFormat("##.##");
        String s1 = dtime.format(meterDistanceBetweenPoints((float) origin.latitude, (float) origin.longitude, (float) location.getLatitude(), (float) location.getLongitude()));
        Double aDouble = Double.valueOf(s1);
        if (aDouble <= 100) {
            startRide.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                // getCurrentlOcation();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f / Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    private void changeStatus() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Tracking/" + pojo.getRide_id());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Tracking tracking = dataSnapshot.getValue(Tracking.class);
                    tracking.setStatus("started");
                    reference.setValue(tracking);
                    startRide.setText(getString(R.string.Ride_Started));
                    startRide.setEnabled(false);

                    askCompactPermissions(permissionAsk, new PermissionResult() {
                        @Override
                        public void permissionGranted() {
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

                                    /*       NavigationLauncherOptions.Builder navigationLauncherOptions = NavigationLauncherOptions.builder();
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    private void callLocationEngine() {

        locationEngine = new LocationEngineProvider(getActivity()).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.setFastestInterval(5);
        locationEngine.setInterval(10);
        locationEngine.setSmallestDisplacement(5);
        locationEngine.activate();

        setListener();
        //locationEngine.addLocationEngineListener(this);
       /* locationEngine.addLocationEngineListener(new LocationEngineListener() {


            @Override
            public void onConnected() {
                locationEngine.requestLocationUpdates();

            }

            @Override
            public void onLocationChanged(Location location) {
            }
        });*/
    }
}
