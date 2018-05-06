package blasterjoni.xblastboard;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
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
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * FXML Controller class
 *
 * @author João Arvana
 */

//TODO: ADD clear path function
public class LayoutWindowController {

    // <editor-fold defaultstate="collapsed" desc=" Fields for fxml injection ">
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

// </editor-fold>
    
    public MainWindowController parentController;
    public Stage stage;
    public boolean success = false;
    
    private String layoutID;
    private Boolean editing = false;
    
    public void init(MainWindowController parent, Stage stage, String id){
        parentController = parent;
        this.stage = stage;
        
        //TODO: Add parameters for editing (LayoutID)
        layoutID = id;
        
        final BackgroundFill[] buttonFills = new BackgroundFill[1];
        final BackgroundImage[] buttonImages = new BackgroundImage[1];
        
        if(layoutID.equals("")){
            
            //Generating new ID
            List<String> layouts = BBFiles.getLayoutList();
            String newID = RandomStringUtils.randomAlphanumeric(10);
            while(layouts.contains(newID)){
                newID = RandomStringUtils.randomAlphanumeric(10);
            }
            layoutIdTextField.setText(newID);
            
            layoutTextTextField.setText("Layout");
            layoutTextColorPicker.setValue(Color.web("#000000"));
            
            buttonTextTextField.setText("Button");
            previewButton.setText("Button");
            buttonTextColorPicker.setValue(Color.web("#000000"));
            previewButton.setTextFill(Color.web("#000000"));
            previewButton.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        else{
            editing = true;
            LayoutProperties layout = BBFiles.getLayoutProperties(layoutID);
            
            layoutIdTextField.setText(layoutID);
            
            //Set layout fields, they get applied automatically when the layout gets added to the combobox
            layoutTextTextField.setText(layout.text);
            layoutTextColorPicker.setValue(layout.textColor);
            if(layout.hasIcon){
                layoutIconTextField.setText(layout.iconPath);
            }  
            if(layout.hasBackgroundImage){
                layoutBackgroundTextField.setText(layout.backgroundImagePath);
            }
            layoutBackgroundColorPicker.setValue(layout.backgroundColor);
            
            //Set button fields
            buttonTextTextField.setText(layout.buttonDefault.text);
            buttonTextColorPicker.setValue(layout.buttonDefault.textColor);
            if(layout.buttonDefault.hasIcon){
                buttonIconTextField.setText(layout.buttonDefault.iconPath);
            }
            if(layout.buttonDefault.hasBackgroundImage){
                buttonBackgroundTextField.setText(layout.buttonDefault.backgroundImagePath);
            }
            buttonBackgroundColorPicker.setValue(layout.buttonDefault.backgroundColor);
            //Update Button
            previewButton.setText(layout.buttonDefault.text);
            previewButton.setTextFill(layout.buttonDefault.textColor);
            if(layout.buttonDefault.hasIcon){
                try (FileInputStream fis = new FileInputStream(layout.buttonDefault.iconPath)){
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
            if(layout.buttonDefault.hasBackgroundImage){
                try (FileInputStream fis = new FileInputStream(layout.buttonDefault.backgroundImagePath)){
                    Image image = new Image(fis);
                    buttonImages[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    buttonImages[0] = null;
                }
            }
            buttonFills[0] = new BackgroundFill(layout.buttonDefault.backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
            previewButton.setBackground(new Background(buttonFills, buttonImages));
        }
        previewComboBox.getItems().add(layoutID);
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
                                try (FileInputStream fis = new FileInputStream(layoutIconTextField.getText())){
                                    rectangle.setImage(new Image(fis));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    rectangle.setImage(null);
                                }
                            }
                            setGraphic(rectangle);
                            
                            BackgroundFill[] fills = {new BackgroundFill(layoutBackgroundColorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)};
                            BackgroundImage[] images = new BackgroundImage[1];
                            if (layoutBackgroundTextField.getText().equals("")) {
                                images[0] = null;
                            } else {
                                try (FileInputStream fis = new FileInputStream(layoutBackgroundTextField.getText())){
                                    Image image = new Image(fis);
                                    images[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
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
                List<String> layouts = BBFiles.getLayoutList();

                int idLength = layoutIdTextField.getLength();
                boolean notExists  = !layouts.contains(layoutIdTextField.getText());
                boolean sameLayoutID = layoutID.equals(layoutIdTextField.getText());
                
                if( idLength > 0 && (notExists || (editing && sameLayoutID)) ){
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
                buttonFills[0] = new BackgroundFill(buttonBackgroundColorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY);
                previewButton.setBackground(new Background(buttonFills, buttonImages));
            }
        });
        
        buttonBackgroundTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (buttonBackgroundTextField.getText().equals("")) {
                    buttonImages[0] = null;
                } else {
                    try (FileInputStream fis = new FileInputStream(buttonBackgroundTextField.getText())){
                        Image image = new Image(fis);
                        buttonImages[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        buttonImages[0] = null;
                    }
                }
                previewButton.setBackground(new Background(buttonFills, buttonImages));
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
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        if(file!=null){
            layoutIconTextField.setText(file.toString());
        } else {
            layoutIconTextField.setText("");
        }
    }
    
    public void layoutBackgroundPathButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        if(file!=null){
            layoutBackgroundTextField.setText(file.toString());
        } else {
            layoutBackgroundTextField.setText("");
        }
    }

// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Button onAction ">
    public void buttonIconPathButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select icon image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        if(file!=null){
            buttonIconTextField.setText(file.toString());
        } else {
            buttonIconTextField.setText("");
        }
    }
    
    public void buttonBackgroundPathButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        if(file!=null){
            buttonBackgroundTextField.setText(file.toString());
        } else {
            buttonBackgroundTextField.setText("");
        }
    }

    // </editor-fold>
    
    public void cancelButtonClicked(){
        success = false;
        stage.close();
    }
    
    public void okButtonClicked(){
        success = true;
        
        if(!editing){
            BBFiles.saveLayout(
                    //Default Layout
                    layoutIdTextField.getText(), 
                    layoutTextTextField.getText(),
                    layoutTextColorPicker.getValue(),
                    layoutIconTextField.getText(),
                    layoutBackgroundTextField.getText(),
                    layoutBackgroundColorPicker.getValue(),
                    //Default Button
                    buttonTextTextField.getText(),
                    buttonTextColorPicker.getValue(),
                    buttonIconTextField.getText(),
                    buttonBackgroundTextField.getText(),
                    buttonBackgroundColorPicker.getValue()
                    );
            //Create the file where the button order is stored
            BBFiles.createNewButtonList(layoutIdTextField.getText());
            
            //Add layout to layout list
            List<String> layoutList = BBFiles.getLayoutList();
            layoutList.add(layoutIdTextField.getText());
            BBFiles.saveLayoutList(layoutList);
            
            stage.close();
        }else{
            //We save to old ID dir first, and then if new ID move the folder
            
            BBFiles.saveLayout(
                    //Default Layout
                    layoutID, 
                    layoutTextTextField.getText(),
                    layoutTextColorPicker.getValue(),
                    layoutIconTextField.getText(),
                    layoutBackgroundTextField.getText(),
                    layoutBackgroundColorPicker.getValue(),
                    //Default Button
                    buttonTextTextField.getText(),
                    buttonTextColorPicker.getValue(),
                    buttonIconTextField.getText(),
                    buttonBackgroundTextField.getText(),
                    buttonBackgroundColorPicker.getValue()
                    );

            stage.close();
            
            if(!layoutID.equals(layoutIdTextField.getText())){
                BBFiles.changeLayoutID(layoutID, layoutIdTextField.getText());
            }
        }
    }
    
}
