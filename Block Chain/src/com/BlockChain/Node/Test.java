package com.BlockChain.Node;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;

public class Test{
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidOutputException, InvalidInputException {
//	     Gson gson=new Gson();
//	     String json=new String(Files.readAllBytes(Paths.get("example.txt")));
//	     parseTransaction[] pends=gson.fromJson(json, parseTransaction[].class);
//         for(parseTransaction t:pends) {
//        	 Transaction t_=t.getRealTransaction();
//        	 t_.getReceipt();
//        	 //t.getDetails();
//         }
		String dir="Block14";
		byte[] data=Files.readAllBytes(Paths.get(dir));
		Block b=new Block(data);
		b.getReceipt();
	}
}