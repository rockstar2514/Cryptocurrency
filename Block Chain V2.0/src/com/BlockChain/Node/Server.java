package com.BlockChain.Node;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
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
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.google.common.io.ByteStreams;

//import App.AddHandler;
//import App.ListHandler;
//import App.PingHandler;

//* This class is the one that will receive Transactions
public class Server {

	  protected static TreeMap<Link,Output> unusedOutputs;//TODO deal with this.quite inefficient
	  protected static String peers[];
	  protected static ArrayList < Transaction > pendingTransactions;
	  protected static BlockChain ledger;
	  protected static ArrayList<String> Peers;
	  public static void main(String[] args) throws IOException {
		  //TODO input URL
		  //TODO get existing BlockChain
		  //TODO get pending transaction//TODO should I use UTF-8 OR UTF-16
		  //Update BlockChain
		  HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);
		  server.createContext("/getPendingTransaction",new PendingHandler());
		  for(int i=0;i<ledger.size();i++) {
			  server.createContext("/getBlock/"+String.valueOf(i),new BlockSender(i));
		  }
		  server.createContext("/newPeers",new PeerAdder());
		  server.createContext("/getPeers",new PeerSender());
		  server.createContext("/newBlock",new MindTheBlock());
		  server.createContext("/newTransaction",new TransactionReciever());
	  }
	  static class TransactionReciever implements HttpHandler{
		@Override
		public void handle(HttpExchange t) throws IOException {
			  Gson gson=new Gson();
			  String json=new String(ByteStreams.toByteArray(t.getRequestBody()));
			  parseTransaction tmp=gson.fromJson(json, parseTransaction.class);
			  tmp.getDetails();
			  String response="Transaction Recieved";
			  try {
				pendingTransactions.add(tmp.getRealTransaction());
			  } catch (NoSuchAlgorithmException | IOException | InvalidOutputException | InvalidInputException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }
			  t.sendResponseHeaders(200, response.length());
			  OutputStream os=t.getResponseBody();
			  os.write(response.getBytes());
			  os.close();
		}
		  
	  }
	  static class MindTheBlock implements HttpHandler{//notice the pun :))))
		  @Override
		  public void handle(HttpExchange t) throws IOException{
	            byte[] message=ByteStreams.toByteArray(t.getRequestBody());
				try {
					Block newb = new Block(message);
					ledger.add(newb);
		            String response="Done";
				    t.sendResponseHeaders(200, response.length());
				    OutputStream os=t.getResponseBody();
				    os.write(response.getBytes());
				    os.close();
				} catch (NoSuchAlgorithmException | InvalidInputException | IOException | InvalidOutputException e) {
					e.printStackTrace();
				}
		  }    
	  }
	  
	  static class PeerSender implements HttpHandler{

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			    StringWriter sw=new StringWriter();
		        JsonWriter writer=new JsonWriter(sw);
			    writer.beginObject();
			    writer.name("peers");
			    writer.beginArray();
			    for(int i=0;i<Peers.size();i++) {
			    	writer.value(Peers.get(i));
			    }
			    writer.endArray();
			    writer.endObject();
			    writer.flush();
			    sw.flush();
			    String s=sw.toString();
			    writer.close();
			    sw.close();
			    exchange.getResponseHeaders().add("Content-Type","application/json");
			    exchange.sendResponseHeaders(200, s.length());
			    OutputStream os=exchange.getResponseBody();
			    os.write(s.getBytes());
			    os.close();
		}
		  
	  }
	  static class PeerAdder implements HttpHandler{

		@Override
		public void handle(HttpExchange exchange) throws IOException {
	
			    Gson gson = new Gson();
			    Reader reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
			    Map<?, ?> map = gson.fromJson(reader, Map.class);
			    for (Map.Entry<?, ?> entry : map.entrySet()) {
			        entry.getKey(); 
			        Peers.add((String)(entry.getValue()));
			    }
			    reader.close();
			    String response="Connection Reached";
			    OutputStream os=exchange.getResponseBody();
			    exchange.sendResponseHeaders(200, response.length());
			    os.write(response.getBytes());
			    os.close();
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
	                String json=sw.toString();
	                sw.close();
	                writer.close();
	                t.getResponseHeaders().add("Content-Type", "application/json");
	                t.sendResponseHeaders(200, json.length());
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
		    exchange.sendResponseHeaders(200, fulldata.length);
		    OutputStream os=exchange.getResponseBody();
		    os.write(fulldata);
		    os.close();
		}
		  
	  }
      protected static boolean verifyTransaction(Transaction t) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException {
    	       
    	       return t.verifyExist();
      }
	
	
}
