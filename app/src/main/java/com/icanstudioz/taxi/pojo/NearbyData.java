package com.icanstudioz.taxi.pojo;

/**
 * Created by android on 17/3/17.
 */

public class NearbyData {
    String user_id;
    String name;
    String email;
    String latitude;
    String longitude;
    String vehicle_info;
    String distance;





    public NearbyData() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getVehicle_info() {
        return vehicle_info;
    }

    public void setVehicle_info(String vehicle_info) {
        this.vehicle_info = vehicle_info;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
