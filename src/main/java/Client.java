import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Collections;
import java.util.function.Consumer;



public class Client extends Thread{

	
	Socket socketClient;
	Data data;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	private Consumer<Serializable> callback;
	
	Client(Consumer<Serializable> call){
		data = new Data();
		callback = call;
	}
	
	public void run() {
		
		try {
			socketClient = new Socket("127.0.0.1",5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) { // send error code 503 if server is down and close the socket
			data.message = "Server is down";
			data.sysMessage = 503;
			callback.accept(data);
			try {
				socketClient.close();
			} catch (Exception t) {	}
		}

		while(true) {
			 
			try {
				if ( socketClient.isConnected()) {
					data = (Data) in.readObject(); // get data from server
					callback.accept(data); // pass it on to GUI
				}else {// send error code 503 if server is down and close the socket
						data.message = "Server is down";
						data.sysMessage = 503;
						callback.accept(data);
						socketClient.close();
						break;
				}
			}
			catch(Exception e) {// send error code 503 if server is down and close the socket
				data.message = "Server is down";
				data.sysMessage = 503;
				callback.accept(data);
				try {
					socketClient.close();
				} catch (Exception ignored) {}
				break;
			}
		}
	
    }
	
	public void send(Data data) {
		try {
			Collections.sort(data.recipientId); // sort the recipients
			out.writeObject(data); // send to server
			out.reset();
		} catch (IOException e) {// send error code 503 if server is down and close the socket
			data.message = "Server is down";
			data.sysMessage = 503;
			callback.accept(data);
			try {
				socketClient.close();
			} catch (Exception t) {	}
		}
	}


}
