package pg.gipter.core.producers;

class DiffDetails {

    private String content;
    private boolean diff;
    private final String projectPath;

    DiffDetails(String projectPath) {
        this.content = content;
        this.diff = diff;
        this.projectPath = projectPath;
    }

    public String getContent() {
        return content;
    }

    public boolean isDiff() {
        return diff;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDiff(boolean diff) {
        this.diff = diff;
    }
}
