package server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class AggiornaParola implements Runnable{
	private AtomicReference<String> parola;	//parola da indovinare
	private int numParole;	//numero di parole nel fileù+
	private long time;
	private String path;
	public AggiornaParola(AtomicReference<String> parola, String s, long t){
		this.parola=parola;
		this.path=s;
		this.time=t;
	}
	@Override
	public void run() {
		FileReader fr;
		try {
			fr = new FileReader(path);
			Scanner read = new Scanner(fr);
			numParole=0;
			while(read.hasNext()) {
				read.nextLine();
				numParole++;
			}
			fr.close();
			read.close();
			
			while(true) {
				try {
					FileReader fr1 = new FileReader("words.txt");
					Scanner read1 = new Scanner(fr1);
					int parolaDaEstrarre=ThreadLocalRandom.current().nextInt(numParole+1);
					String temp;
					int count=0;
					do {
						temp=read1.nextLine();
						count++;
					}while(count!=parolaDaEstrarre);
					fr1.close();
					read1.close();
					parola.set(temp);
					//System.out.println(parola);
					Thread.sleep(time); //aggiorna la parola e aspetta un giorno
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}

}
