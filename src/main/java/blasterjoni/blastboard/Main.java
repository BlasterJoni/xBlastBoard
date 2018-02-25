  package blasterjoni.blastboard;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.ini4j.Ini;

public class Main extends Application {

    public static void main(String[] args){
        // Before anything check if files exist, if not create them.
        String home = System.getProperty("user.home");
        String BlastBoardDir = home + "/.BlastBoard";
        File layoutsDir = new File(BlastBoardDir + "/Layouts");
        File settingsFile = new File(BlastBoardDir + "/settings.ini");
        if(!settingsFile.exists()){
            settingsFile.getParentFile().mkdirs();
            
            Ini settings = new Ini();
            
            settings.put("Audio", "firstOutput", "");
            settings.put("Audio", "secondOutput", "");
            
            settings.put("Main", "local", true);
            settings.put("Main", "localVOL", 100);
            settings.put("Main", "link", true);
            settings.put("Main", "output", true);
            settings.put("Main", "outputVOL", 100);
            
            try{
                settings.store(settingsFile);
            }
            catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
        if(!layoutsDir.exists()){
            layoutsDir.mkdirs();
        }
        // Everywhere forward these files are assumed to exist.
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();
        final MainWindowController controller = loader.getController();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("BlastBoard - Main");
        stage.setMinWidth(650);
        stage.setMinHeight(450);
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
