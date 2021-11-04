package server;

public interface ISocketServerListener {

	void connectFail();

	void showProgessBarPercent(long l);

	void showDialog(String message, String type);
}
