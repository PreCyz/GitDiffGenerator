package pg.gipter;

import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;

import java.time.format.DateTimeFormatter;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public class Main {

    public static final DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        ApplicationProperties applicationProperties = new ApplicationProperties(args);

        DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);

        diffProducer.produceDiff();

        System.exit(0);
    }

}
