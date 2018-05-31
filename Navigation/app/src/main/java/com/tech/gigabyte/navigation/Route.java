package com.tech.gigabyte.navigation;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/*
 * Created by GIGABYTE on 7/26/2017.
 *
 */


class Route {
    Distance distance;
    Duration duration;
    String endAddress;
    LatLng endLocation;
    String startAddress;
    LatLng startLocation;

    List<LatLng> points;
}
