package Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Minimal ImageUtil: stores uploaded images under data/images and returns the
 * absolute path.
 */
public class ImageUtil {

    public static String storeImage(File source) throws IOException {
        if (source == null || !source.exists()) {
            throw new IOException("Source file missing");
        }
        Path imagesDir = Path.of("data", "images");
        Files.createDirectories(imagesDir);
        String ext = "";
        String name = source.getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            ext = name.substring(dot);
        }
        String filename = System.currentTimeMillis() + "-" + UUID.randomUUID().toString() + ext;
        Path dest = imagesDir.resolve(filename);
        Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toAbsolutePath().toString();
    }
}
