package pg.gipter.core.dao.gif;

public class CustomGif {
    private final String name;
    private final String url;
    private final double height;
    private final double width;
    private final boolean success;
    private final boolean partialSuccess;
    private final boolean fail;
    public CustomGif(String name, String url, double height, double width, boolean success, boolean partialSuccess, boolean fail) {
        this.name = name;
        this.url = url;
        this.height = height;
        this.width = width;
        this.success = success;
        this.partialSuccess = partialSuccess;
        this.fail = fail;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isPartialSuccess() {
        return partialSuccess;
    }

    public boolean isFail() {
        return fail;
    }
}
