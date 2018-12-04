package hunter.com.securelink;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Messaging extends AppCompatActivity {
    private EditText inputText;
    private TextView outputText;
    private Button sendButton;

    private int port = 0;
    private String serverIP = "";
    private Scanner input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        inputText = (EditText) findViewById(R.id.inputText);
        outputText = (TextView) findViewById(R.id.outputText);
        sendButton = (Button) findViewById(R.id.sendButton);

        input = new Scanner(System.in);

        System.out.println("Enter 0 to be host, enter 1 to be client.");
        int select = input.nextInt();
        input.nextLine();

        System.out.println("Enter port.");
        port = input.nextInt();
        input.nextLine();

        if (select == 0) {
            Thread serverThread = new Thread(new serverThread());
            serverThread.start();
        }

        else {
            System.out.println("Enter IP.");
            serverIP = input.nextLine();

            Thread clientThread = new Thread(new clientThread());
            clientThread.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // SERVER CODE
    private ServerSocket serverSocket;
    private Socket client;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private boolean shutdownServer = false;

    class serverThread implements Runnable {
        public void run() {
            try {
                serverSocket = new ServerSocket(port);

                client = serverSocket.accept();
                serverIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                serverOut = new PrintWriter(client.getOutputStream(), true);

                Thread serverInput = new Thread(new serverInput());
                serverInput.start();

                String inText;
                while (shutdownServer == false) {
                    inText = serverIn.readLine();

                    if(!inText.equals("")) {
                        System.out.println(inText);

                        if(serverIn.readLine().equals("end")) {
                            shutdownServer = true;
                        }
                    }

                    Thread.sleep(1);
                }
            }
            catch (Exception e) {
                shutdownServer = true;
            }

            try {
                shutdownServer = true;

                serverIn.close();
                serverOut.close();
                client.close();
                serverSocket.close();
                input.close();

                System.out.println("Server successfully closed");
            }
            catch (IOException e) {
                System.out.println("Server failed to close");
                e.printStackTrace();
            }
        }
    }

    class serverInput implements Runnable {
        public void run() {
            String msg;

            while (shutdownServer == false) {
                msg = input.nextLine();
                serverOut.println(msg);
                serverOut.println("");

                if(msg.equals("end")) {
                    shutdownServer = true;
                    System.out.println("Shutting down server");
                }

                try {
                    Thread.sleep(1);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    shutdownServer = true;
                }
            }
        }
    }




    // CLIENT CODE
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean shutdownClient = false;

    class clientThread implements Runnable {
        public void run() {
            try {
                clientSocket = new Socket(serverIP, port);
                System.out.println("Successfully connected");

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                Thread clientInput = new Thread(new clientInput());
                clientInput.start();

                String receivedText;
                while (shutdownClient == false) {
                    try {
                        receivedText = in.readLine();

                        if (!receivedText.equals("")) {
                            System.out.println(receivedText);

                            if (receivedText.equals("end")) {
                                shutdownClient = true;
                                System.out.println("Server has disconnected");
                            }
                        }

                        Thread.sleep(1);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        shutdownClient = true;
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Error in client thread");
                shutdownClient = true;
                e.printStackTrace();
            }

            try {
                out.close();
                in.close();
                clientSocket.close();
                input.close();

                System.out.println("Client successfully closed");
            }
            catch (IOException e) {
                System.out.println("Client failed to close");
            }
        }
    }

    class clientInput implements Runnable {
        public void run() {
            String msg;

            while (shutdownClient == false) {
                msg = input.nextLine();
                out.println(msg);
                out.println("");

                if(msg.equals("end")) {
                    shutdownClient = true;
                    System.out.println("Shutting down client");
                }

                try {
                    Thread.sleep(1);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    shutdownClient = true;
                }
            }
        }
    }
}
