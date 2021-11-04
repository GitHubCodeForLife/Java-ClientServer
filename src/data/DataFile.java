package data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("serial")
public class DataFile implements Serializable {

	public byte[] data;
	public int size;
	public static int MAX_SIZE = 1024000 * 100;// 1 MB

	public void clear() {
		data = new byte[0];
		size = 0;
	}

	public DataFile() {
		data = new byte[0];
		size = 0;
	}

	public DataFile(String fileName) throws IOException {
		File file = new File(fileName);
		data = Files.readAllBytes(Paths.get(fileName));
		System.out.println(data);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return size + " : " + data.toString();
	}

	public void appendByte(byte[] array) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(data);
			outputStream.write(array);
			data = outputStream.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void saveFile(String fileToReceived) {
		// TODO Auto-generated method stub
		Path path = Paths.get(fileToReceived);
		try {
			Files.write(path, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}