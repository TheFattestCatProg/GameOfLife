package com.thefattestcat.GameOfLife;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public final class Settings {

    private static final Pattern INT_REGEX = Pattern.compile("^\\d+$");
    private static final Pattern INT_TUPLE_REGEX = Pattern.compile("^\\d+,\\d+$");

    private static int renderTime = 16;
    private static int simulationTime = 16;
    private static Dimension windowSize = new Dimension(800, 600);
    private static int worldSize = 128;

    private static int simulationButton = KeyEvent.VK_SPACE;
    private static int simulationStepButton = KeyEvent.VK_S;
    private static int clearFieldButton = KeyEvent.VK_V;

    public static int getRenderTime() {
        return renderTime;
    }

    public static int getSimulationTime() {
        return simulationTime;
    }

    public static Dimension getWindowSize() {
        return windowSize;
    }

    public static int getWorldSize() {
        return worldSize;
    }

    public static int getSimulationButton() {
        return simulationButton;
    }

    public static int getSimulationStepButton() {
        return simulationStepButton;
    }

    public static int getClearFieldButton() {
        return clearFieldButton;
    }

    public static void loadFromFile(String path) {
        System.out.printf("Parsing settings file `%s`\n", path);

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            for (String line; (line = br.readLine()) != null; ) {
                int commentIndex = line.indexOf('#');
                if (commentIndex != -1) line = line.substring(0, commentIndex);

                line = line.trim();

                if (line.isEmpty()) continue;
                
                int eqIndex = line.indexOf('=');
                if (eqIndex == -1) {
                    System.err.println("Bad format while loading settings");
                    break;
                }

                String key = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();
                
                setSetting(key, value);
            }
        }
        catch (IOException err) {
            System.err.printf("Cannot read config file `%s`\n", path);
        }
    }

    private static void setSetting(String key, String value) {
        
        switch (key) {
            case "renderTime": {
                if (INT_REGEX.matcher(value).matches())
                    renderTime = Integer.parseInt(value);
                else 
                    System.err.println("Bad int in `renderTime`");
                break;
            }
            case "simulationTime": {
                if (INT_REGEX.matcher(value).matches())
                    simulationTime = Integer.parseInt(value);
                else 
                    System.err.println("Bad int in `simulationTime`");
                break;
            }
            case "worldSize": {
                if (INT_REGEX.matcher(value).matches())
                    worldSize = Integer.parseInt(value);
                else 
                    System.err.println("Bad int in `worldSize`");
                break;
            }
            case "simulationButton": {
                if (INT_REGEX.matcher(value).matches())
                    simulationButton = Integer.parseInt(value);
                else 
                    System.err.println("Bad int in `simulationButton`");
                break;
            }
            case "simulationStepButton": {
                if (INT_REGEX.matcher(value).matches())
                    simulationStepButton = Integer.parseInt(value);
                else 
                    System.err.println("Bad int in `simulationStepButton`");
                break;
            }
            case "clearFieldButton": {
                if (INT_REGEX.matcher(value).matches())
                    clearFieldButton = Integer.parseInt(value);
                else 
                    System.err.println("Bad int in `clearFieldButton`");
                break;
            }
            case "windowSize": {
                if (!INT_TUPLE_REGEX.matcher(value).matches()) {
                    System.err.println("Bad value in `windowSize`");
                    break;
                }

                int i = value.indexOf(',');
                String v1 = value.substring(0, i);
                String v2 = value.substring(i+1);

                windowSize = new Dimension(Integer.parseInt(v1), Integer.parseInt(v2));
                break;
            }
            
            default:
                System.err.printf("Bad key `%s`\n", key);
                break;
        }
    }
}
