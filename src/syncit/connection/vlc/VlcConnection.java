package syncit.connection.vlc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class VlcConnection {
	private String serverLocation;
	private int portFirst;
	private int portSecond;
	private Socket socketFirst;
	private Socket socketSecond;
	private PrintWriter outFirst;
	private PrintWriter outSecond;
	private BufferedReader inFirst;
	private BufferedReader inSecond;
	private int retryCount;
	private boolean ignoreFirst;
	private boolean ignoreSecond;
	private Text currStatus;
	private boolean syncAgainCall;
	
	private InputStream inpStrSecond;
	/*
	 * Meaning of different numbers
	 * -1  :  VLC not started
	 *  0  :  VLC is running and from both sides
	 *  1  :  Only first is running
	 *  2  :  Only second is running
	 *  3  :  None of them are running
	 * */
	private IntegerProperty vlcRunningStatus = new SimpleIntegerProperty(-1);
	
	
	public VlcConnection(String serverLocation, int portFirst, int portSecond) {
		this.serverLocation= serverLocation;
		this.portFirst = portFirst;
		this.portSecond = portSecond;
	}
	
	public void makeConnection() {
		try {
			socketFirst = new Socket(serverLocation, portFirst);
			System.out.println("first socket");
			socketSecond = new Socket(serverLocation, portSecond);
			System.out.println("second socket");
			
			outFirst = new PrintWriter(socketFirst.getOutputStream(), true);
			System.out.println("first pw");
			outSecond = new PrintWriter(socketSecond.getOutputStream(), true);
			System.out.println("sec pw");
			
			outFirst.println("seek 0");
			outSecond.println("seek 0");
			
			inFirst =  new BufferedReader(new InputStreamReader(socketFirst.getInputStream()));
			System.out.println("first bf");
//			inSecond = new BufferedReader(new InputStreamReader(socketSecond.getInputStream()));
//			System.out.println("sec bf");
			inpStrSecond = socketSecond.getInputStream();
			
			vlcRunningStatus.set(0);
			
			
			for(int i = 0; i < 4; i++) {
				System.out.println("Reading First: " + inFirst.readLine());
//				System.out.println("Reading second: " + inSecond.readLine());
			}
			
		} catch (IOException e) {
			if (retryCount < 3) {
				retryCount++;
				System.out.println("Connection refused: retrying (count: " + retryCount + ")");
				makeConnection();
			}
			
		}
	}
	
	public void startReading() {
		/*
		
		
		Thread t2 = new Thread() {
			@Override
			public void run() {
				String readOutput = "";
				try {
					while ( (readOutput = inSecond.readLine()) != null) {
						if(!ignoreSecond) {
							System.out.println(readOutput);
						} else {
							System.out.println("Ignored Second: " + readOutput);
						}
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		
		t2.start();
		
		String readOutput = "";
		try {
			while ( (readOutput = inFirst.readLine()) != null) {
				System.out.println(readOutput);						
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		Thread t1 = new Thread() {
			@Override
			public void run() {
				String readOutput = "";
				try {
					while (vlcRunningStatus.intValue() != 1 && (readOutput = inFirst.readLine()) != null) {
						System.out.println("In thread reading: " + readOutput);
						
						//Checks if the second vlc is running
//						if(inSecond.readLine() == null) {
//							System.out.println("Second not running");
//						}
//						
//						System.out.println("Checking first socket: " + socketFirst.isConnected());
//						System.out.println("Checking second socket: " + socketSecond.);
						
//						InetAddress ia1 = InetAddress.get();
//						System.out.println(ia1);
						

						if (vlcRunningStatus.intValue() == 1){
//							vlcRunningStatus.set(1);
							System.out.println("Second closed");
							break;
						} else {
							if(readOutput.endsWith("Pause")) {
								playPauseSecond();
//								currStatus.setText("Paused");
//								currStatus.setStyle("-fx-background-color: #ff8278; -fx-padding: 3px 6px; -fx-font-weight: bold; -fx-font-style: italic;");
							} else if (readOutput.endsWith("Play")) { 
								playPauseSecond();
//								currStatus.setText("Playing");
//								currStatus.setStyle("-fx-background-color: #9de18b; -fx-padding: 3px 6px; -fx-font-weight: bold; -fx-font-style: italic;");
							} else if (readOutput.contains("time")) {
								seekAll(readOutput.substring(23, readOutput.lastIndexOf('s')));
							} else if (syncAgainCall) {
								seekAll(readOutput);
								syncAgainCall = false;
							} else if (readOutput.startsWith("quit")) {
								System.out.println("Received The quit response");
								break;
							}
						}
					}
					
					if (vlcRunningStatus.intValue() != 1) {
						System.out.println("Read output is null or received quit request");
						quitSecond();
						vlcRunningStatus.set(3);
					} else {
//						inFirst.close();
					}
					
					System.out.println("Reading the output closed break;");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		t1.start();
	}
	
	public String getPlayingTitle() {
		System.out.println("checking title");
		outFirst.println("get_title");
		String readedTitle = "";
		try {
			readedTitle = inFirst.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Returned title");
		return readedTitle;
	}
	
	public IntegerProperty getVlcRunningStatus() {
		return vlcRunningStatus;
	}
	
	public void forUpdatingStatus(Text currentStatus) {
		this.currStatus = currentStatus;
	}
	
	public void playPauseFirst() {
		outFirst.println("pause");
		for(int i = 0; i < 3; i++) {
			try {
				System.out.println(inFirst.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void playPauseSecond() {
		outSecond.println("pause");
		
		try {
			int secondRead = inpStrSecond.read();
			if (secondRead == -1) {
				vlcRunningStatus.set(1);
				closeSecond();
			}
		} catch (IOException e) {
			vlcRunningStatus.set(1);
			closeSecond();
		}
	}
	
	public void playPauseAll() {
		playPauseFirst();
		playPauseFirst();
	}
	
	public void seekAll(String secondToSeek) {
		outSecond.println("seek " + secondToSeek);
		outFirst.println("seek " + secondToSeek);
		
		for(int i = 0; i < 2; i++) {
			try {
				System.out.println("To remove seek buffer" + inFirst.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			int secondRead = inpStrSecond.read();
			if (secondRead == -1) {
				vlcRunningStatus.set(1);
				closeSecond();
			}
		} catch (IOException e) {
			vlcRunningStatus.set(1);
			closeSecond();
		}
	}
	
	public void syncAgain() {
		syncAgainCall = true;
		outFirst.println("get_time");
	}
	
	public void quitFirst() {
		outFirst.println("quit");
		System.out.println("Quit First Sended");
		closeEverything();
	}
	
	public void quitSecond() {
		outSecond.println("quit");
		closeEverything();
	}
	
	public void quitAll() {
		outFirst.println("quit");
		outSecond.println("quit");
		System.out.println("Quit all");
		closeEverything();
	}
	
	public void startSecondConnectAgain() {
		outFirst.close();
		try {
			socketFirst.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			socketFirst = new Socket(serverLocation, portFirst);
			System.out.println("first socket");
			socketSecond = new Socket(serverLocation, portSecond);
			System.out.println("second socket");
			
			outFirst = new PrintWriter(socketFirst.getOutputStream(), true);
			System.out.println("first pw");
			outSecond = new PrintWriter(socketSecond.getOutputStream(), true);
			System.out.println("sec pw");
			
			inFirst =  new BufferedReader(new InputStreamReader(socketFirst.getInputStream()));
			System.out.println("first bf");		
			inpStrSecond = socketSecond.getInputStream();
			
			//Checking the play pause status of first
			outFirst.println("status");
			if(inFirst.readLine().contains("pause")) {
				outSecond.println("pause");
			}
			
			String skipFirstLines = "";
			while( !(skipFirstLines = inFirst.readLine()).equals("status: returned 0 (no error)") ) {
				System.out.println(skipFirstLines);
			}
			
			syncAgainCall = true;
			outFirst.println("get_time");
			vlcRunningStatus.set(0);
			startReading();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("second again");
	}
	
	public void closeFirst() {
		System.out.println("Closing the first");
		try {
			inFirst.close();
			outFirst.close();
			socketFirst.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeSecond() {
		System.out.println("Closing the second");
		
		try {
			inpStrSecond.close();
			outSecond.close();
			socketSecond.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void closeConnections() {
		try {
			System.out.println("Closing connections");
			socketFirst.close();
			socketSecond.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closePWriters() {
		System.out.println("Closing outs");
		outFirst.close();
		outSecond.close();
	}
	
	public void closeBReaders() {
		try {
			System.out.println("Closing BR");
			inFirst.close();
			inpStrSecond.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeEverything() {
		closeBReaders();
		closePWriters();
		closeConnections();
	}
	
}
