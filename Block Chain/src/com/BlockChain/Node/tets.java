package com.BlockChain.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    public static void main(String[] args) throws IOException, InterruptedException {
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
////    	    tmp.getDetails();
//    	HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);
//  	        data=new byte[16];
//  	        data[15]=30;
//  	        for(int i=0;i<16;i++)
//  	        	data[i]=(byte)(i*2);
//    		server.createContext("/getBlock/",new ByteSender());
//    	server.start();
////    	String url="https://google.com/";
////    	HttpClient client = HttpClient.newHttpClient();
////    	HttpRequest request2 = HttpRequest.newBuilder()
////                .uri(URI.create(url+"list"))
////                .build();
////	    HttpResponse<String> response2 = client.send(request2,
////	                     HttpResponse.BodyHandlers.ofString());
////	    System.out.println(response2.statusCode());
    	Thread t=new Thread(new Haha());
    	t.start();
    }
    static class Haha implements Runnable{
		@Override
		public void run() {
			System.out.println("Testing Thread ans Runnable Interface");
		}	
    }
    static class ByteSender implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO send data required
			//TODO some exception handling
			//System.out.println(exchange.getRequestURI());
			String req=exchange.getRequestURI().toString();
			req=req.substring(req.lastIndexOf("/")+1);
			System.out.println(req);
			int ind=Integer.parseInt(req);
			exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
			byte[] dat=new byte[1];
			data[0]=-1;
			if(ind>=data.length) {
				exchange.sendResponseHeaders(404, dat.length);
			}
			else
			{
			dat[0]=data[ind];
			exchange.sendResponseHeaders(200, dat.length);
			}
			 OutputStream os=exchange.getResponseBody();
		    os.write(dat);
		    os.close();
		}
		  
	  }    
}
