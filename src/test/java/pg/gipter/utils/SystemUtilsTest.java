package pg.gipter.utils;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.util.EnumSet;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class SystemUtilsTest {

    @Test
    void whenTmp_thenReturnValue() {
        try {
            assertThat(SystemUtils.tmp()).isNotNull().isNotEmpty();
        } catch (IllegalArgumentException e) {
            fail("Should return tmp");
        }
    }

    @Test
    void verifyJreRelease() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(Paths.get(".", "src", "test", "java", "resources", "release"))) {
            properties.load(is);
        }
        assertThat(properties.getProperty("IMAGE_TYPE", "").toUpperCase()).contains("JRE");
    }

    @Test
    void verifyNoJreRelease() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(
                Paths.get(".", "src", "test", "java", "resources", "releaseCustomImage")
        )) {
            properties.load(is);
        }
        assertThat(properties.getProperty("IMAGE_TYPE", "").toUpperCase()).doesNotContain("JRE");
    }

    @Test
    @Disabled
    void decompress() {
        File z7 = Paths.get(".", "src", "test", "java", "resources", "Gipter-5.0.0-portable.7z").toFile();
        File destination = Paths.get(".", "decompressed").toFile();
        decompress(z7, destination);
    }

    private void decompress(File sevenZSourceFile, File destination) {
        try (SevenZFile sevenZFile = SevenZFile.builder()
                .setSeekableByteChannel(Files.newByteChannel(sevenZSourceFile.toPath(), EnumSet.of(StandardOpenOption.READ)))
                .setDefaultName(sevenZSourceFile.getName())
                .get()) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File currentFile = new File(destination, entry.getName());
                if (currentFile.isFile() && "gifs.json".equalsIgnoreCase(currentFile.getName())) {
                    System.out.printf("[%s] already exist - skipping it %n", currentFile.getName());
                } else {
                    File parent = currentFile.getParentFile();
                    if (!parent.exists()) {
                        final boolean mkdir = parent.mkdirs();
                        System.out.printf("Directory created [%s] [%s]%n", mkdir, parent.getAbsolutePath());
                    }
                    FileOutputStream out = new FileOutputStream(currentFile);
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content, 0, content.length);
                    out.write(content);
                    out.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}