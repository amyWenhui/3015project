import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class TPCserver {
	ServerSocket srvSocket;
	ArrayList<Socket> list = new ArrayList<Socket>();

	public TPCserver(int port) throws IOException {
		srvSocket = new ServerSocket(port);

		while (true) {
			System.out.printf("My server is listening at port %d...\n", port);
			Socket cSocket = srvSocket.accept();

			synchronized (list) {
				list.add(cSocket);
				System.out.printf("Total %d clients are connected.\n", list.size());
			}

			Thread t = new Thread(() -> {
				try {
					serve(cSocket);
				} catch (IOException e) {
					System.err.println("connection dropped.");
				}
				synchronized (list) {
					list.remove(cSocket);
				}
			});
			t.start();
		}
	}

	private void serve(Socket clientSocket) throws IOException {
		byte[] buffer = new byte[1024];
		System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),
				clientSocket.getPort());

		try {
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			while (true) {

				long size = in.readLong();
				String receivedData = "";
				while (size > 0) {
					int len = in.read(buffer, 0, buffer.length);
					receivedData += new String(buffer, 0, len);
					size -= len;

				}

				respond(clientSocket, receivedData);

			}
		} catch (Exception e) {
			System.err.println("ERROR: Connection dropped");
		}
	}

	private void respond(Socket clientSocket, String receivedData) {

		String input = receivedData.trim();
		int spaceAt = input.trim().indexOf(" ");

		String command, argu = "";
		if (spaceAt > 0) {
			command = input.substring(0, spaceAt);
			argu = input.substring(spaceAt + 1).replaceAll("\"", "").trim();
		} else {
			command = input;
		}

		switch (command) {
		case "login":
			String dataArray[] = argu.split(" ");
			String username = dataArray[0];
			String password = dataArray[1];
			try {
				verifyPassward(clientSocket, username, password);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;

		case "ls": // 1. List all the file including some details
		case "dir":
			if (argu.length() == 0)
				ls(command, clientSocket, ".");
			else
				ls(command, clientSocket, argu);
			break;

		case "mkdir": // 2. Create a directory with a name that do not exist
		case "md":
			if (argu == "") {
				sendRespond(clientSocket, command + " You are missing the path and name of the new directory.");
			} else {
				md(command, clientSocket, argu);
			}
			break;

		case "upload": // 3. Client upload file
			upload(command, clientSocket);
			break;

		case "download": // 3. Client download file
			if (argu == "") {
				sendRespond(clientSocket, command + " You are missing the path of the file.");
			} else {
				download(command, clientSocket, argu);
			}
			break;

		case "delF": // 4. Only can delete file
			if (argu == "") {
				sendRespond(clientSocket, command + " You are missing the path of the file.");
			} else {
				delF(command, clientSocket, argu);
			}
			break;

		case "delD": // 5. Only can delete empty directory
			if (argu == "") {
				sendRespond(clientSocket, command + " You are missing the path of the directory.");
			} else {
				delD(command, clientSocket, argu);
			}
			break;

		case "forceDelD": // Can force delete directory with things inside
			if (argu == "") {
				sendRespond(clientSocket, command + " You are missing the path of the directory.");
			} else {
				forceDelD(command, clientSocket, argu);
			}
			break;

		case "rename": // 6. Change the name of the file
			if (argu == "") {
				sendRespond(clientSocket, command + " You are missing the path of the file.");
			} else {
				String dataArray1[] = argu.split(" ");
				if (dataArray1.length < 2) {
					sendRespond(clientSocket, command + " You are missing the new name of the file.");
				} else {
					String path = dataArray1[0];
					String newName = dataArray1[1];
					rename(command, clientSocket, path, newName);
				}
			}
			break;

		// 7. Only can show the detail (file's name,path,size,last modified time) of
		// files, not directory
		case "detailF":
			if (argu == "") {
				sendRespond(clientSocket, command + " You are missing the path of the file.");
			} else {
				detailF(command, clientSocket, argu);
			}
			break;

		case "moveF": // Move the file to a different path
			if (argu == "") {
				sendRespond(clientSocket, command + " You are missing the path of the file.");
			} else {
				String dataArray1[] = argu.split(" ");
				if (dataArray1.length < 2) {
					sendRespond(clientSocket, command + " You are missing the new path of the file.");
				} else {
					String dataArray2[] = argu.split(" ");
					String OriginalPath = dataArray2[0];
					String endDirection = dataArray2[1];
					moveF(command, clientSocket, OriginalPath, endDirection);
				}
			}
			break;

		case "":
			sendRespond(clientSocket, "No Please enter a commend!");
			break;

		case "exit":
			try {
				clientSocket.close();
			} catch (Exception e) {
				System.out.println("Connection dropped! ");
			}
			break;

		default:
			sendRespond(clientSocket, command + " Unknown Command!");
			break;
		}
	}

	private void verifyPassward(Socket clientSocket, String username, String password) throws Exception {
		String reply = "Invalid login!";
		Boolean check = false;

		FileInputStream inputStream = null;
		Scanner sc = null;
		List<String[]> list = new ArrayList<>();
		inputStream = new FileInputStream("userInfo");
		sc = new Scanner(inputStream);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			int s = 0;
			String[] arr = line.split(" ");
			String[] dArr = new String[arr.length];
			for (String ss : arr) {
				if (ss != null) {
					dArr[s++] = ss;
				}
			}
			list.add(dArr);
		}

		int max = 0;
		for (int i = 0; i < list.size(); i++) {
			if (max < list.get(i).length)
				max = list.get(i).length;
		}

		String[][] array = new String[list.size()][max];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < list.get(i).length; j++) {
				array[i][j] = list.get(i)[j];
			}
		}

		inputStream.close();
		sc.close();

		for (int i = 0; i < array.length; i++) {
			if (username.equals(array[i][0]) && password.equals(array[i][1])) {
				check = true;
			}
		}

		if (check) { // valid login
			reply = "Successful login!";
		}

		reply = "login " + reply;
		sendRespond(clientSocket, reply);
	}

	private void ls(String command, Socket clientSocket, String path) { // list file
		
		String reply = command;
		File obj = new File(path);
		if (!obj.exists()) {
			sendRespond(clientSocket, reply + " File Not Found!");
			return;
		}
		if (obj.isDirectory()) {
			File[] files = obj.listFiles();
			if (files.length == 0) {
				sendRespond(clientSocket, reply + " Empty!");
				return;
			}

			for (File f : files) {
				if (f.isDirectory()) {
					reply += new Date(f.lastModified()) + " " + " <DIR> " + f.getName() + "\n";
				} else {
					reply += new Date(f.lastModified()) + " " + f.length() + "B " + f.getName() + "\n";

				}
			}
			sendRespond(clientSocket, reply);

		} else {
			reply += new Date(obj.lastModified()) + " " + obj.length() + "B " + obj.getName() + "\n";
			sendRespond(clientSocket, reply);
		}
	}

	private void md(String command, Socket clientSocket, String path) { // make directory
		String reply = command;
		File obj = new File(path);

		if (obj.exists()) {
			if (obj.isDirectory())
				reply += " Directory already exists";
			else
				reply += " File already exists";

		} else {
			obj.mkdirs();
			reply += " Subdirectory is created successfully";
		}
		sendRespond(clientSocket, reply);
	}

	private void delF(String command, Socket clientSocket, String path) { // delete a file
		String reply = command;
		File obj = new File(path);

		if (!obj.exists()) {
			reply += " File Not Found!";
		} else {
			if (obj.isDirectory()) {
				reply += " To delete a directory, you should use delD command";
			} else {
				obj.delete();
				reply += " Successfully delete!";
			}
		}
		sendRespond(clientSocket, reply);
	}

	private void delD(String command, Socket clientSocket, String path) { // delete a empty directory
		String reply = command;
		File obj = new File(path);

		if (!obj.exists()) {
			reply += " File Not Found!";
		} else {
			if (obj.isDirectory()) {

				File[] files = obj.listFiles();
				if (files.length == 0) {
					obj.delete();
				} else {
					reply += " To delete non-empty directory, you should use forceDelD command";
				}
				reply += " Successfully delete!";
			} else {
				reply += " To delete a file, you should use delF command";
			}
		}
		sendRespond(clientSocket, reply);
	}

	private void forceDelD(String command, Socket clientSocket, String path){ // Force delete a directory although it
							
	   
		String reply = command;
		File obj = new File(path);

		if (!obj.exists()) {
			reply += " File Not Found!";
		} else {
			if (obj.isDirectory()) {

				File[] files = obj.listFiles();
				
				if (files.length == 0) {
					obj.delete();
				} else {
					
					for (File f : obj.listFiles()) {
						deleteFile(f); // delete the file in directory
					}
					obj.delete(); // delete the empty directory
				}
				reply += " Successfully delete!";
			} else {
				reply += "To delete a file, you should use delF command";
			}
		}

		sendRespond(clientSocket, reply);
	}
	
	private static void deleteFile(File file){
        if (file.isFile()){
            file.delete();
        }else{
            String[] childFilePath = file.list();
            for (String path:childFilePath){
                File childFile= new File(file.getAbsoluteFile()+"/"+path);
                deleteFile(childFile);
            }
            file.delete();
        }
    }
	
	
	private void rename(String command, Socket clientSocket, String path, String newName) { // rename
		String reply = command;
		File obj = new File(path);
		String oldName = obj.getName();
		File newFile;
		if (obj.getParent() == null) {
			newFile = new File(newName);
		} else {
			newFile = new File(obj.getParent() + File.separator + newName);
		}
		if (!obj.exists()) {
			reply += " File Not Found!";
		} else {
			if (newName.equals(oldName)) {
				reply += " The new name equals to old name";
			} else if (newFile.exists()) {
				reply += " The new name of file already exists";
			} else {
				if (obj.renameTo(newFile)) {
					reply += " Successfully rename!";
				} else {
					reply += " Rename failed!";
				}
			}
		}
		sendRespond(clientSocket, reply);
	}

	// show file's name,path,size,last modified time
	private void detailF(String command, Socket clientSocket, String path) {

		String reply = command;
		File obj = new File(path);

		if (!obj.exists()) {
			reply += " File Not Found!";
		} else if (obj.isDirectory()) {
			reply += " It is a directory! You only can read the detail of the file.";
		} else {
			reply += " Name: " + obj.getName() + "\n" + "Path: " + obj.getAbsolutePath() + "\n"
					+ "Last modified time: " + new Date(obj.lastModified()) + "\n" + "Size: " + obj.length() + "B";
		}
		sendRespond(clientSocket, reply);
	}

	// move the file to another directory
	// Can't move to directory that not exist
	private void moveF(String command, Socket clientSocket, String path, String direction) {

		String reply = command;
		File startFile = new File(path);

		File endDirection = new File(direction);
		if (!endDirection.exists()) {// if no such directory, create
			reply += " Can't move to directory that not exist!\n";
			
		}

		File endFile = new File(endDirection + File.separator + startFile.getName());

		try {
			if (startFile.renameTo(endFile)) {
				reply += " File moved successfully! " + "\n Source path: " + startFile.getAbsolutePath()
						+ "\n Target path: " + endFile.getAbsolutePath();
			} else {
				reply += " File moved failed! " + "\n Source path: " + startFile.getAbsolutePath() + "\n Target path: "
						+ endFile.getAbsolutePath();
			}
		} catch (Exception e) {
			reply += " Error!";
		}

		sendRespond(clientSocket, reply);
	}

	private void upload(String command, Socket clientSocket) {
		byte[] buffer = new byte[1024];

		String reply = command + " ";

		try {
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());

			int nameLen = in.readInt();
			in.read(buffer, 0, nameLen);
			String name = new String(buffer, 0, nameLen);

			long size1 = in.readLong();
			long size = size1;

			File file = new File(name);
			FileOutputStream out = new FileOutputStream(file);

			while (size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
				size -= len;
			}

			reply += name + " (size: " + size1 + "B) is successfully uploaded.";
			sendRespond(clientSocket, reply);

			out.close();
		} catch (IOException e) {
			reply += "Unable to download file.";
		}
		sendRespond(clientSocket, reply);
	}

	private void download(String command, Socket clientSocket, String filename) {

		String reply = command + " ";
		try {
			File file = new File(filename);

			if (!file.exists()) {
				reply += "File " + filename + " doesn't exist.";
				sendRespond(clientSocket, reply);

			} else if (file.isDirectory()) {
				reply += filename + " is a directory which can't be download.";
				sendRespond(clientSocket, reply);

			} else {
				sendRespond(clientSocket, "downloadReady");

				Thread.sleep(500);

				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

				// file name
				out.writeInt(file.getName().length());
				out.write(file.getName().getBytes());

				// file size
				long size = file.length();
				out.writeLong(size);

				FileInputStream in = new FileInputStream(file);

				byte[] buffer = new byte[1024];
				while (size > 0) {
					int len = in.read(buffer, 0, buffer.length);
					out.write(buffer, 0, len);
					size -= len;
				}
				in.close();
			}
		} catch (Exception e) {
			// System.out.println("Unable to send file");
		}

	}

	private void sendRespond(Socket clientSocket, String reply) {
		try {
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			byte[] data = reply.getBytes();
			out.writeLong(reply.length());
			out.write(reply.getBytes(), 0, reply.length());
		} catch (Exception e) {
			System.err.println("ERROR: Fail to send your reply.");
		}
	}
}