package works.darthpackman.comp3160.manhunt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Results extends AppCompatActivity
{
    Integer host;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView results_tv;
    Integer hunter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://manhunt-f9f08-default-rtdb.firebaseio.com/");
    DatabaseReference gameState;
    DatabaseReference lobbyIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        results_tv = findViewById(R.id.result_tv);

        gameState = firebaseDatabase.getReference("lobbies/0/gamestate");
        lobbyIndex = firebaseDatabase.getReference("lobbies/0");

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        host = sharedPreferences.getInt("HOST_STATUS", 0);
        hunter = sharedPreferences.getInt("HUNTER_STATUS", 0);

        gameState.get().addOnCompleteListener(task ->
        {
            if (hunter == 1 && Integer.parseInt(String.valueOf(task.getResult().getValue())) == 4)
            {
                results_tv.setText("VICTORY");
            }
            else if (hunter == 0 && Integer.parseInt(String.valueOf(task.getResult().getValue())) == 3)
            {
                results_tv.setText("VICTORY");
            }
            else
            {
                results_tv.setText("DEFEAT");
            }
        });
    }

    public void rematch(View view)
    {
        if (host == 1)
        {
            Intent intent = new Intent(this, HostLobby.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this, JoinLobby.class);
            startActivity(intent);
        }
    }

    public void main(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}