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

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;

public class MainView extends View {

    private final Board board = new Board();

    public MainView() {

        getStyleClass().add("game-view");

        setCenter(board);

        FloatingActionButton fab = new FloatingActionButton(
                MaterialDesignIcon.REFRESH.text,
                e -> board.restart());
        
        fab.showOn(this);
    }
   

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setTitleText("Tic Tac Toe");
        appBar.getActionItems().addAll(MaterialDesignIcon.SETTINGS.button(e -> updateHost()));
    }


    private void updateHost() {
        Dialog<String> dialog = new Dialog<String>("Host", null);

        TextField hostField = new TextField(TicTacToe.getHost());
        dialog.setContent(hostField);

        Button okButton = new Button("OK");
        okButton.setOnAction(e -> {
            dialog.setResult(hostField.getText());
            dialog.hide();
        });

        BooleanBinding urlBinding = Bindings.createBooleanBinding(
                () -> isUrlValid(hostField.getText()),
                hostField.textProperty());
        okButton.disableProperty().bind(urlBinding.not());

        Button cancelButton = new Button("CANCEL");
        cancelButton.setOnAction(e -> dialog.hide());
        dialog.getButtons().addAll(okButton,cancelButton);
        dialog.showAndWait().ifPresent(TicTacToe::setHost);
    }



    private boolean isUrlValid( String url ){
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            URL u = new URL(url); // this would check for the protocol
            u.toURI();            // does the extra checking required for validation of URI
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    
}
