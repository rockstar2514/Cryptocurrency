package com.BlockChain.Node;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class testClient {
        public static void main(String[] args) throws IOException, InterruptedException {
        	Scanner sc=new Scanner(System.in);
        	HttpClient client = HttpClient.newHttpClient();
        	do {
        		int choice;
        		System.out.println("Enter 1 for continue anything else will termrinate program");
        		choice=Integer.parseInt(sc.nextLine());
        		if(choice!=1)
        			break;
        		String URL="";
        		System.out.println("Enterr URL");
        		URL=sc.nextLine();
        		int reqtype=-1;
        		System.out.println("ENter 1 for Get and 2 for POST");
        		reqtype=Integer.parseInt(sc.nextLine());
        		if(reqtype==1) {
        			HttpRequest request2 = HttpRequest.newBuilder()
                            .uri(URI.create(URL))
                            .build();
       		             HttpResponse<String> response2 = client.send(request2,
       		                     HttpResponse.BodyHandlers.ofString());
       		            byte[] all=response2.body().getBytes();
       		            System.out.println(all.length);
       		            for(int i=0;i<all.length;i++) {
       		            	System.out.println(all[i]);
       		            }
        		}
        	}
        	while(true);
        }
}
