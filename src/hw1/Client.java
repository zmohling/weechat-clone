package hw1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	Socket serverSocket;
	String serverHostName = "localhost";
	int serverPortNumber = 4444;
	ServerListener sl;
	boolean closed = false;

	Client() {
		// 1. CONNECT TO THE SERVER
		try {
			serverSocket = new Socket(serverHostName, serverPortNumber);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 2. SPAWN A LISTENER FOR THE SERVER. THIS WILL KEEP RUNNING
		// when a message is received, an appropriate method is called
		sl = new ServerListener(this, serverSocket);
		new Thread(sl).start();

		PrintStream out = null;
		BufferedReader consoleInput = null;
		try {
			out = new PrintStream(serverSocket.getOutputStream());
			consoleInput = new BufferedReader(new InputStreamReader(System.in));
			/*
			 * // 3. SEND THREE WISHES TO SERVER out.println("wish 1:  one million bucks ");
			 * out.flush(); // force the output out.println("wish 2: uh oh! "); out.flush();
			 * // force the output out.println("wish 3: get rid of the bucks ");
			 * out.flush(); // force the output
			 */

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (out != null && consoleInput != null) {
			try {
				while (true) {
					out.println(consoleInput.readLine().trim());
					out.flush();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void handleMessage(String cmd, String s) {
		switch (cmd) {
		case "print":
			System.out.println("client side: " + s);
			break;
		case "exit":
			System.exit(-1);
			break;
		default:
			System.out.println("client side: unknown command received:" + cmd);
		}
	}

	public static void main(String[] args) {
		Client lc = new Client();
	} // end of main method

} // end of Client

class ServerListener implements Runnable {
	Client lc;
	Scanner in; // this is used to read which is a blocking call

	ServerListener(Client lc, Socket s) {
		try {
			this.lc = lc;
			in = new Scanner(new BufferedInputStream(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) { // run forever
			System.out.println("Client - waiting to read");
			String cmd = in.next();
			String s = in.nextLine();
			lc.handleMessage(cmd, s);
		}

	}
}
