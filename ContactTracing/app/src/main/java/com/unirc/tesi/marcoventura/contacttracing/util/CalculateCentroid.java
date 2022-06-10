package com.unirc.tesi.marcoventura.contacttracing.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class CalculateCentroid {

    // WGS84 ellipsoid constants
    private static final double a = 6378137; // radius
    private static final double e = 8.1819190842622e-2;  // eccentricity

    private static final double asq = Math.pow(a,2);
    private static final double esq = Math.pow(e,2);

    private static final LatLng first_latLng_centroid = new LatLng(46.461304, 8.435695);


    private static double[] lla2ecef(double[] lla){

        double lat = lla[0];
        double lon = lla[1];
        double alt = lla[2];

        lat = lat * Math.PI/180;
        lon = lon * Math.PI/180;


        double N = a / Math.sqrt(1 - esq * Math.pow(Math.sin(lat),2) );

        double x = (N+alt) * Math.cos(lat) * Math.cos(lon);
        double y = (N+alt) * Math.cos(lat) * Math.sin(lon);
        double z = ((1-esq) * N + alt) * Math.sin(lat);

        return new double[]{x, y, z};
    }


    private static int round(double distance) {

        String[] values = String.valueOf(distance).split(Pattern.quote("."));

        int first = Integer.parseInt(values[0]);
        int res = (int) Math.round(distance);

        if (first % 2 == 0 && res % 2 != 0)
            return first;
        else if (res % 2 != 0)
            return res + 1;
        else
            return res;

    }


    public static double calculateDistanceFromCentralCentroid(LatLng latLng_user){
        return truncateDecimal(SphericalUtil.computeDistanceBetween(first_latLng_centroid, latLng_user), 2);
    }

    private static int calculateRoundDistance(LatLng latLng_user){
        return round(SphericalUtil.computeDistanceBetween(first_latLng_centroid, latLng_user));
    }

    public static int getCentroid(double user_latitude, double user_longitude) {

        LatLng latLng_user = new LatLng(user_latitude, user_longitude);
        int distance = calculateRoundDistance(latLng_user);

        return distance / 2;

    }

    private static LatLng getCommonCentroidLocation(double user_latitude, double user_longitude, double distance_to_common) {

        double heading = angleFromCoordinate(user_latitude, user_longitude);
        return SphericalUtil.computeOffset(first_latLng_centroid, distance_to_common, heading);
    }

    public static double calculateDistanceFromCommonCentroid(double user_latitude, double user_longitude, double distance_to_common){

        LatLng latLng_user = new LatLng(user_latitude, user_longitude);
        LatLng latLng_centroid_relative = getCommonCentroidLocation(user_latitude, user_longitude, distance_to_common);

        Log.d("Lat_user", String.valueOf(latLng_user.latitude));
        Log.d("Long_user", String.valueOf(latLng_user.longitude));
        Log.d("Lat_centroid", String.valueOf(latLng_centroid_relative.latitude));
        Log.d("Long_centroid", String.valueOf(latLng_centroid_relative.longitude));

        return truncateDecimal(SphericalUtil.computeDistanceBetween(latLng_user,latLng_centroid_relative), 2);
    }


    public static double angleFromCoordinate(double user_latitude, double user_longitude) {

        LatLng latLng_user = new LatLng(user_latitude, user_longitude);
        return SphericalUtil.computeHeading(first_latLng_centroid, latLng_user);

    }

    private static double truncateDecimal(double x, int numberofDecimals) {
        if ( x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR).doubleValue();
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING).doubleValue();
        }
    }


}
