package org.example;

import java.io.IOException;

public class TextUtils {
    public static void clearDisplay(){
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    public static void printLogo(){
        System.out.println("\n" +
                ".___________. __________   ___ .___________.   .______     ______     ______    __  ___ \n" +
                "|           ||   ____\\  \\ /  / |           |   |   _  \\   /  __  \\   /  __  \\  |  |/  / \n" +
                "`---|  |----`|  |__   \\  V  /  `---|  |----`   |  |_)  | |  |  |  | |  |  |  | |  '  /  \n" +
                "    |  |     |   __|   >   <       |  |        |   _  <  |  |  |  | |  |  |  | |    <   \n" +
                "    |  |     |  |____ /  .  \\      |  |        |  |_)  | |  `--'  | |  `--'  | |  .  \\  \n" +
                "    |__|     |_______/__/ \\__\\     |__|        |______/   \\______/   \\______/  |__|\\__\\ \n" +
                "                                                                                        \n" +
                "                    _______.  ______     ______  __       ___       __                  \n" +
                "                   /       | /  __  \\   /      ||  |     /   \\     |  |                 \n" +
                "                  |   (----`|  |  |  | |  ,----'|  |    /  ^  \\    |  |                 \n" +
                "                   \\   \\    |  |  |  | |  |     |  |   /  /_\\  \\   |  |                 \n" +
                "               .----)   |   |  `--'  | |  `----.|  |  /  _____  \\  |  `----.            \n" +
                "               |_______/     \\______/   \\______||__| /__/     \\__\\ |_______|            \n" +
                "                                                                                        \n");
    }
}
