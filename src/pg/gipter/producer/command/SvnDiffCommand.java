package pg.gipter.producer.command;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

final class SvnDiffCommand extends AbstractDiffCommand {

    SvnDiffCommand(boolean codeProtected) {
        super(codeProtected);
    }

    @Override
    public List<String> commandAsList(String author, String committerEmail, String startDate, String endDate) {
        throw new NotImplementedException();
    }

    @Override
    List<String> getInitialCommand() {
        throw new NotImplementedException();
        /*LinkedList<String> initialCommand = new LinkedList<>(Arrays.asList("hg", "log"));
        if (codeProtected) {
            initialCommand.add("--style");
            initialCommand.add("changelog");
        } else {
            initialCommand.add("--patch");
        }
        return initialCommand;*/
    }

}
