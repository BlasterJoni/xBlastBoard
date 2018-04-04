/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blasterjoni.blastboard;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tools.ant.taskdefs.Zip;
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
    static final String TMP = BLASTBOARDIR + "/TMP";
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
                Files.copy(Paths.get(layoutIcon), Paths.get(newLayoutDir.getAbsolutePath() + "/icon"), StandardCopyOption.REPLACE_EXISTING);
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
                Files.copy(Paths.get(layoutBackground), Paths.get(newLayoutDir.getAbsolutePath() + "/background"), StandardCopyOption.REPLACE_EXISTING);
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
                Files.copy(Paths.get(buttonIcon), Paths.get(newLayoutDir.getAbsolutePath() + "/buttonIcon"), StandardCopyOption.REPLACE_EXISTING);
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
                Files.copy(Paths.get(buttonBackground), Paths.get(newLayoutDir.getAbsolutePath() + "/buttonBackground"), StandardCopyOption.REPLACE_EXISTING);
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
                Files.copy(Paths.get(buttonIcon), Paths.get(newButtonDir.getAbsolutePath() + "/icon"), StandardCopyOption.REPLACE_EXISTING);
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
                Files.copy(Paths.get(buttonBackground), Paths.get(newButtonDir.getAbsolutePath() + "/background"), StandardCopyOption.REPLACE_EXISTING);
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
            //Checking if buttonSound is still the same sound, if not then move/convert the new one
            //This is needed here but not in the other files because the other files use Files.copy, wich probably alredy checks for this
            //or does some other kind of fuckery, while in the sound file we copying it manualy bit by bit in order to have a progress bar.
            if(!buttonSound.equals(LAYOUTSDIR + "/" + layoutId + "/" + buttonID + "/sound.mp3")){
                try {
                    if(FilenameUtils.getExtension(buttonSound).equals("mp3")){
                        //Files.copy(Paths.get(buttonSound), Paths.get(newButtonDir.getAbsolutePath() + "/sound"), StandardCopyOption.REPLACE_EXISTING);
                        Stage stageACPB = new Stage();

                        FXMLLoader loader = new FXMLLoader(BBFiles.class.getResource("/fxml/ProgressBarWindow.fxml"));
                        Parent root = loader.load();
                        ProgressBarWindowController controller = loader.getController();       
                        Scene scene = new Scene(root);

                        Runnable job = new Runnable() {
                            @Override
                            public void run() {
                                File src = new File(buttonSound);
                                File dst  = new File(newButtonDir.getAbsolutePath() + "/sound.mp3");
                                try {
                                    InputStream in = new FileInputStream(src);
                                    OutputStream out = new FileOutputStream(dst);
                                    // Transfer bytes from in to out
                                    long expectedBytes = src.length(); // This is the number of bytes we expected to copy..
                                    long totalBytesCopied = 0; // This will track the total number of bytes we've copied
                                    byte[] buf = new byte[1024];
                                    int len = 0;
                                    while ((len = in.read(buf)) > 0) {
                                        out.write(buf, 0, len);
                                        totalBytesCopied += len;
                                        double progress = (double)totalBytesCopied / (double)expectedBytes;
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                controller.setProgressValue(progress);
                                            }
                                        });
                                        System.out.println("Moving audio file: " + (int)(progress*100) + "%");
                                    }
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            stageACPB.close();
                                        }
                                    });
                                    in.close();
                                    out.close();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        };


                        stageACPB.setScene(scene);
                        stageACPB.setTitle("BlastBoard - Moving audio file");
                        stageACPB.setMinWidth(550);
                        stageACPB.setMinHeight(150);
                        stageACPB.initModality(Modality.APPLICATION_MODAL);
                        controller.init(stageACPB, job);

                        stageACPB.showAndWait();
                    } else {
                        Stage stageACPB = new Stage();

                        FXMLLoader loader = new FXMLLoader(BBFiles.class.getResource("/fxml/ProgressBarWindow.fxml"));
                        Parent root = loader.load();
                        ProgressBarWindowController controller = loader.getController();       
                        Scene scene = new Scene(root);


                        FFmpeg ffmpeg = new FFmpeg();
                        FFprobe ffprobe = new FFprobe();

                        FFmpegProbeResult in = ffprobe.probe(buttonSound);
                        FFmpegBuilder builder = new FFmpegBuilder()
                                .setInput(in)
                                .overrideOutputFiles(true)
                                .addOutput(newButtonDir.getAbsolutePath() + "/sound.mp3")
                                    .setFormat("mp3")
                                    .setAudioChannels(2)
                                    .setAudioSampleRate(44_100)
                                    .done();

                        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

                        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
                            // Using the FFmpegProbeResult determine the duration of the input
                            final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

                            @Override
                            public void progress(Progress progress) {
                                    double percentage = progress.out_time_ns / duration_ns;
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            controller.setProgressValue(percentage);
                                            if(progress.status.toString().equals("end")){
                                                stageACPB.close(); 
                                            }
                                        }
                                    });
                                    System.out.println("Converting audio file: " + (int)(percentage*100) + "%");
                            }
                        });


                        stageACPB.setScene(scene);
                        stageACPB.setTitle("BlastBoard - Converting audio file");
                        stageACPB.setMinWidth(550);
                        stageACPB.setMinHeight(150);
                        stageACPB.initModality(Modality.APPLICATION_MODAL);
                        controller.init(stageACPB, job);

                        stageACPB.showAndWait();
                    }
                } catch (IOException IOE) {
                    IOE.printStackTrace();
                    System.out.println("FFmpeg not found");
                    buttonProperties.put("sound", false);
                }
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
        button.soundPath = workingButtonDIR + "/sound.mp3";
        
        return button;
    }
    
    //Importing and exporting
    //TODO: export progress bar
    public static void exportLayout(String layoutID, File out){
        try {
            ZipFile zip = new ZipFile(out);
            zip.addFolder(new File(LAYOUTSDIR+"/"+layoutID), new ZipParameters());
        } catch (ZipException ex) {
            ex.printStackTrace();
        }
    } 
    
    public static void importLayout(File in){
        try {
            Stage stageACPB = new Stage();
            
            FXMLLoader loader = new FXMLLoader(BBFiles.class.getResource("/fxml/ProgressBarWindow.fxml"));
            Parent root = loader.load();
            ProgressBarWindowController controller = loader.getController();            
            Scene scene = new Scene(root);
            
            Runnable job = new Runnable() {
                @Override
                public void run() {
                    try {
                        ZipFile zip = new ZipFile(in);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while(true){
                                    int percent = zip.getProgressMonitor().getPercentDone();
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            controller.setProgressValue(percent/100.00f);
                                        }
                                    });
                                    if(percent >= 99){
                                        break;
                                    }
                                    System.out.println("Importing layout : " + percent + "%");
                                }
                            }
                        }).start();
                        zip.extractAll(TMP);
                        
                        File[] directories = new File(TMP).listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File file) {
                                return file.isDirectory();
                            }
                        });
                        
                        String folderName = directories[0].getName();
                        if (new File(LAYOUTSDIR + "/" + folderName).exists()) {
                            folderName = folderName + RandomStringUtils.randomAlphanumeric(10);
                            directories[0].renameTo(new File(TMP + "/" + folderName));
                        }
                        
                        try {
                            Files.move(Paths.get(TMP + "/" + folderName), Paths.get(LAYOUTSDIR + "/" + folderName));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            //Delete if failed to move
                            try {
                                File fileToDelete = new File(TMP + "/" + folderName);
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
                            } catch (Exception ex1) {
                                ex1.printStackTrace();
                            }
                        }
                        
                        List<String> layoutList = BBFiles.getLayoutList();
                        layoutList.add(folderName);
                        BBFiles.saveLayoutList(layoutList);
                        
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                stageACPB.close();
                            }
                        });
                    } catch (ZipException ex) {
                        ex.printStackTrace();
                    }                    
                }
            };
            
            stageACPB.setScene(scene);
            stageACPB.setTitle("BlastBoard - Importing Layout");
            stageACPB.setMinWidth(550);
            stageACPB.setMinHeight(150);
            stageACPB.initModality(Modality.APPLICATION_MODAL);
            controller.init(stageACPB, job);
            
            stageACPB.showAndWait();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }
    
    
    //TODO: export progress bar
    public static void exportButton(String layoutID, String buttonID, File out){
        try {
            ZipFile zip = new ZipFile(out);
            zip.addFolder(new File(LAYOUTSDIR+"/"+layoutID+"/"+buttonID), new ZipParameters());
        } catch (ZipException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void importButton(String layoutID, File in){
        try {
            Stage stageACPB = new Stage();
            
            FXMLLoader loader = new FXMLLoader(BBFiles.class.getResource("/fxml/ProgressBarWindow.fxml"));
            Parent root = loader.load();
            ProgressBarWindowController controller = loader.getController();            
            Scene scene = new Scene(root);
            
            Runnable job = new Runnable() {
                @Override
                public void run() {
                    try {
                        ZipFile zip = new ZipFile(in);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while(true){
                                    int percent = zip.getProgressMonitor().getPercentDone();
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            controller.setProgressValue(percent/100.00f);
                                        }
                                    });
                                    if(percent >= 99){
                                        break;
                                    }
                                    System.out.println("Importing button : " + percent + "%");
                                }
                            }
                        }).start();
                        zip.extractAll(TMP);
                        
                        File[] directories = new File(TMP).listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File file) {
                                return file.isDirectory();
                            }
                        });
                        
                        String folderName = directories[0].getName();
                        if (new File(LAYOUTSDIR + "/" + layoutID + "/" + folderName).exists()) {
                            folderName = folderName + RandomStringUtils.randomAlphanumeric(10);
                            directories[0].renameTo(new File(TMP + "/" + folderName));
                        }
                        
                        try {
                            Files.move(Paths.get(TMP + "/" + folderName), Paths.get(LAYOUTSDIR + "/" + layoutID + "/" + folderName));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            //Delete if failed to move
                            try {
                                File fileToDelete = new File(TMP + "/" + folderName);
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
                            } catch (Exception ex1) {
                                ex1.printStackTrace();
                            }
                        }
                        
                        List<String> buttonList = BBFiles.getButtonList(layoutID);
                        buttonList.add(folderName);
                        BBFiles.saveButtonList(layoutID, buttonList);
                        
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                stageACPB.close();
                            }
                        });
                    } catch (ZipException ex) {
                        ex.printStackTrace();
                    }                    
                }
            };
            
            stageACPB.setScene(scene);
            stageACPB.setTitle("BlastBoard - Importing Button");
            stageACPB.setMinWidth(550);
            stageACPB.setMinHeight(150);
            stageACPB.initModality(Modality.APPLICATION_MODAL);
            controller.init(stageACPB, job);
            
            stageACPB.showAndWait();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }
}
