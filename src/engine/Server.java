package engine;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import engine.things.Object;
import engine.things.Player;
import engine.things.Object.type;

public class Server {
	static InputStream inFromClient;
	public static PrintWriter[] out;
	public static BufferedReader[] in;
	static int clientNumber = 0;

	public static void main(String[] args) {
		Thread t = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					broadcast(Integer.parseInt(args[1]));
				}
			}
		};
		t.start();
		out = new PrintWriter[Integer.parseInt(args[1])];
		in = new BufferedReader[Integer.parseInt(args[1])];
		Main main = new Main();
		int port = Integer.parseInt(args[0]);

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server is listening on port " + port);

			while (clientNumber < Integer.parseInt(args[1])) {
				System.out.println("Searching...");
				Socket socket = serverSocket.accept();
				System.out.println("New client connected " + clientNumber);

				final int cN = clientNumber;
				try {
					out[cN] = new PrintWriter(socket.getOutputStream(), true);
					inFromClient = socket.getInputStream();
					in[cN] = new BufferedReader(new InputStreamReader(inFromClient));

					Player p = new Player(0, 0, cN, Main.game.capitalize(in[cN].readLine()));
					p.setHealth(100);
					p.injury = type.bruises;
					p.currentRoom = Main.game.startingRoom;
					p.currentRoom.objects.add(p);
					p.roomCache = p.currentRoom.getClone();
					p.death = (Engine e) -> {
						Terminal.describesPL(p.name
								+ " falls to the ground, his eyes staring wide open, his mouth open as if in surprise. He shudders before his body falls still, his eyes blank and unseeing.",
								p.id);
						Object obj = Engine.Consumable("dead [corpse] that belonged to " + p.name, "lying on", null, 10);
						obj.injury = Object.type.bruises;
						obj.holdable = null;
						obj.reference = p.currentRoom.floor;
						e.objectQueue.add(obj);
					};
					Main.game.protags.add(p);

					out[cN].println(cN);
					clientNumber++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("HACKING INITIATED");
			t.stop();
			main.start();
		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void broadcast(int totalPlayers) {
		try {
			DatagramSocket c = new DatagramSocket();
			c.setBroadcast(true);

			byte[] sendData = ("serverLANBroadcast" + clientNumber + "/" + totalPlayers).getBytes();

			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
						InetAddress.getByName("255.255.255.255"), 8888);
				c.send(sendPacket);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();

				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue;
				}

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}

					try {
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
						c.send(sendPacket);
					} catch (Exception e) {
					}
				}
			}
			c.close();
		} catch (Exception e) {
		}
	}
}