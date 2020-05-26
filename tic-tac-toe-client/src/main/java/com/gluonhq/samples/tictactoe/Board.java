/*
 * Copyright (c) 2016, 2020, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of Gluon, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.samples.tictactoe;

import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.RestClient;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.TilePane;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.List;

public class Board extends TilePane {

    public final static int SIZE = 3;
    public final static int GAP = 10;

    public static int NUM_CELLS = 9;
    public static char X_MARK = 'X';
    public static char O_MARK = 'O';
    //public static char EMPTY = 'I';

    public static String INITIAL_BOARD = "IIIIIIIII";

    public boolean gameFinished;

    private boolean isComputerMove = true;

    //private final List<Integer> priorMoves = new LinkedList<>();
    private StringBuffer gameBoard = new StringBuffer(INITIAL_BOARD);

    public Board() {

        super(GAP, GAP);
        getStyleClass().add("board");

        build();

        sceneProperty().addListener((o, os, ns) -> {
            DoubleBinding tileSize = getScene().widthProperty().subtract(GAP * 4).divide(SIZE);
            prefTileHeightProperty().bind(tileSize);
            prefTileWidthProperty().bind(tileSize);
        });

        setTileAlignment(Pos.CENTER);
        setPrefColumns(Board.SIZE);
        setAlignment(Pos.CENTER);
    }


    public void restart() {
        clear();
        isComputerMove = true;
        makeComputerMove();
    }

    private void build() {

        getChildren().clear();

        for (int j = 0; j < Board.SIZE; j++) {
            for (int i = 0; i < Board.SIZE; i++) {
                BoardCell cell = new BoardCell();
                getChildren().add(cell);
                final int fi = i;
                final int fj = j;
                cell.setOnMouseClicked(e -> {

                    if (!isComputerMove && cell.isEmpty() && !isFinished()) {

                        if (whoseTurn() == O_MARK) {
                            cell.setValue(BoardCell.Value.PLAYER);
                        }
                        else {
                            cell.setValue(BoardCell.Value.COMPUTER);
                        }

                        //priorMoves.add(Board.SIZE * fi + fj + 1);
                        gameBoard.setCharAt(Board.SIZE * fj + fi, whoseTurn());

                        /*
                        if (!isFinished()) {
                            isComputerMove = true;
                            makeComputerMove();
                        }
                        */

                        if (!gameFinished) {
                            gameFinished = isFinished();
                            isComputerMove = true;
                            makeComputerMove();
                        }
                    }

                });

            }
        }

    }

    private void makeComputerMove() {
        /*
        String pm = "";
        if (!priorMoves.isEmpty()) {
            pm = Integer.toString(priorMoves.get(0));
            for (int i = 1; i < priorMoves.size(); i++) {
                pm = pm + ","+priorMoves.get(i);
            }
        }
        */
//        String pm = priorMoves.stream().reduce("", (a, b) -> a+","+b, String::concat);
//System.out.println("pm = "+pm);
//        RestClient rc = RestClient.create().method("GET").host("http://t2.lodgon.com/tictactoe")
       RestClient rc = RestClient.create()
               .contentType("application/json")
               .header("Content-Type", "application/json")
               .method("GET")
               .host(TicTacToe.getHost())
               .path("move")
               .queryParam("gameBoard", gameBoard.toString())
               .queryParam("strategy", "neuralNetwork");
                //.queryParam("strategy", "default");
        GluonObservableObject<String> retrieved = DataProvider.retrieveObject(rc.createObjectDataReader(String.class));
        retrieved.exceptionProperty().addListener((obs, ov, nv) -> nv.printStackTrace());
        retrieved.stateProperty().addListener((Observable o) -> {
            if (retrieved.getState() == ConnectState.SUCCEEDED && !gameFinished) {
                //int pcMove = Integer.parseInt(retrieved.get().getNextMove());
                //computerMoved(pcMove);
                System.out.println("ConnectState.SUCCEEDED, retrieved: " + retrieved);

                StringBuffer updatedGameBoard = new StringBuffer(retrieved.get()); //.getGameBoard().trim());

                // TODO: Decide whether to detect newest move by comparing gameBoard request from response, so
                //       only the affected cell needs to be updated

                gameBoard = updatedGameBoard;

                for (int j = 0; j < Board.SIZE; j++) {
                    for (int i = 0; i < Board.SIZE; i++) {
                        char mark = gameBoard.charAt(Board.SIZE * j + i);
                        //System.out.println("mark: " + mark);
                        if (mark == X_MARK) {
                            ((BoardCell) getChildren().get(childIndex(i,j))).setValue(BoardCell.Value.COMPUTER);
                        }
                        else if (mark == O_MARK) {
                            ((BoardCell) getChildren().get(childIndex(i,j))).setValue(BoardCell.Value.PLAYER);
                        }
                        else {
                            ((BoardCell) getChildren().get(childIndex(i,j))).setValue(BoardCell.Value.EMPTY);
                        }
                    }
                }

                isComputerMove = false;
                isFinished();
            }
        });

    }

    /*
    public void computerMoved(int idx) {
        System.out.println("computermove: "+idx);
        gameBoard.setCharAt(idx, 'X');
        //priorMoves.add(idx);
        idx --;
        int i = idx/3;
        int j = idx %3;
        ((BoardCell) getChildren().get(childIndex(i,j))).setValue(BoardCell.Value.COMPUTER);
        isComputerMove = false;
        isFinished();
    }
    */

    private static List<List<Pair<Integer,Integer>>> winLines = Arrays.asList(

        Arrays.asList( new Pair(0,0), new Pair(0,1), new Pair(0,2)),
        Arrays.asList( new Pair(1,0), new Pair(1,1), new Pair(1,2)),
        Arrays.asList( new Pair(2,0), new Pair(2,1), new Pair(2,2)),

        Arrays.asList( new Pair(0,0), new Pair(1,0), new Pair(2,0)),
        Arrays.asList( new Pair(0,1), new Pair(1,1), new Pair(2,1)),
        Arrays.asList( new Pair(0,2), new Pair(1,2), new Pair(2,2)),

        Arrays.asList( new Pair(0,0), new Pair(1,1), new Pair(2,2)),
        Arrays.asList( new Pair(2,0), new Pair(1,1), new Pair(0,2))


    );

    public boolean isFinished() {


        // check for winning combinations

        for(  List<Pair<Integer,Integer>> line: winLines) {

            int sum = 0;

            for ( Pair<Integer,Integer> cell: line ) {
                sum += ((BoardCell) getChildren().get(childIndex( cell.getKey(),cell.getValue()))).getValue().getId();
            }

            if ( Math.abs(sum) == SIZE ) {

                for ( Pair<Integer,Integer> cell: line ) {
                    ((BoardCell) getChildren().get(childIndex( cell.getKey(),cell.getValue()))).setSelected(true);
                }

                return true;

            }

        }

        // check if the board is filled up
        for ( Node node: getChildren()) {
            BoardCell cell = (BoardCell)node;
            if (cell.isEmpty()) return false;
        }

        return false;

    }


    private void clear() {
        gameFinished = false;
        for (Node cell : getChildren()) {
            if (cell instanceof BoardCell) {
                ((BoardCell) cell).setValue(BoardCell.Value.EMPTY);
            }
        }
        gameBoard = new StringBuffer(INITIAL_BOARD);
        //priorMoves.clear();
    }

    private Integer[][] getState() {
        Integer[][] state = new Integer[Board.SIZE][Board.SIZE];
        for (int j = 0; j < Board.SIZE; j++) {
            for (int i = 0; i < Board.SIZE; i++) {
                state[i][j] = ((BoardCell) getChildren().get(childIndex(i,j))).getValue().getId();
            }
        }
        return state;
    }

    private void setState(Integer[][] state) {
        for (int j = 0; j < Board.SIZE; j++) {
            for (int i = 0; i < Board.SIZE; i++) {
                ((BoardCell) getChildren().get(childIndex(i,j))).setValue(BoardCell.Value.fromId(state[i][j]));
        }
        }
    }

    private int childIndex( int row, int col ) {
        return (col + row) + ( col * 2);
    }

    /**
     * Calculate whose turn it is by comparing number of X and O marks, given
     * that X always goes first
     * TODO: Decide whether to return an exception if bad state is detected
     *
     * @return X or O
     */
    private char whoseTurn() {
        char retVal = X_MARK;
        int numXs = 0;
        int numOs = 0;

        for (int idx = 0; idx < NUM_CELLS; idx++) {
            char mark = gameBoard.charAt(idx);
            if (mark == X_MARK) {
                numXs++;
            } else if (mark == O_MARK) {
                numOs++;
            }
        }

        if (numXs == numOs) {
            retVal = X_MARK;
        }
        else if (numXs == numOs + 1) {
            retVal = O_MARK;
        }
        else {
            System.out.println("Invalid gameBoard state: " + gameBoard);
        }

        return retVal;
    }

}
