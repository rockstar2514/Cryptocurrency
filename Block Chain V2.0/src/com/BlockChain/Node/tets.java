package com.BlockChain.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import com.BlockChain.Node.Server.BlockSender;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class tets {
	protected static byte data[];
    public static void main(String[] args) throws IOException {
//    	StringWriter sw=new StringWriter();
//        JsonWriter writer=new JsonWriter(sw);
//        //writer.beginObject();
//        writer.beginArray();
//        for(int i=0;i<10;i++) {
//        	//Transaction curr=pendingTransactions.get(i);
//        	writer.beginObject()
//        	writer.name("inputs");
//        	writer.beginArray();
//        	//Input curri=curr.getInput();
//        	for(int j=0;j<5;j++) {
//        		writer.beginObject();
//        		writer.name("transactionID");
//        		writer.value(j);
//        		writer.name("index");
//        		writer.value(j*10);
//        		writer.name("signature");
//        		writer.value(j*100);
//        		writer.endObject();
//        	}
//        	writer.endArray();
//        	writer.name("outputs");
//        	writer.beginArray();
//        	//Output currop=curr.getOutput();
//        	for(int j=0;j<2;j++) {
//        		writer.beginObject();
//        		writer.name("amount");
//        		writer.value(1000);
//        		writer.name("recipent");
//        		writer.value(1000);
//        		writer.endObject();
//        	}
//        	writer.endArray();
//        	writer.endObject();
//        }
//        writer.endArray();
//        //writer.endObject();
//        writer.flush();
//        sw.flush();
//        String json="{"+sw.toString()+"}";
//        writer.close();
//        System.out.println(json);
//    	    Gson gson = new Gson();
//    	    // create a reader
//    	    String json= new String(Files.readAllBytes(Paths.get("D:\\Block Chain V2.0\\please.json")));
//    	    System.out.println(json);
//    	    // convert JSON file to map
//    	   // Map<?, ?> map = gson.fromJson(reader, Map.class);
//    	    // print map entries
////    	    for (Map.Entry<?, ?> entry : map.entrySet()) {
////    	    	System.out.println(entry.getKey() + "=" + entry.getValue().getClass());
////    	    	ArrayList<String> arr=(ArrayList< String >)(entry.getValue());
////    	    	for(int i=0;i<arr.size();i++) {
////    	    		System.out.println(arr.get(i));
////    	    	}
////    	    }//for recieving get peer endpoint
//    	    // close reader
//    	    parseTransaction tmp=gson.fromJson(json, parseTransaction.class);
//    	    tmp.getDetails();
    	HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);
    	data=new byte[15];
    	for(byte i=0;i<15;i++) {
    		data[i]=(byte)(i*2);
    		server.createContext("/getBlock/"+String.valueOf(i),new ByteSender(i));
    	}
    	server.start();
    }
    static class ByteSender implements HttpHandler{
		  private int i;
		  protected ByteSender(int i) {
			  this.i=i;
		  }
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO send data required
			//TODO some exception handling
			exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
			byte[] dat=new byte[1];
			dat[0]=data[i];
			exchange.sendResponseHeaders(200, dat.length);
		    OutputStream os=exchange.getResponseBody();
		    os.write(dat);
		    os.close();
		}
		  
	  }    
}
