package pg.gipter.services.keystore;

import pg.gipter.utils.StringUtils;

import java.io.*;
import java.util.*;

class CommandLineCertificateService extends AbstractCertificateService {

    public CommandLineCertificateService() {
        super();
    }

    @Override
    public void addCertificate(String certPath, String alias) throws IOException {
        final List<String> command = Arrays.asList(
                "keytool", "-import", "-alias", alias,
                "-file", "\"" + certPath + "\"",
                "-keystore", "\"" + getKeystorePath().toString() + "\"",
                "-storepass", storepass
        );
        logger.info("Full command to add certificate [{}]", command);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(getKeytoolPath().getParent().toFile());
        Process process = processBuilder.start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(String.format("%s%n", line));
            }
            logger.info(builder.toString());

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IOException(ex);
        }
    }

    @Override
    public Map<String, String> getCertificates() throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        List<String> command = Arrays.asList(
                "keytool", "-list", "-v",
                "-keystore", "\"" + getKeystorePath().toString() + "\"",
                "-storepass", storepass
        );
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(getKeytoolPath().getParent().toFile());
        Process process = processBuilder.start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(String.format("%s%n", line));
            }

            Scanner scan = new Scanner(builder.toString());
            String alias = "";
            StringBuilder certDetails = new StringBuilder();
            while (scan.hasNextLine()) {
                String nextLine = scan.nextLine();

                if (nextLine.startsWith("Alias name:")) {
                    alias = nextLine.substring(nextLine.indexOf(":") + 2);
                } else if (nextLine.contains("*******************************************") && scan.hasNextLine()) {
                    scan.nextLine();
                    result.put(alias, certDetails.toString());
                    certDetails = new StringBuilder();
                } else if (StringUtils.notEmpty(nextLine)) {
                    certDetails.append(nextLine);
                }
            }
            logger.info("Found {} certificates.", result.size());

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IOException(ex);
        }
        return result;
    }

}
