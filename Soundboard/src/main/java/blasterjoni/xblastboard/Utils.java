/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blasterjoni.xblastboard;

/**
 *
 * @author João Arvana
 */
public class Utils {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    public static boolean isWindows() {
        return (OS.contains("win"));
    }
    public static boolean isMac() {
        return (OS.contains("mac"));
    }

}

//Packaging it allongside
                /*FFmpeg ffmpeg;
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
                }*/