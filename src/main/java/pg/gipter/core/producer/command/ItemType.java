package pg.gipter.core.producer.command;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum ItemType {
    SIMPLE, PROTECTED, STATEMENT, TOOLKIT_DOCS, SHARE_POINT_DOCS;

    public static ItemType valueFor(String value) {
        String errMsg;
        try {
            return ItemType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            String supportedValues = EnumSet.allOf(ItemType.class)
                    .stream()
                    .map(ItemType::name)
                    .collect(Collectors.joining(", "));
            errMsg = String.format("Given value [%s] is not supported. Supported values are: [%s]%n",
                    value, String.join(", ", supportedValues));
        }
        throw new IllegalArgumentException(errMsg);
    }
}
