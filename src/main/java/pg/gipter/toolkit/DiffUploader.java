package pg.gipter.toolkit;

/**Created by Pawel Gawedzki on 11-Oct-2018.*/
public class DiffUploader {

    private final String diffFilePath;
    private final ToolkitClient toolkitClient;

    public DiffUploader(String diffFilePath) {
        this.diffFilePath = diffFilePath;
        this.toolkitClient = new ToolkitClient();
    }

    public void uploadDiff() {
        throw new RuntimeException("Not implemented yet.");
    }
}
