package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LLamaReader {

    private String savedIn = "";
    private StringBuilder response = new StringBuilder();
    private Process process;
    private BufferedReader reader;
    private OutputStream outputStream;

    public static void main(String[] args) {
        LLamaReader ai = new LLamaReader();
        ai.killProcesses();
        ai.checkGPU();
//        ai.testCode(); 
    }
    
    public void checkGPU(){
        StringBuilder gpuInfoBuilder = new StringBuilder();
        
        try {
            String command = "wmic path win32_VideoController get Name,AdapterRAM";
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                gpuInfoBuilder.append(line).append(System.lineSeparator());
            }
            
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println(gpuInfoBuilder.toString());
    }

    public void testCode() {
        try ( Scanner in = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter Input: ");
                String input = in.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                savedIn += "> " + input + "\n";
                runFile(input);
            }
        }

        System.out.println("Input: \n" + savedIn);
        System.out.println("Response: \n" + response.toString());
        close();
    }

    public void runFile(String userIn) {
        try {
            String projectFolderPath = System.getProperty("user.dir");
            String batFilePath = projectFolderPath + "\\llama-master-63d2046-bin-win-avx-x64\\run.bat";

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "call", batFilePath);
            processBuilder.directory(new File(projectFolderPath));
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            outputStream = process.getOutputStream();

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("If you want to submit another line, end your input with '\\'.")) {
                    break;
                }
            }
            System.out.println("File Execution Complete. Waiting for User response.");

            System.out.print("\n> " + userIn);
            if (userIn.equalsIgnoreCase("exit")) {
                killProcesses();
            } else {
                sendInput(userIn);
                killProcesses();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendInput(String input) {
        try {
            if (outputStream != null) {
                outputStream.write((input + "\n").getBytes(StandardCharsets.UTF_8));
                outputStream.flush();

                // Read and print the response
                char[] buffer = new char[4096];
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) != -1) {
                    String output = new String(buffer, 0, bytesRead);
                    System.out.print(output);
                    if (output.contains("#")) {
                        break;
                    }
                    if (response.toString().toLowerCase().contains("user:")) {
                        break;
                    }
                    response.append(output);
                    
                }
            } else {
                System.out.println("No active process to send input to.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void killProcesses() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            if (os.contains("win")) {
                // Windows
                processBuilder = new ProcessBuilder("taskkill", "/F", "/IM", "main.exe");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux or macOS
                processBuilder = new ProcessBuilder("pkill", "main");
            } else {
                throw new UnsupportedOperationException("Unsupported operating system: " + os);
            }

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Unused processes named main.exe have been terminated successfully.");
            } else {
                System.err.println("No unused processes to terminate.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (process != null) {
                process.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
