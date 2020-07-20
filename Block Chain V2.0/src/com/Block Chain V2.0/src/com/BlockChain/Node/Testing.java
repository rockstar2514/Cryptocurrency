package com.BlockChain.Node;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.net.http.HttpRequest.BodyPublishers;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import com.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Testing {
     public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidInputException, InvalidOutputException {
    	 InputStream reader=new FileInputStream("010.dat");
    	 byte[] tdata=reader.readAllBytes();
    	 Transaction t=Transaction.getTransaction(tdata);
    	 byte b1[]=t.getData();     
    	 reader=new FileInputStream("79d728fa8bf712b517da66bbe7c34278ea74a87cd385d3189ff3b436c7ae91bc.dat");
    	 tdata=reader.readAllBytes();
    	 t=Transaction.getTransaction(tdata);
    	 byte b2[]=t.getData();
//    	 byte[] body=new byte[12+b1.length+b2.length];
//    	 byte count[]=Util.toByte(2);
//    	 for(int i=0;i<4;i++) {
//    		 body[i]=count[i];
//    	 }
//    	 count=Util.toByte(b1.length);
//    	 for(int i=0;i<4;i++) {
//    		 body[4+i]=count[i];
//    	 }
//    	 for(int i=0;i<b1.length;i++)
//    		 body[8+i]=b1[i];
//    	 count=Util.toByte(b2.length);
//    	 for(int i=0;i<4;i++) {
//    		 body[8+b1.length+i]=count[i];
//    	 }
//    	 for(int i=0;i<b2.length;i++)
//    		 body[12+b1.length+i]=b2[i];
//    	 byte[] fulldata=new byte[116+body.length];
//    	 for(int i=116;i<fulldata.length;i++) {
//    		 fulldata[i]=body[i-116];
//    	 }
//    	 Block b=new Block(fulldata);
//    	 b.getReceipt();
//    	 String add="";
//    	 Scanner sc=new Scanner(System.in);
//    	 add=sc.nextLine();
//    	 String s="{ \"key\":1,\"value\":\"lol\"}";
//    	 System.out.println(s);
//    	 HttpClient client=HttpClient.newHttpClient();
//    	 HttpRequest request = HttpRequest.newBuilder()
//					.uri(URI.create(add+"/json"))
//					.header("Content-Type", "application/json")
//					.POST(BodyPublishers.ofString(s))
//					.build();//TODO test this out
//    	 try {
//			HttpResponse<String> resp=client.send(request, HttpResponse.BodyHandlers.ofString());
//			System.out.println(resp.body());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	HttpServer server=HttpServer.create(new InetSocketAddress(8000),0);
    	server.createContext("/newBlock",new Haha());
    	server.start();
     }
     static class Haha implements HttpHandler{
		public void handle(HttpExchange t) throws IOException {
			String s=t.getRequestURI().toString();
			System.out.println(s);
			t.getResponseHeaders().add("Content-Type","application/plain-text");
			int num=Integer.parseInt(s.substring(s.lastIndexOf("/")+1,s.length()));
			if(num>35)
			{
				String message="Fuckoff";
				t.sendResponseHeaders(404,message.length());
				OutputStream os=t.getResponseBody();
				os.write(message.getBytes());
				os.close();
				
			}
			else
			{
				String message="Thanks";
				t.sendResponseHeaders(200,message.length());
				OutputStream os=t.getResponseBody();
				os.write(message.getBytes());
				os.close();
			}
		} 
     }
}
