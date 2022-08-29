package syncit.starting.vlc;

import java.io.IOException;

public class StartingVlc {
	private String fileLocation;
	private final String vlcLocation= "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe";
	private final String serverLocation = "127.0.0.1";
	private final int portFirst = 7965;
	private final int portSecond = 2189;
	
	public StartingVlc(String fileLocation) {
		System.out.println("Reached constructor");
		this.fileLocation = fileLocation;
//		startVlc();
	}
	
	public void startVlc() {
		System.out.println("Reached the start");
		String[] commandForFirst = {vlcLocation, fileLocation, "--intf", "rc", "--rc-quiet", "--rc-host", serverLocation + ":" + portFirst, "--extraintf", "qt"};
		String[] commandForSecond = {vlcLocation, fileLocation, "--intf", "rc", "--rc-quiet", "--rc-host", serverLocation + ":" + portSecond, "--extraintf", "qt", "--novideo"};
		
		//Execute the commands
		try {
			
			Process p1 = Runtime.getRuntime().exec(commandForFirst);
			(p1.getInputStream()).close();
			(p1.getErrorStream()).close();
			
			Process p2 = Runtime.getRuntime().exec(commandForSecond);
			(p2.getInputStream()).close();
			(p2.getErrorStream()).close();
			
//            InputStreamReader isr = new InputStreamReader(stderr);
//            BufferedReader br = new BufferedReader(isr);
//            br.close();
//            isr.close();
//            stderr.close();
//            String line = null;
//            System.out.println("<CMD>");
//            while ( (line = br.readLine()) != null)
//                System.out.println(line);
//            System.out.println("</CMD>");
//            int exitVal = p1.waitFor();
//            System.out.println("Process exitValue: " + exitVal);
			
			System.out.println("Commands Executed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void startSecondVlcAgain() {
		String[] commandForSecond = {vlcLocation, fileLocation, "--intf", "rc", "--rc-quiet", "--rc-host", serverLocation + ":" + portSecond, "--extraintf", "qt", "--novideo"};
		
		try {
			Process p2 = Runtime.getRuntime().exec(commandForSecond);
			(p2.getInputStream()).close();
			(p2.getErrorStream()).close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getServerLocation() {
		return serverLocation;
	}
	
	public int getPortFirst() {
		return portFirst;
	}
	
	public int getPortSecond() {
		return portSecond;
	}	
}
