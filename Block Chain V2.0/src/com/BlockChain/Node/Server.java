package com.BlockChain.Node;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

//import App.AddHandler;
//import App.ListHandler;
//import App.PingHandler;

//* This class is the one that will receive Transactions
public class Server {

	  protected static TreeMap<Link,Output> unusedOutputs;//TODO deal with this.quite inefficient
	  protected static String peers[];
	  protected static ArrayList < Transaction > pendingTransactions;
	  protected static BlockChain ledger;
	  public static void main(String[] args) throws IOException {
		  //TODO input URL
		  //TODO get existing BlockChain
		  //TODO get pending transaction
		  //Update BlockChain
		  HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);
		  server.createContext("/getPendingTransaction",new PendingHandler());
		  for(int i=0;i<ledger.size();i++) {
			  server.createContext("/getBlock/"+String.valueOf(i),new BlockSender(i));
		  }
	  }
	  static class PendingHandler implements HttpHandler{
		  @Override
		  public void handle(HttpExchange t) throws IOException {
			        StringWriter sw=new StringWriter();
	                JsonWriter writer=new JsonWriter(sw);
	                writer.beginArray();
	                for(int i=0;i<pendingTransactions.size();i++) {
	                	Transaction curr=pendingTransactions.get(i);
	                	writer.beginObject();
	                	writer.name("inputs");
	                	writer.beginArray();
	                	Input curri=curr.getInput();
	                	for(int j=0;j<curri.getNumberofInputs();j++) {
	                		writer.beginObject();
	                		writer.name("transactionID");
	                		writer.value(curri.getID(j));
	                		writer.name("index");
	                		writer.value(curri.getOutputIndex(j));
	                		writer.name("signature");
	                		writer.value(curri.getSignature(j));
	                		writer.endObject();
	                	}
	                	writer.endArray();
	                	writer.name("outputs");
	                	writer.beginArray();
	                	Output currop=curr.getOutput();
	                	for(int j=0;j<currop.getOutputs();j++) {
	                		writer.beginObject();
	                		writer.name("amount");
	                		writer.value(currop.getCoins(j));
	                		writer.name("recipent");
	                		writer.value(currop.KeyToString(j));
	                		writer.endObject();
	                	}
	                	writer.endObject();
	                }
	                writer.endArray();
	                writer.flush();
	                
	                sw.flush();
	                writer.close();
	                sw.close();
	                String json=sw.toString();
	                t.getResponseHeaders().add("Content-Type", "application/json");
	                OutputStream os = t.getResponseBody();
	                os.write(json.getBytes());
	                os.close();
	        } 
	  }
	  static class BlockSender implements HttpHandler{
		  private int i;
		  protected BlockSender(int i) {
			  this.i=i;
		  }
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO send data required
			//TODO some exception handling
			Block message=ledger.getBlock(i);
			exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
		    byte[] data1=message.sendHeader();
		    byte[] data2=message.getData();
		    byte[] fulldata=new byte[data1.length+data2.length];
		    for(int i=0;i<data1.length;i++)
		    	fulldata[i]=data1[i];
		    for(int j=0;j<data2.length;j++){
		    	fulldata[data1.length+j]=data2[j];
		    }
		    OutputStream os=exchange.getResponseBody();
		    os.write(fulldata);
		    os.close();
		}
		  
	  }
      protected static boolean verifyTransaction(Transaction t) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException {
    	       
    	       return t.verifyExist();
      }
	
	
}
