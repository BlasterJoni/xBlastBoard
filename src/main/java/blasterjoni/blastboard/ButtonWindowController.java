package blasterjoni.blastboard;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * FXML Controller class
 *
 * @author João Arvana
 */
public class ButtonWindowController {

// <editor-fold defaultstate="collapsed" desc=" Fields for fxml injection ">
    @FXML
    private Button previewButton;
    @FXML
    private TextField buttonIdTextField;
    @FXML
    private TextField buttonTextTextField;
    @FXML
    private ColorPicker buttonTextColorPicker;
    @FXML
    private TextField buttonIconTextField;
    @FXML
    private Button buttonIconPathButton;
    @FXML
    private TextField buttonBackgroundTextField;
    @FXML
    private Button buttonBackgroundPathButton;
    @FXML
    private ColorPicker buttonBackgroundColorPicker;
    @FXML
    private TextField buttonSoundTextField;
    @FXML
    private Button buttonSoundPathButton;
    
    @FXML
    private Button okButton;

// </editor-fold>
    
    
    public MainWindowController parentController;
    public Stage stage;
    public boolean success = false;

    private String layoutID;
    private String buttonID;
    private Boolean editing = false;

    public void init(MainWindowController parent, Stage stage, String layoutID, String buttonID){
        parentController = parent;
        this.stage = stage;
        
        //TODO: Add parameters for editing (LayoutID)
        this.layoutID = layoutID;
        this.buttonID = buttonID;
        
        final BackgroundFill[] fills = new BackgroundFill[1];
        final BackgroundImage[] images = new BackgroundImage[1];
        if(buttonID.equals("")){  
            ButtonProperties button = BBFiles.getLayoutProperties(layoutID).buttonDefault;
            
            buttonIdTextField.setText(buttonID);
            
            //Set button fields
            buttonTextTextField.setText(button.text);
            buttonTextColorPicker.setValue(button.textColor);
            if(button.hasIcon){
                buttonIconTextField.setText(button.iconPath);
            }
            if(button.hasBackgroundImage){
                buttonBackgroundTextField.setText(button.backgroundImagePath);
            }
            buttonBackgroundColorPicker.setValue(button.backgroundColor);
            //Update Button
            previewButton.setText(button.text);
            previewButton.setTextFill(button.textColor);
            if(button.hasIcon){
                try (FileInputStream fis = new FileInputStream(button.iconPath)){
                    ImageView imageView = new ImageView(new Image(fis));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    previewButton.setGraphic(imageView);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    previewButton.setGraphic(null);
                }
            }   
            if(button.hasBackgroundImage){
                try (FileInputStream fis = new FileInputStream(button.backgroundImagePath)){
                    Image image = new Image(fis);
                    images[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    images[0] = null;
                }
            }
            fills[0] = new BackgroundFill(button.backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
            previewButton.setBackground(new Background(fills, images));
        }
        else{
            editing = true;
            okButton.setDisable(false);
            ButtonProperties button = BBFiles.getButtonProperties(layoutID, buttonID);
            
            buttonIdTextField.setText(buttonID);
            
            //Set button fields
            buttonTextTextField.setText(button.text);
            buttonTextColorPicker.setValue(button.textColor);
            if(button.hasIcon){
                buttonIconTextField.setText(button.iconPath);
            }
            if(button.hasBackgroundImage){
                buttonBackgroundTextField.setText(button.backgroundImagePath);
            }
            buttonBackgroundColorPicker.setValue(button.backgroundColor);
            if(button.hasSound){
                buttonSoundTextField.setText(button.soundPath);
            }
            //Update Button
            previewButton.setText(button.text);
            previewButton.setTextFill(button.textColor);
            if(button.hasIcon){
                try (FileInputStream fis = new FileInputStream(button.iconPath)){
                    ImageView imageView = new ImageView(new Image(fis));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    previewButton.setGraphic(imageView);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    previewButton.setGraphic(null);
                }
            }
            if(button.hasBackgroundImage){
                try (FileInputStream fis = new FileInputStream(button.backgroundImagePath)){
                    Image image = new Image(fis);
                    images[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    images[0] = null;
                }
            }
            fills[0] = new BackgroundFill(button.backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
            previewButton.setBackground(new Background(fills, images));
        }
        
        // <editor-fold defaultstate="collapsed" desc=" Button Change Listeners ">
        buttonIdTextField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                List<String> buttons = BBFiles.getButtonList(layoutID);

                int idLength = buttonIdTextField.getLength();
                boolean notExists  = !buttons.contains(buttonIdTextField.getText());
                boolean sameButtonID = buttonID.equals(buttonIdTextField.getText());
                
                if( idLength > 0 && (notExists || (editing && sameButtonID)) ){
                    buttonIdTextField.setStyle("-fx-control-inner-background: white;");
                    okButton.setDisable(false);
                } else {
                    buttonIdTextField.setStyle("-fx-control-inner-background: red;");
                    okButton.setDisable(true);
                }
            } 
        });
        
        buttonTextTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                previewButton.setText(buttonTextTextField.getText());
            }
        });
        
        buttonTextColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                previewButton.setTextFill(buttonTextColorPicker.getValue());
            }
        });
        
        buttonIconTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (buttonIconTextField.getText().equals("")) {
                    previewButton.setGraphic(null);
                } else {
                    try (FileInputStream fis = new FileInputStream(buttonIconTextField.getText())){
                        ImageView imageView = new ImageView(new Image(fis));
                        imageView.setPreserveRatio(true);
                        imageView.setFitWidth(16);
                        imageView.setFitHeight(16);
                        previewButton.setGraphic(imageView);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        previewButton.setGraphic(null);
                    }
                }
            }
        });
        
        buttonBackgroundColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                fills[0] = new BackgroundFill(buttonBackgroundColorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY);
                previewButton.setBackground(new Background(fills, images));
            }
        });
        
        buttonBackgroundTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (buttonBackgroundTextField.getText().equals("")) {
                    images[0] = null;
                } else {
                    try (FileInputStream fis = new FileInputStream(buttonBackgroundTextField.getText())){
                        Image image = new Image(fis);
                        images[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        images[0] = null;
                    }
                }
                previewButton.setBackground(new Background(fills, images));
            }
        });
        // </editor-fold>
    }   
    
    // <editor-fold defaultstate="collapsed" desc=" Button onAction ">
    public void buttonIconPathButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select icon image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        buttonIconTextField.setText(file.toString());
    }
    
    public void buttonBackgroundPathButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        buttonBackgroundTextField.setText(file.toString());
    }

    public void buttonSoundPathButtonClicked(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select sound file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sound", "*.wav", "*.aiff", "*.flac", "*.mp3", "*.wma", "*.webm");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        buttonSoundTextField.setText(file.toString());
    }
    // </editor-fold>
    
    public void cancelButtonClicked(){
        success = false;
        stage.close();
    }
    
    public void okButtonClicked(){
        success = true;
        
        if(!editing){
            BBFiles.saveButton(
                    layoutID,
                    buttonIdTextField.getText(),
                    buttonTextTextField.getText(),
                    buttonTextColorPicker.getValue(),
                    buttonIconTextField.getText(),
                    buttonBackgroundTextField.getText(),
                    buttonBackgroundColorPicker.getValue(),
                    buttonSoundTextField.getText()
                    );
            //Add layout to layout list
            List<String> buttonList = BBFiles.getButtonList(layoutID);
            buttonList.add(buttonIdTextField.getText());
            BBFiles.saveButtonList(layoutID, buttonList);
            
            stage.close();
        }else{
            //We save to old ID dir first, and then if new ID move the folder
            
            BBFiles.saveButton(
                    layoutID,
                    buttonID,
                    buttonTextTextField.getText(),
                    buttonTextColorPicker.getValue(),
                    buttonIconTextField.getText(),
                    buttonBackgroundTextField.getText(),
                    buttonBackgroundColorPicker.getValue(),
                    buttonSoundTextField.getText()
                    );

            stage.close();
            
            if(!buttonID.equals(buttonIdTextField.getText())){
                BBFiles.changeButtonID(layoutID, buttonID, buttonIdTextField.getText());
            }
        }
    }
}
