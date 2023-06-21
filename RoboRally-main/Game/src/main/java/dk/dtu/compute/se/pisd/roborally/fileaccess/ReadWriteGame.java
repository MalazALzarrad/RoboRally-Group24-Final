package dk.dtu.compute.se.pisd.roborally.fileaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.exceptions.TheBoardCantFoundException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReadWriteGame {
    private static final String SAVED_BOARDS_FOLDER = "savedBoards";
    private static final String DEFAULT_BOARDS_FOLDER = "boards";
    private static final String JSON_EXT = "json";

    public static void writeGameToDisk(String saveName, String json) {
        ClassLoader classLoader = ReadWriteGame.class.getClassLoader();
        String filename = Objects.requireNonNull(classLoader.getResource(SAVED_BOARDS_FOLDER)).getPath() + "/"
                + saveName + "." + JSON_EXT;
        filename = filename.replaceAll("%20", " ");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>())
                .setPrettyPrinting()
                .create();

        try (FileWriter fileWriter = new FileWriter(filename);
             JsonWriter writer = gson.newJsonWriter(fileWriter)) {

            writer.jsonValue(json);
            writer.flush();

        } catch (IOException ignored) {
        }
    }

    public static String readGameFromDisk(String resourcePath) throws TheBoardCantFoundException {
        ClassLoader classLoader = ReadWriteGame.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) throw new TheBoardCantFoundException(resourcePath);
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TheBoardCantFoundException(resourcePath);
        }
    }

    public static List<String> getNamesOfSavedBoards() {
        return getFileNamesInFolder(SAVED_BOARDS_FOLDER);
    }

    public static List<String> getNamesOfDefaultBoard() {
        return getFileNamesInFolder(DEFAULT_BOARDS_FOLDER);
    }

    private static List<String> getFileNamesInFolder(String folderName) {
        File[] listOfFiles = getFilesInFolder(folderName);
        List<String> fileNames = new ArrayList<>();

        for (File file : listOfFiles) {
            fileNames.add(file.getName().replace(".json", ""));
        }

        return fileNames;
    }

    private static File[] getFilesInFolder(String folderName) {
        ClassLoader classLoader = ReadWriteGame.class.getClassLoader();
        String fullPath = Objects.requireNonNull(classLoader.getResource(folderName)).getPath();

        fullPath = fullPath.replace("%20", " ");
        File folder = new File(fullPath);

        return folder.listFiles();
    }
}
