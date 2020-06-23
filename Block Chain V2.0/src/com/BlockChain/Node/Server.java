package com.BlockChain.Node;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.Util;
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
	  protected static int peerLimit=5;
	  public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidInputException, InvalidOutputException, InvalidKeyException, InvalidAlgorithmParameterException, SignatureException {
		  Scanner sc=new Scanner(System.in);
		  //TODO input URL
		  //TODO get existing BlockChain
		  //TODO get pending transaction//TODO should I use UTF-8 OR UTF-16
		  //Update BlockChain
		  HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);//TODO change backlog value here
		  String adress="";
		  adress=sc.nextLine();
		  String OriginalPeer="";
		  OriginalPeer=sc.nextLine();
		  fetchPeers(OriginalPeer,adress);
		  if(Peers.size()==0) {
			  System.out.println("Sedlyf u r alone");
			  System.exit(1);
		  }
		  fetchBlocks(OriginalPeer);
		  if(OriginalPeer.charAt(OriginalPeer.length()-1)=='/')
			  OriginalPeer=OriginalPeer.substring(0,OriginalPeer.length()-1);
		  HttpClient client=HttpClient.newHttpClient();
		  HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create(OriginalPeer+"/getPendingTransactions"))
                  .build();
		  HttpResponse<String> response = client.send(request,
                  HttpResponse.BodyHandlers.ofString());
		  parseTransaction[] pendingTemp=(new Gson().fromJson(response.body(), parseTransaction[].class));
		  for(parseTransaction pt:pendingTemp) {
			  pendingTransactions.add(pt.getRealTransaction());
		  }
		  sc.close();
		  server.createContext("/getPendingTransaction",new PendingHandler());
		  server.createContext("/getBlock/",new BlockSender());
		  server.createContext("/newPeer",new PeerAdder());
		  server.createContext("/getPeers",new PeerSender());
		  server.createContext("/newBlock",new MindTheBlock());
		  server.createContext("/newTransaction",new TransactionReciever());
	  }
	  protected static void fetchBlocks(String s) throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidInputException, InvalidOutputException, InvalidKeyException, InvalidAlgorithmParameterException, SignatureException {
		  HttpClient client = HttpClient.newHttpClient();
		  int ind=0;
		  if(s.endsWith("/"))
			  s=s.substring(0,s.length()-1);
		  while(true) {
			  HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(s+"/getBlock/"+Integer.toString(ind)))
              .build();
			  HttpResponse<String> response = client.send(request,
	                  HttpResponse.BodyHandlers.ofString());
			  if(response.statusCode()==404)
				  break;
			  byte[] binarydata=response.body().getBytes();
			  Block b=new Block(binarydata);
			  ledger.add(b);//TODO Modify to save on computer otherwise memory will exceed quickly
			  for(int i=0;i<b.transactions.length;i++) {
				  Transaction t=b.transactions[i];
				  if(!verifyTransaction(t))
					  {
					  System.out.println("Peers has send an Invalid Transaction Shtting Down");
					  System.exit(1);
					  }
				  updateUnusedOutputs(t);
			  }
			  ind++;
		  }
		  
		  // here make respective changes int the block
		  //ask for pending Transaction
	  }
	  protected static void updateUnusedOutputs(Transaction t) {
		   Input input=t.getInput();
		   for(int i=0;i<input.getNumberofInputs();i++) {
			   String TID=input.getID(i);
			   int index=input.getOutputIndex(i);
			   unusedOutputs.remove(new Link(TID,index));
		   }
		   Output output=t.getOutput();
		   for(int i=0;i<output.getOutputs();i++) {
			   unusedOutputs.put(new Link(t.getTID(),i), output);
		   }
	  }
	  protected static void fetchPeers(String s,String add) throws IOException, InterruptedException {
		  TreeSet<String> covered=new TreeSet<>();
		  TreeSet<String> potential=new TreeSet<>();
		  potential.add(s);
		  covered.add(s);
		  HttpClient client = HttpClient.newHttpClient();
		  StringWriter sw=new StringWriter();
		  JsonWriter writer=new JsonWriter(sw);
		  writer.beginObject();
		  writer.name("url");
		  writer.value(add);
		  writer.endObject();
		  writer.flush();
		  String json=sw.toString();
		  writer.close();
		  sw.close();
		  while(!potential.isEmpty()&&Peers.size()<peerLimit) {
			 String curr=potential.first();
			 if(curr.endsWith("/"))curr=curr.substring(0,curr.length()-1);
			 HttpRequest request = HttpRequest.newBuilder()
                     .uri(URI.create(curr+"/newPeer"))
                     .POST(HttpRequest.BodyPublishers.ofString(json))
                     .build();//need to test this for sending json as String 
			 HttpResponse<String> response = client.send(request,
	                  HttpResponse.BodyHandlers.ofString());
			 if(response.statusCode()==200){
				  Peers.add(curr);
			 }
			 request=HttpRequest.newBuilder()
                     .uri(URI.create(curr+"/getPeers"))
                     .build();
			 response = client.send(request,
	                  HttpResponse.BodyHandlers.ofString());
			 Gson gson=new Gson();
			 Map<? , ?> map=gson.fromJson(response.body(), Map.class);//test this locally with String aarguement
			 for (Map.Entry<?, ?> entry : map.entrySet()) {
 	    	    ArrayList<String> arr=(ArrayList< String >)(entry.getValue());//exception handling
 	    	    for(int i=0;i<arr.size();i++) {
 	    		    if(!covered.contains(arr.get(i))) {
 	    		    	  potential.add(arr.get(i));
 	    		    	  covered.add(arr.get(i));
 	    		    }
 	          	}
	        } 
		  }
	  }
	  protected static boolean verifyTransaction(Transaction t) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException {
		  long inputcoins=0;
   	   long outputcoins=0;
   	   Input input=t.getInput();
   	   int n=input.getNumberofInputs();
   	   TreeSet<Link> s=new TreeSet<>();
   	   EncryptionClient enc=new EncryptionClient();
   	     for(int i=0;i<n;i++) {
   	    	 String key1=input.getID(i);
   	    	 int val=input.getOutputIndex(i);
   	    	 Link l=new Link(key1,val);
   	    	 if(Server.unusedOutputs.containsKey(l))
   	    	 {
   	    		 if(s.contains(l))
   	    			 return false;
   	    		 else
   	    		 {
   	    			 s.add(l);
   	    			 Output p=Server.unusedOutputs.get(l);//TODO take care of memeory consumption
   	    			 byte[] arr=p.getData();
   	    			 MessageDigest md = MessageDigest.getInstance("SHA-256");  
   	        	     byte[] hash=(md.digest(arr));
   	        	     byte[] signingdata=new byte[68];
   	        	     byte[] hash1=Util.parseHexToByte(key1);
   	        	     
   	        	     for(int j=0;j<32;j++) {
   	        	    	 signingdata[j]=hash1[j];
   	        	     }
   	        	     byte[] more=Util.toByte(val);
   	        	     for(int j=0;j<4;j++) {
   	        	    	 signingdata[32+j]=more[j];
   	        	     }
   	        	     for(int j=0;j<32;j++) {
   	        	    	 signingdata[32+4+j]=hash[j];
   	        	     }
   	        	     if(!enc.verify(input.getSignature(i),signingdata , p.getKey(val)))
   	        	       return false;
   	        	     inputcoins+=p.getCoins(val);
   	    		 }
   	    	 }
   	    	 else {
   	    		 return false;
   	    	 }
   	    	 
   	     }
   	     outputcoins+=t.getOutput().getCoins();
   	     if(inputcoins<=outputcoins)
   	
   	     return true;
   	     else 
   	    	 return false;
	       
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
	            if(Peers.size()>=peerLimit) {
	            	String response="Connection Failed";
	            	OutputStream os=exchange.getResponseBody();
				    exchange.sendResponseHeaders(500, response.length());
				    os.write(response.getBytes());
				    os.close();
	            }
	            else {
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
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO send data required
			//TODO some exception handling
			exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
			int ind=Integer.parseInt(exchange.getRequestURI().toString().substring(exchange.getRequestURI().toString().lastIndexOf("/")+1));//TODO maybe here exception
			if(ind>=ledger.size()||ind<0)
			{
				exchange.sendResponseHeaders(404, 7);
				OutputStream os=exchange.getResponseBody();
				os.write("fuckoff".getBytes());
				os.close();
			}
			else {
				Block message=ledger.getBlock(ind);//TODO make this file retrieval
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
		  
	  }
    
	
	
}
