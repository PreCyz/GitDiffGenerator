package pg.gipter.core.producers.command;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum ItemType {
    SIMPLE, PROTECTED, STATEMENT, TOOLKIT_DOCS;

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

    public static boolean isCodeRelated(ItemType itemType) {
        return EnumSet.of(ItemType.SIMPLE, ItemType.PROTECTED).contains(itemType);
    }

    public static boolean isDocsRelated(ItemType itemType) {
        return ItemType.TOOLKIT_DOCS == itemType;
    }
}
