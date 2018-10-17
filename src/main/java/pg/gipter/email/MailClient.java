package pg.gipter.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**Created by Pawel Gawedzki on 17-Oct-2018.*/
public class MailClient {

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    public static void main(String[] args) {
        new MailClient().sendEmailSuccessMsg("pgawedzki@gmail.com");
        System.exit(0);
    }

    private static final String FROM = "gipter@noresponse.com";
    private static final String HOST = "mail.netcompany.com";

    private Properties emailTlsProperties() {

        /*
        // Setup mail server
        properties.put("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.port", "443");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.port", "443");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
*/
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        return properties;
    }

    private Properties emailSslProperties() {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        return properties;
    }

    public void sendEmailSuccessMsg(String recipient) {
        try {
            //Session session = Session.getDefaultInstance(properties);
            Session session = newSession();
            sendMessage(session, successMessage(recipient, session));
        } catch (MessagingException mex) {
            logger.error("Error when sending success email.", mex);
        }
    }

    private void sendMessage(Session session, MimeMessage message) throws MessagingException {
        Transport transport = session.getTransport();

        transport.connect();
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        transport.close();

        //Transport.send(message);
        System.out.println("Sent message successfully....");
    }

    private Session newSession() {
        return Session.getInstance(emailSslProperties(),
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication("userName", "password");
                        }
                    });
    }

    private MimeMessage successMessage(String recipient, Session session) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject("This is the Subject Line!");
        message.setContent("<h1>Your </h1>", "text/html");
        return message;
    }

    private MimeMessage errorMessage(String recipient, Session session) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject("Error!");
        message.setContent("<h1>Your </h1>", "text/html");
        return message;
    }

    public void sendEmailErrorMsg(String recipient) {
        try {
            Session session = newSession();
            sendMessage(session, errorMessage(recipient, session));
        } catch (MessagingException mex) {
            logger.error("Error when sending error email.", mex);
        }
    }
}
