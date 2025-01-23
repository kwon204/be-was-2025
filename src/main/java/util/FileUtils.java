package util;

import util.exception.NoSuchPathException;

import java.io.*;
import java.util.UUID;

public class FileUtils {
    private static final String ROOT_PATH = "./src/main/resources/static";

    public static File findFile(String target) {
        File file = new File(ROOT_PATH + target);

        return isAvailable(file);
    }

    private static File findFile(File file) {
        return new File(file.getPath() + "/index.html");
    }

    private static File isAvailable(File file) {
        if (file.isDirectory()) {
            file = findFile(file);
        }
        if (!file.exists()) {
            throw new NoSuchPathException();
        }

        return file;
    }

    public static String getExtension(File file) {
        return file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    public static byte[] convertToByte(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return fis.readAllBytes();
    }

    public static String convertToString(File file) throws IOException {
        byte[] b = convertToByte(file);

        return new String(b, "utf-8");
    }

    public static String saveImage(String fileName, byte[] body) throws IOException {
        String extension = fileName.substring(fileName.lastIndexOf("."));

        String filePath = UUID.randomUUID().toString().substring(0, 7) + extension;
        try (FileOutputStream fos = new FileOutputStream(ROOT_PATH + "/img/bucket/" + filePath)) {
            fos.write(body);
            fos.flush();
        }
        return filePath;
    }
}
