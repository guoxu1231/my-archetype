package dominus.intg.mail;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * SMTP send mail
 * IMAP fetch mail
 * POP3 obsoleted
 */
public class TestJavaMail extends DominusJUnit4TestBase {


    @Test
    public void testIMAP() throws MessagingException, IOException {

        Properties props = new Properties();
        props.put("mail.imaps.host", properties.getProperty("mail.imap.host"));
        props.put("mail.imap.port", properties.getProperty("mail.imap.port"));
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.ssl.enable", "true");

        Session session = Session.getDefaultInstance(props);
        //session.setDebug(true);
        Store store = session.getStore("imaps");
        store.connect(properties.getProperty("mail.imap.host"), properties.getProperty("mail.user"), properties.getProperty("mail.pass"));
        //create the folder object and open it
        Folder inboxFolder = store.getFolder("inbox");
        inboxFolder.open(Folder.READ_ONLY);
        // retrieve the messages from the folder in an array and print it
        Message[] messages = inboxFolder.getMessages();
//        assertTrue(messages.length > 1);
        logger.info("{} [total messages count] --- {}", new Date(), messages.length);

        for (int i = 0, n = messages.length; i < n; i++) {
            Message message = messages[i];
            if (!message.getFlags().contains(Flags.Flag.SEEN)) {
                String from = ((InternetAddress) message.getFrom()[0]).getAddress();
                logger.info("---------------------------------");
                logger.info("Email Number {}", (i + 1));
                logger.info("Subject: {}", message.getSubject());
                logger.info("From: {}", from);
                logger.info("Content Type: {}", message.getContentType());
                //only deal with mails with attachment.
                boolean finished = false;
                boolean hasAttachment = false;
                if (message.getContentType().contains("multipart")) {
                    Multipart multiPart = (Multipart) message.getContent();
                    for (int j = 0; j < multiPart.getCount(); j++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(j);
                        // this part is attachment
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            hasAttachment = true;
                            String expectedFileName = String.format("%s_%s_%s", from.substring(0, from.indexOf("@")), message.getSubject(), MimeUtility.decodeText(part.getFileName()));
                            logger.info("attachment will be saved as {}", "/tmp/" + expectedFileName);
                            part.saveFile("/tmp/" + expectedFileName);
                            FileUtils.deleteQuietly(new File("/tmp/" + expectedFileName));
                        }
                    }
                } else {
                    finished = true; //mark all text mail to finished.
                }
//                if (finished || !hasAttachment) {
//                    message.setFlag(Flags.Flag.SEEN, true);
//                    logger.info("{} is marked as read", message.getSubject());
//                }
            }
        }
        inboxFolder.close(false);
        store.close();
    }

}
