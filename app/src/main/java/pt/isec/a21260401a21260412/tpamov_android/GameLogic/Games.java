package pt.isec.a21260401a21260412.tpamov_android.GameLogic;

import java.io.Serializable;

public class Games implements Serializable {
    public String winner,gameMode;
    public int ballsWinner, ballsLoser;

    public Games(String winner, String gameMode, int ballsWinner, int ballsLoser) {
        this.winner = winner;
        this.gameMode = gameMode;
        this.ballsWinner = ballsWinner;
        this.ballsLoser = ballsLoser;
    }

    public String getGameMode() {
        return gameMode;
    }

    public String getWinner() {
        return winner;
    }

    public int getBallsWinner() {
        return ballsWinner;
    }

    public int getBallsLoser() {
        return ballsLoser;
    }
}
