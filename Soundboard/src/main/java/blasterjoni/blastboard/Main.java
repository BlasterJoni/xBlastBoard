package blasterjoni.blastboard;

import java.io.File;
import java.util.List;
import javafx.application.Application;
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
        
        //TODO: Opening new files on startup
        if (args.length > 0) {
            File f = new File(args[0]);
            if (f.isFile()) {
                //Load;
            }
        }
        
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("BlastBoard - Main");
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

        stage.show();
    }
}
