package kz.findmyname284.plugin.setsound;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Main {
    public static void main(String[] args) {
        try {
            setVolume(Integer.parseInt(args[0].trim()));
        } catch (Exception e) {
            // ignore
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static boolean isLinux() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("nix") || osName.contains("nux");
    }

    public static void setVolume(int volume) {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100");
        }

        if (isWindows()) {
            setVolumeWindows(volume);
        } else if (isLinux()) {
            setVolumeLinux(volume - getVolumeLinux() - 1);
        } else {
            System.out.println("Unsupported OS");
        }
    }

    private static void setVolumeWindows(int volume) {
        try {
            // Копируем nircmd.exe из ресурсов во временную папку
            InputStream nircmdStream = Main.class.getResourceAsStream("/nircmd.exe");
            File tempFile = File.createTempFile("nircmd", ".exe");
            assert nircmdStream != null;
            Files.copy(nircmdStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Выполняем команду для изменения громкости
            String command = tempFile.getAbsolutePath() + " setsysvolume " + (65535 * volume / 100);
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            // Удаляем временный файл
            tempFile.deleteOnExit();
        } catch (Exception e) {
            System.out.println("Failed to " + e.getMessage());
        }
    }

    private static void setVolumeLinux(int volume) {
        try {
            String[] cmd = {"amixer", "set", "Master", volume + "%"};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.start();
        } catch (Exception e) {
            System.out.println("Failed to " + e.getMessage());
        }
    }

    public static int getVolumeLinux() {
        try {
            String[] cmd = {"amixer", "get", "Master"};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Playback")) {
                    String[] tokens = line.split("\\[|\\]");
                    return Integer.parseInt(tokens[1].replace("%", ""));
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }
}