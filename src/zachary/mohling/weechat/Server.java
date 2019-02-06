package zachary.mohling.weechat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.util.ArrayList;

public class Server {

	public static ArrayList<ClientHandler> unconfirmedClients = new ArrayList<ClientHandler>();
	public static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

	public static void main(String[] args) throws IOException {

		ServerSocket serverSocket = null;
		int clientNum = 0;

		try {
			serverSocket = new ServerSocket(4400);
			System.out.println(serverSocket);
		} catch (IOException e) {
			System.out.println("Could not listen on port: 4400");
			System.exit(-1);
		}

		// Wait for new connections
		while (true) { // 3.
			Socket clientSocket = null;
			try {
				// BLOCK
				clientSocket = serverSocket.accept();

				// Spawn thread for client handler
				System.out.println("New connection to: client " + ++clientNum);
				
				ClientHandler client = new ClientHandler(clientSocket, clientNum);
				Thread t = new Thread(client);
				unconfirmedClients.add(client);
				
				t.start();

			} catch (IOException e) {
				System.out.println("Accept failed: 4444");
				System.exit(-1);
			}
		}
	}
}

class ClientHandler implements Runnable {
	Socket socket;
	int identifier;
	String name;

	ClientHandler(Socket s, int n) {
		this.socket = s;
		identifier = n;
	}

	public void run() {
		Scanner in;
		PrintWriter out;

		try {
			in = new Scanner(new BufferedInputStream(socket.getInputStream()));
			out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));

			joinSession(in, out);

			// Listener for client's messages
			while (!socket.isClosed()) {
				String s = in.nextLine();
				
				
				if (s.substring(0, 1).equals(":"))
				{
					String cmd = (s.length() > 1) ? s.substring(1, 2) : "";
					switch(cmd)
					{
						case "q":
							this.socket.close();
							s = "    > " + name + " has left the session! <    ";
							
							Server.clients.remove(this);
							
							System.out.println("Closed connection to: client " + identifier);
							break;
							
						default:
							System.out.println("Unknown command: cmd");
							break;
					}
				} else {
					log(s);
					s = "[" + this.name + "]: " + s;
				}
				
				// Dispatch to every other client if message is received
				for (ClientHandler c : Server.clients) {
					if (c.identifier == this.identifier)
						continue;

					c.dispatch(s);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Confirmation prompt for name
	private void joinSession(Scanner in, PrintWriter out) {
		out.println("welcome > Enter your name: ");
		out.flush();
		name = in.nextLine().trim();

		for (ClientHandler c : Server.clients) {
			if (c.identifier == this.identifier)
				continue;

			c.dispatch("    > " + name + " has joined the session! <    ");
		}
		
		Server.unconfirmedClients.remove(this);
		Server.clients.add(this);
	}

	// Server log
	private void log(String s) {
		System.out.println("[" + new Date().toString() + ", Identifier: " + identifier + ", Name: "+ name + ", Message: " + s + "]");
	}

	// Send to client
	private void send(Scanner in, PrintWriter out, String message) {
		out.println("print " + message);
		out.flush(); // force the output
	}
	
	public void dispatch(String message) {
		Scanner in;
		PrintWriter out;

		try {
			in = new Scanner(new BufferedInputStream(socket.getInputStream()));
			out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));

			this.send(in, out, message);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
