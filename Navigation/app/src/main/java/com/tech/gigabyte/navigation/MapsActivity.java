package com.tech.gigabyte.navigation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sa90.materialarcmenu.ArcMenu;
import com.sa90.materialarcmenu.StateChangeListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * Created by GIGABYTE on 7/26/2017.
 *
 */


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener, View.OnClickListener {

    AutoCompleteTextView atvPlaces, etOrigin, etDestination;
    PlacesTask placesTask;
    ParserTask parserTask;
    private GoogleMap mMap;
    Toolbar toolbar;
    ArcMenu MapView;
    Animation up, down, trans;
    View mapView;
    public boolean isClickedFirstTime = true;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    ConnectionDetector cd;
    private static final int PERMISSION_REQUEST_CODE = 1;
    AlertDialogManager alert = new AlertDialogManager();
    Boolean isInternetPresent = false;
    public final static File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temproute.png");
    public final static String EXTRA_MESSAGE = myFile.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        cd = new ConnectionDetector(getApplicationContext());

        //requestPermission();

        // Check if Internet present
        isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            // Internet Connection is not present
            alert.showAlertDialog(MapsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);

            // stop executing code by return
            return;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        MapView = (ArcMenu) findViewById(R.id.mapview_layout);
        MapView.setStateChangeListener(new StateChangeListener() {
            @Override
            public void onMenuOpened() {

            }

            @Override
            public void onMenuClosed() {

            }
        });

        atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
        atvPlaces.setThreshold(1);
        atvPlaces.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
        Button btnFindPath = (Button) findViewById(R.id.btnFindPath);
        Button btnSearchLocation = (Button) findViewById(R.id.btnSearchLocation);

        etOrigin = (AutoCompleteTextView) findViewById(R.id.etOrigin);
        atvPlaces.setThreshold(1);
        etOrigin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence o, int i, int i1, int i2) {
                placesTask = new PlacesTask();
                placesTask.execute(o.toString());
            }

            @Override
            public void onTextChanged(CharSequence o, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etDestination = (AutoCompleteTextView) findViewById(R.id.etDestination);
        atvPlaces.setThreshold(1);
        etDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence d, int i, int i1, int i2) {
                placesTask = new PlacesTask();
                placesTask.execute(d.toString());
            }

            @Override
            public void onTextChanged(CharSequence d, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //method for initialisation
        init();
        btnFindPath.setOnClickListener(this);
        btnSearchLocation.setOnClickListener(this);
    }

    /*private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    //In this case receive empty permissions and results arrays which should be treated as a cancellation
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Toast.makeText(this, "Permission Granted, Now you can access location data and camera.", Toast.LENGTH_SHORT).show();
                    else {

                        Toast.makeText(this, "Permission Denied, You cannot access location data and camera.", Toast.LENGTH_SHORT).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    //On a AlertDialog Interface
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MapsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }*/

    /*********************************************
     ** A method to download json data from url **
     *********************************************/

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d(e.toString(), "Exception while downloading url");
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }

    public void Show(View view) {
        RelativeLayout show = (RelativeLayout) findViewById(R.id.route);
        LinearLayout duration = (LinearLayout) findViewById(R.id.L_duration);
        down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);

        if (isClickedFirstTime) {
            toolbar.setVisibility(View.GONE);
            show.setVisibility(View.VISIBLE);
            show.startAnimation(down);
            duration.setVisibility(View.GONE);
            isClickedFirstTime = false;
        } else {
            toolbar.setVisibility(View.VISIBLE);
            toolbar.startAnimation(down);
            show.clearAnimation();
            show.setVisibility(View.GONE);
            isClickedFirstTime = true;
        }
    }

    /*------------------------------------------------------------------------------
    #                            Changing Map Type                                 #
    #  Google provides 4 kinds of map types Normal, Hybrid, Satellite and Terrain. #
    ------------------------------------------------------------------------------*/

    public void ViewNormal(View view) {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Toast.makeText(this, "NORMAL", Toast.LENGTH_SHORT).show();
    }

    public void ViewHybrid(View view) {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        Toast.makeText(this, "HYBRID", Toast.LENGTH_SHORT).show();
    }

    public void ViewSatellite(View view) {
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        Toast.makeText(this, "SATELLITE", Toast.LENGTH_SHORT).show();
    }

    public void ViewTerrain(View view) {
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        Toast.makeText(this, "TERRAIN", Toast.LENGTH_SHORT).show();
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyBgwjQHydcwJilcaLr0ohmT0zz92tfbilc";

            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input + "&" + types + "&" + sensor + "&" + key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

            try {
                // Fetching the data from we service
                data = downloadUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    /*******************************************************
     ** A class to parse the Google Places in JSON format **
     *******************************************************/
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[]{"description"};
            int[] to = new int[]{android.R.id.text1};

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            atvPlaces.setAdapter(adapter);
            etOrigin.setAdapter(adapter);
            etDestination.setAdapter(adapter);
        }
    }

    private void init() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: {
                saveTemporaryMapImage();
                Toast.makeText(MapsActivity.this, "Map Saved", Toast.LENGTH_LONG).show();
                return true;
            }
            case R.id.show: {
                Intent intent = new Intent(this, ShowActivity.class);
                String message = myFile.toString();
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFindPath:
                LinearLayout duration = (LinearLayout) findViewById(R.id.L_duration);
                trans = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate);
                duration.setVisibility(View.VISIBLE);
                duration.startAnimation(trans);
                sendRequest();
                RelativeLayout dur = (RelativeLayout) findViewById(R.id.route);
                dur.setVisibility(View.GONE);
                dur.clearAnimation();
                toolbar.setVisibility(View.VISIBLE);
                toolbar.startAnimation(down);
                InputMethodManager path = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                path.hideSoftInputFromWindow(etDestination.getWindowToken(), 0);
                break;

            case R.id.btnSearchLocation:
                String search = atvPlaces.getText().toString();
                if (search.isEmpty()) {
                    Toast.makeText(this, "Please enter search address", Toast.LENGTH_SHORT).show();
                    return;
                }
                searchLocation();
                LinearLayout dura = (LinearLayout) findViewById(R.id.L_duration);
                dura.setVisibility(View.GONE);
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(atvPlaces.getWindowToken(), 0);
                atvPlaces.setText("");
                break;
        }
    }

    private void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void searchLocation() {
        String searchText = atvPlaces.getText().toString();
        List<Address> addressList = null;
        Geocoder geocoder = new Geocoder(this);
        try {
            addressList = geocoder.getFromLocationName(searchText, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert addressList != null;
        Address address = addressList.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        // create marker
        MarkerOptions marker = new MarkerOptions().position(latLng).title("Home");

        //Changing Marker icon
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        //Adding Marker
        mMap.addMarker(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission
                        (this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        LatLng Jabalpur = new LatLng(23.1815, 79.9864);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Jabalpur, 11));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Jabalpur")
                .position(Jabalpur)));

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 350, 50, 0);
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = new ProgressDialog(this, R.style.ProgressBarTheme);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 9));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions()
                    .geodesic(true)
                    .color(Color.BLUE)
                    .width(25);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    public void saveTemporaryMapImage() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
                bitmap = snapshot;
                try {
                    FileOutputStream out = new FileOutputStream(myFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                    Toast.makeText(MapsActivity.this, "File Saved Successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MapsActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        };
        mMap.snapshot(callback);
    }
}