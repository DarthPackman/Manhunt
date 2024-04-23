package works.darthpackman.comp3160.manhunt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HunterTag extends AppCompatActivity
{

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference lobbyIndex;
    DatabaseReference gameState;
    DatabaseReference playerIndex;
    ValueEventListener gamestatelistener;
    TextView tag_et;
    Integer player_count;
    Integer tag;
    Integer player_index;
    Integer host;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int counttime;
    int time;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunter_tag);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        tag_et = findViewById(R.id.tag_et);
        lobbyIndex = firebaseDatabase.getReference("lobbies/0");
        gameState = firebaseDatabase.getReference("lobbies/0/gamestate");

        host = sharedPreferences.getInt("HOST_STATUS", 0);
        time = sharedPreferences.getInt("TIMER", 900);
        counttime = time * 1000;

        lobbyIndex.child("players").child("count").get().addOnCompleteListener(task ->
                player_count = Integer.parseInt(String.valueOf(task.getResult().getValue())));

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

        Intent intent = new Intent(this, Results.class);
        gamestatelistener = gameState.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameState.removeEventListener(gamestatelistener);
        editor.putInt("TIMER", time);
        editor.apply();
    }

    public void tag(View view)
    {
        tag = Integer.parseInt(tag_et.getText().toString());
        lobbyIndex.child("players").orderByChild("tag").equalTo(tag).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                lobbyIndex.child("players").child(snapshot.getKey()).child("hunter").setValue(1);

                Intent intent = new Intent(HunterTag.this, HunterPlay.class);
                startActivity(intent);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }
}