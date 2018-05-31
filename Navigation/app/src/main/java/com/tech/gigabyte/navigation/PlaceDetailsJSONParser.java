package com.tech.gigabyte.navigation;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by GIGABYTE on 7/26/2017.
 *
 */

class PlaceDetailsJSONParser {


    /**
     * Receives a JSONObject and returns a list
     */
    List<HashMap<String, String>> parse(JSONObject jObject) {

        Double lat = 0d;
        Double lng = 0d;
        String formattedAddress = "";

        HashMap<String, String> hm = new HashMap<String, String>();
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        try {
            lat = (Double) jObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lat");
            lng = (Double) jObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lng");
            formattedAddress = (String) jObject.getJSONObject("result").get("formatted_address");

        } catch (Exception e) {
            e.printStackTrace();
        }

        hm.put("lat", Double.toString(lat));
        hm.put("lng", Double.toString(lng));
        hm.put("formatted_address", formattedAddress);

        list.add(hm);

        return list;
    }
}