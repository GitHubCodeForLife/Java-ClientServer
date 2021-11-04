package client;

import data.DataFile;

public interface ISocketListener {

	void updateListFile(String[] listFile);

	void setProgress(int n);

	void showDialog(String str, String type);

	String chooserFileToSave(DataFile dataFile);

}
