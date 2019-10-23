package pt.isec.a21260401a21260412.tpamov_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pt.isec.a21260401a21260412.tpamov_android.GameLogic.*;

public class MainActivity extends Activity {
    public static final String FILENAME = "Profile.obj";
    TextView title;
    Button btnOP, btnTP, btnM;
    boolean hasPlayer = false;
    Player profile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.MenuTitle);
        btnOP = findViewById(R.id.btnOnePlayer);
        btnTP = findViewById(R.id.btnTwoPlayers);
        btnM = findViewById(R.id.btnMultiplayer);
        //loadLocate();
        loadProfile();

    }

    public void onOnePlayer(View v){
        if(hasPlayer) {
            Intent onePlayerIntent = new Intent(this, GameActivity.class);
            String mode = "oneplayer";
            onePlayerIntent.putExtra("mode", mode);
            startActivity(onePlayerIntent);
        }else
            Toast.makeText(this, R.string.menu_create_profile_warning,
                    Toast.LENGTH_LONG).show();


    }

    public void onTwoPlayers(View v){
        if(hasPlayer) {
            Intent onePlayerIntent = new Intent(this, GameActivity.class);
            String mode = "twoplayers";
            onePlayerIntent.putExtra("mode", mode);
            startActivity(onePlayerIntent);
        }else
            Toast.makeText(this, R.string.menu_create_profile_warning,
                    Toast.LENGTH_LONG).show();

    }

    public void onMultiplayer(View v){
        if(hasPlayer) {
            Intent multiplayerIntent = new Intent(this, ChooseServerClientActivity.class);
            String mode = "multiplayer";
            multiplayerIntent.putExtra("mode", mode);
            multiplayerIntent.putExtra("profile", profile);
            startActivity(multiplayerIntent);
        }else
            Toast.makeText(this, R.string.menu_create_profile_warning,
                    Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.functionalities_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menuProfile){
            hasPlayer = true;
            Intent profileIntent = new Intent(this, ManageProfileActivity.class);
            startActivity(profileIntent);
        }
        else if(item.getItemId() == R.id.menuHistory){
            Intent historyIntent = new Intent(this, HistoryActivity.class);
            startActivity(historyIntent);
        }else if(item.getItemId() == R.id.menuChangeLanguage){
           showChangeLanguageDialog();
        }else if(item.getItemId() == R.id.menuCredits){
            showCreditsDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCreditsDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.credits);
        alert.setMessage("Marco Lopes n21260401\nMarco Duarte n21260412\n\nReversISEC - 2018/2019");
        // Create TextView
        //final TextView input = new TextView (this);
        //alert.setView(input);

        /*alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //input.setText("hi");
                // Do something with value!
            }
        });
*/
        alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void showChangeLanguageDialog() {
        final String[] lstNames = {"Português", "English"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle(R.string.choose_language);
        mBuilder.setSingleChoiceItems(lstNames, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    setLocate("pt");
                    recreate();
                }else if(which == 1){
                    setLocate("en");
                    recreate();
                }
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setLocate(String type) {
        Locale locale = new Locale(type);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("my_language", type);
        editor.apply();
    }

    public void loadLocate(){
        SharedPreferences preferences = getSharedPreferences("Settings",Activity.MODE_PRIVATE);
        String language = preferences.getString("my_language","");
        setLocate(language);

    }

    public void loadProfile(){
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null || !dir.exists()) {
                //showStatus( "No SDCARD detected");
                return;
            }

            String strFile = dir.getAbsolutePath() + "/" + FILENAME;
            //Toast.makeText(this, "Load2", Toast.LENGTH_LONG).show();

            FileInputStream fis = new FileInputStream(strFile);
            InputStream inputStream = new BufferedInputStream(fis);
            ObjectInputStream in = new ObjectInputStream(inputStream);


            profile = (Player) in.readObject();
            hasPlayer = true;

            //Toast.makeText(this, "Load", Toast.LENGTH_LONG).show();
            //fis.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



}
