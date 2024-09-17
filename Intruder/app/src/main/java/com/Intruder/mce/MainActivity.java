package com.Intruder.mce;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String CHANNEL_ID = "my_channel";
    private static final String CHANNEL_NAME = "My Channel";
    double lat;
    double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        TextView elephantArrivalText = (TextView) findViewById(R.id.text_view_content);
        elephantArrivalText.setTextColor(Color.parseColor("#FFFFFF"));

        CardView cardView = findViewById(R.id.card_view);


        // Reference to your Firebase database
        DatabaseReference ref = database.getReference("Location"); // Replace "your_node_path" with the actual path to your data


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                //Object data = snapshot.getValue();
                // Check if the dataSnapshot exists and has children
                if (dataSnapshot.exists()) {
                    // Get the location node
                    //DataSnapshot locationSnapshot = dataSnapshot.child("Location");
                   // Toast.makeText(MainActivity.this, " " + dataSnapshot, Toast.LENGTH_SHORT).show();

                    // Check if the location node has the required fields
                    if (dataSnapshot.hasChild("Latitude") && dataSnapshot.hasChild("Longitude") && dataSnapshot.hasChild("isElephantArrived")) {
                        // Get the values from the dataSnapshot
                        double latitude = dataSnapshot.child("Latitude").getValue(Double.class);
                        double longitude = dataSnapshot.child("Longitude").getValue(Double.class);
                        boolean isElephantArrived = dataSnapshot.child("isElephantArrived").getValue(Boolean.class);

                        // Now you can use these values
                        // For example, print them
                        System.out.println("Latitude: " + latitude);
                        System.out.println("Longitude: " + longitude);
                        System.out.println("Is Elephant Arrived: " + isElephantArrived);
                        lat = latitude;
                        lng = longitude;

                        if (isElephantArrived) {
                            cardView.setBackgroundColor(Color.parseColor("#FF0000"));
                            elephantArrivalText.setText("Elephant Arrived...!");
                            showNotification(MainActivity.this, "Intruder: Alert", "Elephant Arrived");
                            Toast.makeText(MainActivity.this, "Elephant Arrived...!", Toast.LENGTH_SHORT).show();
                        }else{
                            cardView.setBackgroundColor(Color.parseColor("#228B22"));
                            elephantArrivalText.setText("Safe");

                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


        // Check if the app has location permissions, request if not granted
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, get the current location
            //getCurrentLocation();
        }
// used to retrive users current location

        findViewById(R.id.map_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(MainActivity.this, "hi", Toast.LENGTH_SHORT).show();
//                cardView.setBackgroundColor(Color.parseColor("#006400"));
//                elephantArrivalText.setText("Safe");

                Geocoder geocoder;
                String bestProvider;
                List<Address> user = null;


                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                Criteria criteria = new Criteria();
                bestProvider = lm.getBestProvider(criteria, false);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location = lm.getLastKnownLocation(bestProvider);

                if (location == null){
                    Toast.makeText(MainActivity.this,"Location Not found",Toast.LENGTH_LONG).show();
                }else{
                    geocoder = new Geocoder(MainActivity.this);
                    try {
                        user = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                       double cur_latitude=(double)user.get(0).getLatitude();
                        double cur_longitude=(double)user.get(0).getLongitude();

                        double des_latitude=lat;
                        double des_longitude=lng;



                        String uri = "http://maps.google.com/maps?saddr=" + cur_latitude + "," + cur_longitude + "&daddr=" + des_latitude + "," + des_longitude;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(intent);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }

        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the current location
               // getCurrentLocation();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification Channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.elephant_notification) // Replace with your icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        notificationManager.notify(0, builder.build());
    }
}