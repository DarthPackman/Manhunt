package works.darthpackman.comp3160.manhunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class HostLobby extends AppCompatActivity
{
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference lobbyIndex;
    DatabaseReference playerIndex;
    Integer lobby_index;
    Integer player_count;
    Integer hunter_count;
    Integer hunter_limit;
    Integer hunter_index;
    Integer hunter;
    TextView lobby_name;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_lobby);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
        lobby_index = sharedPreferences.getInt("LOBBY_INDEX", 0);
        lobbyIndex = firebaseDatabase.getReference("lobbies/0");
        playerIndex = firebaseDatabase.getReference("lobbies/0/players/0");
        lobby_name = findViewById(R.id.lobby_name_tv);

        lobbyIndex.child("name").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful())
            {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else
            {
               lobby_name.setText(String.valueOf(task.getResult().getValue()));
            }

        });
                //need to add an updating box to show current players
    }

    public void start(View view)
    {
        lobbyIndex.child("players").child("count").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful())
            {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else
            {
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                player_count = Integer.parseInt(String.valueOf(task.getResult().getValue()));
                hunter_limit = (int) Math.ceil(player_count * 0.9);
                hunter_count = (int) Math.ceil(player_count * 0.1);
                lobbyIndex.child("hunterlimit").setValue(hunter_limit);
                lobbyIndex.child("huntercount").setValue(hunter_count);

                Random random = new Random();

                for (int i = 0; i < hunter_count; i++)
                {
                    hunter_index = random.nextInt(player_count);
                    lobbyIndex.child("players").child(String.valueOf(hunter_index)).child("hunter").setValue(1);
                }

                playerIndex.child("hunter").get().addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful())
                    {
                        Log.e("firebase", "Error getting data", task1.getException());
                    }
                    else
                    {
                        Log.d("firebase", String.valueOf(task1.getResult().getValue()));
                        lobbyIndex.child("gamestate").setValue(1);
                        hunter = Integer.parseInt(String.valueOf(task1.getResult().getValue()));
                        editor.putInt("HUNTER_STATUS", hunter);
                        editor.putInt("HOST_STATUS", 1);
                        editor.apply();
                        if (hunter == 1)
                        {
                            Intent intent = new Intent(view.getContext(), HunterRole.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Intent intent = new Intent(view.getContext(), HuntedRole.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }
}