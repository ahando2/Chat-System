import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;

public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	
	
	Server(Consumer<Serializable> call){
	
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread{
		
		public void run() {
			synchronized (clients) { // synchronized clients
				try (ServerSocket mysocket = new ServerSocket(5555);) { // set the socket of srevre
					System.out.println("Server is waiting for a client!");

					while (true) { // accepting clients

						ClientThread c = new ClientThread(mysocket.accept(), count); // create new client
						callback.accept("client has connected to server: " + "client #" + count);
						clients.add(c); // add the client to threads
						c.start();

						count++;

					}
				}//end of try
				catch (Exception e) {// server is in used already
					callback.accept("Server socket did not launch");
				}
			}//end of while
		}
	}
	

	class ClientThread extends Thread{
			
		
			Socket connection;
			int count;
			ObjectInputStream in;
			ObjectOutputStream out;
			Data data;

			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;
				data = new Data();
			}

			public void updateClients(Data data) { // update everyone
				synchronized (data) { // synchronize data
					for (ClientThread t : clients) {
						updateClient(data, t);
					}
				}
			}

			public void updateClient(Data data, ClientThread t) { // update client t only
				synchronized (data) { // synchronize data
					try {
						t.out.writeObject(data);
						t.out.reset();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}


			}
			
			public void run() {
					try {

						in = new ObjectInputStream(connection.getInputStream());
						out = new ObjectOutputStream(connection.getOutputStream());
						connection.setTcpNoDelay(true);

					} catch (Exception e) {
						System.out.println("Streams not open");
					}
					data.recipientId.add(0);
					data.message = "new client client #" + count;
					data.sysMessage = 1;
					updateClients(data);
					data.sysMessage = 0;
					//0 user send, 1, 3 sys send visible, 2 sys send hidden

					for (int i = 0; i < clients.size() - 1; i++) {
						ClientThread t = clients.get(i);
						data.clientsId.add(t.count);
						data.message = String.valueOf(t.count);
						data.sysMessage = 2;
						updateClient(data,this); // update others
					}

					while (true) {
						synchronized (data) { // synchronize data
							try {
								Data inData = (Data) in.readObject();

								data.dup(inData); // set data to be the new input

								// set default data message as normal chat
								data.message = "client #" + count + " said: " + inData.message;
								data.sysMessage = 0;
								if (data.recipientId.get(0) != 0) { // if recipient is not everyone
									String clientReceiver = "";
									for (int rID : inData.recipientId) {
										clientReceiver += " Client #" + rID; // add recepient list
									}
									// specify the message text to be private and recipients
									data.message = "client #" + count + " said (private:" + clientReceiver + "): " + inData.message;

									// send to only specified clients
									for (ClientThread t : clients) {
										if (data.recipientId.contains(t.count) || t.count == this.count) {
											updateClient(data, t);
										}
									}

								} else { // send to everyone
									updateClients(data);
								}
								callback.accept(data.message.replaceFirst("said","sent")); // send to server

							} catch (Exception e) { // client is down
								callback.accept("client: " + count + " closing down!");
								data.message = "Client #" + count + " has left the server!";
								data.sysMessage = 3;

								clients.remove(this);// remove
								updateClients(data);// update others
								break;
							}
						}
				}//end of run

			}
	}//end of client thread
}


	
	

	
