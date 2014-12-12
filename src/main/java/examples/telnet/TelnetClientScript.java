package examples.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.apache.commons.net.telnet.TelnetClient;

public class TelnetClientScript {

    private final InputStream in;
    private final OutputStream out;
    private final TelnetClient telnet = new TelnetClient();
    TelnetClientScript(String host, int port) throws IOException {
        telnet.connect(host, port);
        telnet.setSoTimeout(1000);
        this.in=telnet.getInputStream();
        this.out=telnet.getOutputStream();
    }
    
    void login() {
        
    }
    void write(String command) throws IOException {
        out.write(command.getBytes());
        out.write('\r');
        out.write('\n');
    }
    
    void readResponse() {
        int i;
        try {
            while((i=in.read()) != -1){
                System.out.print((char) i);
            }
        } catch (SocketTimeoutException e) {
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void main(String [] args) throws IOException{
        if(args.length < 1)
        {
            System.err.println("Usage: TelnetClientScriptExample <remote-ip> [<remote-port>]");
            System.exit(1);
        }

        String remoteip = args[0];

        int remoteport;

        if (args.length > 1)
        {
            remoteport = (new Integer(args[1])).intValue();
        }
        else
        {
            remoteport = 23;
        }

        TelnetClientScript script = new TelnetClientScript(remoteip, remoteport);
        
        script.login();
        script.write("INFORMATION");
        script.readResponse();
        System.out.println("--------------------");
        
        script.write("INFORMATION");
        script.readResponse();
        System.out.println("--------------------");
    }

}
