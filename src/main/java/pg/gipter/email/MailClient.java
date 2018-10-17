package pg.gipter.email;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**Created by Pawel Gawedzki on 17-Oct-2018.*/
public class MailClient {

    public static void main(String[] args) {
        sendEmailSuccessMsg("pawg@netcompany.com");
        System.exit(0);
    }

    public static void sendEmailSuccessMsg(String recipient) {

        // Sender's email ID needs to be mentioned
        String from = "gipter@netcompany.com";

        // Assuming you are sending email from localhost
        String host = "mail.netcompany.com";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.port", "443");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        /*properties.put("mail.smtp.socketFactory.port", "443");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");*/

        //Authenticator auth = new SMTPAuthenticator();

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);


        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Send the actual HTML message, as big as you like
            message.setContent("<h1>Your </h1>", "text/html");

            // Send message
            Transport transport = session.getTransport();

            transport.connect();
            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            transport.close();

            //Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public static void sendEmailErrorMsg(String recipient) {

        // Sender's email ID needs to be mentioned
        String from = "web@gmail.com";

        // Assuming you are sending email from localhost
        String host = "localhost";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Send the actual HTML message, as big as you like
            message.setContent("<h1>Your </h1>", "text/html");

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
