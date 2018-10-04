package pg.gipter.producer;

import java.util.List;

class LinuxDiffProducer extends AbstractDiffProducer {

    LinuxDiffProducer(String[] programParameters) {
        super(programParameters);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd;
    }

}
