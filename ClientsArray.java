import java.net.*;
import java.io.*;
public class ClientsArray {
	private ServerThread [] clients;
	private Socket socket;
	private Messages messages;
	private int max, id = 0;
	public int numClients;

	public ClientsArray(){
		numClients = 0;
		max = 10;
		clients = new ServerThread[max];
	}

	public synchronized int getNumClients(){
		return numClients;
	}

	public synchronized void addThread(Socket s, ClientsArray c, Messages m) {
		clients[numClients] = new ServerThread(s, c, id, m);
		new Thread(clients[numClients]).start();
		numClients++;
		id++;
		System.out.println("Total Clients: " + numClients);
	}

	public synchronized void deleteThread(int id){
		for(int i = 0; i < numClients; i++){
			if(id == clients[i].getID()){
				for(int j = i; j < numClients-1; j++){
					clients[j] = clients[j+1]; 
				}
				numClients--;
				break;
			}
		}
		System.out.println("Total Clients: " + numClients);
	}

	public synchronized ServerThread getThread(int i){
		return clients[i];
	}

}