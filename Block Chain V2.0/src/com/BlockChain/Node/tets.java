package com.BlockChain.Node;

import java.io.IOException;
import java.io.StringWriter;

import com.google.gson.stream.JsonWriter;

public class tets {
    public static void main(String[] args) throws IOException {
    	StringWriter sw=new StringWriter();
        JsonWriter writer=new JsonWriter(sw);
        //writer.beginObject();
        writer.beginArray();
        for(int i=0;i<10;i++) {
        	//Transaction curr=pendingTransactions.get(i);
        	writer.beginObject()
        	writer.name("inputs");
        	writer.beginArray();
        	//Input curri=curr.getInput();
        	for(int j=0;j<5;j++) {
        		writer.beginObject();
        		writer.name("transactionID");
        		writer.value(j);
        		writer.name("index");
        		writer.value(j*10);
        		writer.name("signature");
        		writer.value(j*100);
        		writer.endObject();
        	}
        	writer.endArray();
        	writer.name("outputs");
        	writer.beginArray();
        	//Output currop=curr.getOutput();
        	for(int j=0;j<2;j++) {
        		writer.beginObject();
        		writer.name("amount");
        		writer.value(1000);
        		writer.name("recipent");
        		writer.value(1000);
        		writer.endObject();
        	}
        	writer.endArray();
        	writer.endObject();
        }
        writer.endArray();
        //writer.endObject();
        writer.flush();
        sw.flush();
        String json="{"+sw.toString()+"}";
        writer.close();
        System.out.println(json);
    }
    
}
