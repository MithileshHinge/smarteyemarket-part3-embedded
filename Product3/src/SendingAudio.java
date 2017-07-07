import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;


public class SendingAudio extends Thread{
	
	AudioInputStream audioInputStream;
	static AudioInputStream ais;
	static AudioFormat format;
	
	static int audioport = 6671;
	static int sampleRate = 44100;                //44100;

	static int port = 6670;
	static ServerSocket serverSocket,ss;
	static Socket socket;
	static OutputStream out;
	static InputStream in;
	static DataLine.Info dataLineInfo;
	static SourceDataLine sourceDataLine;
	static DatagramSocket dataSocket;
	static boolean getout = false;
	
	public void run(){
		System.out.println(String.format("Receiving Audio started"));
		try {
			serverSocket = new ServerSocket(port);
            dataSocket = new DatagramSocket(audioport);
            dataSocket.setSoTimeout(100);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while (true) {
			try {
				System.out.println("................next iteration");
				serverSocket.setSoTimeout(0);
				socket = serverSocket.accept();
				System.out.println(String.format("....................................................connection sapadla"));
				out = socket.getOutputStream();
				in = socket.getInputStream();
				int p=in.read();
				
				if(p==1)
				{
					p = 0;
					System.out.println(String.format(".................p=1 received"));
					out.write(2);
				}
				else{
					continue;
				}
	            byte[] receiveData = new byte[4096];   ///1280
	            // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)

	            format = new AudioFormat(sampleRate, 16, 1, true, false);dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
	            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
	            sourceDataLine.open(format);
	            sourceDataLine.start();
	            FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
	            volumeControl.setValue(6f);
	            System.out.println(String.format("here"));
	            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
	            ByteArrayInputStream baiss = new ByteArrayInputStream(receivePacket.getData());
	            while (true) {   
			            System.out.println(String.format("...........................................................here"));
			            try{
		            	out.write(1);
		                out.flush();
			            dataSocket.receive(receivePacket);
			            System.out.println(String.format(".....here....................................................."));
		                ais = new AudioInputStream(baiss, format, receivePacket.getLength());
		                toSpeaker(receivePacket.getData());
		                System.out.println(String.format("..............................................here......"));
		                
			            }catch (SocketTimeoutException s) {
			                System.out.println("Socket timed out!");
			            } 
			            catch (IOException e){
			            	System.out.println("............Audio sending closed");
			            	break;
			            }
	            }
		       
	            }catch (SocketTimeoutException s) {
	                System.out.println(".......Socket timed out!");
	                
	             } 	 
			     catch (IOException e) {
					System.out.println(String.format("connection_prob2"));					
					e.printStackTrace();
			     } catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			 
			    sourceDataLine.drain();
	            sourceDataLine.close();
	    }
	}
	
	public static void toSpeaker(byte soundbytes[]) {
	    try {
	    	System.out.println(String.format("sending to speaker1"));
	        System.out.println("format? :" + sourceDataLine.getFormat());
	        sourceDataLine.write(soundbytes, 0, soundbytes.length);

	    } catch (Exception e) {
	        System.out.println("Not working in speakers...");
	        e.printStackTrace();
	    }
	}

}
