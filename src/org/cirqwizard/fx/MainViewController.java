/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cirqwizard.fx;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.cirqwizard.fx.popover.ManualControlPopOver;
import org.cirqwizard.fx.popover.OffsetsPopOver;
import org.cirqwizard.fx.popover.SettingsPopOver;
import org.controlsfx.control.PopOver;

import java.util.ArrayList;
import java.util.List;

public class MainViewController extends ScreenController
{
    @FXML private HBox breadCrumbBarBox;
    @FXML private AnchorPane contentPane;
    @FXML private Hyperlink offsetsLink;
    @FXML private Hyperlink settingsLink;
    @FXML private Hyperlink manualControlLink;

    private ManualControlPopOver manualControlPopOver = new ManualControlPopOver();
    private OffsetsPopOver offsetsPopOver = new OffsetsPopOver();
    private SettingsPopOver settingsPopOver = new SettingsPopOver();

    private ScreenController currentScreen;

    @Override
    protected String getFxmlName()
    {
        return "MainView.fxml";
    }

    @FXML
    public void initialize()
    {
        settingsLink.managedProperty().bind(settingsLink.visibleProperty());
    }

    public ScreenController getCurrentScreen()
    {
        return currentScreen;
    }

    public void setScreen(ScreenController screen)
    {
        if (currentScreen != null && currentScreen.getShortcutHandler() != null)
            view.removeEventFilter(KeyEvent.KEY_PRESSED, currentScreen.getShortcutHandler());
        this.currentScreen = screen;

        contentPane.getChildren().clear();
        screen.refresh();
        contentPane.getChildren().add(screen.getView());
        AnchorPane.setTopAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setLeftAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setRightAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setBottomAnchor(contentPane.getChildren().get(0), 0.0);
        updateBreadCrumbBar(getPath(screen));
        settingsLink.setVisible(screen instanceof SettingsDependentScreenController);
        if (currentScreen.getShortcutHandler() != null)
            view.addEventFilter(KeyEvent.KEY_PRESSED, currentScreen.getShortcutHandler());
    }

    private List<ScreenController> getPath(ScreenController scene)
    {
        ArrayList<ScreenController> path = new ArrayList<>();
        for (; scene != null; scene = scene.getParent())
        {
            if (scene instanceof ScreenGroup && !((ScreenGroup)scene).isVisible())
                continue;
            path.add(0, scene);
        }
        return path;
    }

    private void updateBreadCrumbBar(List<ScreenController> path)
    {
        breadCrumbBarBox.getChildren().clear();
        for (int i = 0; i < path.size(); i++)
        {
            final ScreenController item = path.get(i);
            Button b = new Button(item.getName());
            b.setFocusTraversable(false);
            b.getStyleClass().setAll("button");
            if (path.size() == 1)
                b.getStyleClass().add("only-button");
            else if (i == 0)
                b.getStyleClass().add("first-button");
            else if (i == path.size() - 1)
                b.getStyleClass().add("last-button");
            else
                b.getStyleClass().addAll("middle-button");

            b.setOnAction(event ->
            {

                List<MenuItem> contextMenuItems = new ArrayList<>();
                List<ScreenController> siblings = getMainApplication().getSiblings(item);
                if (siblings != null)
                {
                    for (ScreenController sibling : siblings)
                    {
                        if (sibling instanceof ScreenGroup && !((ScreenGroup)sibling).isVisible())
                            continue;
                        MenuItem it = new MenuItem(sibling.getName());
                        it.setOnAction(e -> sibling.select());
                        it.setDisable(!sibling.isEnabled());
                        contextMenuItems.add(it);
                    }

                    ContextMenu contextMenu = new ContextMenu(contextMenuItems.toArray(new MenuItem[contextMenuItems.size()]));
                    Button button = (Button) event.getSource();
                    final Scene scene = button.getScene();
                    final Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
                    final Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
                    final Point2D nodeCoord = button.localToScene(0.0, 0.0);
                    final double clickX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());
                    final double clickY = Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY() + button.getHeight());
                    contextMenu.show((Node) event.getSource(), clickX, clickY);
                }
                else
                    item.select();
            });

            breadCrumbBarBox.getChildren().add(b);
        }
    }

    private void hidePopOvers()
    {
        if (manualControlPopOver.getPopOver() != null)
            manualControlPopOver.getPopOver().hide(javafx.util.Duration.millis(0));
        if (offsetsPopOver.getPopOver() != null)
            offsetsPopOver.getPopOver().hide(javafx.util.Duration.millis(0));
        if (settingsPopOver.getPopOver() != null)
            settingsPopOver.getPopOver().hide(javafx.util.Duration.millis(0));
    }

    public void manualControl()
    {
        if (manualControlPopOver.getPopOver() == null)
        {
            manualControlPopOver.setPopOver(new PopOver(manualControlPopOver.getView()));
            manualControlPopOver.setMainApplication(getMainApplication());
            manualControlPopOver.getPopOver().setDetachedTitle("Manual control");
            manualControlPopOver.getPopOver().setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
            getMainApplication().getPrimaryStage().setOnCloseRequest(event -> hidePopOvers());
        }
        manualControlPopOver.refresh();
        manualControlPopOver.getPopOver().show(manualControlLink);
    }

    public void offsets()
    {
        if (offsetsPopOver.getPopOver() == null)
        {
            offsetsPopOver.setPopOver(new PopOver(offsetsPopOver.getView()));
            offsetsPopOver.setMainApplication(getMainApplication());
            offsetsPopOver.getPopOver().setDetachedTitle("Offsets");
            offsetsPopOver.getPopOver().setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
            getMainApplication().getPrimaryStage().setOnCloseRequest(event -> hidePopOvers());
        }
        offsetsPopOver.refresh();
        offsetsPopOver.getPopOver().show(offsetsLink);
    }

    public void settings()
    {
        if (settingsPopOver.getPopOver() == null)
        {
            settingsPopOver.setPopOver(new PopOver(settingsPopOver.getView()));
            settingsPopOver.setMainApplication(getMainApplication());
            settingsPopOver.getPopOver().setDetachedTitle("Settings");
            settingsPopOver.getPopOver().setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
            getMainApplication().getPrimaryStage().setOnCloseRequest(event -> hidePopOvers());
        }
        SettingsDependentScreenController screen = (SettingsDependentScreenController) currentScreen;
        settingsPopOver.setGroup(screen.getSettingsGroup(), screen);
        settingsPopOver.getPopOver().show(settingsLink);
    }

}
