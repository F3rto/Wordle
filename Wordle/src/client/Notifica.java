package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Notifica implements Runnable{
	private MulticastSocket ms;
	private ConcurrentLinkedQueue<String> notifiche;
	private boolean end=true;
	
	public Notifica(MulticastSocket ms, ConcurrentLinkedQueue<String> notifiche) {
		this.ms=ms;
		this.notifiche=notifiche;
	}

	@Override
	public void run() {
		while(end) {
			try {
				notifiche.add(receiveUDPMessage(ms));
				Thread.sleep(5000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String receiveUDPMessage(MulticastSocket socket) throws IOException {
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
		//System.out.println("[Multicast UDP message received] >> " + msg);
		return msg;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}
}
