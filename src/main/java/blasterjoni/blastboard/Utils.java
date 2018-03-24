/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blasterjoni.blastboard;

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
