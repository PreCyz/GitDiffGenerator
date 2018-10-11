package pg.gipter;

import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.DiffUploader;

import java.time.format.DateTimeFormatter;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public class Main {

    public static final DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        ApplicationProperties applicationProperties = new ApplicationProperties(args);

        DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
        diffProducer.produceDiff();

        DiffUploader diffUploader = new DiffUploader(applicationProperties.itemPath());
        diffUploader.uploadDiff();

        System.exit(0);
    }

}
