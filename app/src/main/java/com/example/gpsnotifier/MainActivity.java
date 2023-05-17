package com.example.gpsnotifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "testID";
    private Button button;
    private TextView stateOfLocation;
    private TextView locationTracked;
    private String testString = "test";
    private static final int uniqueID = 40111;

    private static double pLat = -33.7856;
    private static double pLong = 151.1990;

    private static double distanceFrom = 0.001;

    private int stopTracked = -1;


    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    private BusRoute _144ChatswoodToManly_ = new BusRoute(52);


    //LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .build();

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        button = findViewById(R.id.button);
        stateOfLocation = findViewById(R.id.textView);
        locationTracked = findViewById(R.id.textViewLocation);


        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.whorns)
                .setContentTitle("Test notification")
                .setContentText("This is a test notification")
                .setWhen(System.currentTimeMillis())
                .setTicker("This is a ticker")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        // button
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        //locationRequest = new LocationRequest();
        //locationRequest.Builder.setInterval()

        _144ChatswoodToManly_.addStop(-33.79740449048758, 151.1794862107421, "Chatswood Station, Victoria Ave, Stand F");
        _144ChatswoodToManly_.addStop(-33.800287048183776, 151.1795093172719, "Pacific Hwy at Ellis St");
        String[] items = _144ChatswoodToManly_.returnStringOfBusStops();
        autoCompleteTextView = findViewById(R.id.auto_complete_textview);
        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, items);

        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                locationTracked.setText("Tracking " + _144ChatswoodToManly_.busStops[position].name);
                stopTracked = position;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGPS();
                notificationManager.notify(uniqueID, notification.build());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "NO LMAO", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(getApplicationContext())
                    .requestLocationUpdates(locationRequest, locationCallback, null);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (stopTracked >= 0) {
                        if (location == null) {
                            stateOfLocation.setText("Location not found");
                        } else {
                            stateOfLocation.setText(location.getLatitude() + " " + location.getLongitude());
                            double tempLat;
                            double tempLong;

                            tempLat = _144ChatswoodToManly_.busStops[stopTracked].latitude;
                            tempLong = _144ChatswoodToManly_.busStops[stopTracked].longitude;
                            if (location.getLatitude()-distanceFrom < tempLat && tempLat < location.getLatitude()+distanceFrom &&
                                    location.getLongitude()-distanceFrom < tempLong && tempLong < location.getLongitude()+distanceFrom) {
                                stateOfLocation.setText(location.getLatitude() + " " + location.getLongitude() + " near " + _144ChatswoodToManly_.busStops[stopTracked].name);
                            }
                            //                        double tempLat;
                            //                        double tempLong;
                            //                        for (int i = 0; i < _144ChatswoodToManly_.i; i++) {
                            //                            tempLat = _144ChatswoodToManly_.busStops[i].latitude;
                            //                            tempLong = _144ChatswoodToManly_.busStops[i].longitude;
                            //                            if (location.getLatitude()-distanceFrom < tempLat && tempLat < location.getLatitude()+distanceFrom &&
                            //                                    location.getLongitude()-distanceFrom < tempLong && tempLong < location.getLongitude()+distanceFrom) {
                            //                                stateOfLocation.setText(location.getLatitude() + " " + location.getLongitude() + " near " + _144ChatswoodToManly_.busStops[i].name);
                            //                            }
                            //                        }

                        }
                    } else {
                        stateOfLocation.setText("Choose a stop first");
                    }
                }
            });
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "test name";
            String description = "test desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}