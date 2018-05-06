/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blasterjoni.xblastboard;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @author João Arvana
 */
public class WebViewWindowController {
    
    @FXML
    WebView WebView;
    
    AboutWindowController parent;
    Stage stage;
    
    public void init(AboutWindowController parent, Stage stage,String url){
        this.parent = parent;
        this.stage = stage;
        WebView.getEngine().load(url);
    }
    
}
