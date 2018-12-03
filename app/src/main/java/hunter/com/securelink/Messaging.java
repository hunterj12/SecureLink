package hunter.com.securelink;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Messaging extends AppCompatActivity {
    private EditText inputText;
    private TextView outputText;
    private Button sendButton;

    private String message = "";

    private int startupCounter = 0;
    private int hostclient = 0;
    private int port = 0;
    private String serverIP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        inputText = (EditText) findViewById(R.id.inputText);
        outputText = (TextView) findViewById(R.id.outputText);
        sendButton = (Button) findViewById(R.id.sendButton);

        Thread optionsThread = new Thread(new optionsThread());
        optionsThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void sendMessage(View view) {
        if(startupCounter == 0) {
            //0 = host, 1 = client
            hostclient = Integer.parseInt(inputText.getText().toString());
            startupCounter++;
        }

        else if(startupCounter == 1) {
            port = Integer.parseInt(inputText.getText().toString());
            startupCounter++;
        }

        else if(startupCounter == 2 && hostclient == 1) {
            serverIP = inputText.getText().toString();
            startupCounter++;
        }

        else {
            message = inputText.getText().toString();
        }

        inputText.getText().clear();
    }


    class optionsThread implements Runnable {
        public void run() {
            outputText.setText("Enter 0 to be host, enter 1 to be client.");

            while(startupCounter == 0);

            outputText.setText("Enter server port");

            while(startupCounter == 1);

            if(hostclient == 0) {
                Thread serverThread = new Thread(new serverThread());
                serverThread.start();

                serverIP = "127.0.0.1";
            }

            else {
                outputText.setText("Enter server IP");

                while(startupCounter == 2);
            }

            Thread clientThread = new Thread(new clientThread());
            clientThread.start();
        }
    }

    // SERVER CODE
    class serverThread implements Runnable {
        private ServerSocket serverSocket;
        private Socket client0;
        private Socket client1;
        private BufferedReader in0;
        private BufferedReader in1;
        private PrintWriter out0;
        private PrintWriter out1;

        public void run() {
            try {
                serverSocket = new ServerSocket(port);

                client0 = serverSocket.accept();
                //client1 = serverSocket.accept();

                in0 = new BufferedReader(new InputStreamReader(client0.getInputStream()));
                String receivedText0 = "";

                //in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
                //String receivedText1 = in1.readLine();

                out0 = new PrintWriter(client0.getOutputStream(), true);
                //out1 = new PrintWriter(client1.getOutputStream(), true);

                while(!"end".equals(receivedText0)) {
                    receivedText0 = in0.readLine();

                    if(receivedText0 != null) {
                        out0.println(receivedText0);
                    }
                }

                /*while((!"end".equals(receivedText0) || !"end".equals(receivedText1)) && (receivedText0 != null || receivedText1 != null)) {
                    out0.println(receivedText1);
                    out1.println(receivedText0);

                    receivedText0 = in0.readLine();
                    receivedText1 = in1.readLine();
                }*/
            }
            catch (Exception e) {
                outputText.setText("Error in server thread");
            }

            try {
                in0.close();
                //in1.close();

                out0.close();
                //out1.close();

                client0.close();
                //client1.close();

                serverSocket.close();

                outputText.setText("Server successfully closed");
            }
            catch (IOException e) {
                outputText.setText("Server failed to close");
            }
        }
    }


    // CLIENT CODE
    class clientThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public void run() {
            try {
                clientSocket = new Socket(serverIP, port);
                outputText.setText("Successfully connected");

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String receivedText = in.readLine();

                out = new PrintWriter(clientSocket.getOutputStream(), true);

                while(!"end".equals(message)) {
                    if(!"".equals(message)) {
                        out.println(message);
                        message = "";
                    }

                    receivedText = in.readLine();
                    if(receivedText != null) {
                        outputText.setText(receivedText);
                    }
                }
            }
            catch (Exception e) {
                outputText.setText("Error in client thread");
            }

            try {
                out.close();
                in.close();

                clientSocket.close();

                outputText.setText("Client successfully closed");
            }
            catch (IOException e) {
                outputText.setText("Client failed to close");
            }
        }
    }

}
