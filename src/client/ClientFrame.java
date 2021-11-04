package client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import data.DataFile;

public class ClientFrame extends JFrame implements ActionListener, ISocketListener {
	JTextField ipInput, portInput, searchInput;
	JButton connectButton, disconnectButton, searchButton, downLoadFile, uploadFileButton;
	JProgressBar jb;
	JList<String> list;
	ClientSocketThread clientSocketThread = null;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					ClientFrame frame = new ClientFrame();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientFrame() {
		// Connect Sever Form
		JLabel ipLabel = new JLabel("IP: ");
		ipInput = new JTextField("127.0.0.1");
		ipLabel.setBounds(100, 100, 150, 25);
		ipInput.setBounds(300, 100, 200, 25);
		this.add(ipLabel);
		this.add(ipInput);
		JLabel portLabel = new JLabel("PORT: ");
		portInput = new JTextField("10");
		portLabel.setBounds(100, 150, 150, 25);
		portInput.setBounds(300, 150, 200, 25);
		this.add(portLabel);
		this.add(portInput);

		connectButton = new JButton("Connect");
		disconnectButton = new JButton("Disconnect");
		connectButton.setBounds(125, 200, 150, 25);
		disconnectButton.setBounds(325, 200, 150, 25);
		this.add(disconnectButton);
		this.add(connectButton);

		// Search Form
		JLabel searchLabel = new JLabel("Search: ");
		searchInput = new JTextField();
		searchButton = new JButton("Search");
		searchLabel.setBounds(700, 100, 75, 25);
		searchInput.setBounds(900, 100, 200, 25);
		searchButton.setBounds(825, 200, 150, 25);
		this.add(searchButton);
		this.add(searchInput);
		this.add(searchLabel);

		// Result List
		list = new JList<>();
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setBounds(200, 400, 800, 350);
		this.add(listScrollPane);

		// JB
		downLoadFile = new JButton("Download File");
		downLoadFile.setBounds(700, 250, 150, 25);
		this.add(downLoadFile);

		uploadFileButton = new JButton("Upload File");
		uploadFileButton.setBounds(900, 250, 150, 25);
		this.add(uploadFileButton);
		jb = new JProgressBar(0, 100);
		jb.setBounds(700, 300, 100, 25);
		jb.setValue(48);
		jb.setStringPainted(true);
		this.add(jb);

		// Add event
		connectButton.addActionListener(this);
		disconnectButton.addActionListener(this);
		searchButton.addActionListener(this);
		downLoadFile.addActionListener(this);
		uploadFileButton.addActionListener(this);

		// setting Frame
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Client Frame");
		this.setLocationRelativeTo(null);
		this.setLayout(null);
		this.setBounds(0, 0, 1200, 800);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == connectButton) {
			String ip = ipInput.getText();
			String port = portInput.getText();
			System.out.println(ip + " : " + port);
			try {
				clientSocketThread = new ClientSocketThread(this);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			clientSocketThread.setSocket(ip, Integer.parseInt(port));
			clientSocketThread.start();
		} else if (e.getSource() == disconnectButton) {

			String[] data = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
			list.setListData(data);
			if (clientSocketThread == null) {
				this.showDialog("Chua bat SOCKET", "INFOR");
				return;
			}
			clientSocketThread.closeSocket();
		} else if (e.getSource() == searchButton) {
			String search = searchInput.getText();

			if (clientSocketThread != null) {
				if (search.isEmpty())
					clientSocketThread.sendString("VIEW_ALL_FILE");
				else
					clientSocketThread.sendString("SEARCH_FILE" + "--" + search);
			}
		} else if (e.getSource() == downLoadFile) {
			if (list.getSelectedIndex() != -1) {
				String str = list.getSelectedValue();
				List<String> lists = list.getSelectedValuesList();
				clientSocketThread.sendString("DOWNLOAD_FILE" + "--" + str);
			}
		} else if (e.getSource() == uploadFileButton) {
			JFileChooser fileChooser = new JFileChooser();
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				String filePath = fileToSave.getPath();
				clientSocketThread.sendFile(filePath);
			}
		}
	}

	@Override
	public void updateListFile(String[] listFile) {
		// TODO Auto-generated method stub
		list.setListData(listFile);
	}

	@Override
	public void setProgress(int n) {
		// TODO Auto-generated method stub
		jb.setValue(n);
	}

	@Override
	public void showDialog(String str, String type) {
		if (type.equals("ERROR"))
			JOptionPane.showMessageDialog(this, str, type, JOptionPane.ERROR_MESSAGE);
		else if (type.equals("INFOR"))
			JOptionPane.showMessageDialog(this, str, type, JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public String chooserFileToSave(DataFile dataFile) {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			String filePath = fileToSave.getPath();
			try {
				dataFile.saveFile(filePath);
				JOptionPane.showMessageDialog(null, "File Saved");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e);
			}
		}
		return null;
	}

}
