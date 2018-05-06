/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blasterjoni.xblastboard;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author João Arvana
 */
public class AboutWindowController {
    
    MainWindowController parent;
    Stage stage;
    
    public void init(MainWindowController parent, Stage stage){
        this.parent = parent;
        this.stage = stage;
    }
    
    public void depButtonClicked() throws IOException, URISyntaxException{
        Stage stageWebView = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WebViewWindow.fxml"));
        Parent root = loader.load();
        WebViewWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageWebView.setScene(scene);
        stageWebView.setTitle("xBlastBoard - License");
        stageWebView.setMinWidth(650);
        stageWebView.setMinHeight(250);
        stageWebView.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        controller.init(this, stageWebView, AboutWindowController.class.getResource("/depLicenses.html").toExternalForm());
        
        stageWebView.show();
    }
    
    public void xbbButtonClicked() throws IOException {
        Stage stageWebView = new Stage();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WebViewWindow.fxml"));
        Parent root = loader.load();
        WebViewWindowController controller = loader.getController();       
        Scene scene = new Scene(root);
        
        stageWebView.setScene(scene);
        stageWebView.setTitle("xBlastBoard - License");
        stageWebView.setMinWidth(650);
        stageWebView.setMinHeight(250);
        stageWebView.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        controller.init(this, stageWebView, AboutWindowController.class.getResource("/LICENSE.txt").toExternalForm());
        
        stageWebView.show();
    }
    
}
