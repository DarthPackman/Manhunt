package works.darthpackman.comp3160.manhunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HuntedTag extends AppCompatActivity
{
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference lobbyIndex;
    DatabaseReference gameState;
    DatabaseReference playerIndex;
    DatabaseReference hunterstate;
    ValueEventListener gamestatelistener;
    ValueEventListener hunterstatelistener;
    TextView tag_tv;
    Integer tag;
    Integer player_index;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int huntcount;
    Integer host;
    int time;
    int counttime;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunted_tag);

        tag_tv = findViewById(R.id.tag_tv);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        lobbyIndex = firebaseDatabase.getReference("lobbies/0");
        gameState = firebaseDatabase.getReference("lobbies/0/gamestate");
        player_index = sharedPreferences.getInt("PLAYER_INDEX", 9);
        playerIndex = firebaseDatabase.getReference("lobbies/0/players").child(String.valueOf(player_index));
        hunterstate = firebaseDatabase.getReference("lobbies/0/players").child(String.valueOf(player_index)).child("hunter");
        host = sharedPreferences.getInt("HOST_STATUS", 0);

        time = sharedPreferences.getInt("TIMER", 900);
        counttime = time * 1000;

        tag = sharedPreferences.getInt("TAG",0000);
        tag_tv.setText(String.valueOf(tag));

        new CountDownTimer(counttime, 1000) {
            public void onTick(long millisUntilFinished)
            {
                time--;
            }
            public void onFinish() {
                if (host == 1)
                {
                    gameState.setValue(3);
                }
            }
        }.start();

        gamestatelistener = gameState.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Intent intent = new Intent(HuntedTag.this, Results.class);
                gameState.get().addOnCompleteListener(task -> {
                    if (Integer.parseInt(String.valueOf(task.getResult().getValue())) == 3 ||
                            Integer.parseInt(String.valueOf(task.getResult().getValue())) == 4)
                    {
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        hunterstatelistener = hunterstate.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Intent intent = new Intent(HuntedTag.this, HunterPlay.class);
                hunterstate.get().addOnCompleteListener(task -> {
                    if (Integer.parseInt(String.valueOf(task.getResult().getValue())) == 1)
                    {
                        lobbyIndex.child("huntercount").get().addOnCompleteListener(task1 -> {
                            huntcount = Integer.parseInt(String.valueOf(task1.getResult().getValue())) + 1;
                            lobbyIndex.child("huntercount").setValue(huntcount);
                            int hunter = 1;
                            editor.putInt("HUNTER_STATUS", hunter);
                            editor.putInt("TIMER", time);
                            editor.apply();
                            startActivity(intent);
                        });
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
        hunterstate.removeEventListener(hunterstatelistener);
        editor.putInt("TIMER",time);
        editor.apply();
    }
}