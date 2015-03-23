/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.model.Message;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Thread;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.gdata.util.ServiceException;
import java.io.BufferedReader;


import java.io.ByteArrayOutputStream;
import java.io.FileReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.mskcc.cbio.oncokb.config.GoogleAuth;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author zhangh2
 */
@Controller
public class SendEmailController {
    @RequestMapping(value="/sendEmail", method = POST)
    public @ResponseBody Boolean getEmailContent(
            @RequestParam(value="subject", required=false) String subject,
            @RequestParam(value="content", required=false) String body,
            @RequestParam(value="sendTo", required=false) String sendTo) throws IOException, MessagingException, GeneralSecurityException, ServiceException {
        
        if(subject != null && body != null) {
            String from = "jackson.zhang.828@gmail.com";
            String pass = "gmail_privatespace";
            String[] to = new String[1]; // list of recipient email addresses

            if(sendTo != null) {
                to[0] = sendTo;
            }else{
                to[0] = "oncokb.curation@gmail.com";
            }
//            MimeMessage email =  createEmail(to[0], from, subject, body);
//            sendMessage(GoogleAuth.getGmailService(), "me", email);
            sendFromGMail(from, pass, to, subject, body);
            return true;
        }else {
            return false;
        }
    }
    
    private static Boolean sendFromGMail(String from, String pass, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            return true;
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
        
        return false;
    }
    
    /**
    * Send an email from the user's mailbox to its recipient.
    *
    * @param service Authorized Gmail API instance.
    * @param userId User's email address. The special value "me"
    * can be used to indicate the authenticated user.
    * @param email Email to be sent.
    * @throws MessagingException
    * @throws IOException
    */
//    private static void sendMessage(Gmail service, String userId, MimeMessage email)
//        throws MessagingException, IOException {
//      Message message = createMessageWithEmail(email);
//      message = service.users().messages().send(userId, message).execute();
//
//      System.out.println("Message id: " + message.getId());
//      System.out.println(message.toPrettyString());
//    }

    /**
     * Create a Message from an email
     *
     * @param email Email to be set to raw of message
     * @return Message containing base64url encoded email.
     * @throws IOException
     * @throws MessagingException
     */
//    private static Message createMessageWithEmail(MimeMessage email)
//        throws MessagingException, IOException {
//      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//      email.writeTo(bytes);
//      String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
//      Message message = new Message();
//      message.setRaw(encodedEmail);
//      return message;
//    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to Email address of the receiver.
     * @param from Email address of the sender, the mailbox account.
     * @param subject Subject of the email.
     * @param bodyText Body text of the email.
     * @return MimeMessage to be used to send email.
     * @throws MessagingException
     */
    private static MimeMessage createEmail(String to, String from, String subject,
        String bodyText) throws MessagingException {
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);

      MimeMessage email = new MimeMessage(session);
      InternetAddress tAddress = new InternetAddress(to);
      InternetAddress fAddress = new InternetAddress(from);

      email.setFrom(new InternetAddress(from));
      email.addRecipient(javax.mail.Message.RecipientType.TO,
                         new InternetAddress(to));
      email.setSubject(subject);
      email.setText(bodyText);
      return email;
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to Email address of the receiver.
     * @param from Email address of the sender, the mailbox account.
     * @param subject Subject of the email.
     * @param bodyText Body text of the email.
     * @param fileDir Path to the directory containing attachment.
     * @param filename Name of file to be attached.
     * @return MimeMessage to be used to send email.
     * @throws MessagingException
     */
    private static MimeMessage createEmailWithAttachment(String to, String from, String subject,
        String bodyText, String fileDir, String filename) throws MessagingException, IOException {
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);

      MimeMessage email = new MimeMessage(session);
      InternetAddress tAddress = new InternetAddress(to);
      InternetAddress fAddress = new InternetAddress(from);

      email.setFrom(fAddress);
      email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
      email.setSubject(subject);

      MimeBodyPart mimeBodyPart = new MimeBodyPart();
      mimeBodyPart.setContent(bodyText, "text/plain");
      mimeBodyPart.setHeader("Content-Type", "text/plain; charset=\"UTF-8\"");

      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(mimeBodyPart);

      mimeBodyPart = new MimeBodyPart();
      DataSource source = new FileDataSource(fileDir + filename);

      mimeBodyPart.setDataHandler(new DataHandler(source));
      mimeBodyPart.setFileName(filename);
      String contentType = Files.probeContentType(FileSystems.getDefault()
          .getPath(fileDir, filename));
      mimeBodyPart.setHeader("Content-Type", contentType + "; name=\"" + filename + "\"");
      mimeBodyPart.setHeader("Content-Transfer-Encoding", "base64");

      multipart.addBodyPart(mimeBodyPart);

      email.setContent(multipart);

      return email;
    }
}
