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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Host extends AppCompatActivity
{
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference lobbyCount = firebaseDatabase.getReference("lobbies/count");
    DatabaseReference lobbyIndex;
    String lobby_index;
    DatabaseReference playerIndex;
    Integer player_count;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView lobbyNameTV;
    TextView playerNameTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void host(View view)
    {
        //Should do a query to see if a lobby with that name already exists

        lobbyNameTV = this.findViewById(R.id.lobby_name_et);
        String lobby_name = lobbyNameTV.getText().toString();

        playerNameTV = this.findViewById(R.id.player_name_et);
        String player_name = playerNameTV.getText().toString();

        lobbyCount.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful())
            {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else
            {
                //Will need to add code to create a lobby object
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                //this should be  dynamic lobby
                lobbyIndex = firebaseDatabase.getReference("lobbies/0");
                lobbyIndex.child("name").setValue(lobby_name);
                lobbyIndex.child("gamestate").setValue(0);

                player_count = 1;
                playerIndex = firebaseDatabase.getReference("lobbies/0/players/0");
                playerIndex.child("name").setValue(player_name);
                playerIndex.child("host").setValue(1);

                lobbyIndex.child("players").child("count").setValue(1);

                int lobby_count = Integer.parseInt(String.valueOf(task.getResult().getValue()));
                lobbyCount.setValue(lobby_count + 1);

                lobby_index = String.valueOf(lobbyIndex);

                editor.putInt("LOBBY_INDEX", 0);
                editor.putString("PLAYER_NAME", player_name);
                editor.putString("LOBBY_NAME", lobby_name);
                editor.putInt("PLAYER_INDEX", 0);
                editor.apply();
                Intent intent = new Intent(view.getContext(), HostLobby.class);
                startActivity(intent);
            }
        });
    }
}