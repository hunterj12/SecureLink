package hunter.com.securelink;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Messaging extends AppCompatActivity {
    RecyclerViewAdapter adapter;
    ArrayList<String> msgArr;

    private EditText inputText;

    private int port = 0;
    private String serverIP = "";
    private int startupCounter = 0;
    private int hostclient;
    private String message;
    int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        inputText = findViewById(R.id.edittext_chatbox);

        msgArr = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.reyclerview_message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, msgArr);
        recyclerView.setAdapter(adapter);


        Thread optionsThread = new Thread(new optionsThread());
        optionsThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void addToList(String msg) {
        msgArr.add(msg);
        adapter.notifyItemInserted(num);
        num++;
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
            addToList("Enter 0 to be host, enter 1 to be client.");

            while(startupCounter == 0);

            addToList("Enter server port");

            while(startupCounter == 1);

            if(hostclient == 0) {
                Thread serverThread = new Thread(new serverThread());
                serverThread.start();
            }

            else {
                addToList("Enter server IP");

                while(startupCounter == 2);

                Thread clientThread = new Thread(new clientThread());
                clientThread.start();
            }
        }
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
                        addToList(inText);

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

                addToList("Server successfully closed");
            }
            catch (IOException e) {
                addToList("Server failed to close");
                e.printStackTrace();
            }
        }
    }

    class serverInput implements Runnable {
        public void run() {
            while (shutdownServer == false) {
                if(!message.equals("")) {
                    serverOut.println(message);
                    //serverOut.println("");

                    if(message.equals("end")) {
                        shutdownServer = true;
                        addToList("Shutting down server");
                    }

                    message = "";
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
                addToList("Successfully connected");

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                Thread clientInput = new Thread(new clientInput());
                clientInput.start();

                String receivedText;
                while (shutdownClient == false) {
                    try {
                        receivedText = in.readLine();

                        if (!receivedText.equals("")) {
                            addToList(receivedText);

                            if (receivedText.equals("end")) {
                                shutdownClient = true;
                                addToList("Server has disconnected");
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
                addToList("Error in client thread");
                shutdownClient = true;
                e.printStackTrace();
            }

            try {
                out.close();
                in.close();
                clientSocket.close();

                addToList("Client successfully closed");
            }
            catch (IOException e) {
                addToList("Client failed to close");
            }
        }
    }

    class clientInput implements Runnable {
        public void run() {
            while (shutdownClient == false) {
                if(!message.equals("")) {
                    out.println(message);
                    //out.println("");

                    if(message.equals("end")) {
                        shutdownClient = true;
                        addToList("Shutting down client");
                    }

                    message = "";
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
