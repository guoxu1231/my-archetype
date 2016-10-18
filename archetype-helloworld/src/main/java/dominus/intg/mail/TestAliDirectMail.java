package dominus.intg.mail;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StopWatch;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * 使用javamail通过smtp协议发信
 */
@ContextConfiguration(locations = "classpath:spring-container/java_mail_context.xml")
public class TestAliDirectMail extends DominusJUnit4TestBase {
    private static final String ALIDM_SMTP_HOST = "smtpdm.aliyun.com";
    private static final int ALIDM_SMTP_PORT = 25;

    String mailUser;
    String mailPassword;
    String mailTo;
    String mailTemplate;

    @Autowired
    JavaMailSender mailSender;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        mailUser = properties.getProperty("mail.user");
        mailPassword = properties.getProperty("mail.password");
        mailTo = properties.getProperty("mail.to");
        mailTemplate = IOUtils.toString(resourceLoader.
                getResource("classpath:template/html-mail-template.html").getInputStream(), "UTF-8");
        out.println(mailTemplate.substring(200));
    }

    @Test
    public void testSendMail() throws MessagingException {

        // 配置发送邮件的环境属性
        final Properties props = new Properties();
        // 表示SMTP发送邮件，需要进行身份验证
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", ALIDM_SMTP_HOST);
//        props.put("mail.smtp.port", ALIDM_SMTP_PORT);
        // 如果使用ssl，则去掉使用25端口的配置，进行如下配置,
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.port", "465");

        // 发件人的账号
        props.put("mail.user", mailUser);
        // 访问SMTP服务时需要提供的密码
        props.put("mail.password", mailPassword);

        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名、密码
                String userName = props.getProperty("mail.user");
                String password = props.getProperty("mail.password");
                return new PasswordAuthentication(userName, password);
            }
        };
        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = new InternetAddress(
                props.getProperty("mail.user"));
        message.setFrom(form);

        // 设置收件人
        InternetAddress to = new InternetAddress(mailTo);
        message.setRecipient(MimeMessage.RecipientType.TO, to);

        // 设置邮件标题
        message.setSubject("测试邮件");
        // 设置邮件的内容体
        message.setContent("测试的HTML邮件", "text/html;charset=UTF-8");
        // 发送邮件
        Transport.send(message);
    }


    /**
     * html email, link to image url rather than as inline.
     *
     * @throws MessagingException
     */
    @Test
    public void testSpringHtmlMailSend() throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        //EE: Allows for defining a character encoding for the entire message, automatically applied by all methods of this helper class.
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setText(mailTemplate, true);
        helper.setFrom(mailUser);
        helper.setTo(mailTo);
        helper.setSentDate(new Date());
        helper.setSubject("测试邮件");
        assertEquals("UTF-8", helper.getEncoding());
        //EE: javax.mail.Transport is expensive, performance bottleneck, object pool
        mailSender.send(mimeMessage);
    }

    @Test(expected = MailSendException.class)
    public void testError436() throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        //EE: Allows for defining a character encoding for the entire message, automatically applied by all methods of this helper class.
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setText(mailTemplate, true);
        //EE: mail from should conform with authentication user.
        //com.sun.mail.smtp.SMTPSendFailedException: 436 "MAIL FROM" doesn't conform with authentication
        helper.setFrom(mailUser+".cn");
        helper.setTo(mailTo);
        helper.setSentDate(new Date());
        helper.setSubject("测试邮件");
        mailSender.send(mimeMessage);
    }

    /**
     * 180 / running time (millis) = 82348, 457ms per mail
     *
     * @throws MessagingException
     */
    @Ignore
    public void testSendPerformance() throws MessagingException, InterruptedException {
        StopWatch watch = new StopWatch("JavaMail Send Porformance Test");
        watch.start();
        for (int i = 0; i < 1000; i++) {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            //EE: Allows for defining a character encoding for the entire message, automatically applied by all methods of this helper class.
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setText(mailTemplate, true);
            helper.setFrom(mailUser);
            helper.setTo(mailTo);
            helper.setSentDate(new Date());
            helper.setSubject("测试邮件");
            //EE: javax.mail.Transport is expensive, performance bottleneck, object pool
            mailSender.send(mimeMessage);
            out.printf("send %dth mail to %s successful.\n", i, mailTo);
            Thread.sleep(random.nextInt(60000));
        }
        watch.stop();
        out.println(watch);
    }


    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }
}
