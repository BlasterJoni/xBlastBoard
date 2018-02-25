package blasterjoni.blastboard;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.sql.Array;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import sun.swing.BakedArrayList;

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
    
    MainWindowController parentController;
    public Stage stage;
    
    private ItemForLayoutComboBox previewLayout;
    
    public void init(MainWindowController parent, Stage stage){
        parentController = parent;
        this.stage = stage;
        
        //TODO: Add parameters for editing (LayoutID)
        String layoutID = null;
        if(layoutID == null){
            previewLayout = new ItemForLayoutComboBox();
            layoutTextTextField.setText("Layout");
            previewLayout.setIcon("Layout");
            layoutTextColorPicker.setValue(Color.BLACK);
            previewLayout.setTextColor(Color.BLACK);
        }
        else{
            //TODO: import settins
        }
        previewComboBox.getItems().addAll(previewLayout);
        previewComboBox.setValue(previewLayout);
        
        final Callback<ListView<ItemForLayoutComboBox>, ListCell<ItemForLayoutComboBox>> cellFactory;
        cellFactory = new Callback<ListView<ItemForLayoutComboBox>, ListCell<ItemForLayoutComboBox>>() {
            @Override public ListCell<ItemForLayoutComboBox> call(ListView<ItemForLayoutComboBox> p) {
                return new ListCell<ItemForLayoutComboBox>() {
                    private final ImageView rectangle;
                    { 
                        setContentDisplay(ContentDisplay.LEFT);
                        rectangle = new ImageView();
                        rectangle.setPreserveRatio(true);
                        rectangle.setFitWidth(16);
                        rectangle.setFitHeight(16);
                    }
                    
                    @Override protected void updateItem(ItemForLayoutComboBox item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (item == null || empty) {
                            setText(null);
                            setTextFill(null);
                            setGraphic(null);
                            setStyle(null);
                        } else {
                            setText(item.getText());
                            setTextFill(item.getTextColor());
                            rectangle.setImage(item.getIcon());
                            setGraphic(rectangle);
                            BackgroundFill[] fills = {item.getBackgroundColor()};
                            BackgroundImage[] images = {item.getBackgroundImage()};
                            setBackground(new Background(fills, images));
                        }
                    }
                };
            }
        };
        
        previewComboBox.setCellFactory(cellFactory);
        
        previewComboBox.setButtonCell(cellFactory.call(null));
        
        
        layoutTextTextField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                previewLayout.setText(layoutTextTextField.getText());
                
                updateLayoutPreview();
            }   
        });
        
        layoutTextColorPicker.valueProperty().addListener(new ChangeListener<Color>(){
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                previewLayout.setTextColor(layoutTextColorPicker.getValue());
                
                updateLayoutPreview();
            }
        });
        
        layoutIconTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                previewLayout.setIcon(layoutIconTextField.getText());
                
                updateLayoutPreview();
            }
        });
        
        layoutBackgroundTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                previewLayout.setBackgroundImage(layoutBackgroundTextField.getText());
        
                updateLayoutPreview();
            }
        });
        
        layoutBackgroundColorPicker.valueProperty().addListener(new ChangeListener<Color>(){
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                previewLayout.setBackgroundColor(layoutBackgroundColorPicker.getValue());
                
                updateLayoutPreview();
            }       
        });
        
    }
    
    public void updateLayoutPreview(){
        previewComboBox.getItems().clear();
        previewComboBox.getItems().add(previewLayout);

        previewComboBox.setValue(previewLayout);
    }
    
    public void layoutIconPathButtonClicked(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select icon image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        layoutIconTextField.setText(file.toString());
    }
    
    public void layoutBackgroundPathButtonClicked(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        
        layoutBackgroundTextField.setText(file.toString());
    }
    
}
