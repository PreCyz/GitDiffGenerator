package pg.gipter.producer.command;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum UploadType {
    SIMPLE, PROTECTED, STATEMENT, DOCUMENTS, TOOLKIT_DOCUMENTS;

    public static UploadType valueFor(String value) {
        String errMsg;
        try {
            return UploadType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            String supportedValues = EnumSet.allOf(UploadType.class)
                    .stream()
                    .map(UploadType::name)
                    .collect(Collectors.joining(", "));
            errMsg = String.format("Given value [%s] is not supported. Supported values are: [%s]%n",
                    value, String.join(", ", supportedValues));
        }
        throw new IllegalArgumentException(errMsg);
    }
}
