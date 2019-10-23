package pt.isec.a21260401a21260412.tpamov_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.*;

import pt.isec.a21260401a21260412.tpamov_android.GameLogic.*;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
public class GameActivity extends Activity {

    public static final int ONE_PLAYER = 0;
    public static final int TWO_PLAYERS = 1;
    public static final int MULTIPLAYER = 2;
    public static final int SERVER = 0;
    public static final int CLIENT = 1;
    public static final int PORT = 9988; //9988 for emulators or 8899 for real devices
    public static final int ME = 0;
    public static final int OTHER = 1;
    public static final String FILENAME = "Profile.obj";

    public GridView gridView=null;

    public boolean paintInitial = true;
    public boolean playAgain = false;
    public boolean readMove = false;

    Socket socketGame = null;
    ServerSocket serverSocket = null;
    Handler procMsg = null;
    Handler updateView = null;
    DataInputStream input;
    DataOutputStream output;
    ObjectInputStream objInput;
    ObjectOutputStream objOutput;
    PrintWriter pw;
    BufferedReader br;
    ProgressDialog progressDialog;

    JSONObject receivedObject = null;
    JSONObject sentObject = null;

    int mode;
    int connection = SERVER;
    int jogadorAtual = ME;
    int jogada = 0;

    GameLogic gameLogic;
    Player player1, player2, profile;
    TextView playerPieces, opponentPieces, opponentName;
    ImageView opponentImage, playAgainCard, ignoreTurnCard;
    Position pos;
    List<Position> eatedpieces;

    ImageAdapter imageAdapter = null;

    private final static int MESSAGE_UPDATE_TEXT_CHILD_THREAD =1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocate();
        setContentView(R.layout.activity_game);

        loadFromFile();

