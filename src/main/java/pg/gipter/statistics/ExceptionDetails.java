package pg.gipter.statistics;

import java.io.Serializable;

public class ExceptionDetails implements Serializable {

    private String errorMsg;
    private String cause;
    private String errorDate;

    public ExceptionDetails() { }

    public ExceptionDetails(String errorMsg, String cause, String errorDate) {
        this.errorMsg = errorMsg;
        this.cause = cause;
        this.errorDate = errorDate;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getErrorDate() {
        return errorDate;
    }

    public void setErrorDate(String errorDate) {
        this.errorDate = errorDate;
    }

}
