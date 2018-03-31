// The server
import java.net.*;
import java.io.*;
public class chatServer implements Runnable{

	public static final int PORT = 7777;
	private ClientsArray clients;
	private ServerSocket serverSocket;
	private Messages messages;
	private Consumer consumer;

	//initialize other classes
	public chatServer(){
		clients = new ClientsArray();
		serverSocket = null;
		messages = new Messages();
		consumer = new Consumer(clients, messages);
		
		new Thread(this).start();
	}
	  
	public void run(){ 
        try {
        	serverSocket = new ServerSocket(PORT);	
			System.out.println("Server up...");
		} 
		catch (Exception e) {
			System.err.println("Could not listen on port: 7777");
			System.exit(-1);	
		}

		while(true){
			try{
				clients.addThread(serverSocket.accept(), clients, messages);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}

	public static void main(String[] args) throws IOException {
		new chatServer();
	}
}

///////////////////////////////////////////////////////////////////////////////

// One thread per connection, this is it
class ServerThread implements Runnable {
	private Socket socket = null;
	private ClientsArray clients;
	private String username, message;
	private PrintWriter pw;
	private Messages messages;
	private int id;

   	public ServerThread(Socket s, ClientsArray c, int id, Messages m){ 
    	this.socket = s;
    	this.clients = c;
		this.id = id;
		this.messages = m;
		new Thread(this).start();
   	}

    // Handle the connection
  	public void run() {
  	 	try{
			BufferedReader br = new BufferedReader(new InputStreamReader
								(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream(), true);
			
			//Message for user name
			username = br.readLine();
			messages.addMessage("User: "+ username + " connected to server.");

			//Message for each subsequent message
			while((message = br.readLine()) != null){
				messages.addMessage(username + ": " + message);
			}
			br.close();
			pw.close();
			socket.close();
		} 	
		catch (SocketException e) {
			messages.addMessage(username + " just left the chat..");
			clients.deleteThread(id);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
   	}

	public void printString(String s){
		pw.println(s);
	}
	public int getID(){
		return id;
}
}

///////////////////////////////////////////////////////////////////////////////


class ClientsArray {
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

	public synchronized ServerThread getThread(int i){
		return clients[i];
	}

	public synchronized void addThread(Socket s, ClientsArray c, Messages m) {
		clients[numClients] = new ServerThread(s, c, id, m);
		new Thread(clients[numClients]).start();
		numClients++;
		id++;
		System.out.println("Total Clients: " + numClients);
	}

	// In this mesthod we are taking each index of the Array starting
	//from the id provided, and replacing it with the index in front of it.
	//there will be no lost value as the first to be replaced is the only value
	//that will be lost and is the one we want to delete anyway 
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
}

///////////////////////////////////////////////////////////////////////////////

class Messages{
	private String [] buffer;
	private int numMessages, max, nextIn, nextOut;

	public Messages(){
		max = 10;
		numMessages = 0;
		nextIn = 0;
		nextOut = 0;
		buffer = new String[max];
	}

	public synchronized void addMessage(String s){
		try{
			while(numMessages == max){
				wait();
			}

			buffer[nextIn] = s;
			numMessages++;
			nextIn += nextIn % max;

			notifyAll();
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
	}

	public synchronized String removeMessage(){
		try{
			while(numMessages == 0){
				wait();
			}
			String out = buffer[nextOut];
			nextOut += nextOut % max;
			numMessages--;
			notifyAll();
			return out;
		}
		catch(InterruptedException e){
			e.printStackTrace();
			return null;
		}
	}
}

///////////////////////////////////////////////////////////////////////////////

class Consumer implements Runnable{
	private ClientsArray clients;
	private Messages messages;

	public Consumer(ClientsArray c, Messages m){
		this.clients = c;
		this.messages = m;
		new Thread(this).start();
	}

	public void run(){
		while(true){
			String out = messages.removeMessage();
			for(int i = 0; i < clients.getNumClients(); i++){
				clients.getThread(i).printString(out);
			}
		}
	}

}