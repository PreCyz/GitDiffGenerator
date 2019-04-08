package pg.gipter.producer.processor;

import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.util.List;

class ComplexDocumentFinder extends AbstractDocumentFinder {

    ComplexDocumentFinder(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public List<File> find() {
        return null;
    }
}
