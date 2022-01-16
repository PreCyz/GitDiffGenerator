package pg.gipter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SmartZipService {

    private static final long MAX_SIZE = 500000; //500KB
    public static final Logger logger = LoggerFactory.getLogger(SmartZipService.class);

    public Path zipFile(Path pathToFile) {
        Path result = pathToFile;
        if (shouldZip(pathToFile)) {
            String fileName = pathToFile.getFileName().toString();
            Path zippedFilePath = Paths.get(fileName.substring(0, fileName.lastIndexOf(".")) + ".zip");

            try (OutputStream fos = Files.newOutputStream(zippedFilePath);
                 ZipOutputStream zipOut = new ZipOutputStream(fos);
                 InputStream fis = Files.newInputStream(pathToFile)
            ) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                result = zippedFilePath;
            } catch (IOException ex) {
                logger.error("Could not zip file [{}]", pathToFile, ex);
            }
        }
        return result;
    }

    public boolean shouldZip(Path pathToFile) {
        if (!pathToFile.getFileName().toString().endsWith(".zip")) {
            boolean result = pathToFile.toFile().length() > MAX_SIZE;
            logger.info("Size of the item [{}] in bytes [{}]. Item is{}going to be zipped.",
                    pathToFile, pathToFile.toFile().length(), result ? " " : " not ");
            return result;
        }
        logger.info("Item [{}] has been already zipped.", pathToFile);
        return false;
    }
}
