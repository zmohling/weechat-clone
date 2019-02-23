package zachary.mohling.weechat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client
{

	Socket serverSocket;
	String serverHostname = "linux.beck.ai";
	int serverPortNumber = 4400;
	ServerListener sl;
	String name = null;

	ArrayList<String> servers = new ArrayList<String>();
	int margin = 0;

	PrintStream out;
	BufferedReader consoleInput; 

	int CONSOLE_LINES = Integer.parseInt(System.getenv("LINES"));
	int CONSOLE_COLS = Integer.parseInt(System.getenv("COLUMNS"));

	Client()
	{ 
		servers.add("example.com");
		servers.add("foo.org");
		servers.add("bar.biz");
		
		for (String s : servers) {
			if((s.length() + 3) > margin)
				margin = s.length() + 4;
		}

		for (int i = 0; i < CONSOLE_LINES; i++)
		{
			System.out.println();
		}

		println(getCurrentTimeStamp() + " | " + "\t              (C) 2019 Zachary Mohling");
		println(getCurrentTimeStamp() + " | " + "\t  _______ _____ ______    _______   _________  ________");
		println(getCurrentTimeStamp() + " | " + "\t / ___/ // / _ /_  __/___/ ___/ /  /  _/ __/ |/ /_  __/");
		println(getCurrentTimeStamp() + " | " + "\t/ /__/ _  / __ |/ / /___/ /__/ /___/ // _//    / / /   ");
		println(getCurrentTimeStamp() + " | " + "\t\\___/_//_/_/ |_/_/      \\___/____/___/___/_/|_/ /_/    ");
		println(getCurrentTimeStamp() + " | ");
		println(getCurrentTimeStamp() + " | " + "\tWelcome to the chat client. Quit with the command ':q'.");
		println(getCurrentTimeStamp() + " | " + "\t          Enter your name to start chatting.           ");
		println(getCurrentTimeStamp() + " | ");
		println(getCurrentTimeStamp() + " | ");
		println(getCurrentTimeStamp() + " | ");
		println(getCurrentTimeStamp() + " | ");
		pushToFeed("");

		// Connect to the server
		try
		{
			serverSocket = new Socket(serverHostname, serverPortNumber);
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		// Spawn server listener
		sl = new ServerListener(this, serverSocket);
		new Thread(sl).start();

		try
		{
			out = new PrintStream(serverSocket.getOutputStream());
			consoleInput = new BufferedReader(new InputStreamReader(System.in));
			
			// Input listener
			while (true)
			{
				String message = consoleInput.readLine().trim();

				if (message.length() > 0)
				{
					if (name == null)
					{
						name = message;
						
						out.println(message);
						out.flush();

						message = ConsoleColors.CYAN + name + " has joined the server." + ConsoleColors.RESET;
					} else {
						
						out.println(message);
						out.flush();
						
						message = "[" + this.name + "]: " + message; 

					}
				} else
				{
					message = ConsoleColors.WHITE_BRIGHT + "Error: Cannot send empty messages." + ConsoleColors.RESET;
				}

				pushToFeed(message);
			}

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void handleMessage(String cmd, String s)
	{
		switch (cmd)
		{

		case "welcome":
			System.out.print(s);
			break;

		case "print":
			println(" ");
			pushToFeed(s);
			break;

		case "quit":
			pushToFeed("Disconnecting from the server...");

			try
			{
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			System.exit(0);
			break;

		default:
			System.out.println("Unknown command received:" + cmd);
			break;
		}
	}

	private void println(String message)
	{
		System.out.print("\033[" + (margin + 1) + "C" + message + "\n");
	}

	private void update()
	{
		System.out.print("\033[s" + ConsoleColors.RESET + "\033[H");

		for (int i = 0; i < CONSOLE_LINES; i++)
		{
			if (i < servers.size())
			{
				if (i == 0)
				{
					System.out.print(ConsoleColors.CYAN_BACKGROUND + (i + 1) + ". " + servers.get(i) + new String(new char[(margin - 1) - (servers.get(i).length() + 3)]).replace("\0", " ") + ConsoleColors.RESET + "│");
					
				} else
				{
					System.out.print((i + 1) + ". " + servers.get(i) + new String(new char[(margin - 1) - (servers.get(i).length() + 3)]).replace("\0", " ") + "│");
				}
			} else {
				System.out.print(new String(new char[margin - 1]).replace("\0", " ") + "│");
			}
			
			

			if (i != CONSOLE_LINES - 1)
				System.out.print("\n");

		}

		System.out.print("\033[u");
		System.out.print(ConsoleColors.CYAN_BACKGROUND);
	}

	private void pushToFeed(String message)
	{
		message = getCurrentTimeStamp() + " | " + message;

		System.out.print(String.format("\033[A")); // Move up
		System.out.print(ConsoleColors.RESET + "\033[2K"); // Erase line content
		System.out.print(
				("\033[" + (CONSOLE_LINES - 1) + ";" + (margin + 2) + "f") + ConsoleColors.RESET + message 
		);
		System.out.print(
				ConsoleColors.RESET
						+ "\n" + ConsoleColors.CYAN_BACKGROUND + ConsoleColors.WHITE_BOLD + ("\033[" + (margin)
								+ "C")
						+ "> " + "\033[s" + new String(new char[CONSOLE_COLS - 2 - margin]).replace("\0", " ") + "\033[u"
		);
		update();
	}

	public String getCurrentTimeStamp()
	{
		return new SimpleDateFormat("HH:mm:ss").format(new Date());
	}

	public static void main(String[] args)
	{
		@SuppressWarnings("unused")
		Client lc = new Client();
	}
}

class ServerListener implements Runnable
{
	Client lc;
	Scanner in;

	ServerListener(Client lc, Socket s)
	{
		try
		{
			this.lc = lc;
			in = new Scanner(new BufferedInputStream(s.getInputStream()));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				String cmd = in.next();
				String s = in.nextLine();
				lc.handleMessage(cmd, s.substring(1));
			} catch (Exception e)
			{
				e.printStackTrace();
				break;
			}

		}

	}
}
