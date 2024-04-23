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

public class Join extends AppCompatActivity
{
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference lobbyIndex;
    DatabaseReference playerIndex;
    String lobby_index;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Integer player_count;
    TextView lobbyNameTV;
    TextView playerNameTV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void join(View view)
    {
        lobbyNameTV = this.findViewById(R.id.lobby_name_et);
        String lobby_name = lobbyNameTV.getText().toString();

        playerNameTV = this.findViewById(R.id.player_name_et);
        String player_name = playerNameTV.getText().toString();

        //need to develop a search function to query database for matching lobby
        //need to replace 0 with resulting lobby index
        lobbyIndex = firebaseDatabase.getReference("lobbies/0");

        lobbyIndex.child("name").get().addOnCompleteListener(task ->
        {
            if (!task.isSuccessful())
            {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else
            {
                String actual_name = String.valueOf(task.getResult().getValue());
                if (actual_name.equals(lobby_name))
                {
                    lobbyIndex.child("players").child("count").get().addOnCompleteListener(task1 -> {
                        if (!task.isSuccessful())
                        {
                            Log.e("firebase", "Error getting data", task1.getException());
                        }
                        else
                        {
                            Log.d("firebase", String.valueOf(task1.getResult().getValue()));
                            player_count = Integer.parseInt(String.valueOf(task1.getResult().getValue()));

                            //this should create player objects
                            playerIndex = lobbyIndex.child("players").child(String.valueOf(player_count));
                            playerIndex.child("name").setValue(player_name);
                            playerIndex.child("host").setValue(0);

                            lobbyIndex.child("players").child("count").setValue(player_count + 1);

                            lobby_index = String.valueOf(lobbyIndex);

                            editor.putInt("LOBBY_INDEX",0);
                            editor.putString("PLAYER_NAME", player_name);
                            editor.putString("LOBBY_NAME", lobby_name);
                            editor.putInt("PLAYER_INDEX", player_count);
                            editor.apply();
                            Intent intent = new Intent(view.getContext(), JoinLobby.class);
                            startActivity(intent);
                        }
                    });
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"No Lobby with that name exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}