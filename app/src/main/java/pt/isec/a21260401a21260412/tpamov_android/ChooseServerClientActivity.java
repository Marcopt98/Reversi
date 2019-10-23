package pt.isec.a21260401a21260412.tpamov_android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import java.util.Locale;

import pt.isec.a21260401a21260412.tpamov_android.GameLogic.Player;

public class ChooseServerClientActivity extends Activity {
    Player profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocate();
        setContentView(R.layout.activity_choose_server_client);
        profile = (Player)getIntent().getSerializableExtra("profile");

    }

    public void onServer(View v){
        Intent serverIntent = new Intent(this, GameActivity.class);
        serverIntent.putExtra("mode", "multiplayer");
        serverIntent.putExtra("connection", GameActivity.SERVER);
        serverIntent.putExtra("profile", profile);
        startActivity(serverIntent);
    }

    public void onClient(View v){
        Intent clientIntent = new Intent(this, GameActivity.class);
        clientIntent.putExtra("mode", "multiplayer");
        clientIntent.putExtra("connection", GameActivity.CLIENT);
        clientIntent.putExtra("profile", profile);
        startActivity(clientIntent);
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
}
