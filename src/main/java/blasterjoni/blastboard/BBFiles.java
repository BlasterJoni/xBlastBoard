/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blasterjoni.blastboard;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.ini4j.Ini;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author João Arvana
 */
public class BBFiles {
    static final String HOME = System.getProperty("user.home");
    static final String BLASTBOARDIR = HOME + "/.BlastBoard";
    static final File LAYOUTSDIR = new File(BLASTBOARDIR + "/Layouts");
    static final File LAYOUTSFILE = new File(LAYOUTSDIR + "/layouts.json");
    static final File SETTINGSFILE = new File(BLASTBOARDIR + "/settings.ini");
    
    public static void createNewSettings(){
        SETTINGSFILE.getParentFile().mkdirs();
            
        Ini settings = new Ini();

        settings.put("Audio", "firstOutput", "");
        settings.put("Audio", "secondOutput", "");

        settings.put("Main", "local", true);
        settings.put("Main", "localVOL", 100);
        settings.put("Main", "link", true);
        settings.put("Main", "output", true);
        settings.put("Main", "outputVOL", 100);
        settings.put("Main", "currentLayout", "");

        try{
            settings.store(SETTINGSFILE);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    public static Ini loadSettings(){
        try (FileReader fr = new FileReader(SETTINGSFILE)){
            return new Ini(fr);
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        return null;
    }
    
    public static void saveSettings(Ini settings){
        try{
            settings.store(SETTINGSFILE);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        
    }
    
    
    /**
     * Used to create a new layoutList at startup.
     */
    public static void createNewLayoutList(){
        LAYOUTSFILE.getParentFile().mkdirs();
            
        JSONObject layouts = new JSONObject();

        layouts.put("layouts", new ArrayList<>());

        try(FileWriter file = new FileWriter(LAYOUTSFILE.getAbsolutePath())){
            file.write(layouts.toJSONString());
            file.flush();
        }catch(IOException IOE){
            IOE.printStackTrace();
        }
    }
    
    public static List<String> getLayoutList(){
        JSONParser parser = new JSONParser();
        
        try(FileReader fr = new FileReader(LAYOUTSFILE)){
            JSONObject layouts = (JSONObject) parser.parse(fr);
            return (List<String>) layouts.get("layouts");
        } catch (IOException ioe){
            ioe.printStackTrace();
        } catch (ParseException pe){
            pe.printStackTrace();
        }
        return null;
    }
    
    public static void saveLayoutList(List<String> layouts){
        JSONObject layoutsJSON = new JSONObject();
        
        layoutsJSON.put("layouts", layouts);
        
        try(FileWriter file = new FileWriter(LAYOUTSFILE)){
            file.write(layoutsJSON.toJSONString());
            file.flush();
        }catch(IOException IOE){
            IOE.printStackTrace();
        }     
    }
    
    /**
     * Saves the layout's files to the layoutID directory.
     * <b>Doesn't add the layout to the layoutList.</b>
     * @param layoutId
     * @param layoutText
     * @param layoutTextColor
     * @param layoutIcon
     * @param layoutBackground
     * @param layoutBackgroundColor
     * @param buttonText
     * @param buttonTextColor
     * @param buttonIcon
     * @param buttonBackground
     * @param buttonBackgroundColor 
     */
    public static void saveLayout(String layoutId, String layoutText, Color layoutTextColor, 
                           String layoutIcon, String layoutBackground, Color layoutBackgroundColor,
                           String buttonText, Color buttonTextColor, String buttonIcon,
                           String buttonBackground, Color buttonBackgroundColor){
        
        File newLayoutDir = new File(LAYOUTSDIR + "/" + layoutId);
        newLayoutDir.mkdirs();

        JSONObject layoutProperties = new JSONObject();
        
        // Layout Settings
        layoutProperties.put("text", layoutText);
        layoutProperties.put("textColor", layoutTextColor.toString());

        if(!layoutIcon.equals("")){
            layoutProperties.put("icon", true);
            try {
                java.nio.file.Files.copy(Paths.get(layoutIcon), Paths.get(newLayoutDir.getAbsolutePath() + "/icon"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException IOE) {
                IOE.printStackTrace();
                layoutProperties.put("icon", false);
            }
        } else {
            layoutProperties.put("icon", false);
        }

        if(!layoutBackground.equals("")){
            layoutProperties.put("backgroundImage", true);
            try {
                java.nio.file.Files.copy(Paths.get(layoutBackground), Paths.get(newLayoutDir.getAbsolutePath() + "/background"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException IOE) {
                IOE.printStackTrace();
                layoutProperties.put("backgroundImage", false);
            }
        } else {
            layoutProperties.put("backgroundImage", false);
        }
        layoutProperties.put("backgroundColor", layoutBackgroundColor.toString());

        
        // Button defaults
        JSONObject defaultButtonProperties = new JSONObject();

        defaultButtonProperties.put("text", buttonText);
        defaultButtonProperties.put("textColor", buttonTextColor.toString());

        if(!buttonIcon.equals("")){
            defaultButtonProperties.put("icon", true);
            try {
                java.nio.file.Files.copy(Paths.get(buttonIcon), Paths.get(newLayoutDir.getAbsolutePath() + "/buttonIcon"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException IOE) {
                IOE.printStackTrace();
                defaultButtonProperties.put("icon", false);
            }
        } else {
            defaultButtonProperties.put("icon", false);
        }

        if(!buttonBackground.equals("")){
            defaultButtonProperties.put("backgroundImage", true);
            try {
                java.nio.file.Files.copy(Paths.get(buttonBackground), Paths.get(newLayoutDir.getAbsolutePath() + "/buttonBackground"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException IOE) {
                IOE.printStackTrace();
                defaultButtonProperties.put("backgroundImage", false);
            }
        } else {
            defaultButtonProperties.put("backgroundImage", false);
        }
        defaultButtonProperties.put("backgroundColor", buttonBackgroundColor.toString());


        layoutProperties.put("buttonDefaultProperties", defaultButtonProperties);
        
        // Write json to file
        try(FileWriter file = new FileWriter(newLayoutDir + "/layout.json")){
            file.write(layoutProperties.toJSONString());
            file.flush();
        }catch(IOException IOE){
            IOE.printStackTrace();
        }
    }
    
    /**
     * Moves the given layout files from the old layoutID directory to the new layoutID directory,
     * also replaces the old ID in the layoutList with the new one.
     * @param layoutID
     * @param newLayoutID 
     */
    public static void changeLayoutID(String layoutID, String newLayoutID){
        File dir = new File(LAYOUTSDIR + "/" + layoutID);
        File newName = new File(LAYOUTSDIR + "/" + newLayoutID);
        try {
            Files.move(dir.toPath(), newName.toPath(), REPLACE_EXISTING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        List<String> layoutList = getLayoutList();
        layoutList.set(layoutList.indexOf(layoutID), newLayoutID);
        saveLayoutList(layoutList);
    }
    
    /**
     * Deletes the given layout directory.
     * <b>Doesn't remove the layout from the layoutList.</b>
     * @param layoutID 
     */
    public static void deleteLayout(String layoutID){
        try {
            File fileToDelete = new File(LAYOUTSDIR + "/" + layoutID);
            Files.walkFileTree(fileToDelete.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static LayoutProperties getLayoutProperties(String id){
        String workingLayoutDIR = LAYOUTSDIR + "/" + id;
        
        JSONParser parser = new JSONParser();
        JSONObject layoutJSON = null;
        JSONObject buttonDefaultJSON = null;
        try(FileReader fr = new FileReader(workingLayoutDIR + "/layout.json")){
            layoutJSON = (JSONObject) parser.parse(fr);
            buttonDefaultJSON = (JSONObject) layoutJSON.getOrDefault("buttonDefaultProperties", null);
        } catch (IOException ioe){
            ioe.printStackTrace();
        } catch (ParseException pe){
            pe.printStackTrace();
        }

        LayoutProperties layout = new LayoutProperties();
        
        layout.text = (String) layoutJSON.getOrDefault("text", "");
        try{
            layout.textColor = Color.web((String) layoutJSON.getOrDefault("textColor", "Black"));            
        }catch(Exception exc){
            exc.printStackTrace();
            layout.textColor = Color.BLACK;
        }
        layout.hasIcon = (Boolean) layoutJSON.getOrDefault("icon", false);
        layout.iconPath = workingLayoutDIR + "/icon";
        layout.hasBackgroundImage = (Boolean) layoutJSON.getOrDefault("backgroundImage", false);
        layout.backgroundImagePath = workingLayoutDIR + "/background";
        try{
            layout.backgroundColor = Color.web((String) layoutJSON.getOrDefault("backgroundColor", "White"));
        }catch(Exception exc){
            exc.printStackTrace();
            layout.backgroundColor = Color.WHITE;
        }
        
        layout.buttonDefault = new ButtonProperties();
        if (buttonDefaultJSON == null){
            layout.buttonDefault.text = "";
            layout.buttonDefault.textColor = Color.BLACK;
            layout.buttonDefault.hasIcon = false;
            layout.buttonDefault.iconPath = workingLayoutDIR + "/buttonIcon";
            layout.buttonDefault.hasBackgroundImage = false;
            layout.buttonDefault.backgroundImagePath = workingLayoutDIR + "/buttonBackground";
            layout.buttonDefault.backgroundColor = Color.WHITE;
        }else{
            layout.buttonDefault.text = (String) buttonDefaultJSON.getOrDefault("text", "");
            try{
                layout.buttonDefault.textColor = Color.web((String) buttonDefaultJSON.getOrDefault("textColor", "Black"));
            }catch(Exception exc){
                 exc.printStackTrace();
                 layout.buttonDefault.textColor = Color.BLACK;
            }
            layout.buttonDefault.hasIcon = (Boolean) buttonDefaultJSON.getOrDefault("icon", false);
            layout.buttonDefault.iconPath = workingLayoutDIR + "/buttonIcon";
            layout.buttonDefault.hasBackgroundImage = (Boolean) buttonDefaultJSON.getOrDefault("backgroundImage", false);
            layout.buttonDefault.backgroundImagePath = workingLayoutDIR + "/buttonBackground";
            try{
                layout.buttonDefault.backgroundColor = Color.web((String) buttonDefaultJSON.getOrDefault("backgroundColor", "White"));
            }catch(Exception exc){
                exc.printStackTrace();
                layout.buttonDefault.backgroundColor = Color.WHITE;
            }
        }   
        
        
        return layout;
    }
    
    
    /**
     * Used to create the button list upon new layout creation.
     * @param layoutID 
     */
    public static void createNewButtonList(String layoutID){
        String filePath = LAYOUTSDIR + "/" + layoutID + "/buttons.json";
        
        JSONObject layoutsJSON = new JSONObject();
        
        layoutsJSON.put("buttons", new ArrayList<String>());
        
        try(FileWriter file = new FileWriter(filePath)){
            file.write(layoutsJSON.toJSONString());
            file.flush();
        }catch(IOException IOE){
            IOE.printStackTrace();
        }
    }
    
    public static List<String> getButtonList(String layoutID){
        String file = LAYOUTSDIR + "/" + layoutID + "/buttons.json";
        
        JSONParser parser = new JSONParser();
        
        try(FileReader fr = new FileReader(file)){
            JSONObject layouts = (JSONObject) parser.parse(fr);
            return (List<String>) layouts.get("buttons");
        } catch (IOException ioe){
            ioe.printStackTrace();
        } catch (ParseException pe){
            pe.printStackTrace();
        }
        return null;
    }
    
    public static void saveButtonList(String layoutID, List<String> buttons){
        String filePath = LAYOUTSDIR + "/" + layoutID + "/buttons.json";
        
        JSONObject layoutsJSON = new JSONObject();
        
        layoutsJSON.put("buttons", buttons);
        
        try(FileWriter file = new FileWriter(filePath)){
            file.write(layoutsJSON.toJSONString());
            file.flush();
        }catch(IOException IOE){
            IOE.printStackTrace();
        } 
    }
    
    /**
     * Saves the button's files to the buttonID directory inside the layoutID directory.
     * <b>Doesn't add the button to the buttonList.</b>
     * @param layoutId
     * @param buttonID
     * @param buttonText
     * @param buttonTextColor
     * @param buttonIcon
     * @param buttonBackground
     * @param buttonBackgroundColor
     * @param buttonSound 
     */
    public static void saveButton(String layoutId, String buttonID, String buttonText, Color buttonTextColor, 
                           String buttonIcon, String buttonBackground, Color buttonBackgroundColor, String buttonSound){
        File newButtonDir = new File(LAYOUTSDIR + "/" + layoutId + "/" + buttonID);
        newButtonDir.mkdirs();

        // Button defaults
        JSONObject buttonProperties = new JSONObject();

        buttonProperties.put("text", buttonText);
        buttonProperties.put("textColor", buttonTextColor.toString());

        if(!buttonIcon.equals("")){
            buttonProperties.put("icon", true);
            try {
                java.nio.file.Files.copy(Paths.get(buttonIcon), Paths.get(newButtonDir.getAbsolutePath() + "/icon"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException IOE) {
                IOE.printStackTrace();
                buttonProperties.put("icon", false);
            }
        } else {
            buttonProperties.put("icon", false);
        }

        if(!buttonBackground.equals("")){
            buttonProperties.put("backgroundImage", true);
            try {
                java.nio.file.Files.copy(Paths.get(buttonBackground), Paths.get(newButtonDir.getAbsolutePath() + "/background"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException IOE) {
                IOE.printStackTrace();
                buttonProperties.put("backgroundImage", false);
            }
        } else {
            buttonProperties.put("backgroundImage", false);
        }
        buttonProperties.put("backgroundColor", buttonBackgroundColor.toString());
        
        if(!buttonSound.equals("")){
            buttonProperties.put("sound", true);
            try {
                //java.nio.file.Files.copy(Paths.get(buttonSound), Paths.get(newButtonDir.getAbsolutePath() + "/sound"), StandardCopyOption.REPLACE_EXISTING);
                
                //TODO: Find a good crossplatform solution, ill just use this lib and include them all (for all os) in the resources
                FFmpeg ffmpeg;
                FFprobe ffprobe;
                
                String path = BBFiles.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                File jar = new File(path);
                String parentDir = jar.getParentFile().getAbsolutePath();
                String decodedParentDir = URLDecoder.decode(parentDir, "UTF-8");

                if(Utils.isWindows()){
                    if(System.getProperty("sun.arch.data.model").equalsIgnoreCase("64")){
                        ffmpeg = new FFmpeg(decodedParentDir+"/ffmpeg/win/x64/ffmpeg.exe");
                        ffprobe = new FFprobe(decodedParentDir+"/ffmpeg/win/x64/ffprobe.exe");
                    } else {
                        ffmpeg = new FFmpeg(decodedParentDir+"/ffmpeg/win/x32/ffmpeg.exe");
                        ffprobe = new FFprobe(decodedParentDir+"/ffmpeg/win/x32/ffprobe.exe");
                    }   
                } else if(Utils.isMac()){
                    ffmpeg = new FFmpeg(decodedParentDir+"/ffmpeg/macos/x64/ffmpeg");
                    ffprobe = new FFprobe(decodedParentDir+"/ffmpeg/macos/x64/ffprobe");
                } else {
                    ffmpeg = new FFmpeg();
                    ffprobe = new FFprobe(); 
                }
                
                FFmpegBuilder builder = new FFmpegBuilder()
                        .setInput(buttonSound)
                        .overrideOutputFiles(true)
                        .addOutput(newButtonDir.getAbsolutePath() + "/sound")
                            .setFormat("wav")
                            .setAudioChannels(2)
                            .setAudioSampleRate(44_100)
                            .done();
                
                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
                
                executor.createJob(builder).run();
            } catch (IOException IOE) {
                IOE.printStackTrace();
                System.out.println("FFmpeg not found");
                buttonProperties.put("sound", false);
            }
        } else {
            buttonProperties.put("sound", false);
        }
        
        // Write json to file
        try(FileWriter file = new FileWriter(newButtonDir + "/button.json")){
            file.write(buttonProperties.toJSONString());
            file.flush();
        }catch(IOException IOE){
            IOE.printStackTrace();
        }
    }
    
    /**
     * Moves the given button files from the old buttonID directory to the new buttonID directory,
     * also replaces the old ID in the buttonList with the new one.
     * @param layoutID
     * @param buttonID
     * @param newButtonID 
     */
    public static void changeButtonID(String layoutID, String buttonID, String newButtonID){
        File dir = new File(LAYOUTSDIR + "/" + layoutID + "/" + buttonID);
        File newName = new File(LAYOUTSDIR + "/" + layoutID + "/" + newButtonID);
        try {
            Files.move(dir.toPath(), newName.toPath(), REPLACE_EXISTING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        List<String> buttonList = getButtonList(layoutID);
        buttonList.set(buttonList.indexOf(buttonID), newButtonID);
        saveButtonList(layoutID, buttonList);
    }
    
    /**
     * Deletes the given button directory inside the layoutID directory.
     * <b>Doesn't remove the button from the buttonList.</b>
     * @param layoutID
     * @param buttonID 
     */
    public static void deleteButton(String layoutID, String buttonID){
        try {
            File fileToDelete = new File(LAYOUTSDIR + "/" + layoutID + "/" + buttonID);
            Files.walkFileTree(fileToDelete.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static ButtonProperties getButtonProperties(String layoutID, String buttonID){
        String workingButtonDIR = LAYOUTSDIR + "/" + layoutID + "/" + buttonID;
        
        JSONParser parser = new JSONParser();
        JSONObject buttonJSON = null;
        try(FileReader fr = new FileReader(workingButtonDIR + "/button.json")){
            buttonJSON = (JSONObject) parser.parse(fr);
        } catch (IOException ioe){
            ioe.printStackTrace();
        } catch (ParseException pe){
            pe.printStackTrace();
        }

        ButtonProperties button = new ButtonProperties();
        
        button.text = (String) buttonJSON.getOrDefault("text", "");
        try{
            button.textColor = Color.web((String) buttonJSON.getOrDefault("textColor", "Black"));            
        }catch(Exception exc){
            exc.printStackTrace();
            button.textColor = Color.BLACK;
        }
        button.hasIcon = (Boolean) buttonJSON.getOrDefault("icon", false);
        button.iconPath = workingButtonDIR + "/icon";
        button.hasBackgroundImage = (Boolean) buttonJSON.getOrDefault("backgroundImage", false);
        button.backgroundImagePath = workingButtonDIR + "/background";
        try{
            button.backgroundColor = Color.web((String) buttonJSON.getOrDefault("backgroundColor", "White"));
        }catch(Exception exc){
            exc.printStackTrace();
            button.backgroundColor = Color.WHITE;
        }
        button.hasSound = (Boolean) buttonJSON.getOrDefault("sound", false);
        button.soundPath = workingButtonDIR + "/sound";
        
        return button;
    }
}
