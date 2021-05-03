package pg.gipter.ui;

import pg.gipter.utils.BundleUtils;

public class UploadResult {
    private final String configName;
    private final Boolean success;
    private final Throwable throwable;

    public UploadResult(String configName, Boolean success, Throwable throwable) {
        this.configName = configName;
        this.success = success;
        this.throwable = throwable;
    }

    public Boolean getSuccess() {
        return success;
    }

    public Throwable getCause() {
        return throwable.getCause();
    }

    public String getThrowableMsg() {
        return throwable.getMessage();
    }

    public String logMsg() {
        StringBuilder message = new StringBuilder();
        message.append(BundleUtils.getMsg("upload.result.configuration", configName));
        if (throwable != null) {
            String cause = throwable.getMessage();
            cause = cause.substring(cause.lastIndexOf(":") + 1);
            message.append(BundleUtils.getMsg("upload.result.status", BundleUtils.getMsg("upload.result.status.fail")))
                    .append(System.getProperty("line.separator"))
                    .append(BundleUtils.getMsg("upload.result.cause", cause));

        } else {
            message.append(BundleUtils.getMsg("upload.result.status", BundleUtils.getMsg("upload.result.status.success")));
        }
        return message.toString();
    }
}
