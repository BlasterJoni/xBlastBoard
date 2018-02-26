package blasterjoni.blastboard;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.simple.JSONObject;

/**
 * FXML Controller class
 *
 * @author João Arvana
 */
public class LayoutWindowController {

    @FXML
    private ComboBox previewComboBox;
    @FXML
    private TextField layoutIdTextField;
    @FXML
    private TextField layoutTextTextField;
    @FXML
    private ColorPicker layoutTextColorPicker;
    @FXML
    private TextField layoutIconTextField;
    @FXML
    private Button layoutIconPathButton;
    @FXML
    private TextField layoutBackgroundTextField;
    @FXML
    private Button layoutBackgroundPathButton;
    @FXML
    private ColorPicker layoutBackgroundColorPicker;
    
    @FXML
    private Button previewButton;
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
    private Button okButton;
    
    MainWindowController parentController;
    public Stage stage;
    
    private String layoutID;
    private Boolean editing = false;
    
    public void init(MainWindowController parent, Stage stage, String id){
        parentController = parent;
        this.stage = stage;
        
        //TODO: Add parameters for editing (LayoutID)
        layoutID = id;
        if(layoutID.equals("")){  
            layoutTextTextField.setText("Layout");
            layoutTextColorPicker.setValue(Color.BLACK);
            
            buttonTextTextField.setText("Button");
            previewButton.setText("Button");
            buttonTextColorPicker.setValue(Color.BLACK);
        }
        else{
            //TODO: import settins
        }
        previewComboBox.getItems().addAll(layoutID);
        previewComboBox.setValue(layoutID);
        
        final Callback<ListView<String>, ListCell<String>> cellFactory;
        cellFactory = new Callback<ListView<String>, ListCell<String>>() {
            @Override public ListCell<String> call(ListView<String> p) {
                return new ListCell<String>() {
                    private final ImageView rectangle;
                    { 
                        setContentDisplay(ContentDisplay.LEFT);
                        rectangle = new ImageView();
                        rectangle.setPreserveRatio(true);
                        rectangle.setFitWidth(16);
                        rectangle.setFitHeight(16);
                    }
                    
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (item == null || empty) {
                            setText(null);
                            setTextFill(null);
                            setGraphic(null);
                            setStyle(null);
                        } else {
                            setText(layoutTextTextField.getText());
                            setTextFill(layoutTextColorPicker.getValue());
                                
                            if (layoutIconTextField.getText().equals("")) {
                                rectangle.setImage(null);
                            } else {
                                try {
                                    rectangle.setImage(new Image(new FileInputStream(layoutIconTextField.getText())));
                                } catch (FileNotFoundException FNFE) {
                                    FNFE.printStackTrace();
                                    rectangle.setImage(null);
                                }
                            }
                            setGraphic(rectangle);
                            
                            BackgroundFill[] fills = {new BackgroundFill(layoutBackgroundColorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)};
                            BackgroundImage[] images = new BackgroundImage[1];
                            if (layoutBackgroundTextField.getText().equals("")) {
                                images[0] = null;
                            } else {
                                try {
                                    Image image = new Image(new FileInputStream(layoutBackgroundTextField.getText()));
                                    images[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                                } catch (FileNotFoundException FNFE) {
                                    FNFE.printStackTrace();
                                    images[0] = null;
                                }
                            }
                            setBackground(new Background(fills, images));
                        }
                    }
                };
            }
        };        
        previewComboBox.setCellFactory(cellFactory);
        previewComboBox.setButtonCell(cellFactory.call(null));
        
        // <editor-fold defaultstate="collapsed" desc=" Layout Change Listeners ">
        layoutIdTextField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                File Dir = new File(parentController.layoutsDir + "/" + layoutIdTextField.getText());
                if(layoutIdTextField.getLength() > 0 && !Dir.exists()){
                    layoutIdTextField.setStyle("-fx-control-inner-background: white;");
                    okButton.setDisable(false);
                } else {
                    layoutIdTextField.setStyle("-fx-control-inner-background: red;");
                    okButton.setDisable(true);
                }
            } 
        });
        
        layoutTextTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateLayoutPreview();
            }            
        });
        
        layoutTextColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                updateLayoutPreview();
            }
        });
        
        layoutIconTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateLayoutPreview();
            }
        });
        
        layoutBackgroundTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateLayoutPreview();
            }
        });
        
        layoutBackgroundColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                updateLayoutPreview();
            }            
        });
