import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class TPCclient extends JFrame {
	boolean loginValid = false;
    JLabel label;
    JTextArea textArea;
    JFileChooser fileChooser;
    FileInputStream fileInStream;
    JFrame frame = new JFrame("File List");
    Container cp = frame.getContentPane();
    String commend = " ";
	public TPCclient(int tcpPort, CopyOnWriteArrayList<DatagramPacket> clientList) throws IOException {
		tpcClient(tcpPort, clientList);
			
	}
	
	private void tpcClient(int tcpPort, CopyOnWriteArrayList<DatagramPacket> clientList) {

		boolean welcome = true;
		while (welcome) {

			System.out.println("----------------------------------------");
			System.out.println("Please input the option number:");
			System.out.println("1. List out all the avaliable servers.\n" + "2. Find a server to connect and login.\n"
					+ "3. Exit\n");
			Scanner scanner2 = new Scanner(System.in);
			int option = scanner2.nextInt();

			if (option == 1) {
				synchronized (clientList) {
					System.out.printf("Total %d server(s) in the list:\n", clientList.size());
					printList(clientList);
				}

			} else if (option == 2) {
				if (clientList.isEmpty()) {
					System.out.println("No server can be chosen.");
				} else {
					chooseServerConnect(tcpPort, clientList);
					welcome = false;
				}
			} else if (option == 3) {
				System.out.println("bye!");
				System.exit(0);

			} else {
				System.out.println("Invalid input!");
			}
		}
	}

	public void printList(CopyOnWriteArrayList<DatagramPacket> cList) {
		int i = 1;
		for (DatagramPacket client : cList) {
			String computerName = new String(client.getData(), 0, client.getLength());
			System.out.println(i + ". " + computerName + " (" + client.getAddress().toString() + ")");
			i++;
		}
	}

//	public void chooseServerConnect(int tcpPort, CopyOnWriteArrayList<DatagramPacket> clientList) {
//
//		if (!clientList.isEmpty()) {
//
//			this.setSize(new Dimension(320, 240));
//			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			Container container = this.getContentPane();
//			String[] data = new String[clientList.size()];
//			int i = 0;
//			for (DatagramPacket client : clientList) {
//				String computerName = new String(client.getData(), 0, client.getLength());
//				data[i] = (i + 1) + ". " + computerName + " (" + client.getAddress().toString() + ")";
//				i++;
//			}
//
//			JList<String> listServer = new JList<String>(data);
//			JScrollPane sp = new JScrollPane(listServer);
//			container.add(sp, BorderLayout.CENTER);
//			listServer.addMouseListener(new MouseAdapter() {
//				public void mouseClicked(MouseEvent e) {
//					if (e.getClickCount() == 2) {
//						SwingUtilities.invokeLater(() -> {
//							try {
//								DatagramPacket chosenPacket = clientList.get(listServer.getSelectedIndex());
//								String computerName = new String(chosenPacket.getData(), 0, chosenPacket.getLength());
//								String ipAddress = chosenPacket.getAddress().toString();
//								ipAddress = ipAddress.substring(1, ipAddress.length());
//								System.out.println("Chosen server: " + computerName + " with IP address " + ipAddress);
//								String s = null;
//								int p = 0;
//								try {
//									s = ipAddress;
//									p = tcpPort;
//								} catch (IndexOutOfBoundsException | NumberFormatException e1) {									System.err.println("Usage: java chosenServerConnect ipaddress portNum");
//									System.exit(-1);
//								}
//
//								try {
//
//									tpcClient(s, p);
//								} catch (IOException e2) {
//									// TODO Auto-generated catch block
//									e2.printStackTrace();
//								}
//							} catch (Exception ex) {
//
//							}
//						});
//					}
//				}
//			});
//
//			this.setVisible(true);
//		}
//
//	}

	public void chooseServerConnect(int tcpPort, CopyOnWriteArrayList<DatagramPacket> clientList){
		Scanner scanner = new Scanner(System.in);
		boolean inputServer = true;

		while (inputServer == true) {

			System.out.printf("Total %d server(s) in the list.\n", clientList.size());
			printList(clientList);
			System.out.println();
			System.out.println("Please enter the number of the server that you want to connect.");
			int serverNum = scanner.nextInt();
			if (serverNum > clientList.size()) {
				System.out.println("No such server!");
			} else {
				DatagramPacket chosenPacket;
				synchronized (clientList) {
					chosenPacket = clientList.get((serverNum - 1));
				}
				String computerName = new String(chosenPacket.getData(), 0, chosenPacket.getLength());
				String ipAddress = chosenPacket.getAddress().toString();
				ipAddress = ipAddress.substring(1, ipAddress.length());
				System.out.println("Chosen server: " + computerName + " with IP address " + ipAddress);

				String s = null;
				int p = 0;
				try {
					s = ipAddress;
					p = tcpPort;
				} catch (IndexOutOfBoundsException | NumberFormatException e) {
					System.err.println("Usage: java chosenServerConnect ipaddress portNum");
					System.exit(-1);
				}

				try {
					tpcClient(s, p);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	public void tpcClient(String server, int tcpPort) throws IOException, InterruptedException {
		Socket socket = new Socket(server, tcpPort);

		Thread t = new Thread(() -> {
			byte[] buffer = new byte[1024];
			try {
				DataInputStream in = new DataInputStream(socket.getInputStream());
				while (true) {

					long size = in.readLong();
					String receivedData = "";
					while (size > 0) {
						int len = in.read(buffer, 0, buffer.length);
						receivedData += new String(buffer, 0, len);
						size -= len;
					}

					respondLogin(socket, receivedData);
					String data = receivedData.trim();
					String dataArray[] = data.split(" ");
					String commend = dataArray[0];
					if (commend.equals("downloadReady")) {
						download(socket);
					} else {
						System.out.println(receivedData.substring(commend.length() + 1, receivedData.length()));
					}
					if (loginValid == true)
						System.out.println("Please enter the commend:");

				}
			} catch (IOException ex) {
				System.err.println("Connection dropped!");
				System.exit(-1);
			}
		});
		t.start();

		

		while (!loginValid) {
			
			login(socket);
			 
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		view(socket);
		
		Scanner scanner = new Scanner(System.in);
		String commend = "";
		
		while (true) {
			commend = scanner.nextLine();
			String data = commend.trim();
			String dataArray[] = data.split(" ");
			if (dataArray[0].equals("upload")) {
				if (dataArray.length < 2) {
					System.out.println("You are missing the path of the file.");
					System.out.println("Please enter the commend:");
				} else if(dataArray.length > 2){
					System.out.println("Cannot upload more than one file.");
					System.out.println("Please enter the commend:");
				}else {
					String filename = dataArray[1];
					File file = new File(dataArray[1]);
					if (!file.exists()) {
						System.out.println("File " + filename + " doesn't exist.");
						System.out.println("Please enter the commend:");
					} else if (file.isDirectory()) {
						System.out.println(filename + " is a directory which can't be uploaded.");
						System.out.println("Please enter the commend:");
					} else {
						sendRequest(socket, dataArray[0]);
						upload(socket, filename, file);
					}
				}
			} else {
				sendRequest(socket, commend);
			}
		}
		
	}

	private void upload(Socket socket, String filename, File file) {
		try {
			Thread.sleep(500);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// file name
			out.writeInt(file.getName().length());
			out.write(file.getName().getBytes());

			// file
			long size = file.length();
			out.writeLong(size);

			FileInputStream in = new FileInputStream(file);
			System.out.println(file.getName() + " (size: " + size + "B) is uploading");

			byte[] buffer = new byte[1024];
			while (size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
				size -= len;
			}
			in.close();
			return;

		} catch (Exception e) {
			System.out.println("Fail to upload the file");
		}
	}

	private void download(Socket socket) {
		byte[] buffer = new byte[1024];

		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());

			int nameLen = in.readInt();
			in.read(buffer, 0, nameLen);
			String name = new String(buffer, 0, nameLen);

			long size = in.readLong();

			File file = new File(name);
			FileOutputStream out = new FileOutputStream(file);

			System.out.print("Downloading file " + name);
			while (size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
				size -= len;
				System.out.print(".");
			}

			System.out.println("\nFile " + name + " download completed");
			out.close();
			return;

		} catch (IOException e) {
			System.out.println("\nFail to download the file.");
		}
	}

	public void login(Socket socket) throws InterruptedException {
		JFrame frame = new JFrame("Login Example");
		frame.setSize(350, 200);
		JPanel panel = new JPanel();
		frame.add(panel);
		panel.setLayout(null);

		JLabel userLabel = new JLabel("User:");
		userLabel.setBounds(10, 20, 80, 25);
		panel.add(userLabel);

		JTextField userText = new JTextField(20);
		userText.setBounds(100, 20, 165, 25);
		panel.add(userText);

		JLabel passwordLabel = new JLabel("Password:");
		passwordLabel.setBounds(10, 50, 80, 25);
		panel.add(passwordLabel);

		JPasswordField passwordText = new JPasswordField(20);
		passwordText.setBounds(100, 50, 165, 25);
		panel.add(passwordText);

		JButton loginButton = new JButton("login");
		loginButton.setBounds(10, 80, 80, 25);
		panel.add(loginButton);

		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					sendRequest(socket, "login " + userText.getText() + " " + passwordText.getText());
					frame.dispose();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Operation error", "", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
	}

	public void respondLogin(Socket socket, String receivedData) {
		String data = receivedData.trim();
		String dataArray[] = data.split(" ");
		String commend = dataArray[0];

		if (commend.equals("login")) {
			if (dataArray[1].equals("Successful")) {
				loginValid = true;
			}
		}
	}

	private void sendRequest(Socket socket, String request) {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			byte[] data = request.getBytes();
			out.writeLong(request.length());
			out.write(request.getBytes(), 0, request.length());
		} catch (Exception e) {
			System.out.println("Fail to send your request.");
		}
	}
	
	
	private void  view(Socket socket){
		String[] args = new String[0];
	    frame.setForeground(Color.black);
	    frame.setBackground(Color.lightGray);
	    JMenuBar menuBar1=new JMenuBar();  
        frame.setJMenuBar(menuBar1);
       
        JMenu menu1=new JMenu("delete");
        JMenu menu2=new JMenu("copy file");
        JMenu menu3=new JMenu("rename");
        JMenu menu4=new JMenu("delD");
        JMenu menu5=new JMenu("move");
        JMenu menu6=new JMenu("exit");
        
        menuBar1.add(menu1);
        menuBar1.add(menu2);
        menuBar1.add(menu3);
        menuBar1.add(menu4);
        menuBar1.add(menu5);
        menuBar1.add(menu6);
		
        
        JMenuItem item1 = new JMenuItem("file");
        JMenuItem item2 = new JMenuItem("directory");
        menu1.add(item1);
        menu1.addSeparator();
        menu1.add(item2);
      
        item1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				commend ="delF ";
			}
		});
        
        item2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				commend ="delD ";
			}
		});
        
       
        File dir = new File(".");
	    setLayout(new BorderLayout());
		JTree tree = new JTree(addNodes(null, dir));
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e ) {
				JTree t = (JTree) e.getSource();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) t.getLastSelectedPathComponent();
				String name = node.toString();
				String data = commend + name;
				System.out.println(data);
				String dataArray[] = data.split(" ");
				if (dataArray[0].equals("upload")) {
					if (dataArray.length < 2) {
						System.out.println("You are missing the path of the file.");
						System.out.println("Please enter the commend:");
					} else if(dataArray.length > 2){
						System.out.println("Cannot upload more than one file.");
						System.out.println("Please enter the commend:");
					}else {
						String filename = dataArray[1];
						File file = new File(dataArray[1]);
						if (!file.exists()) {
							System.out.println("File " + filename + " doesn't exist.");
							System.out.println("Please enter the commend:");
						} else if (file.isDirectory()) {
							System.out.println(filename + " is a directory which can't be uploaded.");
							System.out.println("Please enter the commend:");
						} else {
							sendRequest(socket, dataArray[0]);
							upload(socket, filename, file);
						}
					}
				} else {
					sendRequest(socket, data);
				}
				
			}
		});
		
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		cp.add(BorderLayout.CENTER, scrollpane);
	    frame.pack();
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
	  }
	
	DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
		String curPath = dir.getPath();
		DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(curPath);
		if (curTop != null) { // should only be null at root
			curTop.add(curDir);
		}
		Vector ol = new Vector();
		String[] tmp = dir.list();
		for (int i = 0; i < tmp.length; i++)
			ol.addElement(tmp[i]);
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		Vector files = new Vector();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = (String) ol.elementAt(i);
			String newPath;
			if (curPath.equals("."))
				newPath = thisObject;
			else
				newPath = curPath + File.separator + thisObject;
			if ((f = new File(newPath)).isDirectory())
				addNodes(curDir, f);
			else
				files.addElement(thisObject);
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++)
			curDir.add(new DefaultMutableTreeNode(files.elementAt(fnum)));
		return curDir;
	}

	public Dimension getMinimumSize() {
		return new Dimension(600, 700);
	}

	public Dimension getPreferredSize() {
		return new Dimension(600, 700);
	}


}