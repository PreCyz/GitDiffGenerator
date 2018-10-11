package pg.gipter.producer;

/**Created by Pawel Gawedzki on 20-Sep-2018.*/
public final class DiffProducerFactory {

    private DiffProducerFactory() { }

    public static DiffProducer getInstance(String[] programArguments) {
        String platform = System.getProperty("os.name");
        System.out.printf("Running on platform [%s].%n", platform);

        if ("Linux".equalsIgnoreCase(platform)) {
            return new LinuxDiffProducer(programArguments);
        } else if (platform.startsWith("Windows")) {
            return new WindowsDiffProducer(programArguments);
        } else {
            System.err.printf("Platform %s not supported yet.%n", platform);
            throw new RuntimeException("Not implemented yet!!!");
        }
    }
}
