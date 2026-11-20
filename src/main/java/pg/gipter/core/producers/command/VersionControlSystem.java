package pg.gipter.core.producers.command;

import pg.gipter.core.ArgName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        final IllegalArgumentException iae = new IllegalArgumentException(
                String.format("Can not determine version control system for%n[%s].", path.toString())
        );
        try (Stream<Path> list = Files.list(path)){
            final Map<String, VersionControlSystem> vcsStringMap = EnumSet.allOf(VersionControlSystem.class)
                    .stream()
                    .collect(toMap(k -> k.dirName, v -> v, (v1, v2) -> v1));
            return list
                    .filter(p -> vcsStringMap.containsKey(p.getFileName().toString()))
                    .findFirst()
                    .map(vcs -> vcsStringMap.get(vcs.toFile().getName()))
                    .orElseThrow(() -> iae);
        } catch (Exception ex) {
            iae.setStackTrace(ex.getStackTrace());
            throw iae;
        }
    }

    public static Map<String, VersionControlSystem> inspectProjectPaths(Iterable<String> paths) {
        Map<String, VersionControlSystem> repositories = new LinkedHashMap<>();
        for (String path : paths) {
            if (path.isBlank() || ArgName.projectPath.defaultValue().equals(path)) {
                continue;
            }
            try {
                repositories.put(path, valueFrom(Path.of(path)));
            } catch (IllegalArgumentException ex) {
                repositories.put(path, NA);
            }
        }
        return repositories;
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
