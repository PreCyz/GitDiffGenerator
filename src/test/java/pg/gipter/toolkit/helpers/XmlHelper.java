package pg.gipter.toolkit.helpers;

import java.nio.file.Paths;

public final class XmlHelper {

    private XmlHelper() { }

    public static String getFullXmlPath(String xmlFileName) {
        return Paths.get(".","src", "test", "java", "resources", "xml", xmlFileName)
                .toAbsolutePath()
                .toString();
    }

}
