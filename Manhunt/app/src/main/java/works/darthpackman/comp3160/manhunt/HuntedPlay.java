package works.darthpackman.comp3160.manhunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Random;

public class HuntedPlay extends AppCompatActivity implements OnMapReadyCallback
{
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference lobbyIndex;
    DatabaseReference playerIndex;
    DatabaseReference gameState;
    Integer player_index;
    Integer host;
    Integer tag;
    TextView timer;
    private int min;
    private int second;
    private int time;
    int counttime;
    int i;
    double lat;
    double lng;
    Integer player_count;
    Integer hunterStatus;
    String hunterName;
    Double hunterLat;
    Double hunterLong;
    LatLng hunterLatLng;
    ValueEventListener gamestatelistener;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location lastKnownLocation;
    private final LatLng defaultLocation = new LatLng(50.67089119571005, -120.36306805334952);
    private static final int DEFAULT_ZOOM = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunted_play);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        lobbyIndex = firebaseDatabase.getReference("lobbies/0");
        player_index = sharedPreferences.getInt("PLAYER_INDEX", 0);
        playerIndex = firebaseDatabase.getReference("lobbies/0/players/" + player_index);
        gameState = firebaseDatabase.getReference("lobbies/0/gamestate");
        host = sharedPreferences.getInt("HOST_STATUS", 0);

        tag = new Random().nextInt(10000);
        playerIndex.child("tag").setValue(tag);
        editor.putInt("TAG", tag);
        editor.apply();

        time = sharedPreferences.getInt("TIMER", 900);
        counttime = time * 1000;

        player_count = sharedPreferences.getInt("PLAYER_COUNT", 0);

        timer = findViewById(R.id.timer_tv);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NONE).compassEnabled(false).rotateGesturesEnabled(false).tiltGesturesEnabled(false);
        mapFragment.newInstance(options);

        Intent intent = new Intent(this, Results.class);

        new CountDownTimer(counttime, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
                min = time / 60;
                second = time - (min * 60);
                timer.setText(min + ":" + second);
                time--;
                getDeviceLocation();
            }

            public void onFinish()
            {
                if (host == 1) {
                    gameState.setValue(3);
                }
            }
        }.start();

        gamestatelistener = gameState.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                gameState.get().addOnCompleteListener(task -> {
                    if (Integer.parseInt(String.valueOf(task.getResult().getValue())) == 3 ||
                            Integer.parseInt(String.valueOf(task.getResult().getValue())) == 4) {
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        gameState.removeEventListener(gamestatelistener);
        editor.putInt("TIMER", time);
        editor.apply();
    }

    public void tag(View view)
    {
        Intent intent = new Intent(this, HuntedTag.class);
        startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap Map)
    {
        this.map = Map;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);
        map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        map.getUiSettings().setScrollGesturesEnabled(false);
        getDeviceLocation();

        new CountDownTimer(counttime, 1000)
        {
            @Override
            public void onTick(long l)
            {
                if ((second % 5) == 0)
                {
                    lobbyIndex.child("players").get().addOnCompleteListener(task -> {
                        DataSnapshot players = task.getResult();
                        map.clear();
                        for (i = 0; i < player_count; i++) { // for each player
                            DataSnapshot player = players.child(String.valueOf(i));
                            hunterStatus = player.child("hunter").getValue(Integer.class);
                            if (hunterStatus != null && hunterStatus == 1) {
                                hunterLat = player.child("lat").getValue(Double.class);
                                hunterLong = player.child("long").getValue(Double.class);
                                hunterName = player.child("name").getValue(String.class);
                                hunterLatLng = new LatLng(hunterLat, hunterLong);
                                map.addMarker(new MarkerOptions().position(hunterLatLng).title(hunterName));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFinish()
            {

            }
        }.start();
    }

    private void getDeviceLocation()
    {
        @SuppressLint("MissingPermission") Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful())
                {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.getResult();
                    if (lastKnownLocation != null) {
                        lat = lastKnownLocation.getLatitude();
                        lng = lastKnownLocation.getLongitude();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
                        playerIndex.child("lat").setValue(lat);
                        playerIndex.child("long").setValue(lng);
                    }
                } else
                {
                    map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        });
    }
}