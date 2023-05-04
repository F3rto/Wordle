package server;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;


public class Game implements Runnable{
	private Socket socket;
	private Scanner in;
	private PrintWriter out;
	private ConcurrentLinkedQueue<Utente> utenti;
	private Gson gson;
	private AtomicReference<String> parola;
	private MulticastSocket ms;
	
	public Game(Socket s, ConcurrentLinkedQueue<Utente> utenti, AtomicReference<String> parola,MulticastSocket ms) {
		this.socket=s;
		this.utenti=utenti;
		this.parola=parola;
		this.ms=ms;
		try {
			in = new Scanner(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream());
			gson =new Gson();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {	
		Utente u;
		int scelta=0;
		while(scelta!=3) {
			scelta = Integer.parseInt(in.nextLine());
			switch(scelta) {
				case 1:
					register(in, out, utenti);
					break;
				case 2:
					u=login(in, out, utenti);
					if(u!=null) {
						//System.out.println(u.toString());
						game(in,out,utenti,u,parola);
					}
					break;
				default:
					return;
			}
		}
	}
	
	private void register(Scanner in, PrintWriter out, ConcurrentLinkedQueue<Utente> utenti) {
		String username = in.nextLine();
		String password = in.nextLine();
		Utente u = new Utente(username,password);
		if(password.equals("")) {
			out.println("2");	//se la password manda errore 2
			out.flush();
			return;
		}
		for(Utente k:utenti) {
			//System.out.println(new Gson().toJson(k));
			if(k.getUsername().equals(username)) {
				//System.out.println(username);
				out.println("2");	//se l'username è già presente manda errore 2
				out.flush();
				return;
			}
		}
		utenti.add(u);
		out.println("1");	//se non c'è username uguale allora registra e manda al client 1
		out.flush();
		return;
	}
	private Utente login(Scanner in, PrintWriter out, ConcurrentLinkedQueue<Utente> utenti) {
		String username = in.nextLine();
		String password = in.nextLine();

		for(Utente k :utenti) {
			if(k.getUsername().equals(username) && k.getPassword().equals(password) && k.getIsLogged()==false) {
				out.println("1");
				out.flush();
				k.setIsLogged(true);
				//aggiungo k al gruppo multicast
				return k;
			}
		}
		
		out.println("2");
		out.flush();
		return null;
	}
	private void game(Scanner in, PrintWriter out, ConcurrentLinkedQueue<Utente> utenti, Utente u, AtomicReference<String> parola) {
		while(true) {
			int scelta = Integer.parseInt(in.nextLine());
			switch(scelta) {
				case 1:
					//System.out.println(u+"\n\n"+utenti);
					logout(u);
					return;
				case 2:
					play(in,out,u, parola);
					break;
				case 3:
					sendMeStatistics(u);
					break;
				case 4:
					share(u,ms);
					break;
				case 5:
					showMeSharing();
					break;
			}
		}
	}
	
	private void logout(Utente u) {
		u.setIsLogged(false);
		u.setTentativi(12);		//l'utente ha fatto il logout quindi non potrà più giocare con la parola corrente
		return;
	}
	
	private void play(Scanner in, PrintWriter out, Utente u, AtomicReference<String> parola) {
		String temp=parola.get();
		if(u.getWord().equals(temp) && u.getTentativi()==12 || u.getWord().equals(temp) && u.isIndovinata()) {
			out.println("2");		//se l'utente ha già giocato con questa parola invia errore 2
			out.flush();
			return;
		}
		u.setPartite_giocate(u.getPartite_giocate()+1);
		
		u.setWord(temp);
		u.setTentativi(0);
		u.setIndovinata(false);
		out.println("1");
		out.flush();
		sendWord(in, out, u);
		u.setPerc_vinte();
		return;
	}
	
	private void sendWord(Scanner in, PrintWriter out, Utente u) {
		int scelta;
		String guess;
		while(u.getTentativi()<=12) {
			scelta=Integer.parseInt(in.nextLine());
			switch(scelta) {
				case 1:
					guess=in.nextLine();
					String parola=u.getWord();
					System.out.println(parola+"\n"+guess);
					
					if(guess.equals(parola)) {
						u.setTentativi(u.getTentativi()+1);
						u.setIndovinata(true);
						u.setPartite_vinte(u.getPartite_vinte()+1);
						u.setStreak(u.getStreak()+1);
						if(u.getStreak()>u.getMax_streak()) {
							u.setMax_streak(u.getStreak());
						}
						out.println("1"); //parola indovinata
						out.flush();
						u.setGuess_distribution();
						return;
					} else {
						try {
							Scanner read = new Scanner(new FileReader("words.txt"));
							boolean trovata=false;
							while(read.hasNext()) {
								if(read.nextLine().equals(guess)) {
									u.setTentativi(u.getTentativi()+1);
									if(u.getTentativi()==12) {
										u.setStreak(0);
										out.println("4");	//ultimo tentativo, parola non indovinata
										out.flush();
										return;
									}
									trovata=true;
									out.println("2");	//parola esistente ma sbagliata
									out.flush();
									
									String suggerimento="";
									for(int i=0;i<10;i++) {
										if(guess.charAt(i)==parola.charAt(i)) {
											suggerimento+="+";
										} else if(parola.contains(guess.charAt(i)+"")) {
											suggerimento+="?";
										}else {
											suggerimento+="X";
										}
									}
									out.println(suggerimento);
									out.flush();
									break;
								}
							}
							read.close();
							if(!trovata) {
								out.println("3");
								out.flush();
							}
							
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				default:
					return;
			}
		}
	}
	
	private void sendMeStatistics(Utente u) {
		out.println(u.statistics());
		out.flush();
	}
	
	private void sendUDPMessage(String message, InetAddress group, int port) {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
			byte[] msg = message.getBytes();
			DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
			socket.send(packet);
			socket.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void share(Utente u, MulticastSocket ms) {
		String message=u.getUsername()+"\t"+u.statistics();
		try {
			sendUDPMessage(message, InetAddress.getByName("230.0.0.1"), 4321);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void showMeSharing() {
		
	}
	
}

