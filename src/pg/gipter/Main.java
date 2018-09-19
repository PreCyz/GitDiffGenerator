package pg.gipter;

import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.LinuxDiffProducer;
import pg.gipter.producer.WindowsDiffProducer;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public class Main {

    private static DiffProducer diffProducer;

    public static void main(String[] args) {

        String platform = System.getProperty("os.name");
        System.out.printf("Running on platform [%s].%n", platform);

        if (platform.equalsIgnoreCase("Linux")) {
            diffProducer = new LinuxDiffProducer(args);
        } else {
            diffProducer = new WindowsDiffProducer(args);
        }

        diffProducer.produceDiff();

        System.exit(0);
    }

}
