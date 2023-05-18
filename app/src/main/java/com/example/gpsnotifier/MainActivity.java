package com.example.gpsnotifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

    private static final String CHANNEL_ID = "GPSNotifierID";
    private Button button;
    private Button snoozeButton;
    private Button meterButton;
    private TextView stateOfLocation;
    private TextView locationTracked;
    private TextView routeTracked;

    private TextView meterText;
    private EditText inputNumber;
    private static final int uniqueID = 40111;

    private static double pLat = -33.7856;
    private static double pLong = 151.1990;

    private double distanceFromMeters = 100;

    private final double METER_TO_COORD_OFFSET = 111000;

    private int stopTracked = -1;
    private boolean started = false;
    boolean playAlarm = true;
    private Handler handler = new Handler();

    private final int UPDATE_DELAY_SECONDS = 5;

    NotificationCompat.Builder notification;
    NotificationManagerCompat notificationManager;

    Ringtone r;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateGPS();
            if(started) {
                start();
            }
        }
    };

    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void start() {
        started = true;
        handler.postDelayed(runnable, UPDATE_DELAY_SECONDS*1000);
    }

    AutoCompleteTextView autoCompleteTextViewRoute;
    ArrayAdapter<String> adapterItemsRoute;

    AutoCompleteTextView autoCompleteTextViewStops;
    ArrayAdapter<String> adapterItemsStops;
    int busRouteLength = 0;
    private BusRoute[] busRoutes = new BusRoute[2];
    private BusRoute _144ChatswoodToManly_ = new BusRoute(51, "144 Chatswood to Manly");
    private BusRoute _144ManlyToChatswood_ = new BusRoute(51, "144 Manly to Chatswood");


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
    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        Uri alarmNoise = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = RingtoneManager.getRingtone(getApplicationContext(), alarmNoise);

        //meterText.setText(distanceFromMeters + "m radius");

        // buttons!
        button = findViewById(R.id.button);
        snoozeButton = findViewById(R.id.snoozeButton);
        meterButton = findViewById(R.id.meterButton);

        // text
        stateOfLocation = findViewById(R.id.textView);
        locationTracked = findViewById(R.id.textViewLocation);
        routeTracked = findViewById(R.id.textViewRoute);

        meterText = findViewById(R.id.textViewMeters);
        inputNumber = findViewById(R.id.editTextNumber);

        meterText.setText(distanceFromMeters + "m radius");

        // Notification

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notificationpng)
                .setContentTitle("GPSNotifier")
                .setContentText("You are close to your selected destination")
                .setWhen(System.currentTimeMillis())
                .setTicker("This is a ticker")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        notificationManager = NotificationManagerCompat.from(this);

        // yucky bus route stuff
        _144ChatswoodToManly_.addStop(-33.79740449048758, 151.1794862107421, "Chatswood Station, Victoria Ave, Stand F");
        _144ChatswoodToManly_.addStop(-33.800287048183776, 151.1795093172719, "Pacific Hwy at Ellis St");
        _144ChatswoodToManly_.addStop(-33.802413886402014, 151.1796451964106, "Pacific Hwy at Gordon Ave");
        _144ChatswoodToManly_.addStop(-33.804910245849634, 151.1793872576956, "Pacific Hwy before Mowbray Rd");
        _144ChatswoodToManly_.addStop(-33.80727415547247, 151.17971347430841, "Pacific Hwy at Palmer St");
        _144ChatswoodToManly_.addStop(-33.808478148351305, 151.17880310238186, "Pacific Hwy at Eric Rd");
        _144ChatswoodToManly_.addStop(-33.81142187106321, 151.1760416408483, "Pacific Hwy opp Burley St");
        _144ChatswoodToManly_.addStop(-33.814296157812585, 151.17773341537668, "Pacific Hwy opp Allison Ave");
        _144ChatswoodToManly_.addStop(-33.81568914365838, 151.1784692993381, "Pacific Hwy opp Cobden Ave");
        _144ChatswoodToManly_.addStop(-33.8173405273138, 151.18099558144087, "Pacific Hwy at Dickson Ave");
        _144ChatswoodToManly_.addStop(-33.81974821276202, 151.18439430332907, "Gore Hill Technology Park, Pacific Hwy");
        _144ChatswoodToManly_.addStop(-33.82148932568225, 151.18608626641912, "TAFE St Leonards, Pacific Hwy");
        _144ChatswoodToManly_.addStop(-33.82353135162514, 151.18748217003923, "Pacific Hwy opp Bellevue Ave");
        _144ChatswoodToManly_.addStop(-33.824193108839594, 151.1917685045518, "Pacific Hwy before Reserve Rd");
        _144ChatswoodToManly_.addStop(-33.82322252985101, 151.19473479974127, "St Leonards Station, Pacific Hwy, Stand A");
        _144ChatswoodToManly_.addStop(-33.824210419605144, 151.19772336861598, "Pacific Hwy after Albany St");
        _144ChatswoodToManly_.addStop(-33.82704805975892, 151.20021979063716, "Pacific Hwy before Falcon St");
        _144ChatswoodToManly_.addStop(-33.82772869626291, 151.2016536263906, "Falcon St at Alexander St");
        _144ChatswoodToManly_.addStop(-33.82844714001008, 151.20620548603245, "Falcon St at West St");
        _144ChatswoodToManly_.addStop(-33.82899721021986, 151.20997601728484, "Falcon St at Bardsley Gdns");
        _144ChatswoodToManly_.addStop(-33.82941765968753, 151.21263119458283, "Falcon St at Moodie St");
        _144ChatswoodToManly_.addStop(-33.82990222860303, 151.21843760003188, "Big Bear Shopping Centre, Military Rd");
        _144ChatswoodToManly_.addStop(-33.83121372224993, 151.22172732740734, "Neutral Bay Junction, Military Rd, Stand E");
        _144ChatswoodToManly_.addStop(-33.83079779534203, 151.2249819070523, "Military Rd opp Hampden Ave");
        _144ChatswoodToManly_.addStop(-33.829478889506355, 151.22770194995255, "Military Rd opp Holt Ave");
        _144ChatswoodToManly_.addStop(-33.82641783599469, 151.23224212319232, "Military Rd at Prince St");
        _144ChatswoodToManly_.addStop(-33.82427504032457, 151.23712778584186, "Military Rd at Bond St");
        _144ChatswoodToManly_.addStop(-33.82400821577909, 151.2414673224173, "Spit Junction B-Line, Spit Rd");
        _144ChatswoodToManly_.addStop(-33.82136957570774, 151.2437279823178, "Spit Rd at Awaba St");
        _144ChatswoodToManly_.addStop(-33.817234948337884, 151.24340935213135, "Spit Rd opp Parriwi Rd");
        _144ChatswoodToManly_.addStop(-33.813566559770166, 151.24378108733785, "Spit Rd before Medusa St");
        _144ChatswoodToManly_.addStop(-33.81022579413996, 151.2448962929591, "Spit Rd at Pearl Bay Ave");
        _144ChatswoodToManly_.addStop(-33.80621039940885, 151.2464135795049, "Spit West Reserve, Spit Rd");
        _144ChatswoodToManly_.addStop(-33.800177497734175, 151.2467549689777, "Battle Bvd at Manly Rd");
        _144ChatswoodToManly_.addStop(-33.801463543768214, 151.2432196913159, "Battle Bvd at Seaforth Cres");
        _144ChatswoodToManly_.addStop(-33.799647943731664, 151.24377350091595, "Palmerston Pl after Alan Ave");
        _144ChatswoodToManly_.addStop(-33.79833840600141, 151.24475017604755, "Ponsonby Pde opp Palmerston Pl");
        _144ChatswoodToManly_.addStop(-33.79806201980247, 151.24677256164367, "Ponsonby Pde at Panorama Pde");
        _144ChatswoodToManly_.addStop(-33.798298827083904, 151.24850743498192, "Ponsonby Pde opp Old Sydney Rd");
        _144ChatswoodToManly_.addStop(-33.796802668195355, 151.25145301637713, "Sydney Rd after Kempbridge Ave");
        _144ChatswoodToManly_.addStop(-33.795657026867794, 151.25404734599044, "Balgowlah Golf Club, Sydney Rd");
        _144ChatswoodToManly_.addStop(-33.79467625212147, 151.25739653312831, "Sydney Rd before Wanganella St");
        _144ChatswoodToManly_.addStop(-33.79407187722228, 151.2594570806337, "Sydney Rd at Rickard St");
        _144ChatswoodToManly_.addStop(-33.79397114766473, 151.26228315665662, "Sydney Rd before Woodland St");
        _144ChatswoodToManly_.addStop(-33.7942942300019, 151.2644134344364, "Sydney Rd at Condamine St");
        _144ChatswoodToManly_.addStop(-33.794936025978835, 151.2688922192855, "Sydney Rd at Hill St");
        _144ChatswoodToManly_.addStop(-33.79493602597778, 151.27159070720805, "Sydney Rd at Austin St");
        _144ChatswoodToManly_.addStop(-33.795047357393436, 151.273721428188, "Sydney Rd at Brisbane St");
        _144ChatswoodToManly_.addStop(-33.79561991666592, 151.27543748787184, "Sydney Rd at Cohen St");
        _144ChatswoodToManly_.addStop(-33.79589559198284, 151.27703871830633, "Sydney Rd at Thornton St");
        _144ChatswoodToManly_.addStop(-33.79595390780698, 151.27991582954058, "Sydney Rd opp George St");
        _144ChatswoodToManly_.addStop(-33.7964137277086, 151.28271380549666, "Ivanhoe Park, Sydney Rd");

        _144ManlyToChatswood_.addStop(-33.79803519560409, 151.28464922381917, "Manly Wharf, Belgrave St, Stand G");
        _144ManlyToChatswood_.addStop(-33.796592209459384, 151.2828314872177, "Sydney Rd opp Ivanhoe Park");
        _144ManlyToChatswood_.addStop(-33.79619543060268, 151.28057905998295, "Sydney Rd opp Birkley Rd");
        _144ManlyToChatswood_.addStop(-33.79610713172836, 151.2777697403344, "Sydney Rd opp Crescent St");
        _144ManlyToChatswood_.addStop(-33.79582035762176, 151.27558645510138, "Sydney Rd opp Cohen St");
        _144ManlyToChatswood_.addStop(-33.79510195022769, 151.27316176734172, "Sydney Rd at Bellevue St");
        _144ManlyToChatswood_.addStop(-33.795098797941904, 151.27140361155435, "Sydney Rd at Austin St");
        _144ManlyToChatswood_.addStop(-33.795054665947724, 151.2686781855942, "Sydney Rd after Hill St");
        _144ManlyToChatswood_.addStop(-33.794607773363786, 151.26564002474004, "Sydney Rd before Condamine St");
        _144ManlyToChatswood_.addStop(-33.794244940126504, 151.2630915402876, "Sydney Rd at Woodland St");
        _144ManlyToChatswood_.addStop(-33.79407759872399, 151.26016207700238, "Sydney Rd at West St");
        _144ManlyToChatswood_.addStop(-33.79462154380534, 151.25828537028957, "Sydney Rd at Wanganella St");
        _144ManlyToChatswood_.addStop(-33.795820337109795, 151.25420998388086, "Sydney Rd at Coral St");
        _144ManlyToChatswood_.addStop(-33.79891894583187, 151.25164560691286, "Manly Rd at Heaton Ave");
        _144ManlyToChatswood_.addStop(-33.799945075114, 151.24893087414566, "Manly Rd at Avona Cres");
        _144ManlyToChatswood_.addStop(-33.805262019050566, 151.24653508145659, "Spit East Reserve, Spit Rd");
        _144ManlyToChatswood_.addStop(-33.812875714535316, 151.2446439382591, "Spit Rd opp Medusa St");
        _144ManlyToChatswood_.addStop(-33.817014881860175, 151.24359130014116, "Spit Rd opp Bickell Rd");
        _144ManlyToChatswood_.addStop(-33.819141551212795, 151.24408159049554, "Spit Rd at Stanton Rd");
        _144ManlyToChatswood_.addStop(-33.82175001298617, 151.24394125891942, "Spit Rd after Awaba St");
        _144ManlyToChatswood_.addStop(-33.824147944923425, 151.24165086431975, "Spit Junction B-Line, Spit Rd");
        _144ManlyToChatswood_.addStop(-33.82447066260181, 151.23774735457823, "Military Rd at Wudgong St");
        _144ManlyToChatswood_.addStop(-33.82623668155318, 151.23273195844337, "Military Rd opp Prince St");
        _144ManlyToChatswood_.addStop(-33.82846908767893, 151.22976788748608, "Military Rd before Cabramatta Rd");
        _144ManlyToChatswood_.addStop(-33.82902801686861, 151.22859152811247, "Military Rd at Spencer Rd");
        _144ManlyToChatswood_.addStop(-33.830686066800844, 151.2257973119592, "Military Rd before Hampden Ave");
        _144ManlyToChatswood_.addStop(-33.83164453363238, 151.22307973488358, "Neutral Bay Junction, Military Rd, Stand C");
        _144ManlyToChatswood_.addStop(-33.83027844906698, 151.21865810611044, "Military Rd opp Big Bear Shopping Centre");
        _144ManlyToChatswood_.addStop(-33.83015831239021, 151.21801826071888, "Watson Street Interchange, Stand B");
        _144ManlyToChatswood_.addStop(-33.8290819832848, 151.2091514897882, "Falcon St at Miller St");
        _144ManlyToChatswood_.addStop(-33.82869784742967, 151.20700064687918, "North Sydney Boys High School, Falcon St");
        _144ManlyToChatswood_.addStop(-33.827953155579515, 151.2021337293793, "Falcon St at Alexander St");
        _144ManlyToChatswood_.addStop(-33.827163567503625, 151.20004254595463, "Pacific Hwy after Shirley Rd");
        _144ManlyToChatswood_.addStop(-33.82632221995384, 151.1992635582607, "Pacific Hwy at Hume St");
        _144ManlyToChatswood_.addStop(-33.823499090337556, 151.1963204938401, "Pacific Hwy before Christie St");
        _144ManlyToChatswood_.addStop(-33.82370761877501, 151.19370967279477, "St Leonards Station, Pacific Hwy, Stand C");
        _144ManlyToChatswood_.addStop(-33.82479502686459, 151.19051963694685, "Pacific Hwy opp Gore Hill Oval");
        _144ManlyToChatswood_.addStop(-33.8236639481769, 151.1872864162714, "Pacific Hwy at Bellevue Ave");
        _144ManlyToChatswood_.addStop(-33.821347838571626, 151.1856843268921, "Pacific Hwy opp TAFE St Leonards");
        _144ManlyToChatswood_.addStop(-33.81841504724499, 151.1827073828333, "Pacific Hwy opp Campbell St");
        _144ManlyToChatswood_.addStop(-33.817427523039605, 151.1803572688355, "Pacific Hwy opp Dickson Ave");
        _144ManlyToChatswood_.addStop(-33.816059734489556, 151.178404625206, "Pacific Hwy opp Hotham Pde");
        _144ManlyToChatswood_.addStop(-33.81428333585632, 151.1773686364082, "Pacific Hwy at Allison Ave");
        _144ManlyToChatswood_.addStop(-33.81147680488826, 151.17565913824617, "Pacific Hwy at Burley St");
        _144ManlyToChatswood_.addStop(-33.80891251461936, 151.17829742065467, "Pacific Hwy opp Eric Rd");
        _144ManlyToChatswood_.addStop(-33.80744482761205, 151.17935259521323, "Pacific Hwy opp Palmer St");
        _144ManlyToChatswood_.addStop(-33.80494712693076, 151.17916666136634, "Pacific Hwy after Mowbray Rd");
        _144ManlyToChatswood_.addStop(-33.802297694661256, 151.17941246569578, "Pacific Hwy at Sutherland Rd");
        _144ManlyToChatswood_.addStop(-33.796766041992015, 151.18043349704834, "Chatswood Station");


        // add bus route to dropdown menu
        busRoutes[0] = _144ChatswoodToManly_;
        busRoutes[1] = _144ManlyToChatswood_;

        String[] items = {busRoutes[0].nam, busRoutes[1].nam};
        autoCompleteTextViewRoute = findViewById(R.id.auto_complete_textviewroute);
        adapterItemsRoute = new ArrayAdapter<String>(MainActivity.this, R.layout.list_item, items);

        autoCompleteTextViewRoute.setAdapter(adapterItemsRoute);

        autoCompleteTextViewRoute.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                routeTracked.setText("Selected " + busRoutes[position].nam);
                String[] items = busRoutes[position].returnStringOfBusStops();
                autoCompleteTextViewStops = findViewById(R.id.auto_complete_textviewstops);
                adapterItemsStops = new ArrayAdapter<String>(MainActivity.this, R.layout.list_item, items);

                autoCompleteTextViewStops.setAdapter(adapterItemsStops);

                autoCompleteTextViewStops.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        locationTracked.setText("Tracking " + _144ChatswoodToManly_.busStops[position].name);
                        stopTracked = position;
                        updateGPS();
                        if (!started) {

                            start();
                        }
                    }
                });
            }
        });



        // buttons again
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGPS();
            }
        });

        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playAlarm) {
                    r.stop();
                    playAlarm = false;
                    snoozeButton.setText("Unsnooze");
                } else {
                    playAlarm = true;
                    snoozeButton.setText("Snooze");
                }
            }
        });

        meterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inputNumber.length() > 0) {
                    distanceFromMeters = Integer.parseInt(inputNumber.getText().toString());
                }
                meterText.setText(distanceFromMeters + "m radius");
                r.stop();
                updateGPS();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        r.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //updateGPS();
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
                            stateOfLocation.setText("Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
                            double tempLat;
                            double tempLong;
                            double calculatedDistance = distanceFromMeters/METER_TO_COORD_OFFSET;

                            tempLat = _144ChatswoodToManly_.busStops[stopTracked].latitude;
                            tempLong = _144ChatswoodToManly_.busStops[stopTracked].longitude;
                            if (location.getLatitude()-calculatedDistance < tempLat && tempLat < location.getLatitude()+calculatedDistance &&
                                    location.getLongitude()-calculatedDistance < tempLong && tempLong < location.getLongitude()+calculatedDistance) {
                                stateOfLocation.setText("Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude() + " You are within " + (int)distanceFromMeters + "m from " + _144ChatswoodToManly_.busStops[stopTracked].name);

                                // notification


                                if (!r.isPlaying() && playAlarm) {
                                    notificationManager.notify(uniqueID, notification.build());

                                    r.play();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        r.setLooping(false);
                                    }
                                }

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
            CharSequence name = "GPSNotifier";
            String description = "For the GPS thingo";
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