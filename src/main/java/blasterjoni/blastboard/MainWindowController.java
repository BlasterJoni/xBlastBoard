package blasterjoni.blastboard;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
            
    private final String home = System.getProperty("user.home");
    private final String BlastBoardDir = home + "/.BlastBoard";
    private final File settingsFile = new File(BlastBoardDir + "/settings.ini");
    
    public Stage stage;
    public Ini settings;
    public AudioEngine ae;
    
    public void init(Stage stage){
        //Setting default values
        this.stage = stage;
        try{
            this.settings = new Ini(new FileReader(settingsFile));
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        localCheckBox.setSelected(settings.get("Main", "local", boolean.class));
        localSlider.setValue(settings.get("Main", "localVOL", double.class));
        linkToggleButton.setSelected(settings.get("Main", "link", boolean.class));
        outputCheckBox.setSelected(settings.get("Main", "output", boolean.class));
        outputSlider.setValue(settings.get("Main", "outputVOL", double.class));
        ae = new AudioEngine(settings.get("Audio", "firstOutput"), settings.get("Audio", "secondOutput"), settings.get("Main", "localVOL", double.class), settings.get("Main", "outputVOL", double.class));
        
        //Volume changing events
        localSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if(linkToggleButton.isSelected()){
                   outputSlider.setValue(localSlider.getValue());
                   if(outputCheckBox.isSelected()){
                       ae.setSecondVolume(localSlider.getValue());
                   }
                }
                if(localCheckBox.isSelected()){
                    ae.setFirstVolume(localSlider.getValue());
                }
            }
        });
        
        outputSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if(linkToggleButton.isSelected()){
                   localSlider.setValue(outputSlider.getValue());
                   if(localCheckBox.isSelected()){
                       ae.setFirstVolume(outputSlider.getValue());
                   }
                }
                if(outputCheckBox.isSelected()){
                    ae.setSecondVolume(outputSlider.getValue());
                }
            }
        });
    }
    
    public void close(){
        ae.stop();
        
        settings.put("Main", "local", localCheckBox.isSelected());
        settings.put("Main", "localVOL", localSlider.getValue());
        settings.put("Main", "link", linkToggleButton.isSelected());
        settings.put("Main", "output", outputCheckBox.isSelected());
        settings.put("Main", "outputVOL", outputSlider.getValue());
        try{
            settings.store(settingsFile);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        
        Platform.exit();
    }
    
    public void start(){

        ae.play(Main.class.getResource("/sounds/Grapefruit Technique (online-audio-converter.com).wav"));

    }
    
    //Controls - except for slider wich are in innit cause they don't have an onAction
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
    
    //Layout context menu
    public void addLayoutContexMenuItemClicked() throws Exception{
        Stage stageLayout = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LayoutWindow.fxml"));
        Parent root = loader.load();
        LayoutWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageLayout.setScene(scene);
        stageLayout.setTitle("BlastBoard - Layout");
        stageLayout.setMinWidth(650);
        stageLayout.setMinHeight(450);
        stageLayout.initModality(Modality.APPLICATION_MODAL);
        controller.init(this, stageLayout);
        
        stageLayout.showAndWait();
    }
    
    public void editLayoutContexMenuItemClicked() throws Exception{}
    
    public void removeLayoutContexMenuItemClicked() throws Exception{}
    
    //Menu bar
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
        stageSettings.setTitle("BlastBoard - Settings");
        stageSettings.setMinWidth(550);
        stageSettings.setMinHeight(250);
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
    
    public void test(){
        
    }
}
