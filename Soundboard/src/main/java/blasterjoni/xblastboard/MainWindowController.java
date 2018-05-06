package blasterjoni.xblastboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.bramp.ffmpeg.FFmpeg;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.Ini;

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
public class MainWindowController{

    // <editor-fold defaultstate="collapsed" desc=" Fields for fxml injection ">
    @FXML
    private CheckBox localCheckBox;
    @FXML
    private Slider localSlider;
    @FXML
    private ToggleButton linkToggleButton;
    @FXML
    private CheckBox outputCheckBox;
    @FXML
    private Slider outputSlider;
    @FXML
    private ComboBox layoutComboBox;
    
    @FXML
    private FlowPane buttonsFlowPane;
    // </editor-fold>
    private Boolean buttonContextMenuActivated = false;
    
    public Stage stage;
    public Ini settings;
    public AudioEngine ae;
    
    public void init(Stage stage){
        //Setting default values
        this.stage = stage;
        
        this.settings = BBFiles.loadSettings();
        
        localCheckBox.setSelected(settings.get("Main", "local", boolean.class));
        localSlider.setValue(settings.get("Main", "localVOL", double.class));
        linkToggleButton.setSelected(settings.get("Main", "link", boolean.class));
        outputCheckBox.setSelected(settings.get("Main", "output", boolean.class));
        outputSlider.setValue(settings.get("Main", "outputVOL", double.class));
        ae = new AudioEngine(settings.get("Audio", "firstOutput"), settings.get("Audio", "secondOutput"), settings.get("Main", "localVOL", double.class), settings.get("Main", "outputVOL", double.class));
        
        // <editor-fold defaultstate="collapsed" desc=" Volume Listeners ">
        localSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (linkToggleButton.isSelected()) {
                    outputSlider.setValue(localSlider.getValue());
                    if (outputCheckBox.isSelected()) {
                        ae.setSecondVolume(localSlider.getValue());
                    }
                }
                if (localCheckBox.isSelected()) {
                    ae.setFirstVolume(localSlider.getValue());
                }
            }
        });
        
        outputSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (linkToggleButton.isSelected()) {
                    localSlider.setValue(outputSlider.getValue());
                    if (localCheckBox.isSelected()) {
                        ae.setFirstVolume(outputSlider.getValue());
                    }
                }
                if (outputCheckBox.isSelected()) {
                    ae.setSecondVolume(outputSlider.getValue());
                }
            }
        });

// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" FlowPaneContextMenu ">
        ContextMenu buttonContextMenu = createButtonContextMenu("");

        //Stuff so it doesnt bug up and activates on button rigth click.
        buttonsFlowPane.setOnContextMenuRequested(e -> {
            if (buttonContextMenu.isShowing()) {
                //Gotta hide it first or it'll look weird when we open it again
                buttonContextMenu.hide();
            }
            if(!buttonContextMenuActivated){
               buttonContextMenu.show(buttonsFlowPane, e.getScreenX(), e.getScreenY()); 
            }
            buttonContextMenuActivated = false;
        });
        buttonsFlowPane.setOnMouseClicked(e -> {
            if (buttonContextMenu.isShowing()) {
                buttonContextMenu.hide();
            }
        });
        // </editor-fold>

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
                            LayoutProperties layout = BBFiles.getLayoutProperties(item);
                            
                            setText(layout.text);
                            setTextFill(layout.textColor);
                                
                            if (!layout.hasIcon) {
                                rectangle.setImage(null);
                            } else {
                                try (FileInputStream fis = new FileInputStream(layout.iconPath)){
                                    rectangle.setImage(new Image(fis));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    rectangle.setImage(null);
                                }
                            }
                            setGraphic(rectangle);
                            
                            BackgroundFill[] fills = {new BackgroundFill(layout.backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)};
                            BackgroundImage[] images = new BackgroundImage[1];
                            if (!layout.hasBackgroundImage) {
                                images[0] = null;
                            } else {
                                try (FileInputStream fis = new FileInputStream(layout.backgroundImagePath)){
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
        layoutComboBox.setCellFactory(cellFactory);
        layoutComboBox.setButtonCell(cellFactory.call(null));
        
        layoutComboBox.setOnContextMenuRequested(e->{
            MenuItem removeItem = layoutComboBox.getContextMenu().getItems().get(5);
            if(layoutComboBox.getItems().size() == 1){
                removeItem.setDisable(true);
            }else{
                removeItem.setDisable(false);
            }
        });
        
        updateLayouts();
        if(settings.get("Main", "currentLayout").equals("")){
            layoutComboBox.setValue(layoutComboBox.getItems().get(0)); 
        }else{
            layoutComboBox.setValue(settings.get("Main", "currentLayout"));
        }
        updateButtons();
    }
    
    public void updateLayouts(){
        layoutComboBox.getItems().clear();
        layoutComboBox.getItems().addAll(BBFiles.getLayoutList());
    }
    
    public void updateButtons(){
        buttonsFlowPane.getChildren().clear();
        for(String bId : BBFiles.getButtonList((String) layoutComboBox.getValue())){
            ButtonProperties bp = BBFiles.getButtonProperties((String) layoutComboBox.getValue(), bId);
            Button buttonToAdd = new Button();
            
            //Setting button properties
            buttonToAdd.setPrefSize(75, 75);
            buttonToAdd.setMinSize(75, 75);
            
            buttonToAdd.setText(bp.text);
            buttonToAdd.setTextFill(bp.textColor);

            if (!bp.hasIcon) {
                buttonToAdd.setGraphic(null);
            } else {
                try (FileInputStream fis = new FileInputStream(bp.iconPath)){
                    ImageView imageView = new ImageView(new Image(fis));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    buttonToAdd.setGraphic(imageView);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    buttonToAdd.setGraphic(null);
                }
            }

            BackgroundFill[] fills = {new BackgroundFill(bp.backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)};
            BackgroundImage[] images = new BackgroundImage[1];
            if (!bp.hasBackgroundImage) {
                images[0] = null;
            } else {
                try (FileInputStream fis = new FileInputStream(bp.backgroundImagePath)){
                    Image image = new Image(fis);
                    images[0] = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    images[0] = null;
                }
            }
            buttonToAdd.setBackground(new Background(fills, images));
            
            ContextMenu buttonContextMenu = createButtonContextMenu(bId);
            
            //Setting button events
            buttonToAdd.setOnContextMenuRequested(e -> {
                //Variable saying if opened is needed because the flowpane contextMenu 
                //is fired afterwards if we dont tell it not to
                buttonContextMenuActivated = true;
                buttonContextMenu.show(buttonToAdd, e.getScreenX(), e.getScreenY());
            });
            buttonToAdd.setOnMouseClicked(e -> {
                //Mouse clicked gets called first
                if (buttonContextMenu.isShowing()) {
                    //Gotta hide it first or it'll look weird when we open it again
                    buttonContextMenu.hide();
                }
                if(bp.hasSound && !e.getButton().equals(MouseButton.SECONDARY)){
                    ae.play(bp.soundPath);
                }
            });
            
            buttonsFlowPane.getChildren().add(buttonToAdd);
        }
    }
    
    public ContextMenu createButtonContextMenu(String bId){
        //Creating button context menu
            ContextMenu buttonContextMenu = new ContextMenu();
            MenuItem addButton = new MenuItem("Add Button");
            addButton.setOnAction((ActionEvent e) -> {
                try {
                    addButtonContexMenuItemClicked();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            MenuItem importButton = new MenuItem("Import Button");
            importButton.setOnAction((ActionEvent e) -> {
                try {
                    importButtonContextMenuItemClicked();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            MenuItem editButton = new MenuItem("Edit Button");
            editButton.setOnAction(e->{
                try {
                    editButtonContexMenuItemClicked(bId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            MenuItem exportButton = new MenuItem("Export Button");
            exportButton.setOnAction(e->{
                try {
                    exportButtonContexMenuItemClicked(bId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            MenuItem removeButton = new MenuItem("Remove Button");
            removeButton.setOnAction(e->{
                try {
                    removeButtonContexMenuItemClicked(bId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            
            if(bId.equals("")){
                editButton.setDisable(true);
                exportButton.setDisable(true);
                removeButton.setDisable(true);
            }
            
            buttonContextMenu.getItems().addAll(addButton, importButton, new SeparatorMenuItem(), editButton, exportButton, removeButton);
            return buttonContextMenu;
    }
    
    public void openFile(File f){
        String ext = FilenameUtils.getExtension(f.getName());
        if(ext.equals("xbbl")){
            BBFiles.importLayout(f);
            updateLayouts();
            layoutComboBox.setValue(layoutComboBox.getItems().get(layoutComboBox.getItems().size() - 1));  
        } else if (ext.equals("xbbb")){
            BBFiles.importButton((String) layoutComboBox.getValue(), f);
            updateButtons();
        }
    }
    
    public void close(){
        ae.stop();
        
        settings.put("Main", "local", localCheckBox.isSelected());
        settings.put("Main", "localVOL", localSlider.getValue());
        settings.put("Main", "link", linkToggleButton.isSelected());
        settings.put("Main", "output", outputCheckBox.isSelected());
        settings.put("Main", "outputVOL", outputSlider.getValue());
        settings.put("Main", "currentLayout", layoutComboBox.getValue());
        
        BBFiles.saveSettings(settings);
        
        Platform.exit();
    }

    //Controls - except for slider wich are in init cause they don't have an onAction
    public void stop(){
        ae.stop();
    }
    
    public void localCheckBoxChanged(){
        if(!localCheckBox.isSelected()){
            ae.setFirstVolume(0D);
        }
        else{
            ae.setFirstVolume(localSlider.getValue());
        }
    }
    
    public void outputCheckBoxChanged(){
        if(!outputCheckBox.isSelected()){
            ae.setSecondVolume(0D);
        }
        else{
            ae.setSecondVolume(outputSlider.getValue());
        }
    }
    
    public void layoutComboBoxChanged(){
        if (layoutComboBox.getValue() != null)
            updateButtons();
    }
    
    //Layout context menu
    public void addLayoutContexMenuItemClicked() throws Exception{
        Stage stageLayout = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LayoutWindow.fxml"));
        Parent root = loader.load();
        LayoutWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageLayout.setScene(scene);
        stageLayout.setTitle("xBlastBoard - Layout");
        stageLayout.setMinWidth(650);
        stageLayout.setMinHeight(450);
        stageLayout.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stageLayout.initModality(Modality.APPLICATION_MODAL);
        controller.init(this, stageLayout, "");
        
        stageLayout.showAndWait();
        
        if(controller.success){
            updateLayouts();
            layoutComboBox.setValue(layoutComboBox.getItems().get(layoutComboBox.getItems().size() - 1));  
        }
    }
    
    public void editLayoutContexMenuItemClicked() throws Exception{
        Stage stageLayout = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LayoutWindow.fxml"));
        Parent root = loader.load();
        LayoutWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageLayout.setScene(scene);
        stageLayout.setTitle("xBlastBoard - Layout");
        stageLayout.setMinWidth(650);
        stageLayout.setMinHeight(450);
        stageLayout.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stageLayout.initModality(Modality.APPLICATION_MODAL);
        controller.init(this, stageLayout, (String) layoutComboBox.getValue());
        
        stageLayout.showAndWait();
        
        if(controller.success){
            int index = layoutComboBox.getItems().indexOf(layoutComboBox.getValue());
            updateLayouts();
            layoutComboBox.setValue(layoutComboBox.getItems().get(index)); 
        }
    }
    
    public void removeLayoutContexMenuItemClicked() throws Exception{
        String layoutID = (String) layoutComboBox.getValue();
        
        List<String> layoutList = BBFiles.getLayoutList();
        layoutList.remove(layoutID);
        BBFiles.saveLayoutList(layoutList);
        
        updateLayouts();
        layoutComboBox.setValue(layoutComboBox.getItems().get(layoutComboBox.getItems().size() - 1));
        //Need to hide the popup or it looks weird
        layoutComboBox.hide();
        
        BBFiles.deleteLayout(layoutID);
    }
    
    public void exportLayoutContextMenuItemClicked(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("xBlastBoard Layout Files", "*.xbbl");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(stage);
        if(file != null){
            BBFiles.exportLayout((String) layoutComboBox.getValue(), file);
        }
    }
    
    public void importLayoutContextMenuItemClicked(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("xBlastBoard Layout Files", "*.xbbl");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            BBFiles.importLayout(file);
        }
        updateLayouts();
        layoutComboBox.setValue(layoutComboBox.getItems().get(layoutComboBox.getItems().size() - 1));  
    }
    
    //Button context menu
    public void addButtonContexMenuItemClicked() throws Exception{
        Stage stageButton = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ButtonWindow.fxml"));
        Parent root = loader.load();
        ButtonWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageButton.setScene(scene);
        stageButton.setTitle("xBlastBoard - Button");
        stageButton.setMinWidth(650);
        stageButton.setMinHeight(295);
        stageButton.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stageButton.initModality(Modality.APPLICATION_MODAL);
        controller.init(this, stageButton, (String) layoutComboBox.getValue(), "");
        
        stageButton.showAndWait();
        
        if(controller.success){
            updateButtons();
        }
    }
    
    public void editButtonContexMenuItemClicked(String buttonID) throws Exception{
        Stage stageButton = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ButtonWindow.fxml"));
        Parent root = loader.load();
        ButtonWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageButton.setScene(scene);
        stageButton.setTitle("xBlastBoard - Button");
        stageButton.setMinWidth(650);
        stageButton.setMinHeight(295);
        stageButton.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stageButton.initModality(Modality.APPLICATION_MODAL);
        controller.init(this, stageButton, (String) layoutComboBox.getValue(), buttonID);
        
        stageButton.showAndWait();
        
        if(controller.success){
            updateButtons();
        }
    }
    
    public void removeButtonContexMenuItemClicked(String buttonID) throws Exception{
        List<String> buttonList = BBFiles.getButtonList((String) layoutComboBox.getValue());
        buttonList.remove(buttonID);
        BBFiles.saveButtonList((String) layoutComboBox.getValue(), buttonList);
        
        updateButtons();
        
        BBFiles.deleteButton((String) layoutComboBox.getValue(), buttonID);
    }
    
    public void exportButtonContexMenuItemClicked(String buttonID){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("xBlastBoard Button Files", "*.xbbb");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(stage);
        if(file != null){
            BBFiles.exportButton((String) layoutComboBox.getValue(), buttonID, file);
        }
    }
    
    public void importButtonContextMenuItemClicked(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("xBlastBoard Button Files", "*.xbbb");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            BBFiles.importButton((String) layoutComboBox.getValue(), file);
        }
        updateButtons();
    }
    
    //Menu bar
    //Settings
    public void audioSettingsMenuBarItemClicked() throws Exception{
        openSettings(0);
    }
    
    public void remoteSettingsMenuBarItemClicked() throws Exception{
        openSettings(1);
    }
    
    public void openSettings(int tabIndex) throws Exception{
        Stage stageSettings = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SettingsWindow.fxml"));
        Parent root = loader.load();
        SettingsWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageSettings.setScene(scene);
        stageSettings.setTitle("xBlastBoard - Settings");
        stageSettings.setMinWidth(550);
        stageSettings.setMinHeight(250);
        stageSettings.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stageSettings.initModality(Modality.APPLICATION_MODAL);
        controller.init(this, stageSettings, tabIndex);
        
        stageSettings.showAndWait();
        if(controller.success){
            if(controller.audioChanged){
                ae.stop();
                ae = new AudioEngine(settings.get("Audio", "firstOutput"), settings.get("Audio", "secondOutput"), localSlider.getValue(), outputSlider.getValue());
            }
            if(controller.remoteChanged){
                //TODO: Restart remote server.
            }
        }
    }
    
    //Help
    public void aboutHelpMenuBarItemClicked() throws IOException{
        Stage stageAbout = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AboutWindow.fxml"));
        Parent root = loader.load();
        AboutWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageAbout.setScene(scene);
        stageAbout.setTitle("xBlastBoard - About");
        stageAbout.setMinWidth(550);
        stageAbout.setMinHeight(250);
        stageAbout.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        controller.init(this, stageAbout);
        
        stageAbout.show();
    }
    
    public void test() throws IOException{
        File f = new File("D:\\Ficheiros\\Documentos\\CodingWorkspace\\MyRepositories\\Java\\xBlastBoard\\Soundboard\\src\\main\\resources\\images\\icon.png");
        System.out.println(f.getName());
    }
}
