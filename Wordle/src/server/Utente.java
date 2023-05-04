package server;

import java.io.Serializable;
import java.util.ArrayList;

public class Utente implements Serializable{
	private String username;
	private String password;
	private boolean isLogged;
	private String word;
	private int tentativi;
	private boolean indovinata;
	private int partite_giocate;
	private int partite_vinte;
	private float perc_vinte;
	private int streak;
	private int max_streak;
	private ArrayList<Integer> guess_distribution;
	
	public Utente(String u, String p) {
		this.username=u;
		this.password=p;
		isLogged=false;
		setTentativi(0);
		setWord("");
		indovinata=false;
		partite_giocate=0;
		partite_vinte=0;
		setStreak(0);
		max_streak=0;
		guess_distribution=new ArrayList<Integer>();
	}
	
	public String statistics() {
		String s="Partite giocate: "+partite_giocate+"\t"
				+ "Partite vinte: "+partite_vinte+"\t"
				+ "Percentuale vittorie: "+perc_vinte+"\t"
				+ "Streak corrente: "+streak+"\t"
				+ "Streak più lungo: "+max_streak+"\t"
				+ "Guess distribustion vittorie: "+guess_distribution;
		return s;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean getIsLogged() {
		return isLogged;
	}
	public void setIsLogged(boolean isLogged) {
		this.isLogged=isLogged;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getTentativi() {
		return tentativi;
	}
	public void setTentativi(int tentativi) {
		this.tentativi = tentativi;
	}
	@Override
	public String toString() {
		return "username: "+username+"\npassword: "+password+"\nisLogged: "+isLogged+"\nword: "+word+"\ntentativi: "+tentativi;
	}
	public boolean isIndovinata() {
		return indovinata;
	}

	public void setIndovinata(boolean indovinata) {
		this.indovinata = indovinata;
	}

	public int getPartite_giocate() {
		return partite_giocate;
	}

	public void setPartite_giocate(int partite_giocate) {
		this.partite_giocate = partite_giocate;
	}

	public int getPartite_vinte() {
		return partite_vinte;
	}

	public void setPartite_vinte(int partite_vinte) {
		this.partite_vinte = partite_vinte;
	}

	public float getPerc_vinte() {
		return perc_vinte;
	}
	
	public void setPerc_vinte() {
		this.perc_vinte=partite_vinte*100/partite_giocate;
	}

	public int getStreak() {
		return streak;
	}

	public void setStreak(int streak) {
		this.streak = streak;
	}

	public int getMax_streak() {
		return max_streak;
	}

	public void setMax_streak(int max_streak) {
		this.max_streak = max_streak;
	}

	public ArrayList<Integer> getGuess_distribution() {
		return guess_distribution;
	}

	public void setGuess_distribution() {
		this.guess_distribution.add(tentativi);
	}
	
}
