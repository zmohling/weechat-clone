package zachary.mohling.weechat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {

	Socket serverSocket;
	String serverHostname = "linux.beck.ai";
	int serverPortNumber = 4400;
	ServerListener sl;
	String name = null;
	
	PrintStream out;
	BufferedReader consoleInput;
	
	int HEADER_LINES = 5;
	int CONSOLE_LINES = Integer.parseInt(System.getenv("LINES"));
	int CONSOLE_COLS = Integer.parseInt(System.getenv("COLUMNS"));

	Client() {
		
		for (int i = HEADER_LINES; i < CONSOLE_LINES - 1; i++)
			System.out.println();
		
		// Connect to the server
		try {
			serverSocket = new Socket(serverHostname, serverPortNumber);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Spawn server listener
		sl = new ServerListener(this, serverSocket);
		new Thread(sl).start();

		try {
			out = new PrintStream(serverSocket.getOutputStream());
			consoleInput = new BufferedReader(new InputStreamReader(System.in));
			
			// Input listener
			while (true) {
				String message = consoleInput.readLine().trim();

				if (name == null) {
					name = message;
					System.out.print("    > Welcome, " + name + "! <    ");
					System.out.print("\n" + ConsoleColors.CYAN_BACKGROUND + ConsoleColors.WHITE_BOLD + "> " + "\033[s" + new String(new char[CONSOLE_COLS - 2]).replace("\0", " ") + "\033[u");
					
					out.println(message);
					out.flush();

				} else {
					if (message.length() > 0) {
						out.println(message);
						out.flush();
						
						message = "[" + this.name + "]: " + message;
					} else {
						message = "Error: Cannot send empty messages.";
					}
					
					System.out.print(String.format("\033[%dA", 1)); // Move up
					System.out.print("\r\033[2K"); // Erase line content
					System.out.print(ConsoleColors.RESET + message + new String(new char[CONSOLE_COLS - message.length()]).replace("\0", " "));
					System.out.print("\n" + ConsoleColors.CYAN_BACKGROUND + ConsoleColors.WHITE_BOLD + "> " + "\033[s" + new String(new char[CONSOLE_COLS - 2]).replace("\0", " ") + "\033[u");
				}
				
				if (message.length() > 0) {

				}
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
			System.out.print("\r\033[2K"); // Erase line content
			System.out.print(ConsoleColors.RESET + s + new String(new char[CONSOLE_COLS - s.length()]).replace("\0", " "));
			System.out.print("\n" + ConsoleColors.CYAN_BACKGROUND + ConsoleColors.WHITE_BOLD + "> " + "\033[s" + new String(new char[CONSOLE_COLS - 2]).replace("\0", " ") + "\033[u");
			break;

		default:
			System.out.println("Unknown command received:" + cmd);
			break;
		}
	}
	
	public void print(String message)
	{
		System.out.print(String.format("\033[%dA", 1)); // Move up
		System.out.print("\r\033[2K"); // Erase line content
		System.out.print(ConsoleColors.RESET + message + new String(new char[CONSOLE_COLS - message.length()]).replace("\0", " "));
		System.out.print("\n" + ConsoleColors.CYAN_BACKGROUND + ConsoleColors.WHITE_BOLD + "> " + "\033[s" + new String(new char[CONSOLE_COLS - 2]).replace("\0", " ") + "\033[u");
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
			try {
				String cmd = in.next();
				String s = in.nextLine();
				lc.handleMessage(cmd, s.substring(1));
			} catch (NoSuchElementException e) {
				System.out.println(ConsoleColors.RESET + "Disconnected from server");
				System.exit(0);
				break;
			}

		}

	}
}