        if(this.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_game);
        }
        Intent thisIntent = getIntent();
        String playMode = thisIntent.getStringExtra("mode");
        String playerName = thisIntent.getStringExtra("playername");

        if(playMode.compareToIgnoreCase("oneplayer") == 0){
            mode = ONE_PLAYER;
            if(profile == null)
                player1 = new Player(playerName, "");
            else {
                profile.setPieces(2);
                profile.setPlayAgainCount(1);
                profile.setIgnoreCardCount(1);
                player1 = profile;
            }
            player2 = new Player("Computador", "");
        }else if(playMode.compareToIgnoreCase("twoplayers") == 0){
            mode = TWO_PLAYERS;
            if(profile == null)
                player1 = new Player(playerName, "");
            else {
                profile.setPieces(2);
                profile.setPlayAgainCount(1);
                profile.setIgnoreCardCount(1);
                player1 = profile;
            }
            player2 = new Player("Jogador2", "");
        }else if(playMode.compareToIgnoreCase("multiplayer") == 0){
            mode = MULTIPLAYER;
            connection = thisIntent.getIntExtra("connection", SERVER);
            player1 = (Player)thisIntent.getSerializableExtra("profile");
            player2 = new Player("Jogador2", "");
            //player1 = new Player(playerName, "");
        }


        //Checks the connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, R.string.error_netconn, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        procMsg = new Handler();

        playerPieces = findViewById(R.id.PlayerPieces);
        opponentPieces = findViewById(R.id.OpponentPieces);
        opponentName = findViewById(R.id.OpponentName);
        playerPieces.setText("" + playerPieces.getText() + player1.getPieces());
        opponentPieces.setText("" + opponentPieces.getText() + player2.getPieces());
        opponentName.setText("" + player2.getName());



        gameLogic = new GameLogic(player1, player2);
        eatedpieces = new ArrayList<Position>();

        opponentImage = findViewById(R.id.OpponentImage);
        playAgainCard = (ImageView) findViewById(R.id.PlayAgainCard);
        ignoreTurnCard = (ImageView) findViewById(R.id.IgnoreTurnCard);


        gridView = findViewById(R.id.gameGridView);
        imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);


        playAgainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(jogada >= 5){
                    if(jogadorAtual == ME) {
                        if(player1.getPlayAgainCount() ==  1) {
                            playAgain = true;
                            player1.setPlayAgainCount(0);
                            playAgainCard.setImageResource(R.drawable.back_cards);
                        }
                    }else if(jogadorAtual == OTHER){
                        playAgain = true;
                        player2.setPlayAgainCount(0);
                        playAgainCard.setImageResource(R.drawable.back_cards);
                    }
                }
            }
        });

        ignoreTurnCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jogada >= 5) {
                    if (jogadorAtual == ME) {
                        if (player1.getIgnoreCardCount() == 1) {
                            if(mode == ONE_PLAYER) {
                                computerPlay();
                                player1.setIgnoreCardCount(0);
                                ignoreTurnCard.setImageResource(R.drawable.back_cards);
                            }
                            if(mode == TWO_PLAYERS){
                                jogadorAtual = OTHER;
                                player1.setIgnoreCardCount(0);
                                ignoreTurnCard.setImageResource(R.drawable.back_cards);
                            }

                        } else {
                            //Toast a avisar o jogador
                            Toast.makeText(GameActivity.this, R.string.card_warning, Toast.LENGTH_SHORT).show();
                        }
                    }else if(jogadorAtual == OTHER){
                        if(player2.getIgnoreCardCount() == 1){
                            if(mode == TWO_PLAYERS){
                                jogadorAtual = ME;
                                player2.setIgnoreCardCount(0);
                                ignoreTurnCard.setImageResource(R.drawable.back_cards);
                            }
                        } else {
                        // Toast a avisar o jogador
                            Toast.makeText(GameActivity.this, R.string.card_warning, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });



        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //Toast.makeText(GameActivity.this, "" + position,
                //Toast.LENGTH_SHORT).show();
                paintInitial = false;

                checkTurn(view, position);
                //imageAdapter.fields[0] = R.mipmap.black_ball;
                imageAdapter.notifyDataSetChanged();
                gridView.invalidate();


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.change_type_game, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menuOnePlayer) {
            if(mode != ONE_PLAYER){
                if(mode == MULTIPLAYER){

                    try {
                        socketGame.close();
                        communicationThread.interrupt();
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                //Toast.makeText(GameActivity.this, "" + position,
                                //Toast.LENGTH_SHORT).show();
                                paintInitial = false;

                                checkTurn(view, position);
                                //imageAdapter.fields[0] = R.mipmap.black_ball;
                                imageAdapter.notifyDataSetChanged();
                                gridView.invalidate();


                            }
                        });
                        opponentName.setText(R.string.opponent_name_computer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                mode = ONE_PLAYER;
                jogadorAtual = ME;
            }
        }
        if(item.getItemId() == R.id.menuTwoPlayers){
            if(mode != TWO_PLAYERS){
                mode = TWO_PLAYERS;
                jogadorAtual = ME;

                if(mode == MULTIPLAYER){

                    try {
                        socketGame.close();
                        communicationThread.interrupt();
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                //Toast.makeText(GameActivity.this, "" + position,
                                //Toast.LENGTH_SHORT).show();
                                paintInitial = false;

                                checkTurn(view, position);
                                //imageAdapter.fields[0] = R.mipmap.black_ball;
                                imageAdapter.notifyDataSetChanged();
                                gridView.invalidate();


                            }
                        });
                        opponentName.setText(R.string.opponent_name_player);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mode == MULTIPLAYER) {
            if (connection == SERVER) {
                server();
            } else {
                clientDialogBox();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            communicationThread.interrupt();
            if(socketGame != null)
                socketGame.close();
            if(input != null)
                input.close();
            if(output != null)
                output.close();
        }catch (Exception e){
            socketGame = null;
            input = null;
            output = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("Data", gameLogic);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        gameLogic = (GameLogic)savedInstanceState.getSerializable("Data");
    }

    public int positionConvertor(Position pos) {
        int tmpX, tmpY, position;
        String contat=null;
        tmpX = pos.getPosX();
        tmpY = pos.getPosY();

        if (tmpX == 0) {
            tmpX = 1;
            contat = Integer.toString(tmpX) + Integer.toString(tmpY);
        } else if (tmpX == 1) {
            if(tmpY == 0){
                tmpY = 9;
            }else{
                tmpX = 2;
                tmpY -= 1;
            }
            contat = Integer.toString(tmpX) + Integer.toString(tmpY);

        } else if (tmpX == 2) {
            if(tmpY < 2){
                tmpY += 8;
            }else{
                tmpX = 3;
                tmpY -= 2;
            }
            contat = Integer.toString(tmpX) + Integer.toString(tmpY);
        } else if (tmpX == 3) {
            if(tmpY < 3){
                tmpY += 7;
            }else{
                tmpX = 4;
                tmpY -= 3;
            }
            contat = Integer.toString(tmpX) + Integer.toString(tmpY);
        } else if (tmpX == 4) {
            if(tmpY < 4){
                tmpY += 6;
            }else{
                tmpX = 5;
                tmpY -= 4;
            }
            contat = Integer.toString(tmpX) + Integer.toString(tmpY);
        } else if (tmpX == 5) {
            if(tmpY < 5){
                tmpY += 5;
            }else{
                tmpX = 6;
                tmpY -= 5;
            }
            contat = Integer.toString(tmpX) + Integer.toString(tmpY);
        } else if (tmpX == 6) {
            if(tmpY < 6){
                tmpY += 4;
            }else{
                tmpX = 7;
                tmpY -= 6;
            }
            contat = Integer.toString(tmpX) + Integer.toString(tmpY);
        } else if (tmpX == 7) {
            if(tmpY < 7){
                tmpY += 3;
            }else{
                tmpX = 8;
                tmpY -= 7;
            }
            contat = Integer.toString(tmpX) + Integer.toString(tmpY);
        }

        position = Integer.parseInt(contat);
        return position;
    }

    public Position positionConvertor(int position) {
        Position pos;
        int lastDigit, x = 0, y = 0;
        lastDigit = position % 10;

        if (position >= 10 && position < 18) {
            x = 0;
            y = lastDigit;
        } else if (position >= 19 && position < 27) {
            x = 1;
            y = (lastDigit + 1) % 10;
        } else if (position >= 28 && position < 36) {
            x = 2;
            y = (lastDigit + 2) % 10;
        } else if (position >= 37 && position < 45) {
            x = 3;
            y = (lastDigit + 3) % 10;
        } else if (position >= 46 && position < 54) {
            x = 4;
            y = (lastDigit + 4) % 10;
        } else if (position >= 55 && position < 63) {
            x = 5;
            y = (lastDigit + 5) % 10;
        } else if (position >= 64 && position < 72) {
            x = 6;
            y = (lastDigit + 6) % 10;
        } else if (position >= 73 && position < 81) {
            x = 7;
            y = (lastDigit + 7) % 10;
        }

        pos = new Position(x, y);

        return pos;
    }

    public void computerPlay(){

        ImageView imageToPaint = null;

        gameLogic.makeAllPossiblePlays(OTHER);
        //if(gameLogic.getPossiblePlays().size() == 0)
        //gameLogic.checkBestPlay();


        if(gameLogic.getPossiblePlays().size() < 1) {
            gameLogic.makeAllPossiblePlays(OTHER);
            if(gameLogic.getPossiblePlays().size() < 1)
                checkWinner();
            return;
        }

        Random r = new Random();
        int low = 0;
        int high = 1;
        if(gameLogic.getPossiblePlays().size() > 1)
             high = gameLogic.getPossiblePlays().size() - 1;
        else
             high = 1;

        int result = r.nextInt(high-low) + low;

        if(result >= 0)
            pos = gameLogic.getPossiblePlays().get(result);




        gameLogic.setPlayOnMatrix(OTHER, pos);
        gameLogic.eatOpponentPieces(pos, OTHER);
        eatedpieces = gameLogic.geteatedPieces();

        int computerPos = positionConvertor(pos);
        imageToPaint = (ImageView) gridView.getChildAt(computerPos);
        imageToPaint.setBackgroundColor(Color.parseColor("#008000"));
        imageToPaint.setImageResource(R.mipmap.black_ball);
        imageAdapter.notifyDataSetChanged();

        for(int i=0;i<eatedpieces.size();i++){
            int fieldPos = positionConvertor(eatedpieces.get(i));
            imageToPaint = (ImageView) gridView.getChildAt(fieldPos);
            imageToPaint.setBackgroundColor(Color.parseColor("#008000"));
            imageToPaint.setImageResource(R.mipmap.black_ball);
            imageAdapter.notifyDataSetChanged();

        }

        gameLogic.cleareatedPieces();
        gameLogic.clearPossiblePlays();
        eatedpieces = new ArrayList<Position>();

        player2.setPieces(GameLogic.countPieces(OTHER));
        player1.setPieces(GameLogic.countPieces(ME));
        opponentPieces.setText("" + getText(R.string.black_pieces) + player2.getPieces());
        playerPieces.setText("" + getText(R.string.white_pieces) + player1.getPieces());

        return;
    }


    public void checkTurn(View view, int position) {
        final int posM = position;

        ImageView imageView = (ImageView) view;
        ImageView imageToPaint = null;

        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (jogadorAtual == ME) {
            gameLogic.makeAllPossiblePlays(ME);

            if(gameLogic.getPossiblePlays().size() < 1)
                checkWinner();

            pos = positionConvertor(position);
            //verify if the play is on all possible plays array
            if (gameLogic.checkPlayerMove(pos)) {
                gameLogic.setPlayOnMatrix(ME, pos);
                gameLogic.eatOpponentPieces(pos, ME);
                eatedpieces = gameLogic.geteatedPieces();


                for(int i=0;i<eatedpieces.size();i++){

                    //imageAdapter.fields[0] = R.mipmap.white_ball;
                    int fieldPos = positionConvertor(eatedpieces.get(i));

                    imageToPaint = (ImageView) gridView.getChildAt(fieldPos);

                    imageToPaint.setBackgroundColor(Color.parseColor("#008000"));

                    imageToPaint.setImageResource(R.mipmap.white_ball);

                    imageAdapter.notifyDataSetChanged();
                }


                gameLogic.cleareatedPieces();
                gameLogic.clearPossiblePlays();
                eatedpieces = new ArrayList<Position>();
                jogada++;
                if(jogada == 5){
                    //VIRAR AS CARTAS

                    ignoreTurnCard.setImageResource(R.mipmap.ignore_turn_card);
                    playAgainCard.setImageResource(R.mipmap.play_again_card);

                }
            } else {
                Toast.makeText(GameActivity.this, R.string.invalid_warning, Toast.LENGTH_SHORT).show();
                gameLogic.clearPossiblePlays();
                return;
            }

                imageView.setImageResource(R.mipmap.white_ball);
            player2.setPieces(GameLogic.countPieces(OTHER));
            player1.setPieces(GameLogic.countPieces(ME));
            opponentPieces.setText("" + getText(R.string.black_pieces) + player2.getPieces());
            playerPieces.setText("" + getText(R.string.white_pieces) + player1.getPieces());


            if(mode == ONE_PLAYER){
                if(playAgain) {
                    playAgain = false;
                    return;
                }
                computerPlay();

                return;
            }else if(mode == TWO_PLAYERS) {
                if(playAgain){
                    playAgain = false;
                    return;
                }
            }else if(mode == MULTIPLAYER){
                if(playAgain){
                    playAgain = false;
                    return;
                }
            }

            if(mode == MULTIPLAYER){
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Log.d("RPS", "Sending a move: " + moves[ME]);
                            sentObject = new JSONObject();
                            sentObject.put("Pos", posM);
                            output.writeUTF(sentObject.toString());
                            readMove = true;
                        } catch (Exception e) {
                            Log.d("RPS", "Error sending a move");
                        }
                    }
                });
                t.start();
            }
            jogadorAtual = OTHER;
        } else { // se chega aqui é porque é two players

            gameLogic.makeAllPossiblePlays(OTHER);

            if(gameLogic.getPossiblePlays().size() < 1)
                checkWinner();

            pos = positionConvertor(position);
            //verify if the play is on all possible plays array
            if (gameLogic.checkPlayerMove(pos)) {
                gameLogic.setPlayOnMatrix(OTHER, pos);
                gameLogic.eatOpponentPieces(pos, OTHER);
                eatedpieces = gameLogic.geteatedPieces();


                for(int i=0;i<eatedpieces.size();i++){
                    int fieldPos = positionConvertor(eatedpieces.get(i));

                    imageToPaint = (ImageView) gridView.getChildAt(fieldPos);

                    imageToPaint.setBackgroundColor(Color.parseColor("#008000"));
/*                    if(mode == MULTIPLAYER){
                        if(connection == SERVER)
                            imageView.setImageResource(R.mipmap.white_ball);
                        else
                            imageView.setImageResource(R.mipmap.black_ball);
                    }
                    else*/
                    imageToPaint.setImageResource(R.mipmap.black_ball);

                    imageAdapter.notifyDataSetChanged();
                }

                gameLogic.cleareatedPieces();
                gameLogic.clearPossiblePlays();
                eatedpieces = new ArrayList<Position>();
            } else {
                Toast.makeText(GameActivity.this, R.string.invalid_warning, Toast.LENGTH_SHORT).show();
                gameLogic.clearPossiblePlays();
                return;
            }
/*            if(mode == MULTIPLAYER){
                if(connection == SERVER)
                    imageView.setImageResource(R.mipmap.white_ball);
                else
                    imageView.setImageResource(R.mipmap.black_ball);
            }
            else*/
            imageView.setImageResource(R.mipmap.black_ball);
            player2.setPieces(GameLogic.countPieces(OTHER));
            player1.setPieces(GameLogic.countPieces(ME));
            opponentPieces.setText("" + getText(R.string.black_pieces) + player2.getPieces());
            playerPieces.setText("" + getText(R.string.white_pieces) + player1.getPieces());

            if(playAgain){
                playAgain = false;
                return;
            }

            if(mode == MULTIPLAYER){
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Log.d("RPS", "Sending a move: " + moves[ME]);
                            sentObject = new JSONObject();
                            sentObject.put("Pos", posM);
                            output.writeUTF(sentObject.toString());
                            readMove = true;
                        } catch (Exception e) {
                            Log.d("RPS", "Error sending a move");
                        }
                    }
                });
                t.start();
            }
            jogadorAtual = ME;
        }

    }



    public void saveProfile(){

        try {
            File dir = getExternalFilesDir(null);
            if (dir == null || !dir.exists()) {
                //showStatus( "No SDCARD detected");
                return;
            }
            File file = new File(dir,FILENAME);
            //String strFile = dir.getAbsolutePath() + "/" + FILENAME;
            FileOutputStream fos = new FileOutputStream(file);
            OutputStream outputStream = new BufferedOutputStream(fos);
            ObjectOutputStream ps = new ObjectOutputStream(outputStream);

            ps.writeObject(player1);
            //Toast.makeText(this, "Save", Toast.LENGTH_LONG).show();
            //fos.close();
            ps.close();
            //showStatus( "Written in ES: " + str);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }


    private void checkWinner() {
        if(player1.getPieces() > player2.getPieces()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            if(mode == ONE_PLAYER)
                player1.addGames(new Games(player1.getName(),"Single Player", player1.getPieces(),player2.getPieces()));
            else if(mode == TWO_PLAYERS)
                player1.addGames(new Games(player1.getName(),"Two Players", player1.getPieces(),player2.getPieces()));
            else if(mode == MULTIPLAYER)
                player1.addGames(new Games(player1.getName(),"Multiplayer", player1.getPieces(),player2.getPieces()));


            saveProfile();

            alert.setTitle(R.string.game_winner_title);
            alert.setMessage("The winner of the game is " + player1.getName());

            alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            alert.show();
        }
        if(player1.getPieces() < player2.getPieces()){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            if(mode == ONE_PLAYER)
                player1.addGames(new Games(player2.getName(),"Single Player", player1.getPieces(),player2.getPieces()));
            else if(mode == TWO_PLAYERS)
                player1.addGames(new Games(player2.getName(),"Two Players", player1.getPieces(),player2.getPieces()));
            else if(mode == MULTIPLAYER)
                player1.addGames(new Games(player2.getName(),"Multiplayer", player1.getPieces(),player2.getPieces()));

            saveProfile();

            alert.setTitle(R.string.game_winner_title);
            alert.setMessage("The winner of the game is " + player2.getName());

            alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            alert.show();
        }
    }


    //Function of the server
    public void server(){

        String ipAddress = getLocalIpAddress();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.server_dialog_msg) + "\n(IP: )" + ipAddress + ")");
        progressDialog.setTitle(getString(R.string.server_dialog_title));
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
                if(serverSocket != null){
                    try {
                        serverSocket.close();
                    }catch (IOException e){
                    }
                    serverSocket = null;
                }
            }
        });
        progressDialog.show();

        //Thread of the server
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    serverSocket = new ServerSocket(PORT);
                    socketGame = serverSocket.accept();
                    serverSocket.close();
                    serverSocket = null;
                    communicationThread.start();
                }catch (Exception e){
                    e.printStackTrace();
                    socketGame = null;
                }
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if(socketGame == null)
                            finish();
                    }
                });

            }
        });
        t.start();
    }
    //Function of the Dialog Box in the client
    public void clientDialogBox(){
        final EditText editIP = new EditText(this);
        AlertDialog setIP = new AlertDialog.Builder(this).setTitle(R.string.client_dialog_title).setMessage(R.string.client_dialog_msg).setView(editIP)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        client(editIP.getText().toString(), PORT);
                    }

                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                }).create();

        setIP.show();
    }

    //Function of the Client
    public void client(final String ip, final int port){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socketGame = new Socket(ip, port);
                }catch (Exception e){
                    socketGame = null;
                }
                if(socketGame == null){
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                    return;
                }
                communicationThread.start();
            }
        });
        t.start();
    }

    //Thread for the in game communication
    Thread communicationThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try{

                output = new DataOutputStream(socketGame.getOutputStream());
                input = new DataInputStream(socketGame.getInputStream());


                while(!Thread.currentThread().isInterrupted()) {

                    while (opponentName.getText().equals("Jogador2")) {



                        sentObject = new JSONObject();
                        sentObject.put("Name", player1.getName());
                        //sentObject.put("Image", encodedUrl);
                        output.writeUTF(sentObject.toString());

                        receivedObject = new JSONObject(input.readUTF());


                        //if(receivedObject.has("Name")){
                        GameActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    opponentName.setText(receivedObject.getString("Name"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                            try {
                                    String receivedString = input.readUTF();
                                    receivedObject = new JSONObject(receivedString);
                                    readMove = false;
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }catch (IOException e) {
                                e.printStackTrace();
                            }catch (Exception e){}

                            procMsg.post(new Runnable() {
                                @Override
                                public void run() {
                                    //Read JSON Object and convert info into Position Object
                                    try {
                                        if (receivedObject != null) {
                                            int pos = receivedObject.getInt("Pos");
                                            ImageView view = (ImageView)gridView.getChildAt(pos);
                                            paintInitial = false;
                                            checkTurn(view, pos);
                                            imageAdapter.notifyDataSetChanged();
                                            gridView.invalidate();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }catch (Exception e){}
                                }
                            });
                        //}
                    }
                //}
            }catch (Exception e){
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socketGame.close();

                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                    //Toast.makeText(GameActivity.this, "" + position,
                                    //Toast.LENGTH_SHORT).show();
                                    paintInitial = false;

                                    checkTurn(view, position);
                                    //imageAdapter.fields[0] = R.mipmap.black_ball;
                                    imageAdapter.notifyDataSetChanged();
                                    gridView.invalidate();


                                }
                            });
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        opponentName.setText(R.string.opponent_name_computer);
                        mode = ONE_PLAYER;
                        jogadorAtual = ME;
                        communicationThread.interrupt();
                        //finish();
                        //Toast.makeText(getApplicationContext(), "The game was finished", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    });

    public static String getLocalIpAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    class UpdateUIThread implements Runnable{
        private String msg;

        public UpdateUIThread(String msg){
            this.msg = msg;
        }

        @Override
        public void run() {
            opponentName.setText(msg);
        }
    }

    /* Update ui text.*/
    private void updateText()
    {
        try {
            opponentName.setText(receivedObject.getString("Name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* Create Handler object in main thread. */
    private void createUpdateUiHandler()
    {
        if(updateView == null)
        {
            updateView = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    // Means the message is sent from child thread.
                    if(msg.what == MESSAGE_UPDATE_TEXT_CHILD_THREAD)
                    {
                        // Update ui in main thread.
                        updateText();
                    }
                }
            };
        }
    }

    //Function to save data containing the winners
    public void saveToFile() {
        try {
            FileOutputStream fileToSave = openFileOutput("historyFile", MODE_APPEND);
            // String infoToSave = //ir buscar as cenas a cada variavel
            //fileToSave.write(infoToSave.getBytes());
            fileToSave.close();

        } catch (FileNotFoundException error) {
            messageToShow("FILE Error", "" + error);
        } catch (IOException errorr) {
            messageToShow("FILE Error", "" + errorr);
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


            profile = (Player) in.readObject();


            //Toast.makeText(this, "Load", Toast.LENGTH_LONG).show();
            //fis.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Function to show a alertDialog on Activity with the message you want
    public void messageToShow(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GameActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton("OK", null);
        alertDialog.show();
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


    public class ImageAdapter extends BaseAdapter {
        private Context context;

        public ImageAdapter(Context c) {
            this.context = c;
        }

        @Override
        public int getCount() {
            return fields.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(105, 105));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //imageView.setPadding(1,8,1,8);
            } else {
                imageView = (ImageView) convertView;
            }
            if (position == 0 || position == 1 || position == 2 || position == 3 || position == 4 || position == 5 || position == 6 || position == 7 || position == 8 || position == 9 || position == 18 || position == 27 || position == 36 || position == 45 || position == 54 || position == 63 || position == 72)
                imageView.setImageResource(fields[position]);
            else
                imageView.setBackgroundColor(fields[position]);

            if(paintInitial) {

                    if (position == 40) {
                        imageView.setBackgroundColor(Color.parseColor("#008000"));
                        imageView.setImageResource(balls[0]);
                    } else if (position == 41) {
                        imageView.setBackgroundColor(Color.parseColor("#008000"));
                        imageView.setImageResource(balls[1]);
                    } else if (position == 49) {
                        imageView.setBackgroundColor(Color.parseColor("#008000"));
                        imageView.setImageResource(balls[1]);
                    } else if (position == 50) {
                        imageView.setBackgroundColor(Color.parseColor("#008000"));
                        imageView.setImageResource(balls[0]);
                    }
                //}
            }

            return imageView;
        }


        private Integer[] balls = {
                R.mipmap.black_ball, R.mipmap.white_ball
        };


        public Integer[] fields = {R.drawable.corner, R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e, R.drawable.f, R.drawable.g,
                R.drawable.h, R.drawable.number1, Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"),
                Color.parseColor("#008000"), Color.parseColor("#008000"), R.drawable.number2, Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"),
                Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), R.drawable.number3, Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"),
                Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), R.drawable.number4, Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"),
                Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), R.drawable.number5, Color.parseColor("#008000"), Color.parseColor("#008000"),
                Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), R.drawable.number6, Color.parseColor("#008000"),
                Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), R.drawable.number7,
                Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"),
                R.drawable.number8, Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000"), Color.parseColor("#008000")

        };
    }


}