// </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc=" Button Change Listeners ">
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
                    try {
                        ImageView imageView = new ImageView(new Image(new FileInputStream(buttonIconTextField.getText())));
                        imageView.setPreserveRatio(true);
                        imageView.setFitWidth(16);
                        imageView.setFitHeight(16);
                        previewButton.setGraphic(imageView);
                    } catch (FileNotFoundException FNFE) {
                        FNFE.printStackTrace();
                        previewButton.setGraphic(null);
                    }
                }
            }
        });
        
        final BackgroundFill[] fills = new BackgroundFill[1];
        final BackgroundImage[] images = new BackgroundImage[1];
        
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
                    try {
                        Image image = new Image(new FileInputStream(buttonBackgroundTextField.getText()));
                        images[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                    } catch (FileNotFoundException FNFE) {
                        FNFE.printStackTrace();
                        images[0] = null;
                    }
                }
                previewButton.setBackground(new Background(fills, images));
            }
        });
// </editor-fold>
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Layout onAction ">
    public void updateLayoutPreview() {
        previewComboBox.getItems().clear();
        previewComboBox.getItems().addAll(layoutID);
        
        previewComboBox.setValue(layoutID);
    }
    
    public void layoutIconPathButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select icon image");
        //FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        //fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        layoutIconTextField.setText(file.toString());
    }
    
    public void layoutBackgroundPathButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        layoutBackgroundTextField.setText(file.toString());
    }

// </editor-fold>
    
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

// </editor-fold>
    
    public void cancelButtonClicked(){
        stage.close();
    }
    
    public void okButtonClicked(){
        if(!editing){
            File newLayoutDir = new File(parentController.layoutsDir + "/" + layoutIdTextField.getText());
            newLayoutDir.mkdirs();
            
            JSONObject layoutProperties = new JSONObject();
            
            layoutProperties.put("text", layoutTextTextField.getText());
            layoutProperties.put("textColor", "#" + Integer.toHexString(layoutTextColorPicker.getValue().hashCode()));
            
            if(!layoutIconTextField.getText().equals("")){
                layoutProperties.put("icon", true);
                try {
                    Files.copy(Paths.get(layoutIconTextField.getText()), Paths.get(newLayoutDir.getAbsolutePath() + "/icon"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException IOE) {
                    IOE.printStackTrace();
                }
            } else {
                layoutProperties.put("icon", false);
            }
            
            if(!layoutBackgroundTextField.getText().equals("")){
                layoutProperties.put("backgroundImage", true);
                try {
                    Files.copy(Paths.get(layoutBackgroundTextField.getText()), Paths.get(newLayoutDir.getAbsolutePath() + "/background"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException IOE) {
                    IOE.printStackTrace();
                }
            } else {
                layoutProperties.put("backgroundImage", false);
            }
            layoutProperties.put("backgroundColor", "#" + Integer.toHexString(layoutBackgroundColorPicker.getValue().hashCode()));
            
            
            JSONObject defaultButtonProperties = new JSONObject();
            
            defaultButtonProperties.put("text", buttonTextTextField.getText());
            defaultButtonProperties.put("textColor", "#" + Integer.toHexString(buttonTextColorPicker.getValue().hashCode()));
            
            if(!buttonIconTextField.getText().equals("")){
                defaultButtonProperties.put("icon", true);
                try {
                    Files.copy(Paths.get(buttonIconTextField.getText()), Paths.get(newLayoutDir.getAbsolutePath() + "/buttonIcon"), StandardCopyOption.REPLACE_EXISTING);
                    File f = new File(buttonIconTextField.getText());
                } catch (IOException IOE) {
                    IOE.printStackTrace();
                }
            } else {
                defaultButtonProperties.put("icon", false);
            }
            
            if(!buttonBackgroundTextField.getText().equals("")){
                defaultButtonProperties.put("backgroundImage", true);
                try {
                    Files.copy(Paths.get(buttonBackgroundTextField.getText()), Paths.get(newLayoutDir.getAbsolutePath() + "/buttonBackground"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException IOE) {
                    IOE.printStackTrace();
                }
            } else {
                defaultButtonProperties.put("backgroundImage", false);
            }
            defaultButtonProperties.put("backgroundColor", "#" + Integer.toHexString(buttonBackgroundColorPicker.getValue().hashCode()));
            
            
            layoutProperties.put("buttonDefaultProperties", defaultButtonProperties);
            
            try(FileWriter file = new FileWriter(newLayoutDir + "/layout.json")){
                file.write(layoutProperties.toJSONString());
                file.flush();
            }catch(IOException IOE){
                IOE.printStackTrace();
            }
        }
        //TODO: if edditing, change settings and maybe change folder name
    }
    
}
