package pg.gipter.producer.command;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum VersionControlSystem {
    GIT, MERCURIAL, SVN;

    public static VersionControlSystem valueFor(String value) {
        String errMsg;
        try {
            return VersionControlSystem.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            String supportedVcs = EnumSet.allOf(VersionControlSystem.class)
                    .stream()
                    .map(VersionControlSystem::name)
                    .collect(Collectors.joining(", "));
            errMsg = String.format("Given value [%s] is not supported. Supported version control systems are: [%s]%n",
                    value, String.join(", ", supportedVcs));
        }
        throw new IllegalArgumentException(errMsg);
    }
}
