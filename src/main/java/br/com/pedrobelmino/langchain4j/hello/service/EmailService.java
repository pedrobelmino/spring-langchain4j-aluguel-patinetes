package br.com.pedrobelmino.langchain4j.hello.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${mailjet.api-key}")
    private String mailjetApiKey;

    @Value("${mailjet.secret-key}")
    private String mailjetSecretKey;

    @Value("${EMAIL_USER}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) throws MailjetException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        
        // Apply lowercase to the email
        to = to.toLowerCase();

        client = new MailjetClient(
                ClientOptions.builder()
                        .apiKey(mailjetApiKey)
                        .apiSecretKey(mailjetSecretKey)
                        .build());
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", fromEmail))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", to)))
                                .put(Emailv31.Message.SUBJECT, subject)
                                .put(Emailv31.Message.TEXTPART, body)
                                .put(Emailv31.Message.HTMLPART, "<h3>" + body + "</h3>")));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }
}