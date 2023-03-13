package com.example.hunterqrhunter.page;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.hunterqrhunter.page.Qrcode;
import com.example.hunterqrhunter.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.hunterqrhunter.databinding.ActivityQrMapBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QRMapScreen extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityQrMapBinding binding;
    FirebaseFirestore db;
    ArrayAdapter<com.example.hunterqrhunter.page.Qrcode> QRAdapter;
    ArrayList<com.example.hunterqrhunter.page.Qrcode> QRDataList;
    com.example.hunterqrhunter.page.CustomList customList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        binding = ActivityQrMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Initialize arrays with some data
        String[] hashnames = { "Hash1", "Hash2", "Hash3" };
//        String[] hashimages = { "Image1", "Image2", "Image3" };
        GeoPoint[] locations = { new GeoPoint(0, 0), new GeoPoint(1, 1), new GeoPoint(2, 2) };
        int[] scores = { 1, 2, 3 };
        String[][] owners = { { "Owner1", "Owner2" }, { "Owner3" }, {} };
//        String[][] commentarray = { { "Comment1", "Comment2" }, {}, { "Comment3" } };
        QRDataList = new ArrayList<>();
        for(int i=0;i<hashnames.length;i++){
            QRDataList.add((new com.example.hunterqrhunter.page.Qrcode(hashnames[i], locations[i], scores[i], owners[i])));
        }

        QRAdapter = new com.example.hunterqrhunter.page.CustomList(this, QRDataList);

        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = db.collection("QR");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                // Clear the old list
                QRDataList.clear();
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                    String qrName = doc.getString("hashName");
                    Log.d(TAG, "hashName: " + qrName);

//                    String qrImage = doc.getString("QR Image");
//                    Log.d(TAG, "QR Image: " + qrImage);

                    GeoPoint location = doc.getGeoPoint("location");
                    Log.d(TAG, "Location: " + location);

                    int qrScore = doc.getLong("score").intValue();
                    Log.d(TAG, "QR Score: " + qrScore);

                    Object qrOwnedByObj = doc.get("ownedBy");
                    String[] qrOwnedBy = null;
                    // if the value is a list, convert it to an array
                    List<String> qrOwnedByList = (List<String>) qrOwnedByObj;
                    qrOwnedBy = new String[qrOwnedByList.size()];
                    for (int i = 0; i < qrOwnedByList.size(); i++) {
                        qrOwnedBy[i] = qrOwnedByList.get(i);
                    }
                    Log.d(TAG, "QR OwnedBy: " + Arrays.toString(qrOwnedBy));

//                    String[] qrComments = doc.get("QR comments", String[].class);
//                    Log.d(TAG, "QR Comments: " + Arrays.toString(qrComments));

                    com.example.hunterqrhunter.page.Qrcode qrCode = new com.example.hunterqrhunter.page.Qrcode(qrName, location, qrScore, qrOwnedBy);
                    QRDataList.add(qrCode);
                    // Add a marker for each location to the map
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(qrName));
                }
                QRAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud

            } });
        mMap = googleMap;
        Qrcode highestScore = QRDataList.get(0);
        for (Qrcode element : QRDataList) {
            if (element.score > highestScore.score) {
                highestScore = element;
            }
        }
        for (Qrcode element : QRDataList) {
            LatLng location = new LatLng(element.location.getLatitude(), element.location.getLongitude());
            if (element == highestScore) {
                mMap.addMarker(new MarkerOptions().position(location).icon(getMarkerIcon("#ffe922")));
            }
            else {
                mMap.addMarker(new MarkerOptions().position(location).title(element.hashName + "Score = " + element.score));
            }
        }
        // Add a marker based on the lat/long we receieve and plot it
        LatLng highestscorelocation = new LatLng(highestScore.location.getLatitude(), highestScore.location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(highestscorelocation));
    }
    // method definition
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }
}