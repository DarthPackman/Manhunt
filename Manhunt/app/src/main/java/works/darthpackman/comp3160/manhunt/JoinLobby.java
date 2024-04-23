package works.darthpackman.comp3160.manhunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinLobby extends AppCompatActivity
{
    Integer hunter;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference lobbyIndex;
    DatabaseReference playerIndex;
    DatabaseReference gameState;
    TextView lobby_name;
    Integer player_index;
    ValueEventListener gamestatelistener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_lobby);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        lobbyIndex = firebaseDatabase.getReference("lobbies/0");
        player_index = sharedPreferences.getInt("PLAYER_INDEX", 9);
        playerIndex = firebaseDatabase.getReference("lobbies/0/players").child(String.valueOf(player_index));
        gameState = firebaseDatabase.getReference("lobbies/0/gamestate");

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

        gamestatelistener = gameState.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                gameState.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task)
                    {
                        if (Integer.parseInt(String.valueOf(task.getResult().getValue())) == 1)
                        {
                            playerIndex.child("hunter").get().addOnCompleteListener(task1 ->
                            {
                                if (!task.isSuccessful())
                                {
                                    Log.e("firebase", "Error getting data", task1.getException());
                                }
                                else
                                {
                                    Log.d("firebase", String.valueOf(task1.getResult().getValue()));
                                    hunter = Integer.parseInt(String.valueOf(task1.getResult().getValue()));
                                    editor.putInt("HUNTER_STATUS", hunter);
                                    editor.putInt("HOST_STATUS", 0);
                                    editor.apply();
                                    if (hunter == 1)
                                    {
                                        Intent intent = new Intent(JoinLobby.this, HunterRole.class);
                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        Intent intent = new Intent(JoinLobby.this, HuntedRole.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
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
    protected void onDestroy() {
        super.onDestroy();
        gameState.removeEventListener(gamestatelistener);
    }
}