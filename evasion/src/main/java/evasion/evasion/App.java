package evasion.evasion;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class App 
{
	private static CountDownLatch messageLatch;
	
	private static String SENT_MESSAGE = "";
	
	private static String publisherEndpoint = "ws://localhost:1990",
				   		  hunterEndpoint = "ws://localhost:1991",
				   		  preyEndpoint = "ws://localhost:1992";
	    
    public static void main( String[] args )
    {        
        try {
            messageLatch = new CountDownLatch(1);

            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

            ClientManager client = ClientManager.createClient();
            
            Endpoint hunter = new Endpoint() {

				@Override
				public void onOpen(Session session, EndpointConfig arg1) {
					try {
                        session.addMessageHandler(new MessageHandler.Whole<String>() {
                            public void onMessage(String message) {
                                System.out.println("Received message: "+message);
                                messageLatch.countDown();
                            }
                        });
                        SENT_MESSAGE = getPositionsCommand();
                        session.getBasicRemote().sendText(SENT_MESSAGE);
                        SENT_MESSAGE = getWallsCommand();
                        session.getBasicRemote().sendText(SENT_MESSAGE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
					
				}
            };
            client.connectToServer(hunter, cec, new URI(hunterEndpoint));
            messageLatch.await(100, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
        
    public static String getPositionsCommand() {
    	return runCommand("P");
    }
    
    public static String getWallsCommand() {
    	return runCommand("W");
    }
    
    public static String runCommand(String command) {
    	String action = "";
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode node = mapper.createObjectNode();
        node.put("command", command); 
        try {
			action = mapper.writeValueAsString(node);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return action;
    }
}

