package pg.gipter.core.dao.command;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.utils.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class CustomCommand {

    private VersionControlSystem vcs;
    private String command;
    private List<String> commandList;
    private boolean override;

    public CustomCommand() {
    }

    public CustomCommand(VersionControlSystem vcs) {
        this.vcs = vcs;
    }

    public CustomCommand(VersionControlSystem vcs, String command, boolean override) {
        this(vcs);
        this.command = command;
        this.override = override;
    }

    public VersionControlSystem getVcs() {
        return vcs;
    }

    public void setVcs(VersionControlSystem vcs) {
        this.vcs = vcs;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<String> commandList) {
        this.commandList = commandList;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public LinkedList<String> fullCommand(ApplicationProperties applicationProperties) {
        LinkedList<String> result = new LinkedList<>();
        if (command != null) {
            String commandStr = replacePlaceholders(command, applicationProperties);
            result.addAll(commandAsList(commandStr));
        } else if (commandList != null && !commandList.isEmpty()) {
            final Map<String, String> placeholderMap = placeholderMap(applicationProperties);
            List<String> commandReplaced = new LinkedList<>();
            for (String cmd : commandList) {
                if (cmd.contains("$") && cmd.contains(" ")) {
                    final Iterator<String> iterator = Stream.of(cmd.split(" ")).iterator();
                    StringBuilder argument = new StringBuilder();
                    do {
                        String param = iterator.next();
                        param = placeholderMap.getOrDefault(param, param);
                        argument.append(param).append(" ");
                    } while (iterator.hasNext());
                    commandReplaced.add(argument.toString().trim());
                } else if (cmd.contains("$")) {
                    String placeHolder = cmd.substring(cmd.indexOf("$"), cmd.indexOf("}") + 1);
                    cmd = cmd.replace(placeHolder, placeholderMap.getOrDefault(placeHolder, cmd));
                    cmd = placeholderMap.getOrDefault(cmd, cmd);
                    commandReplaced.add(cmd.replaceAll("\"", "'"));
                }
                else {
                    commandReplaced.add(placeholderMap.getOrDefault(cmd, cmd));
                }
            }
            result.addAll(commandReplaced);
        }
        return result;
    }

    private List<String> commandAsList(String command) {
        LinkedList<String> result = new LinkedList<>();
        if (command != null) {
            final Iterator<String> iterator = Stream.of(command.split(" ")).iterator();
            while (iterator.hasNext()) {
                final String cmd = iterator.next();
                if (cmd.contains("'")) {
                    StringBuilder argument = handleQuotedText(iterator, cmd, "'");
                    result.add(argument.toString().replaceAll("'", ""));
                } else if (cmd.contains("\"") && cmd.indexOf("\"") == cmd.lastIndexOf("\"")) {
                    StringBuilder argument = handleQuotedText(iterator, cmd, "\"");
                    result.add(argument.toString().replaceAll("\"", "'"));
                } else {
                    result.add(cmd.replaceAll("\"", "'"));
                }
            }
        }
        return result;
    }

    private StringBuilder handleQuotedText(Iterator<String> iterator, String cmd, String quote) {
        StringBuilder argument = new StringBuilder(cmd);
        String param;
        do {
            param = iterator.next();
            argument.append(" ").append(param);
        } while (!param.contains(quote) && iterator.hasNext());
        return argument;
    }

    private String replacePlaceholders(String command, ApplicationProperties applicationProperties) {
        String commandStr = command;
        for (Map.Entry<String, String> entry : escapedPlaceholderMap(applicationProperties).entrySet()) {
            commandStr = commandStr.replaceAll(entry.getKey(), entry.getValue());
        }
        return commandStr;
    }

    private Map<String, String> escapedPlaceholderMap(ApplicationProperties applicationProperties) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("\"\\$\\{" + ArgName.author + "}\"", new LinkedList<>(applicationProperties.authors()).getFirst());
        result.put("\"\\$\\{" + ArgName.gitAuthor + "}\"", applicationProperties.gitAuthor());
        result.put("\"\\$\\{" + ArgName.svnAuthor + "}\"", applicationProperties.svnAuthor());
        result.put("\"\\$\\{" + ArgName.mercurialAuthor + "}\"", applicationProperties.mercurialAuthor());
        result.put("\\$\\{" + ArgName.committerEmail + "}", applicationProperties.committerEmail());
        result.put("\\$\\{" + ArgName.author + "}", "\"" + new LinkedList<>(applicationProperties.authors()).getFirst() + "\"");
        result.put("\\$\\{" + ArgName.gitAuthor + "}", "\"" + applicationProperties.gitAuthor() + "\"");
        result.put("\\$\\{" + ArgName.svnAuthor + "}", "\"" + applicationProperties.svnAuthor() + "\"");
        result.put("\\$\\{" + ArgName.mercurialAuthor + "}", "\"" + applicationProperties.mercurialAuthor() + "\"");
        result.put("\\$\\{" + ArgName.startDate + "}", applicationProperties.startDate().format(DateTimeFormatter.ISO_DATE));
        result.put("\\$\\{" + ArgName.endDate + "}", applicationProperties.endDate().format(DateTimeFormatter.ISO_DATE));
        return result;
    }

    private Map<String, String> placeholderMap(ApplicationProperties applicationProperties) {
        Map<String, String> result = new HashMap<>();
        result.put("\"${" + ArgName.author + "}\"", new LinkedList<>(applicationProperties.authors()).getFirst());
        result.put("\"${" + ArgName.gitAuthor + "}\"", applicationProperties.gitAuthor());
        result.put("\"${" + ArgName.svnAuthor + "}\"", applicationProperties.svnAuthor());
        result.put("\"${" + ArgName.mercurialAuthor + "}\"", applicationProperties.mercurialAuthor());
        result.put("${" + ArgName.committerEmail + "}", applicationProperties.committerEmail());
        result.put("${" + ArgName.author + "}", "\"" + new LinkedList<>(applicationProperties.authors()).getFirst() + "\"");
        result.put("${" + ArgName.gitAuthor + "}", "\"" + applicationProperties.gitAuthor() + "\"");
        result.put("${" + ArgName.svnAuthor + "}", "\"" + applicationProperties.svnAuthor() + "\"");
        result.put("${" + ArgName.mercurialAuthor + "}", "\"" + applicationProperties.mercurialAuthor() + "\"");
        result.put("${" + ArgName.startDate + "}", applicationProperties.startDate().format(DateTimeFormatter.ISO_DATE));
        result.put("${" + ArgName.endDate + "}", applicationProperties.endDate().format(DateTimeFormatter.ISO_DATE));
        return result;
    }

    public boolean containsCommand(VersionControlSystem vcs) {
        if (StringUtils.notEmpty(getCommand())) {
            return getVcs() == vcs || getCommand().startsWith(vcs.command());
        } else if (getCommandList() != null && !getCommandList().isEmpty()) {
            return getVcs() == vcs || getCommandList().get(0).startsWith(vcs.command());
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomCommand that = (CustomCommand) o;
        return vcs == that.vcs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vcs);
    }
}
