package pg.gipter.ui;

import java.util.EnumSet;

public enum UploadStatus {
    SUCCESS, PARTIAL_SUCCESS, FAIL, N_A;

    public static boolean isFailed(UploadStatus uploadStatus) {
        return EnumSet.of(FAIL, N_A).contains(uploadStatus);
    }

    public static boolean isSuccess(UploadStatus uploadStatus) {
        return EnumSet.of(SUCCESS, PARTIAL_SUCCESS).contains(uploadStatus);
    }
}
