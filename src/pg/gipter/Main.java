package pg.gipter;

import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public class Main {

    public static void main(String[] args) {
        DiffProducer diffProducer = DiffProducerFactory.getInstance(args);

        diffProducer.produceDiff();

        System.exit(0);
    }

}
