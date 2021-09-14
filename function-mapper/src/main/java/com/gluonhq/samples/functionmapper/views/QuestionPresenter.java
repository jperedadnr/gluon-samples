/*
 * Copyright (c) 2017, 2021, Gluon
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
package com.gluonhq.samples.functionmapper.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.samples.functionmapper.FunctionMapper;
import com.gluonhq.samples.functionmapper.model.StackEntry;
import com.gluonhq.samples.functionmapper.service.RemoteService;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.inject.Inject;

public class QuestionPresenter extends GluonPresenter<FunctionMapper> {

    @Inject
    private RemoteService remoteService;
    
    @FXML
    private View question;
    
    @FXML
    private WebView webView;

    public void initialize() {
        question.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.CHEVRON_LEFT.button(e ->
                        AppViewManager.DETAIL_VIEW.switchView()));
                appBar.setTitleText("Question");
            }
        });
    }
    
    public void setStackEntry(StackEntry stackEntry) {
        String body = stackEntry.getBody();
        WebEngine engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((obs, ov, nv) -> {
           if (nv == Worker.State.SUCCEEDED) {
//               engine.executeScript("document.style.overflow = 'hidden';");
           }
        });
        engine.loadContent("<html><head>" +
//                "<style type=\"text/css\">html, body {overflow-x: hidden;}</style>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.sstatic.net/Shared/stacks.css\">" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.sstatic.net/Sites/stackoverflow/primary.css\">" +
                "<style type=\"text/css\">html, body {width: 200px}</style>" +
                "</head><body>" + body + "</body></html");
        webView.setZoom(0.8);
    }
}
