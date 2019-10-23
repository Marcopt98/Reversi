package pt.isec.a21260401a21260412.tpamov_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pt.isec.a21260401a21260412.tpamov_android.GameLogic.*;

public class HistoryActivity extends Activity {
    RecyclerView recyclerView;
    Player player;
    List<Games> lstGames;
    public static final String FILENAME = "Profile.obj";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocate();
        setContentView(R.layout.activity_history);

        loadFromFile();



        recyclerView = findViewById(R.id.RecyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new MyAdapter());


        loadGames();



    }

    public void loadGames(){
        lstGames = new ArrayList<Games>();
        lstGames = player.getGamesList();

        recyclerView.requestLayout();
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



    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

        class MyViewHolder extends RecyclerView.ViewHolder{

            ImageView imageViewHistory;
            TextView textViewHistoryTitle, textViewHistoryScore, textViewHistoryModeGame;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                imageViewHistory = itemView.findViewById(R.id.imageViewHistory);
                textViewHistoryTitle = itemView.findViewById(R.id.textViewHistoryTitle);
                textViewHistoryScore = itemView.findViewById(R.id.textViewHistoryScore);
                textViewHistoryModeGame = itemView.findViewById(R.id.textViewHistoryModeGame);
            }
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_list_layout,viewGroup,false);
            HistoryActivity.MyAdapter.MyViewHolder mvh = new HistoryActivity.MyAdapter.MyViewHolder(v);
            return mvh;
        }


        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder myViewHolder, int i) {
            Games game = lstGames.get(i);

            if(game.winner.toString().compareTo(player.name) == 0)
                myViewHolder.textViewHistoryTitle.setText(R.string.win);
            else
                myViewHolder.textViewHistoryTitle.setText(R.string.lost);

            myViewHolder.textViewHistoryModeGame.setText(game.getGameMode());
            myViewHolder.textViewHistoryScore.setText(""+game.getBallsWinner() + "Balls - " + game.getBallsLoser() +" Balls");

            myViewHolder.imageViewHistory.setImageDrawable(getApplicationInfo().loadIcon(getPackageManager()));
        }

        @Override
        public int getItemCount() {
            if(lstGames==null){
                return 0;
            }else{
                return lstGames.size();
            }
        }
    }




    public void loadFromFile(){
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


            player = (Player) in.readObject();


            //Toast.makeText(this, "Load", Toast.LENGTH_LONG).show();
            //fis.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Function to show a alertDialog on Activity with the message you want
    public void messageToShow (String title, String message){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HistoryActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton("OK",null);
        alertDialog.show();
    }
}
