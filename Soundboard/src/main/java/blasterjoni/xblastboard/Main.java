package blasterjoni.xblastboard;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import java.io.File;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    MainWindowController controller;
    
    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Before anything check if files exist, if not create them.
        String home = System.getProperty("user.home");
        String BlastBoardDirPath = home + "/.BlastBoard";
        File BlastBoardDir = new File(BlastBoardDirPath);
        if(!BlastBoardDir.exists()){
            BlastBoardDir.mkdirs();
            BBFiles.createNewSettings();
            BBFiles.createNewLayoutList();
            
            BBFiles.saveLayout("Default", "Default Layout", Color.BLACK, "", "", Color.WHITE, "Button", Color.BLACK, "", "", Color.WHITE);
            BBFiles.createNewButtonList("Default");
            List<String> layoutList = BBFiles.getLayoutList();
            layoutList.add("Default");
            BBFiles.saveLayoutList(layoutList);
        }
        // Everywhere forward these files are assumed to exist.
        
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("xBlastBoard - Main");
        stage.setMinWidth(650);
        stage.setMinHeight(450);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                controller.close();
            }
        });
        controller.init(stage);
        
        //Startup code that ensures there is only one instance
        String appId = "xBlastBoard";
        List<String> args = getParameters().getRaw();
	try {
            JUnique.acquireLock(appId, new MessageHandler() {
                @Override
                public String handle(String message) {
                    // A brand new argument received! Handle it!
                    if (message.equals("open")){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                stage.setAlwaysOnTop(true);
                                stage.setAlwaysOnTop(false);
                            }
                        });
                    } else {
                        System.out.println("here");
                        File f = new File(message.trim());
                        if (f.isFile()) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    controller.openFile(f);
                                    stage.setAlwaysOnTop(true);
                                    stage.setAlwaysOnTop(false);
                                }
                            });
                        }
                    }
                    return null;
                }
            });
            //If it gets here this is the first instance
            stage.show();
            if (args.size() > 0){
                File f = new File(args.get(0));
                if (f.isFile()) {
                    controller.openFile(f);
                }
            }
	} catch (AlreadyLockedException e) {
            //Theres already another instance
            if (args.size() > 0){
                //Message is cutting off the last character so I'll just had a space so it removes it instead
                //This is fucking retarded, can't understand why it only removes it here but not when I send open
                JUnique.sendMessage(appId, args.get(0) + " ");
            } else {
                JUnique.sendMessage(appId, "open");
            }
	}
        
    }
}
