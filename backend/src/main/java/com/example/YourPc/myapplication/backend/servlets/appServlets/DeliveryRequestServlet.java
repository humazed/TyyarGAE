package com.example.YourPc.myapplication.backend.servlets.appServlets;


import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Muhammad Saeed on 3/3/2017.
 */
public class DeliveryRequestServlet extends HttpServlet {
    static Logger Log = Logger.getLogger("com.example.guestbook.servlets.appServlets.DeliveryRequestServlet");

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            Log.info("Sending the todo list email.");
            String outString;
            outString = "<p>Sending the todo list email.</p><p><strong>Note:</strong> ";
            outString = outString.concat("the servlet must be deployed to App Engine in order to ");
            outString = outString.concat("send the email. Running the server locally writes a message ");
            outString = outString.concat("to the log file instead of sending an email message.</p>");

            resp.getWriter().println(outString);

            // Note: Ensure that the [PRIVATE_KEY_FILENAME].json has read
            // permissions set.
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setServiceAccount(getServletContext().getResourceAsStream("/WEB-INF/My Project-ba2c7da6c85e.json"))
                    .setDatabaseUrl("https://amiable-aquifer-157201.firebaseio.com/")
                    .build();

    /*FileInputStream serviceAccount = new FileInputStream("/WEB-INF/My Project-ba2c7da6c85e.json");
    FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
            .setDatabaseUrl("https://amiable-aquifer-157201.firebaseio.com/")
            .build();*/

            FirebaseApp.initializeApp(options);
            try {
                FirebaseApp.getInstance();
            } catch (Exception error) {
                Log.info("doesn't exist...");
            }

            try {
                FirebaseApp.initializeApp(options);
            } catch (Exception error) {
                Log.info("already exists...");
            }
            // As an admin, the app has access to read and write all data, regardless of Security Rules
            DatabaseReference ref = FirebaseDatabase
                    .getInstance()
                    .getReference("todoItems");

            // This fires when the servlet first runs, returning all the existing values
            // only runs once, until the servlet starts up again.
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Object document = dataSnapshot.getValue();
                    Log.info("new value: " + document);

                    String todoText = "Don't forget to...\n\n";

                    Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();

                    while (children.hasNext()) {
                        DataSnapshot childSnapshot = (DataSnapshot) children.next();
                        todoText = todoText + " * " + childSnapshot.getValue().toString() + "\n";
                    }

                    // Now send the email

                    // Note: When an application running in the development server calls the Mail
                    // service to send an email message, the message is printed to the log.
                    // The Java development server does not send the email message.

                    // You can test the email without waiting for the cron job to run by
                    // loading http://[FIREBASE_PROJECT_ID].appspot.com/send-email in your browser.

                    Properties props = new Properties();
                    Session session = Session.getDefaultInstance(props, null);
                    try {
                        Message msg = new MimeMessage(session);
                        //Make sure you substitute your project-id in the email From field
                        try {
                            msg.setFrom(new InternetAddress("reminder@amiable-aquifer-157201.appspotmail.com",
                                    "Todo Reminder"));
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                        msg.addRecipient(Message.RecipientType.TO,
                                new InternetAddress("mohammud.saeed.batekh@gmail.com", "Recipient"));
                        msg.setSubject("Things to do today");
                        msg.setText(todoText);
                        Transport.send(msg);
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        Log.warning(e.getMessage());
                    }

                    // Note: in a production application you should replace the hard-coded email address
                    // above with code that populates msg.addRecipient with the app user's email address.
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("Error: " + error);
                }
            });
            resp.getWriter().print(viewInPage("finished!!"));

        } catch (Exception e) {
            resp.getWriter().print(errorMessage(e));
        }
    }


    public String errorMessage(Exception e) {
        StackTraceElement[] stackTraceElement = e.getStackTrace();
        String sta = "";
        for (int i = 0; i < stackTraceElement.length; i++) {
            sta += stackTraceElement[i] + " ";
        }
        String msg = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>Title</title></head>\n" +
                "<body>\n" +
                "myError :" + "<br>" + "message " + e.getMessage() + "<br>" + "string " + e.toString() + "<br>" + "cause " + e.getCause() +
                "<br>" + sta +
                "</body>\n" +
                "</html>";
        return msg;
    }

    public String viewInPage(String st) {
        String msg = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>Title</title></head>\n" +
                "<body>\n" +
                " " + st +
                "</body>\n" +
                "</html>";
        return msg;
    }
}