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

public class MoveInfo {
    
    public MoveInfo() {}
    
    private String firstPlayer;
    private String priorMoves;
    private String nextMove;
    private String gameStatus; 

    /**
     * @return the firstPlayer
     */
    public String getFirstPlayer() {
        return firstPlayer;
    }

    /**
     * @param firstPlayer the firstPlayer to set
     */
    public void setFirstPlayer(String firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    /**
     * @return the priorMoves
     */
    public String getPriorMoves() {
        return priorMoves;
    }

    /**
     * @param priorMoves the priorMoves to set
     */
    public void setPriorMoves(String priorMoves) {
        this.priorMoves = priorMoves;
    }

    /**
     * @return the nextMove
     */
    public String getNextMove() {
        return nextMove;
    }

    /**
     * @param nextMove the nextMove to set
     */
    public void setNextMove(String nextMove) {
        this.nextMove = nextMove;
    }

    /**
     * @return the gameStatus
     */
    public String getGameStatus() {
        return gameStatus;
    }

    /**
     * @param gameStatus the gameStatus to set
     */
    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }
}
