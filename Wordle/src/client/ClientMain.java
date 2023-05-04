package client;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ClientMain {

	public static void main(String[] args) {
		String ipGroup = null, server=null; int portUDP = 0, tcpPort=0;
		FileReader fr;
		try {
			fr = new FileReader("configClient.txt");
			Scanner read = new Scanner(fr);
			ipGroup=read.nextLine();
			server=read.nextLine();
			portUDP=Integer.parseInt(read.nextLine());
			tcpPort=Integer.parseInt(read.nextLine());
			fr.close();
			read.close();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Socket socket=null;
		ConcurrentLinkedQueue<String> notifiche = new ConcurrentLinkedQueue<String>();
		Thread t1 = null;
		try {
			MulticastSocket ms = new MulticastSocket(portUDP);
			InetAddress group = InetAddress.getByName(ipGroup);
			
			socket = new Socket(server,tcpPort);
			socket.setSoTimeout(15000);
			Scanner inSocket = new Scanner(socket.getInputStream());
			PrintWriter outSocket = new PrintWriter(socket.getOutputStream());
			String s;
			Scanner scanner = new Scanner(System.in);
			
			int scelta=1;
			while(scelta == 1 || scelta == 2) {
				System.out.println("1 - Registrati\n"
						+ "2 - Login\n"
						+ "3 - exit");
				scelta=scanner.nextInt();
				
				switch(scelta) {
					case 1:
						boolean flag = register(inSocket,outSocket);
						if(!flag) {
							System.out.println("Username già utilizzato o password vuota");
							break; //se non ti sei registrato con successo interrompi il programma
						}
						System.out.println("Registrazione avvenuta con successo");
						
					case 2:
						if(login(inSocket,outSocket)) { //se il login va bene allora esegui il gioco
							System.out.println("Login avvenuto con successo");
							ms.joinGroup(group);
							t1 = new Thread(new Notifica(ms, notifiche));
							t1.start();
							game(inSocket, outSocket,ms, notifiche);
							ms.leaveGroup(group);
							
						}else System.out.println("Credenziali errate o utente già loggato");
						/*
						 * se siamo a questo punto siamo usciti dal gioco dopo esserci loggati ed aver giocato, quindi dobbiamo sloggare
						 * oppure il login è andato male e dobbiamo riprovare
						 */
						break;
					default:
						t1.interrupt();
						outSocket.println("3");
						outSocket.flush();
						break;
						
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private static boolean register(Scanner in, PrintWriter out) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("--REGISTER--");
		System.out.println("Insert your username: ");
		String username = scanner.nextLine();
		System.out.println("Insert your password: ");
		String password = scanner.nextLine();
		out.println("1"); //comunico al server che voglio fare un'operazione di registrazione
		out.flush();
		out.println(username); //mando username al server
		out.flush();
		out.println(password);	//mando password al server
		out.flush();
		int codice = Integer.parseInt(in.nextLine());
		if(codice == 1) return true;	//se il server invia 1, registrazione avvenuta con successo
		else if(codice == 2) return false;	//utente già presente
		return false;
	}
	
	private static boolean login(Scanner in, PrintWriter out) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("--LOGIN--");
		System.out.println("Insert your username: ");
		String username = scanner.nextLine();
		System.out.println("Insert your password: ");
		String password = scanner.nextLine();
		out.println("2"); //comunico al server che voglio fare un'operazione di login
		out.flush();
		out.println(username); //mando username al server
		out.flush();
		out.println(password);	//mando password al server
		out.flush();
		int codice = Integer.parseInt(in.nextLine());
		if(codice == 1) return true;	//se il server invia 1, login avvenuto con successo
		else if(codice == 2) return false;	//utente inesistente o credenziali errate
		return false;
	}
	
	private static void game(Scanner in, PrintWriter out, MulticastSocket ms, ConcurrentLinkedQueue<String> n) {
		int scelta=0;
		Scanner scanner = new Scanner(System.in);
		do {
			System.out.print(menu());
			scelta=scanner.nextInt();
			if(scelta>=1 && scelta <=6) {
				out.println(scelta+"");
				out.flush();
			}
			switch(scelta) {
				case 1:
					logout();
					return;
				case 2:
					play(in,out);
					break;
				case 3:
					System.out.println(sendMeStatistics(in));
					break;
				case 4:
					share();
					break;
				case 5:
					showMeSharing(n);
					break;
			}
				
		}while(true);
	}

	private static void logout() {
		return;
	}
	
	private static void play(Scanner in, PrintWriter out) {
		int codice = Integer.parseInt(in.nextLine());
		if(codice==2) {
			System.out.println("Hai già giocato per oggi, torna domani.");
		} else if(codice==1) {
			sendWords(in,out);
		}
		return;
	}
	
	private static void sendWords(Scanner in, PrintWriter out) {
		System.out.println("--Gioco Iniziato--");
		int scelta;
		Scanner scanner = new Scanner(System.in);
		String guess="";
		while(true) {
			System.out.println("1 - Invia parola\n"
					+ "2 - exit");
			scelta=scanner.nextInt();
			out.println(scelta+"");
			out.flush();
			switch(scelta) {
				case 1:
					scanner.nextLine();
					boolean flag=true;
					while(flag){	//controllo sulla lunghezza della parola inserita dall'utente
						System.out.println("Inserisci parola di 10 caratteri");
						guess=scanner.nextLine();
						if(guess.length()!=10) {
							System.out.println("Parola non valida");
						}else {flag=false;}
					}while(flag);
					
					out.println(guess); //invio la parola al server
					out.flush();
					
					int codice = Integer.parseInt(in.nextLine());
					//System.out.println(codice);
					if(codice==1) {
						System.out.println("Parola indovinata");
						return;
					} else if(codice==3) {
						System.out.println("Parola inesistente");
					} else if(codice==2){	//la parola esistema è sbagliata
						System.out.println(in.nextLine());
					} else if(codice==4) {
						System.out.println("Numero tentativi superato! Parola non indovinata.");
						return;
					}
					break;
				default:
					return;
			}
		}
	}
	
	private static void share() {
		System.out.println("Le tue statistiche sono state condivise.");
	}
	
	private static String sendMeStatistics(Scanner in) {
		return "Statistiche personali:\n"+in.nextLine();
	}
	
	private static void showMeSharing(ConcurrentLinkedQueue<String> n) {
		System.out.println("Statistiche giocatori:\n");
		for(String s : n) {
			System.out.println(s);
		}
	}
	
	private static String menu() {
		return "1 - Logout\n"
				+ "2 - Play WORDLE\n"
				+ "3 - Le mie statistiche\n"
				+ "4 - Condividi le mie statistiche\n"
				+ "5 - Statistiche globali\n";
	} 
}
