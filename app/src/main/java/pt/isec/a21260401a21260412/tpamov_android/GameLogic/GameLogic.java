package pt.isec.a21260401a21260412.tpamov_android.GameLogic;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameLogic implements Serializable {

    public static final int NONE = -1;
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int TOP_RIGHT = 4;
    public static final int TOP_LEFT = 5;
    public static final int BOTTOM_RIGHT = 6;
    public static final int BOTTOM_LEFT = 7;
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int EMPTY = -1;

    int move = NONE;

    static int[][] GameTable;

    Player player1, player2;
    List<Position> possiblePlays;
    List<Position> eatedPieces;

    public GameLogic(Player p1, Player p2){
        player1 = p1;
        player2 = p2;
        GameTable = new int[8][8];
        possiblePlays = new ArrayList<Position>();
        eatedPieces = new ArrayList<Position>();
        prepareInitialTable();
    }

    public GameLogic(Player p1) {
        player1 = p1;
        GameTable = new int[8][8];
        possiblePlays = new ArrayList<Position>();
        eatedPieces = new ArrayList<Position>();
        prepareInitialTable();
    }

    public void prepareInitialTable() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                GameTable[i][j] = EMPTY;
            }
        }

        GameTable[3][3] = BLACK;
        GameTable[4][4] = BLACK;
        GameTable[3][4] = WHITE;
        GameTable[4][3] = WHITE;
    }

    public static int countPieces(int pieceColor) {
        int count = 0;
        for(int i=0;i<7;i++){
            for(int j=0;j<7;j++){
                if(GameTable[i][j] == pieceColor)
                    count++;
            }
        }
        return count;
    }

    public int[][] getGameTable() {
        return GameTable;
    }

    public void clearPossiblePlays() {
        possiblePlays = new ArrayList<Position>();
    }

    public void cleareatedPieces() {
        eatedPieces = new ArrayList<Position>();
    }

    public boolean checkPlayerMove(Position pos) {
        for (int i = 0; i < possiblePlays.size(); i++) {
            if (possiblePlays.get(i).getPosX() == pos.getPosX() && possiblePlays.get(i).getPosY() == pos.getPosY())
                return true;
        }
        return false;
    }

    public List<Position> geteatedPieces() {
        return eatedPieces;
    }

    public List<Position> getPossiblePlays() {
        return possiblePlays;
    }

    public void checkBestPlay() {
        int best = 0;

        for (int i = 0; i < possiblePlays.size(); i++) {
            if (possiblePlays.get(i).getPosX() == 1 || possiblePlays.get(i).getPosX() == 6 || possiblePlays.get(i).getPosY() == 1 || possiblePlays.get(i).getPosY() == 6) {
                if ((possiblePlays.size() - 1) > 1) {
                    possiblePlays.remove(possiblePlays.get(i));
                    continue;
                }
            }

            if (possiblePlays.get(i).getPosX() == 0 || possiblePlays.get(i).getPosX() == 7 || possiblePlays.get(i).getPosY() == 0 || possiblePlays.get(i).getPosY() == 7) {
                for (int j = 0; j < possiblePlays.size(); j++) {
                    if ((possiblePlays.get(i).getPosX() != 0 || possiblePlays.get(i).getPosX() != 7) && (possiblePlays.get(i).getPosY() != 0 || possiblePlays.get(i).getPosY() != 7)) {
                        if ((possiblePlays.size() - 1) > 1) {
                            possiblePlays.remove(possiblePlays.get(i));
                            continue;
                        }
                    }
                }
            }

        }
    }

    public void eatOpponentPieces(Position pos, int pieceColor) {
        int tmpX, tmpY;


        if (pos.getPosX() < 7) {
            if (GameTable[pos.getPosX() + 1][pos.getPosY()] != EMPTY && GameTable[pos.getPosX() + 1][pos.getPosY()] != pieceColor) {
                tmpX = pos.getPosX();
                tmpY = pos.getPosY();
                int count = 0;

                while (tmpX < 7) {
                    if (GameTable[tmpX][tmpY] == EMPTY)
                        count++;

                    if (GameTable[tmpX + 1][tmpY] == pieceColor && count == 0) {
                        int tmpXX = tmpX + 1;
                        while (tmpXX > pos.getPosX()) {
                            tmpXX--;
                            eatedPieces.add(new Position(tmpXX, tmpY));
                            GameTable[tmpXX][tmpY] = pieceColor;
                        }
                        break;
                    }
                    tmpX++;
                }

                /*
                while (tmpX < 7 && GameTable[tmpX + 1][tmpY] != EMPTY && GameTable[tmpX + 1][tmpY] != pieceColor) {
                    tmpX++;
                    eatedPieces.add(new Position(tmpX, tmpY));
                    //TODO modificicar a matriz da logica do jogo
                    GameTable[tmpX][tmpY] = pieceColor;
                }
                */
            }
        }

        if (pos.getPosY() < 7) {
            if (GameTable[pos.getPosX()][pos.getPosY() + 1] != EMPTY && GameTable[pos.getPosX()][pos.getPosY() + 1] != pieceColor) {
                tmpX = pos.getPosX();
                tmpY = pos.getPosY();
                int count = 0;

                while (tmpY < 7) {
                    if (GameTable[tmpX][tmpY] == EMPTY)
                        count++;

                    if (GameTable[tmpX][tmpY + 1] == pieceColor && count == 0) {
                        int tmpYY = tmpY + 1;
                        while (tmpYY > pos.getPosY()) {
                            tmpYY--;
                            eatedPieces.add(new Position(tmpX, tmpYY));
                            GameTable[tmpX][tmpYY] = pieceColor;
                        }
                        break;
                    }
                    tmpY++;
                }

                /*
                while (tmpY < 7 && GameTable[tmpX][tmpY + 1] != EMPTY && GameTable[tmpX][tmpY + 1] != pieceColor) {
                    tmpY++;
                    eatedPieces.add(new Position(tmpX, tmpY));
                    //TODO modificicar a matriz da logica do jogo
                    GameTable[tmpX][tmpY] = pieceColor;
                }
                */
            }
        }

        if (pos.getPosX() > 0) {
            if (GameTable[pos.getPosX() - 1][pos.getPosY()] != EMPTY && GameTable[pos.getPosX() - 1][pos.getPosY()] != pieceColor) {
                tmpX = pos.getPosX();
                tmpY = pos.getPosY();
                int count = 0;

                while (tmpX > 0) {
                    if (GameTable[tmpX][tmpY] == EMPTY)
                        count++;

                    if (GameTable[tmpX - 1][tmpY] == pieceColor && count == 0) {
                        int tmpXX = tmpX - 1;
                        while (tmpXX < pos.getPosX()) {
                            tmpXX++;
                            eatedPieces.add(new Position(tmpXX, tmpY));
                            GameTable[tmpXX][tmpY] = pieceColor;
                        }
                        break;
                    }
                    tmpX--;
                }

                /*
                while (tmpX > 0 && GameTable[tmpX - 1][tmpY] != EMPTY && GameTable[tmpX - 1][tmpY] != pieceColor) {
                    tmpX--;
                    eatedPieces.add(new Position(tmpX, tmpY));
                    //TODO modificicar a matriz da logica do jogo
                    GameTable[tmpX][tmpY] = pieceColor;
                }
                */
            }
        }

        if (pos.getPosY() > 0) {
            if (GameTable[pos.getPosX()][pos.getPosY() - 1] != EMPTY && GameTable[pos.getPosX()][pos.getPosY() - 1] != pieceColor) {
                tmpX = pos.getPosX();
                tmpY = pos.getPosY();
                int count = 0;

                while (tmpY > 0) {
                    if (GameTable[tmpX][tmpY] == EMPTY)
                        count++;

                    if (GameTable[tmpX][tmpY - 1] == pieceColor && count == 0) {
                        int tmpYY = tmpY - 1;
                        while (tmpYY < pos.getPosY()) {
                            tmpYY++;
                            eatedPieces.add(new Position(tmpX, tmpYY));
                            GameTable[tmpX][tmpYY] = pieceColor;
                        }
                        break;
                    }
                    tmpY--;
                }
                /*
                while (tmpX > 0 && GameTable[tmpX][tmpY - 1] != EMPTY && GameTable[tmpX][tmpY - 1] != pieceColor) {
                    tmpY--;
                    eatedPieces.add(new Position(tmpX, tmpY));
                    //TODO modificicar a matriz da logica do jogo
                    GameTable[tmpX][tmpY] = pieceColor;
                }
                */
            }
        }


        //VERIFICAR NA DIAGONAL
        if (pos.getPosX() < 7 && pos.getPosY() < 7) {
            if (GameTable[pos.getPosX() + 1][pos.getPosY() + 1] != EMPTY && GameTable[pos.getPosX() + 1][pos.getPosY() + 1] != pieceColor) {
                tmpX = pos.getPosX();
                tmpY = pos.getPosY();
                int count = 0;

                while (tmpX < 7 && tmpY < 7) {
                    if (GameTable[tmpX][tmpY] == EMPTY)
                        count++;

                    if (GameTable[tmpX + 1][tmpY + 1] == pieceColor && count == 0) {
                        int tmpXX = tmpX + 1;
                        int tmpYY = tmpY + 1;
                        while (tmpXX > pos.getPosX() && tmpYY > pos.getPosY()) {
                            tmpXX--;
                            tmpYY--;
                            eatedPieces.add(new Position(tmpXX, tmpYY));
                            GameTable[tmpXX][tmpYY] = pieceColor;
                        }
                        break;
                    }
                    tmpX++;
                    tmpY++;
                }
                /*
                while (tmpX < 7 && tmpY < 7 && GameTable[tmpX + 1][tmpY + 1] != EMPTY && GameTable[tmpX + 1][tmpY + 1] != pieceColor) {
                    tmpX++;
                    tmpY++;
                    eatedPieces.add(new Position(tmpX, tmpY));
                    //TODO modificicar a matriz da logica do jogo
                    GameTable[tmpX][tmpY] = pieceColor;
                }*/
            }
        }

        if (pos.getPosX() > 0 && pos.getPosY() > 0) {
            if (GameTable[pos.getPosX() - 1][pos.getPosY() - 1] != EMPTY && GameTable[pos.getPosX() - 1][pos.getPosY() - 1] != pieceColor) {
                tmpX = pos.getPosX();
                tmpY = pos.getPosY();
                int count = 0;

                while (tmpX > 0 && tmpY > 0) {
                    if (GameTable[tmpX][tmpY] == EMPTY)
                        count++;

                    if (GameTable[tmpX - 1][tmpY - 1] == pieceColor && count == 0) {
                        int tmpXX = tmpX - 1;
                        int tmpYY = tmpY - 1;
                        while (tmpXX < pos.getPosX() && tmpYY < pos.getPosY()) {
                            tmpXX++;
                            tmpYY++;
                            eatedPieces.add(new Position(tmpXX, tmpYY));
                            GameTable[tmpXX][tmpYY] = pieceColor;
                        }
                        break;
                    }
                    tmpX--;
                    tmpY--;
                }


                /*
                while (tmpX > 0 && tmpY > 0 && GameTable[tmpX - 1][tmpY - 1] != EMPTY && GameTable[tmpX - 1][tmpY - 1] != pieceColor) {
                    tmpX--;
                    tmpY--;
                    eatedPieces.add(new Position(tmpX, tmpY));
                    //TODO modificicar a matriz da logica do jogo
                    GameTable[tmpX][tmpY] = pieceColor;
                }*/
            }
        }

        if (pos.getPosX() < 7 && pos.getPosY() > 0) {
            if (GameTable[pos.getPosX() + 1][pos.getPosY() - 1] != EMPTY && GameTable[pos.getPosX() + 1][pos.getPosY() - 1] != pieceColor) {
                tmpX = pos.getPosX();
                tmpY = pos.getPosY();
                int count = 0;

                while (tmpX < 7 && tmpY > 0) {
                    if (GameTable[tmpX][tmpY] == EMPTY)
                        count++;

                    if (GameTable[tmpX + 1][tmpY - 1] == pieceColor && count == 0) {
                        int tmpXX = tmpX + 1;
                        int tmpYY = tmpY - 1;
                        while (tmpXX > pos.getPosX() && tmpYY < pos.getPosY()) {
                            tmpXX--;
                            tmpYY++;
                            eatedPieces.add(new Position(tmpXX, tmpYY));
                            GameTable[tmpXX][tmpYY] = pieceColor;
                        }
                        break;
                    }
                    tmpX++;
                    tmpY--;
                }

                /*
                while (tmpX < 7 && tmpY > 0 && GameTable[tmpX + 1][tmpY - 1] != EMPTY && GameTable[tmpX + 1][tmpY - 1] != pieceColor) {
                    tmpX++;
                    tmpY--;
                    eatedPieces.add(new Position(tmpX, tmpY));
                    //TODO modificicar a matriz da logica do jogo
                    GameTable[tmpX][tmpY] = pieceColor;
                }*/
            }
        }
        if (pos.getPosX() > 0 && pos.getPosY() < 7) {
            if (GameTable[pos.getPosX() - 1][pos.getPosY() + 1] != EMPTY && GameTable[pos.getPosX() - 1][pos.getPosY() + 1] != pieceColor) {
                tmpX = pos.getPosX();
                tmpY = pos.getPosY();
                int count = 0;
                while (tmpX > 0 && tmpY < 7) {
                    if (GameTable[tmpX][tmpY] == EMPTY)
                        count++;

                    if (GameTable[tmpX - 1][tmpY + 1] == pieceColor && count == 0) {
                        int tmpXX = tmpX - 1;
                        int tmpYY = tmpY + 1;
                        while (tmpXX < pos.getPosX() && tmpYY > pos.getPosY()) {
                            tmpXX++;
                            tmpYY--;
                            eatedPieces.add(new Position(tmpXX, tmpYY));
                            GameTable[tmpXX][tmpYY] = pieceColor;
                        }
                        break;
                    }
                    tmpX--;
                    tmpY++;
                }


                /*
                while (tmpX > 0 && tmpY < 7 && GameTable[tmpX - 1][tmpY + 1] != EMPTY && GameTable[tmpX - 1][tmpY + 1] != pieceColor) {
                    tmpX--;
                    tmpY++;
                    eatedPieces.add(new Position(tmpX, tmpY));
                    //TODO modificicar a matriz da logica do jogo
                    GameTable[tmpX][tmpY] = pieceColor;
                }*/
            }


        }


        return;
    }

    public void setPlayOnMatrix(int pieceColor, Position pos) {
        GameTable[pos.posX][pos.posY] = pieceColor;
    }


    public void makeAllPossiblePlays(int pieceColor) {
        int tmpX, tmpY;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (GameTable[i][j] == pieceColor) {

                    //JOGADOR COM PEÇAS PRETAS
                    if (pieceColor == BLACK) {
                        tmpX = i;
                        tmpY = j;
                        if (tmpX > 0 && tmpY > 0 && tmpX < 7 && tmpY < 7) {
                            if (GameTable[tmpX - 1][tmpY] == WHITE) {
                                tmpX--;
                                while (tmpX > 0 && GameTable[tmpX][tmpY] == WHITE)
                                    tmpX--;

                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX + 1][tmpY] == WHITE) {
                                tmpX++;
                                while (tmpX < 7 && GameTable[tmpX][tmpY] == WHITE)
                                    tmpX++;

                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX][tmpY - 1] == WHITE) {
                                tmpY--;
                                while (tmpY > 0 && GameTable[tmpX][tmpY] == WHITE)
                                    tmpY--;

                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX][tmpY + 1] == WHITE) {
                                tmpY++;
                                while (tmpY < 7 && GameTable[tmpX][tmpY] == WHITE)
                                    tmpY++;

                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            //VERIFICAR NA DIAGONAL

                            if (GameTable[tmpX + 1][tmpY + 1] == WHITE) {
                                tmpX++;
                                tmpY++;
                                while ((tmpX < 7 && tmpY < 7) && GameTable[tmpX][tmpY] == WHITE) {
                                    tmpX++;
                                    tmpY++;
                                }
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX + 1][tmpY - 1] == WHITE) {
                                tmpX++;
                                tmpY--;
                                while ((tmpX < 7 && tmpY > 0) && GameTable[tmpX][tmpY] == WHITE) {
                                    tmpX++;
                                    tmpY--;
                                }
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX - 1][tmpY + 1] == WHITE) {
                                tmpX--;
                                tmpY++;
                                while ((tmpX > 0 && tmpY < 7) && GameTable[tmpX][tmpY] == WHITE) {
                                    tmpX--;
                                    tmpY++;
                                }
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX - 1][tmpY - 1] == WHITE) {
                                tmpX--;
                                tmpY--;
                                while ((tmpX > 0 && tmpY > 0) && GameTable[tmpX][tmpY] == WHITE) {
                                    tmpX--;
                                    tmpY--;
                                }
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                        }
                    }


                    //JOGADOR COM PEÇAS BRANCAS
                    else if (pieceColor == WHITE) {
                        tmpX = i;
                        tmpY = j;

                        if (tmpX > 0 && tmpY > 0 && tmpX < 7 && tmpY < 7) {
                            if (GameTable[tmpX - 1][tmpY] == BLACK) {
                                tmpX--;
                                while (tmpX > 0 && GameTable[tmpX][tmpY] == BLACK)
                                    tmpX--;
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX + 1][tmpY] == BLACK) {
                                tmpX++;
                                while (tmpX < 7 && GameTable[tmpX][tmpY] == BLACK)
                                    tmpX++;

                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX][tmpY - 1] == BLACK) {
                                tmpY--;
                                while (tmpY > 0 && GameTable[tmpX][tmpY] == BLACK)
                                    tmpY--;

                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX][tmpY + 1] == BLACK) {
                                tmpY++;
                                while (tmpY < 7 && GameTable[tmpX][tmpY] == BLACK)
                                    tmpY++;

                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }


                            //VERIFICAR NA DIAGONAL

                            if (GameTable[tmpX + 1][tmpY + 1] == BLACK) {
                                tmpX++;
                                tmpY++;
                                while ((tmpX < 7 && tmpY < 7) && GameTable[tmpX][tmpY] == BLACK) {
                                    tmpX++;
                                    tmpY++;
                                }
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX + 1][tmpY - 1] == BLACK) {
                                tmpX++;
                                tmpY--;
                                while ((tmpX < 7 && tmpY > 0) && GameTable[tmpX][tmpY] == BLACK) {
                                    tmpX++;
                                    tmpY--;
                                }
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX - 1][tmpY + 1] == BLACK) {
                                tmpX--;
                                tmpY++;
                                while ((tmpX > 0 && tmpY < 7) && GameTable[tmpX][tmpY] == BLACK) {
                                    tmpX--;
                                    tmpY++;
                                }
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                                tmpX = i;
                                tmpY = j;
                            }

                            if (GameTable[tmpX - 1][tmpY - 1] == BLACK) {
                                tmpX--;
                                tmpY--;
                                while ((tmpX > 0 && tmpY > 0) && GameTable[tmpX][tmpY] == BLACK) {
                                    tmpX--;
                                    tmpY--;
                                }
                                if (GameTable[tmpX][tmpY] == EMPTY)
                                    possiblePlays.add(new Position(tmpX, tmpY));

                            }
                        }
                    }
                }
            }
        }


    }


}
