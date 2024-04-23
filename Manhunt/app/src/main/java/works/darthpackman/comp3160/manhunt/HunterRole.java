package works.darthpackman.comp3160.manhunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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

public class HunterRole extends AppCompatActivity
{
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference lobbyIndex;
    DatabaseReference playerIndex;
    DatabaseReference gameState;
    Integer player_index;
    Integer player_count;
    Integer host;
    TextView timer;
    private int min;
    private int second;
    private int time = 120;
    ValueEventListener gamestatelistener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunter_role);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        lobbyIndex = firebaseDatabase.getReference("lobbies/0");
        player_index = sharedPreferences.getInt("PLAYER_INDEX", 0);
        playerIndex = firebaseDatabase.getReference("lobbies/0/player/"+ player_index);
        gameState = firebaseDatabase.getReference("lobbies/0/gamestate");
        host = sharedPreferences.getInt("HOST_STATUS", 0);
        editor.putInt("TIMER", 900);
        editor.apply();

        lobbyIndex.child("players").child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                player_count = Integer.parseInt(String.valueOf(task.getResult().getValue()));
                editor.putInt("PLAYER_COUNT", player_count);
                editor.apply();
            }
        });

        Intent intent = new Intent(this, HunterPlay.class);

        timer = findViewById(R.id.timer_tv);

        new CountDownTimer(120000, 1000) {

            public void onTick(long millisUntilFinished)
            {
                min = time / 60;
                second = time - (min * 60);
                timer.setText(min + ":" + second);
                time--;
            }

            public void onFinish()
            {
                if (host == 1)
                {
                    gameState.setValue(2);
                }
            }
        }.start();

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
                        if (Integer.parseInt(String.valueOf(task.getResult().getValue())) == 2)
                        {
                            startActivity(intent);
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