package pg.gipter.core.producers;

import java.nio.file.Path;

class DiffDetails {

    private Path filePath;
    private boolean diff;
    private final String projectPath;

    DiffDetails(String projectPath) {
        this.projectPath = projectPath;
    }

    public Path getFilePath() {
        return filePath;
    }

    public boolean isDiff() {
        return diff;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public void setDiff(boolean diff) {
        this.diff = diff;
    }
}
