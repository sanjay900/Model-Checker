package mc.webserver.webobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.Main;
import mc.compiler.ast.ProcessNode;
import mc.util.Location;
import mc.webserver.WebSocketServer;

import javax.annotation.Nullable;

import static org.fusesource.jansi.Ansi.ansi;

@AllArgsConstructor
@Getter
/**
 * A message to send back to the client
 */
public class LogMessage {
    private String message;
    /**
     * Clear the log window
     */
    private boolean clear = false;
    /**
     * Tell the client this is an error
     */
    private boolean error = false;
    private Location location = null;
    private int clearAmt = -1;
    public LogMessage(String message) {
        this.message = message;
    }

    public LogMessage(String function, ProcessNode process) {
        this(function+" @|black "+process.getIdentifier()+" "+formatLocation(process.getLocation())+"|@",true,false,null,-1);
    }

    public LogMessage(String message, boolean clear, boolean error) {
        this(message,clear,error,null,-1);
    }

    public LogMessage(String message, int clearAmt) {
        this(message);
        this.clearAmt = clearAmt;
        this.clear = true;
    }

    private static String formatLocation(Location location) {
        return "("+location.getLineStart()+":"+location.getColStart()+")";
    }

    public void send() {
        boolean hasClient = WebSocketServer.hasClient();
        //Web server is black on white, terminal is white on black.
        if (!hasClient) message = message.replace("@|black","@|white");
        this.message = ansi().render(message).toString();
        if (hasClient)
            WebSocketServer.send("log",this);
        else
            System.out.println(message);
    }
}
