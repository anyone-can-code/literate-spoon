package engine;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import java.util.ArrayList;
import engine.things.Object;
import engine.things.Player;
import engine.things.Object.type;

//Instructions:
//refer to other person as just "player"
//use "say" to converse

public class Server {
	static InputStream inFromClient;
	public static PrintWriter[] out = new PrintWriter[2];
	public static BufferedReader[] in = new BufferedReader[2];

	public static void main(String[] args) {
		Main main = new Main();
		int port = Integer.parseInt(args[0]);

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server is listening on port " + port);
			int clientNumber = 0;
			while (clientNumber < 2) {
				System.out.println("Searching...");
				Socket socket = serverSocket.accept();
				System.out.println("New client connected " + clientNumber);

				final int cN = clientNumber;
				Player p = new Player(0, 0, cN);
				p.setHealth(100);
				p.injury = type.bruises;
				p.currentRoom = Main.game.startingRoom;
				p.currentRoom.objects.add(p);
				p.roomCache = p.currentRoom.getClone();
				p.death = (Engine e, ArrayList<Object> objectQueue) -> {
					Terminal.describesPL("Player " + p.id
							+ " falls to the ground, his eyes staring wide open, his mouth open as if in surprise. He shudders before his body falls still, his eyes blank and unseeing.",
							p.id);
					Object obj = Engine.Consumable("dead [corpse]", "lying on", null, 10);
					obj.injury = Object.type.bruises;
					obj.holdable = null;
					obj.reference = p.currentRoom.floor;
					objectQueue.add(obj);
				};
				Main.game.protags.add(p);
				try {
					out[cN] = new PrintWriter(socket.getOutputStream(), true);
					inFromClient = socket.getInputStream();
					in[cN] = new BufferedReader(new InputStreamReader(inFromClient));
				} catch (Exception e) {
					e.printStackTrace();
				}
				out[cN].println(cN);
				clientNumber++;
			}
			System.out.println("HACKING INITIATED");
			main.start();
		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
}