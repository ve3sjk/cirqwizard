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

package org.cirqwizard.fx.pp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.PPSettings;
import org.cirqwizard.settings.PredefinedLocationSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.PPPoint;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class ComponentPlacement extends ScreenController implements Initializable
{
    @FXML private Label header;
    @FXML private ComboBox<String> componentName;

    @FXML private TitledPane pickupPane;
    @FXML private RealNumberTextField pickupX;
    @FXML private RealNumberTextField pickupY;
    @FXML private Button gotoPickupButton;
    @FXML private Button pickupButton;
    @FXML private Button gotoTargetButton;
    @FXML private Button pickupNGoButton;

    @FXML private TitledPane placementPane;
    @FXML private RealNumberTextField placementX;
    @FXML private RealNumberTextField placementY;
    @FXML private RealNumberTextField placementAngle;

    @FXML private RealNumberTextField manualZ;

    @FXML private RealNumberTextField targetX;
    @FXML private RealNumberTextField targetY;
    @FXML private RealNumberTextField targetAngle;

    @FXML private Button moveHeadAwayButton;
    @FXML private Button vacuumOffButton;

    private ObservableList<String> componentNames;
    private HashMap<Integer, Integer[]> placementOffsets = new HashMap<>();

    private static final int feederOffsetX = 10 * ApplicationConstants.RESOLUTION;
    private static final int feederOffsetY = -15 * ApplicationConstants.RESOLUTION;

    private static final DecimalFormat coordinatesFormat = new DecimalFormat("0.00");

    private boolean atPickupLocation = false;

    private Integer placementZ;

    @Override
    protected String getFxmlName()
    {
        return "ComponentPlacement.fxml";
    }

    @Override
    protected String getName()
    {
        return "Placement";
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        componentNames = FXCollections.observableArrayList();
        componentName.setItems(componentNames);
        componentName.valueProperty().addListener((v, oldV, newV) -> updateComponent());

        KeyboardHandler keyboardHandler = new KeyboardHandler();
        pickupX.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        pickupY.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementX.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementY.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementAngle.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        manualZ.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
    }

    private class KeyboardHandler implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent event)
        {
            if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN)
                return;
            RealNumberTextField textField = (RealNumberTextField) event.getSource();
            try
            {
                RealNumber delta = textField == placementAngle ? new RealNumber(1) : new RealNumber("0.1");
                if (event.getCode() == KeyCode.DOWN)
                    delta = delta.negate();
                RealNumber currentValue = textField.getRealNumberText() == null ? new RealNumber(0) : new RealNumber(textField.getRealNumberText());
                textField.setText(coordinatesFormat.format(currentValue.add(delta).getValue()));
                textField.fireEvent(new ActionEvent());
            }
            catch (NumberFormatException e)
            {
                // Not a big deal
            }
        }
    }


    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        ComponentId id =  context.getCurrentComponent();
        header.setText(id.getPackaging() + " " + id.getValue());

        componentNames.setAll(context.getPcbLayout().getComponentsLayer().getPoints().stream().
                filter(p -> p.getId().equals(id)).map(PPPoint::getName).collect(Collectors.toList()));
        componentName.getSelectionModel().select(0);
        pickupNGoButton.setDisable(true);
        placementPane.setDisable(true);
        manualZ.setDisable(true);

        int x = SettingsFactory.getMachineSettings().getReferencePinX().getValue();
        pickupX.setIntegerValue(x + feederOffsetX + context.getComponentPitch() / 2);
        int y = SettingsFactory.getMachineSettings().getReferencePinY().getValue();
        pickupY.setIntegerValue(context.getFeeder().getYForRow(y + feederOffsetY, context.getFeederRow()));

        gotoTargetButton.setDisable(true);

        boolean noMachineConnected = getMainApplication().getCNCController() == null;
        gotoPickupButton.setDisable(noMachineConnected);
        pickupButton.setDisable(noMachineConnected);
        moveHeadAwayButton.setDisable(noMachineConnected);
        vacuumOffButton.setDisable(noMachineConnected);
    }

    private void updateComponent()
    {
        Context context = getMainApplication().getContext();
        for (PPPoint p : context.getPcbLayout().getComponentsLayer().getPoints())
        {
            if (p.getName().equals(componentName.getValue()))
            {
                targetX.setIntegerValue(p.getPoint().getX());
                targetY.setIntegerValue(p.getPoint().getY());
                targetAngle.setIntegerValue(p.getAngle());

                Integer[] offsets = placementOffsets.get(p.getAngle());
                if (offsets != null)
                {
                    placementX.setIntegerValue(offsets[0]);
                    placementY.setIntegerValue(offsets[1]);
                    placementAngle.setIntegerValue(offsets[2]);
                }
                else
                {
                    placementX.setIntegerValue(null);
                    placementY.setIntegerValue(null);
                    placementAngle.setIntegerValue(null);
                }

                break;
            }
        }
        pickupPane.setDisable(false);
        atPickupLocation = false;
    }

    private void rotatePP(int angle)
    {
        getMainApplication().getCNCController().rotatePP(angle, SettingsFactory.getPpSettings().getRotationFeed().getValue());
    }

    public void gotoPickup()
    {
        Integer moveHeight = SettingsFactory.getPpSettings().getMoveHeight().getValue();
        getMainApplication().getCNCController().moveTo(pickupX.getIntegerValue(), pickupY.getIntegerValue(),
                moveHeight);
        rotatePP(0);
        manualZ.setDisable(false);
        manualZ.setIntegerValue(moveHeight);
        atPickupLocation = true;
    }

    public void pickup()
    {
        PPSettings settings = SettingsFactory.getPpSettings();
        Integer moveHeight = settings.getMoveHeight().getValue();
        if (!atPickupLocation)
        {
            getMainApplication().getCNCController().moveTo(pickupX.getIntegerValue(), pickupY.getIntegerValue(),
                    moveHeight);
            rotatePP(0);
        }
        getMainApplication().getCNCController().pickup(settings.getPickupHeight().getValue(), moveHeight);
        manualZ.setDisable(false);
        manualZ.setIntegerValue(moveHeight);
        gotoTargetButton.setDisable(false);
    }

    private int getTargetX()
    {
        int x = targetX.getIntegerValue() + getMainApplication().getContext().getG54X();
        if (placementX.getIntegerValue() != null)
            x += placementX.getIntegerValue();
        return x;
    }

    private int getTargetY()
    {
        int y = targetY.getIntegerValue() + getMainApplication().getContext().getG54Y();
        if (placementY.getIntegerValue() != null)
            y += placementY.getIntegerValue();
        return y;
    }

    private int getTargetAngle()
    {
        int angle = targetAngle.getIntegerValue();
        if (placementAngle.getIntegerValue() != null)
            angle += placementAngle.getIntegerValue();
        angle -= 90 * ApplicationConstants.RESOLUTION;
        return angle;
    }

    public void gotoTarget()
    {
        Integer moveHeight = SettingsFactory.getPpSettings().getMoveHeight().getValue();
        getMainApplication().getCNCController().moveTo(getTargetX(), getTargetY(),
                moveHeight);
        rotatePP(getTargetAngle());
        manualZ.setDisable(false);
        manualZ.setIntegerValue(moveHeight);
        pickupPane.setDisable(true);
        placementPane.setDisable(false);
    }

    public void pickupAndGo()
    {
        gotoPickup();
        pickup();
        gotoTarget();
        if (placementZ != null)
        {
            manualZ.setIntegerValue(placementZ);
            manualZ.fireEvent(new ActionEvent());
        }
    }

    public void place()
    {
        PPSettings settings = SettingsFactory.getPpSettings();
        int z = settings.getPickupHeight().getValue() - 3 * ApplicationConstants.RESOLUTION;
        getMainApplication().getCNCController().place(z, settings.getMoveHeight().getValue());
        manualZ.setDisable(false);
        manualZ.setIntegerValue(settings.getMoveHeight().getValue());
        placementPane.setDisable(true);
        gotoTargetButton.setDisable(true);
        int selectedIndex = componentName.getSelectionModel().getSelectedIndex();
        if (selectedIndex == componentName.getItems().size() - 1)
            pickupPane.setDisable(true);
        else
        {
            componentName.getSelectionModel().select(selectedIndex + 1);
            pickupX.setIntegerValue(pickupX.getIntegerValue() + getMainApplication().getContext().getComponentPitch());
            pickupNGoButton.setDisable(false);
        }
    }

    public void moveHeadAway()
    {
        PredefinedLocationSettings settings = SettingsFactory.getPredefinedLocationSettings();
        getMainApplication().getCNCController().moveTo(settings.getFarAwayX().getValue(),
                settings.getFarAwayY().getValue(), settings.getFarAwayZ().getValue());
    }

    public void vacuumOff()
    {
        getMainApplication().getCNCController().vacuumOff();
        placementPane.setDisable(true);
        pickupPane.setDisable(false);
    }

    public void pickupLocationUpdated()
    {
        if (pickupX.getIntegerValue() == null || pickupY.getIntegerValue() == null)
            return;
        getMainApplication().getCNCController().moveTo(pickupX.getIntegerValue(), pickupY.getIntegerValue(), null);
        atPickupLocation = true;
    }

    public void placementLocationUpdated()
    {
        getMainApplication().getCNCController().moveTo(getTargetX(), getTargetY(), null);
        rotatePP(getTargetAngle());
        placementOffsets.put(targetAngle.getIntegerValue(),
                new Integer[]{placementX.getIntegerValue(), placementY.getIntegerValue(), placementAngle.getIntegerValue()});
    }

    public void manualZUpdated()
    {
        if (!placementPane.isDisabled())
            placementZ = manualZ.getIntegerValue();
        getMainApplication().getCNCController().moveZ(manualZ.getIntegerValue());
    }

}
