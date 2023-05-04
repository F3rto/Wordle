package server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;

public class ServerMain {

	public static void main(String[] args) {
		String path = null, ipGroup = null, words=""; long time=0; int port = 0;
		int tcp=0;
		FileReader fr;
		try {
			fr = new FileReader("configServer.txt");
			Scanner read = new Scanner(fr);
			path=read.nextLine();
			ipGroup=read.nextLine();
			words=read.nextLine();
			time=Long.parseLong(read.nextLine());
			port=Integer.parseInt(read.nextLine());
			tcp=Integer.parseInt(read.nextLine());
			fr.close();
			read.close();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		ServerSocket server = null;
		Socket socket;
		//String path="account.json";
		ExecutorService pool = Executors.newCachedThreadPool();
		ConcurrentLinkedQueue<Utente> utenti = new ConcurrentLinkedQueue<Utente>();
		Gson gson= new Gson();
		PrintWriter pw = null;
		//volatile String parola;
		AtomicReference<String> parola = new AtomicReference<String>();	//parola da indovinare
		
		Thread t1 = new Thread(new AggiornaParola(parola, words, time));	//avvia un thread che aggiorna periodicamente la parola da indovinare
		t1.start();
			
		try {
			MulticastSocket ms = new MulticastSocket(port);
			InetAddress group = InetAddress.getByName(ipGroup);
			ms.joinGroup(group);
	        File file = new File(path);
			if(!file.exists()) {
	        	file.createNewFile();
	        	pw = new PrintWriter(new FileWriter(path));
	        	pw.close();
			}
	        Scanner rf = new Scanner(new FileReader(path));
        	while(rf.hasNextLine()) {		
        		utenti.add(gson.fromJson(rf.nextLine(), Utente.class));		//carico in una lista condivisa tutti gli utenti registrati al gioco
        	}
        	rf.close();
        	
        	server = new ServerSocket(tcp);
			while(true) {
				socket = server.accept();
				pool.execute(new Game(socket,utenti,parola,ms));
			}
				//pool.shutdown();
				//while(!pool.isTerminated()) {}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				
				server.close();
				t1.stop();
				pw = new PrintWriter(new FileWriter(path));
				pw.write("");
				for(Utente k:utenti) {
					//System.out.print(gson.toJson(k,Utente.class)+"\n");
					pw.append(gson.toJson(k,Utente.class)+"\n"); //aggiorno il file per mantenere permanentemente le informazionni anche col server spento
				}
				pw.close();
				System.out.println("Server closed");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}