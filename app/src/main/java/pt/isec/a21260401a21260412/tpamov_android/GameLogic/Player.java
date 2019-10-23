package pt.isec.a21260401a21260412.tpamov_android.GameLogic;



import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Player implements Serializable {

    public String name,photoPath;
    public List <Games> listOfGames;
    public int pieces;



    public int ignoreCardCount,playAgainCount;


    public Player (String name, String photoPath){
        this.name = name;
        this.photoPath = photoPath;
        listOfGames = new ArrayList<>();
        pieces = 2;
        ignoreCardCount = playAgainCount = 1;
    }



    public void addGames (Games game){
        listOfGames.add(game);
    }

    public List getGamesList (){return listOfGames;}

    public int getPieces(){
        return pieces;
    }

    public void setPieces(int pieces){
        this.pieces = pieces;
    }

    public int getIgnoreCardCount() {
        return ignoreCardCount;
    }

    public void setIgnoreCardCount(int ignoreCardCount) {
        this.ignoreCardCount = ignoreCardCount;
    }

    public int getPlayAgainCount() {
        return playAgainCount;
    }

    public void setPlayAgainCount(int playAgainCount) {
        this.playAgainCount = playAgainCount;
    }


    public String getName() {
        return name;
    }
}
