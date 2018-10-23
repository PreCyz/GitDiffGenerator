package pg.gipter.producer.command;

import java.io.File;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum VersionControlSystem {
    GIT(".git"),
    MERCURIAL(".hg"),
    SVN(".svn");

    private String dirName;

    VersionControlSystem(String dirName) {
        this.dirName = dirName;
    }

    public static VersionControlSystem valueFrom(File file) {
        if (file == null || file.listFiles() == null) {
            throw new IllegalArgumentException("Can not determine version control system.");
        }
        for (VersionControlSystem vcs : VersionControlSystem.values()) {
            for (File dir : file.listFiles()) {
                if (dir.isDirectory() && vcs.dirName.equals(dir.getName())) {
                    return vcs;
                }
            }
        }
        throw new IllegalArgumentException("Can not determine version control system.");
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