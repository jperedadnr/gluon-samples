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

import com.gluonhq.attach.browser.BrowserService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.samples.functionmapper.FunctionMapper;
import com.gluonhq.samples.functionmapper.model.StackEntry;
import com.gluonhq.samples.functionmapper.model.StackOwner;
import com.gluonhq.samples.functionmapper.model.StackResponse;
import com.gluonhq.samples.functionmapper.service.RemoteService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class DetailPresenter extends GluonPresenter<FunctionMapper> {

    @Inject
    private RemoteService remoteService;
    
    @FXML
    private View detail;
    
    @FXML
    private ListTile listTile;

    @FXML
    private CharmListView<StackEntry, Integer> charmListView;
    
    public void initialize() {
        charmListView.setPlaceholder(new Label("No answers yet"));
        charmListView.setHeadersFunction(StackEntry::getScore);
        charmListView.setHeaderComparator((e1, e2) -> e2.compareTo(e1));
        charmListView.setComparator((e1, e2) -> e2.getOwner().getReputation() - e1.getOwner().getReputation());
        charmListView.setHeaderCellFactory(p -> new CharmListCell<StackEntry>() {
            
            private final Label label;
            private final Icon up, down;
            {
                label = new Label();
                up = new Icon(MaterialDesignIcon.THUMB_UP);
                up.getStyleClass().add("up");
                down = new Icon(MaterialDesignIcon.THUMB_DOWN);
                down.getStyleClass().add("down");
            }
            @Override
            public void updateItem(StackEntry item, boolean empty) {
                super.updateItem(item, empty); 
                if (item != null && !empty) {
                    final int score = item.getScore();
                    label.setGraphic(score >= 0 ? up : down);
                    label.setText("Score: " + score);
                    setGraphic(label);
                } else {
                    setGraphic(null);
                }
            }
            
        });
        charmListView.setCellFactory(p -> new CharmListCell<StackEntry>() {
            
            private final ListTile tile;
            private final ImageView imageView;
            private final Icon icon;
            private final Icon iconAnswer;
            {
                tile = new ListTile();
                imageView = new ImageView();
                tile.setPrimaryGraphic(imageView);
                icon = new Icon(MaterialDesignIcon.CHECK_CIRCLE);
                iconAnswer = new Icon(MaterialDesignIcon.QUESTION_ANSWER);
                tile.setSecondaryGraphic(new VBox(10, icon, iconAnswer));
            }
            
            @Override
            public void updateItem(StackEntry item, boolean empty) {
                super.updateItem(item, empty); 
                if (item != null && !empty) {
                    imageView.setImage(Util.getImage(item.getOwner().getProfile_image()));
                    tile.textProperty().setAll(item.getOwner().getDisplay_name(),
                            "Reputation: " + item.getOwner().getReputation(), 
                            "Answered: " + Util.FORMATTER.format(LocalDateTime.ofEpochSecond(item.getCreation_date(), 0, ZoneOffset.UTC)));
                    icon.setVisible(item.isIs_accepted());
                    iconAnswer.setOnMouseClicked(e ->
                            AppViewManager.QUESTION_VIEW.switchView().ifPresent(p ->
                                    ((QuestionPresenter) p).setStackEntry(item)));

                    setGraphic(tile);
                } else {
                    setGraphic(null);
                }
            }
            
        });
        
        detail.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.CHEVRON_LEFT.button(e -> 
                        getApp().goHome()));
                appBar.setTitleText("Question Details");
            }
        });
    }
    
    public void setStackEntry(StackEntry stackEntry) {
        StackOwner stackOwner = stackEntry.getOwner();
        if (stackOwner != null) {
            getQuestion(stackEntry);
            ImageView imageView = new ImageView(Util.getImage(stackOwner.getProfile_image()));
            imageView.setPreserveRatio(true);
            listTile.setPrimaryGraphic(imageView);
            listTile.setWrapText(true);
            listTile.textProperty().setAll(stackEntry.getTitle(), 
                    stackOwner.getDisplay_name(), 
                    "Created: " + Util.FORMATTER.format(LocalDateTime.ofEpochSecond(stackEntry.getCreation_date(), 0, ZoneOffset.UTC)) + 
                        " - Answers: " + stackEntry.getAnswer_count());
            Icon iconURL = new Icon(MaterialDesignIcon.OPEN_IN_BROWSER);
            Icon iconQuestion = new Icon(MaterialDesignIcon.QUESTION_ANSWER);
            iconQuestion.setOnMouseClicked(e ->
                    AppViewManager.QUESTION_VIEW.switchView().ifPresent(p ->
                            ((QuestionPresenter) p).setStackEntry(stackEntry)));
            iconURL.setOnMouseClicked(e -> {
                String url = stackEntry.getLink();

                BrowserService.create()
                        .ifPresent(browser -> {
                                try {
                                    browser.launchExternalBrowser(url);
                                } catch (IOException | URISyntaxException ex) {
                                    System.out.println("Error launching url ex = " + ex);
                                }
                            });
            });
            listTile.setSecondaryGraphic(new VBox(10, iconURL, iconQuestion));
        }
        getAnswers(String.valueOf(stackEntry.getQuestion_id()));
      }

    private void getAnswers(String questionId) {
        charmListView.setItems(FXCollections.emptyObservableList());
        GluonObservableObject<StackResponse> answersStackOverflow = remoteService.answersStackOverflow(questionId);
        answersStackOverflow.exceptionProperty().addListener((obs, ov, nv) ->
        {
            System.out.println("Error getting answers for questionId " + questionId + ": "  + nv);
            nv.printStackTrace();
        });
        answersStackOverflow.initializedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                charmListView.setItems(FXCollections.observableArrayList(answersStackOverflow.get().getItems()));
            }
        });
    }

    private void getQuestion(StackEntry stackEntry) {
        String questionId = String.valueOf(stackEntry.getQuestion_id());
        GluonObservableObject<StackResponse> questionStackOverflow = remoteService.SOQuestion(questionId);
        questionStackOverflow.exceptionProperty().addListener((obs, ov, nv) ->
                System.out.println("Error looking for questionId " + questionId + ": "  + nv));
        questionStackOverflow.initializedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                List<StackEntry> items = questionStackOverflow.get().getItems();
                if (!items.isEmpty()) {
                    stackEntry.setBody(items.get(0).getBody());
                }
            }
        });
    }
}
