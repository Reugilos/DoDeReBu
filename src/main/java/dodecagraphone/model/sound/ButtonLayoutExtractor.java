package dodecagraphone.model.sound;

/**
 *
 * @author paugpt
 */
import java.io.*;
import java.util.regex.*;

public class ButtonLayoutExtractor {
    public static void main(String[] args) {
        String inputFilePath = "MyButtonPanel.java";
        String outputFilePath = "ButtonLayout.txt";
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
             
            String line;
            // Adjusted regex pattern to match both MyToggle and MyButton formats
            Pattern pattern = Pattern.compile("new\\s+(MyToggle|MyButton)\\((\\d+),\\s*(\\w+),\\s*(\\w+),\\s*\\w+,\\s*\\w+,\\s*\\w+,\\s*\\w+,\\s*\"([^\"]+)\"(?:,\\s*\"([^\"]+)\")?\\)");
            
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                
                if (matcher.find()) {
                    String type = matcher.group(1);
                    String id = matcher.group(2);
                    String col = matcher.group(3);
                    String row = matcher.group(4);
                    String textOn = matcher.group(5);
                    String textOff = matcher.group(6) != null ? matcher.group(6) : "";  // textOff only for MyToggle
                    boolean isToggle = type.equals("MyToggle");
                    
                    // Format output as: id;name;isToggle;row;column;textOn;textOff
                    bw.write(id + ";Button" + id + ";" + isToggle + ";" + row + ";" + col + ";" + textOn + ";" + textOff);
                    bw.newLine();
                }
            }
            
            System.out.println("Button layout successfully extracted to " + outputFilePath);
            
        } catch (IOException e) {
            System.err.println("Error reading or writing files: " + e.getMessage());
        }
    }
}
