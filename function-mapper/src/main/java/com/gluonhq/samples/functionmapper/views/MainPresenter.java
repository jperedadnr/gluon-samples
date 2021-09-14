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
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.control.ToggleButtonGroup;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.samples.functionmapper.FunctionMapper;
import com.gluonhq.samples.functionmapper.model.StackEntry;
import com.gluonhq.samples.functionmapper.model.StackResponse;
import com.gluonhq.samples.functionmapper.service.RemoteService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

public class MainPresenter extends GluonPresenter<FunctionMapper> {

    private static final List<String> TAGS = Arrays.asList("javafx", "gluon", "gluon-mobile", "scenebuilder");
    private static final List<String> ORDER = Arrays.asList("activity", "creation", "votes", "relevance");

    @Inject
    private RemoteService remoteService;
    
    @FXML
    private View main;

    @FXML
    private ComboBox<String> tagComboBox;

    @FXML
    private CharmListView<StackEntry, Integer> charmListView;

    private String lastOrder = "relevance";
    private BooleanProperty inProgress = new SimpleBooleanProperty();

    public void initialize() {
        tagComboBox.getItems().addAll(TAGS);
        tagComboBox.setEditable(true);
        
        charmListView.setPlaceholder(new Label("No items yet\nSelect a tag"));
        charmListView.setHeadersFunction(StackEntry::getScore);
        charmListView.setHeaderComparator((e1, e2) -> e2.compareTo(e1));
        charmListView.setComparator((e1, e2) -> e2.getAnswer_count() - e1.getAnswer_count());
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
        
        charmListView.setCellFactory(p -> new CharmListCell<>() {
            
            private final ListTile tile;
            private final ImageView imageView;
            private final Icon icon;
            {
                tile = new ListTile();
                imageView = new ImageView();
                tile.setPrimaryGraphic(imageView);
                icon = new Icon(MaterialDesignIcon.CHEVRON_RIGHT);
                tile.setSecondaryGraphic(icon);
            }
            
            @Override
            public void updateItem(StackEntry item, boolean empty) {
                super.updateItem(item, empty); 
                if (item != null && !empty) {
                    imageView.setImage(Util.getImage(item.getOwner().getProfile_image()));
                    tile.textProperty().setAll(item.getTitle(),
                            item.getOwner().getDisplay_name(), 
                            "Created: " + Util.FORMATTER.format(LocalDateTime.ofEpochSecond(item.getCreation_date(), 0, ZoneOffset.UTC)) + 
                                    " - Answers: " + item.getAnswer_count());
                    icon.setOnMouseClicked(e -> AppViewManager.DETAIL_VIEW.switchView()
                            .ifPresent(presenter -> ((DetailPresenter) presenter).setStackEntry(item)));
                    setGraphic(tile);
                } else {
                    setGraphic(null);
                }
            }
            
        });
        
        tagComboBox.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            search(lastOrder, nv);
        });

        main.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> getApp().getDrawer().open()));
                appBar.setTitleText("StackOverflow questions");
                /*
                ToggleButtonGroup toggleButtonGroup = new ToggleButtonGroup();
                toggleButtonGroup.setSelectionType(SelectionMode.SINGLE);
                SidePopupView popup = new SidePopupView(toggleButtonGroup);
                popup.setSide(Side.RIGHT);

                if (appBar.getMenuItems().isEmpty()) {
                    MenuItem itemActivity = new MenuItem(null, MaterialDesignIcon.LOCAL_ACTIVITY.graphic());
                    itemActivity.setOnAction(e -> search("activity", tagComboBox.getValue()));
                    MenuItem itemDate = new MenuItem(null, MaterialDesignIcon.DATE_RANGE.graphic());
                    itemDate.setOnAction(e -> search("creation", tagComboBox.getValue()));
                    MenuItem itemVotes = new MenuItem(null, MaterialDesignIcon.PLUS_ONE.graphic());
                    itemVotes.setOnAction(e -> search("votes", tagComboBox.getValue()));
                    MenuItem itemRelevance = new MenuItem(null, MaterialDesignIcon.NEW_RELEASES.graphic());
                    itemRelevance.setOnAction(e -> search("relevance", tagComboBox.getValue()));
                    appBar.getMenuItems().addAll(itemActivity, itemDate, itemVotes, itemRelevance);
                }
                appBar.setProgress(-1);
                appBar.setProgressBarVisible(false);
                inProgress.addListener((ob, ov, nv) -> appBar.setProgressBarVisible(nv));

                 */
            }
        });
    }
    
    private void search(String order, String tag) {
        if (order == null || order.isEmpty()) {
            return;
        }
        lastOrder = order;
        if (tag == null || tag.isEmpty()) {
            return;
        }
        charmListView.setItems(FXCollections.emptyObservableList());
        inProgress.set(true);
        GluonObservableObject<StackResponse> searchStackOverflow = remoteService.SOSearch(tag, order);
        searchStackOverflow.exceptionProperty().addListener((obs, ov, nv) -> {
            if (nv != null) {
                System.out.println("error = " + nv);
                inProgress.set(false);
            }
        });
        searchStackOverflow.initializedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                charmListView.setItems(FXCollections.observableArrayList(searchStackOverflow.get().getItems()));
                inProgress.set(false);
            }
        });
    }
}
