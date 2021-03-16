import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BoxLayout;
import javax.swing.JFrame;

public class project {
	class Server {
		String ip;
		int count = 0;
	}

	ServerSocket srvSocket;
	final CopyOnWriteArrayList<DatagramPacket> clientList = new CopyOnWriteArrayList<DatagramPacket>();
	final CopyOnWriteArrayList<Server> countList = new CopyOnWriteArrayList<Server>();
	int sendCount = 0;

	public project() throws IOException {

		InetAddress myIp = InetAddress.getLocalHost();
		String computerName = myIp.getHostName();

		System.out.println("My computer name: " + computerName);

		DatagramSocket socket = new DatagramSocket(9998);
		DatagramPacket packet = new DatagramPacket(computerName.getBytes(), computerName.length(),
				InetAddress.getByName("255.255.255.255"), 9998);

		Thread t1 = new Thread(() -> {
			try {
				udpServer(9998, computerName, socket, packet);

			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		t1.start();

		Thread t2 = new Thread(() -> {
			try {
				
				udpReceiver(9998, computerName, socket, packet);

			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		t2.start();

		
		Thread t3 = new Thread(() -> {
			try {
				TPCclient tpcClient = new TPCclient(9999, clientList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		t3.start();
		

		Thread t4 = new Thread(() -> {
			try {
				TPCserver tpcServer = new TPCserver(9999);

			} catch (IOException e) {

				e.printStackTrace();
			}
		});
		t4.start();

}

	public void udpServer(int port, String computerName, DatagramSocket socket, DatagramPacket packet)
			throws IOException {

		socket.send(packet);
		System.out.println("Searching servers...");

		while (true) {
			try {
				Thread.sleep(10000); 
				if (sendCount == 2) {
					deleteIpCountZero();
					sendCount = 0;
					setCountListCountZero();
				}
				socket.send(packet); // broadcasting my computer name every 10s
				sendCount++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void udpReceiver(int port, String computerName, DatagramSocket socket, DatagramPacket packet)
			throws IOException {

		while (true) {
			DatagramPacket receivedPacket = new DatagramPacket(new byte[1024], 1024);
			socket.receive(receivedPacket);
			String receivedData = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
			String srcAddr = receivedPacket.getAddress().toString();

			// only the computerName is different from my computer 
			// and the IP address is not included in the clientList can be added to the list
			// !receivedData.equals(computerName) &&
			if (!checkPacketExistInList(srcAddr)) {
				synchronized (clientList) {
					clientList.add(receivedPacket);
				}

				// reply the computer name
				packet = new DatagramPacket(computerName.getBytes(), computerName.length(), receivedPacket.getAddress(),
						receivedPacket.getPort());
				socket.send(packet);
			}
			if (!checksrcAddrExistInCountList(srcAddr)) {
				synchronized (countList) {
					Server server = new Server();
					server.ip = srcAddr;
					countList.add(server);
				}
			}

		}
	}

	public void deleteIpCountZero() {
		synchronized (countList) {
			for (Server server : countList) {
				if (server.count == 0) {
					synchronized (clientList) {
						for (DatagramPacket client : clientList) {
							String ListIpAddress = client.getAddress().toString();
							if (ListIpAddress.equals(server.ip)) {
								clientList.remove(client);
							}
						}
					}
					countList.remove(server);
				}
			}
		}
	}

	public void setCountListCountZero() {
		synchronized (countList) {
			for (Server server : countList) {
				server.count = 0;
			}
		}
	}

	public boolean checksrcAddrExistInCountList(String srcAddr) {
		synchronized (countList) {
			for (Server server : countList) {
				if (server.ip.equals(srcAddr)) {
					server.count++;
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkPacketExistInList(String srcAddr) { // check packet exist in list by ip address
		synchronized (clientList) {
			for (DatagramPacket client : clientList) {
				String ListIpAddress = client.getAddress().toString();
				if (ListIpAddress.equals(srcAddr)) {
					return true;
				}

			}
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		project s = new project();
	}

}
