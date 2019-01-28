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
	int serverPortNumber = 2000;
	ServerListener sl;
	String name = null;

	Client() {
		// Connect to the server
		try {
			serverSocket = new Socket(serverHostName, serverPortNumber);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Spawn server listener
		sl = new ServerListener(this, serverSocket);
		new Thread(sl).start();

		PrintStream out = null;
		BufferedReader consoleInput = null;
		try {
			out = new PrintStream(serverSocket.getOutputStream());
			consoleInput = new BufferedReader(new InputStreamReader(System.in));

			// Input listener
			while (true) {
				out.println(consoleInput.readLine().trim());
				out.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handleMessage(String cmd, String s) {
		switch (cmd) {
		case "welcome":
			System.out.print(s);
			break;
		case "print":
			System.out.println(s);
			break;
		default:
			System.out.println("Unknown command received:" + cmd);
		}
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Client lc = new Client();
	}
}

class ServerListener implements Runnable {
	Client lc;
	Scanner in;

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
		while (true) {
			String cmd = in.next();
			String s = in.nextLine();
			lc.handleMessage(cmd, s);
		}

	}
}
