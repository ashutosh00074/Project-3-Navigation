package com.tech.gigabyte.navigation;

import java.util.List;

/*
 * Created by GIGABYTE on 7/26/2017.
 *
 */


interface DirectionFinderListener {
    void onDirectionFinderStart();

    void onDirectionFinderSuccess(List<Route> route);
}
