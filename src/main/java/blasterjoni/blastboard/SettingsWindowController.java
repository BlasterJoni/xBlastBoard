package blasterjoni.blastboard;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

/**
 * FXML Controller class
 *
 * @author João Arvana
 */
public class SettingsWindowController{
    
    @FXML
    private TabPane settingsTabPane;
    @FXML
    private ComboBox firstComboBox;
    @FXML
    private ComboBox secondComboBox;
    @FXML
    private Button audioApplyButton;
    @FXML
    private Button remoteApplyButton;
    
    MainWindowController parentController;
    public Stage stage;
    public boolean audioChanged = false;
    public boolean remoteChanged = false;
    public boolean success = false;
    
    public void init(MainWindowController parent, Stage stage, int tabIndex){
        parentController = parent;
        this.stage = stage;
        settingsTabPane.getSelectionModel().select(tabIndex);
       
        ArrayList mixersToUse = new ArrayList();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info a : mixers) {
            Mixer mixer = AudioSystem.getMixer(a);
            if(mixer.getSourceLineInfo().length > 0 && !a.toString().startsWith("Port ")){
                mixersToUse.add(a.getName());
            }
        }        
        firstComboBox.setItems(FXCollections.observableArrayList(mixersToUse));
        secondComboBox.setItems(FXCollections.observableArrayList(mixersToUse));
        
        firstComboBox.setValue(parentController.settings.get("Audio", "firstOutput"));
        secondComboBox.setValue(parentController.settings.get("Audio", "secondOutput"));
    }
    
    public void audioSettingChanged(){
        audioApplyButton.setDisable(false);
    }
    
    public void audioApplyButtonClicked(){
        audioChanged = true;
        audioApplyButton.setDisable(true);
    }
    
    public void remoteApplyButtonClicked(){
        remoteChanged = true;
        remoteApplyButton.setDisable(true);
    }
    
    //TODO: Clicking ok automaticly changes the changed of the selected tab to true
    public void okButtonClicked(){
        success = true;
        
        int selectedTab = settingsTabPane.getSelectionModel().getSelectedIndex();
        if(selectedTab == 0){
            audioChanged = true;
        }
        else if(selectedTab == 1){
            remoteChanged = true;
        }
        
        if(audioChanged){
            parentController.settings.put("Audio", "firstOutput", firstComboBox.getValue());
            parentController.settings.put("Audio", "secondOutput", secondComboBox.getValue());
        }
        if(remoteChanged){
            //TODO: Save remote settings  
        }
        stage.close();
    }

    public void cancelButtonClicked(){
        stage.close();
    }

    //In direct sound
    //Source -> Can Play
    //Target -> Can Record
}
