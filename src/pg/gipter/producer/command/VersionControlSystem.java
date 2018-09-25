package pg.gipter.producer.command;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum VersionControlSystem {
    GIT, MERCURIAL;

    public static VersionControlSystem valueFor(String value) {
        try {
            return VersionControlSystem.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            String supportedVcs = EnumSet.allOf(VersionControlSystem.class)
                    .stream()
                    .map(VersionControlSystem::name)
                    .collect(Collectors.joining(", "));
            System.err.printf("Given value [%s] is not supported. Supported version control systems are: [%s]%n",
                    value, String.join(", ", supportedVcs));
            System.exit(-1);
        }
        throw new NotImplementedException();
    }
}
