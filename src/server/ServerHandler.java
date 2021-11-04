package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import data.DataFile;
import data.SEND_TYPE;

public class ServerHandler extends Thread {

	private Socket socket;
	private boolean isStop = false;

	// Receive
	InputStream is;
	private ISocketServerListener iSocketServerListener;

	// SEND
	OutputStream os;
	SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
	String message;
	String fileName;

	FileWorker fileWorker;
	private long fileSize;
	private String fileNameReceived;
	private long currentSize;
	DataFile m_dtf;

	public ServerHandler(Socket socket, ISocketServerListener iSocketServerListener) throws Exception {
		this.socket = socket;
		os = socket.getOutputStream();
		is = socket.getInputStream();

		this.iSocketServerListener = iSocketServerListener;
		fileWorker = new FileWorker();
		SendDataThread sendDataThread = new SendDataThread();
		sendDataThread.start();

		m_dtf = new DataFile();
	}

	@Override
	public void run() {
		System.out.println("Processing: " + socket);
		// TODO Auto-generated method stub
		while (!isStop) {
			try {
				readData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				connectClientFail();
				e.printStackTrace();
				break;
			}
		}
		System.out.println("Complete processing: " + socket);

		closeSocket();

	}

	void readData() throws Exception {
		try {

			System.out.println("Recieving...");
			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();

			if (obj instanceof String) {
				readString(obj);
			} else if (obj instanceof DataFile) {
				readFile(obj);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			connectClientFail();
			closeSocket();
		}
	}

	public String readString(Object obj) {
		String str = obj.toString();
		iSocketServerListener.showDialog(str, "STRING INFOR");

		if (str.equals("STOP"))
			isStop = true;
		else if (str.equals("VIEW_ALL_FILE")) {
			String[] files = fileWorker.getAllFileName();
			String data = "ALL_FILE";
			for (String file : files) {
				data += "--" + file;
			}
			this.sendString(data);
		} else if (str.contains("SEARCH_FILE")) {
			String[] searches = str.split("--");

			String[] files = fileWorker.searchFile(searches[1]);
			String data = "ALL_FILE";
			for (String file : files) {
				data += "--" + file;
			}
			this.sendString(data);
		} else if (str.contains("DOWNLOAD_FILE")) {
			String[] array = str.split("--");
			sendFile(array[1]);
		} else if (str.contains("START_SEND_FILE")) {
			this.sendType = SEND_TYPE.START_SEND_FILE;
		} else if (str.contains("SEND_FILE")) {
			String[] fileInfor = str.split("--");
			System.out.println(fileInfor[1]);
			fileNameReceived = fileWorker.getFileName(fileInfor[1]);
			fileSize = Integer.parseInt(fileInfor[2]);
			System.out.println("File Size: " + fileSize);
			currentSize = 0;
			m_dtf.clear();
			if (fileWorker.checkFile(fileNameReceived))
				this.sendString("START_SEND_FILE");
			else
				this.sendString("ERROR--FILE Trung Ten");
		} else if (str.contains("END_FILE")) {
			m_dtf.saveFile(FileWorker.URL_FOLDER + "\\" + fileNameReceived);
		}

		return str;
	}

	void readFile(Object obj) throws Exception {
		DataFile dtf = (DataFile) obj;
		currentSize += 512;

		int percent = (int) (currentSize * 100 / fileSize);
		// System.out.println(currentSize + " : " + fileSize);
		m_dtf.appendByte(dtf.data);
		iSocketServerListener.showProgessBarPercent(percent);
	}

	class SendDataThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!isStop) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (sendType != SEND_TYPE.DO_NOT_SEND)
					sendData();
			}
		}
	}

	private void sendData() {
		// TODO Auto-generated method stub
		if (sendType == SEND_TYPE.SEND_STRING) {
			sendMessage(message);
		} else if (sendType == SEND_TYPE.SEND_FILE) {
			File source = new File(FileWorker.URL_FOLDER + "\\" + fileName);
			InputStream fin;
			try {
				fin = new FileInputStream(source);
				long lenghtOfFile = source.length();
				// Send message : fileName + size
				sendMessage("SEND_FILE" + "--" + fileName + "--" + lenghtOfFile);
				fin.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (sendType == SEND_TYPE.START_SEND_FILE) {
			File source = new File(FileWorker.URL_FOLDER + "\\" + fileName);
			InputStream fin = null;
			long lenghtOfFile = source.length();
			// Send file : file data
			byte[] buf = new byte[512];
			long total = 0;
			int len;
			try {
				fin = new FileInputStream(source);
				while ((len = fin.read(buf)) != -1) {
					total += len;
					DataFile dtf = new DataFile();
					dtf.data = buf;
					sendMessage(dtf);
					iSocketServerListener.showProgessBarPercent(total * 100 / lenghtOfFile);
					// iSocketServerListener.showDialog("File send " + total * 100 / lenghtOfFile,
					// "INFOR");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Send End File: fileName + size
			sendMessage("END_FILE--" + fileName + "--" + lenghtOfFile);

		}
		sendType = SEND_TYPE.DO_NOT_SEND;
	}

	void sendString(String str) {
		System.out.println("SENDING STRING	");
		sendType = SEND_TYPE.SEND_STRING;
		message = str;
	}

	void sendFile(String fileName) {
		System.out.println("SENDING FILE	");
		sendType = SEND_TYPE.SEND_FILE;
		this.fileName = fileName;

	}

	// void send Message
	public synchronized void sendMessage(Object obj) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			// only send text
			if (obj instanceof String) {
				String message = obj.toString();
				oos.writeObject(message);
				oos.flush();
			}
			// send attach file
			else if (obj instanceof DataFile) {
				oos.writeObject(obj);
				oos.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void connectClientFail() {
		// TODO Auto-generated method stub
		// serverHelper.connectFail();
		isStop = true;
		closeSocket();
	}

	private void closeSocket() {
		isStop = true;
		try {
			this.sendString("STOP");
			if (os != null)
				os.close();
			if (is != null)
				is.close();
			if (socket != null)
				socket.close();
			iSocketServerListener.showDialog("Closed Server Socket", "INFOR");

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			iSocketServerListener.showDialog("Connect Fail", "ERROR");
		}
	}

}
