/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blasterjoni.blastboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 *
 * @author João Arvana
 */
public class ItemForLayoutComboBox {

    private String text = null;
    private Color textColor = null;
    private Image icon = null;
    private BackgroundFill backgroundColor = null;
    private BackgroundImage backgroundImage = null;


    @Override
    public String toString(){
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setIcon(String icon) {
        if (icon == null) {
            this.icon = null;
        } else {
            try {
                this.icon = new Image(new FileInputStream(icon));
            } catch (FileNotFoundException FNFE) {
                FNFE.printStackTrace();
                this.icon = null;
            }
        }
    }

    public Image getIcon() {
        return icon;
    }

    public void setBackgroundImage(String backgroundImage) {
        if (backgroundImage == null) {
            this.backgroundImage = null;
        } else {
            try {
                Image image = new Image(new FileInputStream(backgroundImage));
                this.backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(0, 0, true, true, false, true));
            } catch (FileNotFoundException FNFE) {
                FNFE.printStackTrace();
                this.backgroundImage = null;
            }
        }
    }

    public BackgroundImage getBackgroundImage() {
        return backgroundImage;
    }
    
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
    }

    public BackgroundFill getBackgroundColor() {
        return backgroundColor;
    }
}
