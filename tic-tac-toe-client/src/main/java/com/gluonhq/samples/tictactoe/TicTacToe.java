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

import com.gluonhq.attach.settings.SettingsService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

public class TicTacToe extends MobileApplication {

    private static String HOST =  null;
    private static final String HOST_KEY = "tic.tac.toe.url";

    @Override
    public void init() {
        addViewFactory(HOME_VIEW, MainView::new);
    }

    @Override
    public void postInit(Scene scene) {

        Swatch.BLUE_GREY.assignTo(scene);

        scene.getStylesheets().add(getClass().getResource("/tictactoe.css").toExternalForm());

        ((Stage) scene.getWindow()).getIcons().add(new Image(TicTacToe.class.getResourceAsStream("/icon.png")));

    }

    private static final Optional<SettingsService> SETTINGS_SERVICE = SettingsService.create();

    static String getHost() {
        if (HOST == null) {
            SETTINGS_SERVICE.ifPresent(service -> HOST =  service.retrieve(HOST_KEY));
        }
        return HOST == null ? "http://localhost:8090/server" : HOST;
    }

    static void setHost(String newHost) {
        SETTINGS_SERVICE.ifPresent(service -> service.store(HOST_KEY, newHost));
        HOST = newHost;
    }
}
