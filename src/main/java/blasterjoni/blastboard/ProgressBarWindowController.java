package blasterjoni.blastboard;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import net.bramp.ffmpeg.job.FFmpegJob;

/**
 * FXML Controller class
 *
 * @author João Arvana
 */
public class ProgressBarWindowController {

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Label titleLabel;
    
    private Thread jobThread;
    private String currentAction;
    
    public Stage stage;
    public void init(Stage stage, Runnable job) {
        this.stage = stage;
        this.jobThread = new Thread(job);
        
        titleLabel.setText(stage.getTitle());
        
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (jobThread.getState().equals(Thread.State.NEW)){
                    jobThread.start();
                }
            }
        });
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
            }
        });
    } 
    
    public void setProgressValue(double value){
        progressBar.setProgress(value);
        int percentage = (int) (value * 100);
        progressLabel.setText(currentAction + ": " + Integer.toString(percentage)+"%");
    }
    
    public void setCurrentAction(String action){
        currentAction = action;
    }
    
    public void close(){
        stage.close();
    }
    
    
}
