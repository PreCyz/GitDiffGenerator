package pg.gipter.services.upgrade;

class UpgradeZipTest {

    /*private void decompress(File sevenZSourceFile, File destination) throws IOException {
        try (SevenZFile sevenZFile = SevenZFile.builder()
                .setSeekableByteChannel(Files.newByteChannel(sevenZSourceFile.toPath(), EnumSet.of(StandardOpenOption.READ)))
                .setDefaultName(sevenZSourceFile.getName())
                .get()) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    Path directory = Paths.get(destination.getName(), entry.getName());
                    if (!Files.exists(directory)) {
                        Files.createDirectory(directory);
                    }
                    continue;
                }
                File currentFile = new File(destination, entry.getName());
                if (currentFile.isFile() && DaoConstants.GIFS_JSON.equalsIgnoreCase(currentFile.getName())) {
                    System.out.println("[gifs.json] already exist - skipping it.");
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
            System.err.println("What da hell? " + ex.getMessage());
            throw ex;
        }
    }

    @Test
    void name() throws IOException {
        Path destinationDir = Paths.get("distro");
        decompress(
                Paths.get("C:", "Workspace", "GitDiffGenerator", "src", "test", "java", "resources", "11+Gipter-v5.0.1.7z").toFile(),
                destinationDir.toFile()
        );

        createShortcut(destinationDir);
    }

    public void createShortcut(Path destinationDir) {

        Path jar = Paths.get(
                "C:",
                "Workspace",
                "GitDiffGenerator",
                "distro",
                "Gipter.jar"
        );
        Path runtimeFolder = Paths.get(destinationDir.toAbsolutePath().normalize().toString(), "runtime");

        if (Files.exists(runtimeFolder) && Files.exists(jar)) {
            try {
                Path shortcutLnkPath = Paths.get(
                        "C:",
                        "Workspace",
                        "GitDiffGenerator",
                        "distro",
                        "Gipter.lnk"
                );
                String target = Paths.get(destinationDir.normalize().toAbsolutePath().toString(), "runtime", "bin", "javaw.exe")
                        .normalize().toAbsolutePath().toString();

                int iconNumber = 0;
                ShellLink shellLink = ShellLink.createLink(target)
                        .setWorkingDir(destinationDir.toAbsolutePath().normalize().toString())
                        .setIconLocation(Paths.get(destinationDir.toAbsolutePath().normalize().toString(), "gipter.ico").toAbsolutePath().toString())
                        .setCMDArgs(" -jar Gipter.jar "*//* + ArgName.useUI.name() + "=Y"*//*);
                shellLink.getHeader().setIconIndex(iconNumber);
                shellLink.saveTo(shortcutLnkPath.toAbsolutePath().toString());
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }*/
}