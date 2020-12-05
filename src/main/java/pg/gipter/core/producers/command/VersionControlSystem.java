package pg.gipter.core.producers.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public enum VersionControlSystem {
    GIT(".git"),
    MERCURIAL(".hg"),
    SVN(".svn"),
    NA("");

    private final String dirName;

    VersionControlSystem(String dirName) {
        this.dirName = dirName;
    }

    public String command() {
        return dirName.substring(1);
    }

    String dirName() {
        return dirName;
    }

    public static VersionControlSystem valueFrom(Path path) {
        final IllegalArgumentException iae = new IllegalArgumentException("Can not determine version control system.");
        if (path == null) {
            throw iae;
        }
        try {
            if (Files.list(path) != null) {
                final Map<String, VersionControlSystem> vcsStringMap = EnumSet.allOf(VersionControlSystem.class)
                        .stream()
                        .collect(toMap(k -> k.dirName, v -> v, (v1, v2) -> v1));
                return Files.list(path)
                        .filter(p -> vcsStringMap.containsKey(p.getFileName().toString()))
                        .findFirst()
                        .map(vcs -> vcsStringMap.get(vcs.toFile().getName()))
                        .orElseThrow(() -> iae);
            }
        } catch (Exception ex) {
            iae.setStackTrace(ex.getStackTrace());
            throw iae;
        }
        throw iae;
    }

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