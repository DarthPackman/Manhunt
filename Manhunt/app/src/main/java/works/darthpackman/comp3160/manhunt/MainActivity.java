package works.darthpackman.comp3160.manhunt;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity
{
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        checkMyPermissions();
    }

    private void checkMyPermissions()
    {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener()
        {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse)
            {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                editor.putBoolean("PERMISSION", true);
                editor.apply();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse)
            {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken)
            {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public void host(View view)
    {
        Intent intent = new Intent(this, Host.class);
        startActivity(intent);
    }

    public void join(View view)
    {
        Intent intent = new Intent(this, Join.class);
        startActivity(intent);
    }
}