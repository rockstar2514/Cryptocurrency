package com.BlockChain.Node;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
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
	//Big Tests
	//Test1 try to see if BodyPublishers Will giev problem somwhere in sending json as String
	//Test utf-8 or utf-16
	protected volatile static TreeMap<Link,Output> unusedOutputs;
	protected volatile static TreeSet < Transaction > pendingTransactions;
	protected volatile static BlockChain ledger;
	protected volatile static ArrayList<String> Peers;
	protected volatile static int peerLimit=5;
	protected volatile static boolean pTb=true;
	protected volatile static boolean uOb=true;
	protected volatile  static boolean b=true;//Rename this 
	public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidInputException, InvalidOutputException, InvalidKeyException, InvalidAlgorithmParameterException, SignatureException {
		Scanner sc=new Scanner(System.in);
		//TODO should I use UTF-8 OR UTF-16 for pending transaction
		//Update BlockChain
		HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);//TODO change backlog value here//Listening to port 8000 on local host
		String dir=sc.nextLine();
		String target=sc.nextLine();
		ledger=new BlockChain(dir,target);
		pendingTransactions=new TreeSet<Transaction>();
		unusedOutputs=new TreeMap<Link,Output>();
		Peers=new ArrayList<String>();
		String address="";//Input your own address after tunneling via ngrok
		address=sc.nextLine();
		String OriginalPeer="";//Input initial peers//TODO add optionally functionality to input more than 1 address
		OriginalPeer=sc.nextLine();
		if(OriginalPeer.charAt(OriginalPeer.length()-1)=='/')
			OriginalPeer=OriginalPeer.substring(0,OriginalPeer.length()-1);
		fetchPeers(OriginalPeer,address);//Verified
		if(Peers.size()==0) {
			System.out.println("Sedlyf u r alone");
			System.exit(1);
		}
		fetchBlocks(OriginalPeer);
		HttpClient client=HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(OriginalPeer+"/getPendingTransactions"))
				.build();
		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());
		parseTransaction[] pendingTemp=(new Gson().fromJson(response.body(), parseTransaction[].class));
		for(parseTransaction pt:pendingTemp) {
			Transaction tok=pt.getRealTransaction();
			if(verifyTransaction(tok))
				modifyPT(tok,1);
		}
		sc.close();
		server.createContext("/getPendingTransaction",new PendingHandler());
		server.createContext("/getBlock/",new BlockSender());
		server.createContext("/newPeer",new PeerAdder());
		server.createContext("/getPeers",new PeerSender());
		server.createContext("/newBlock",new MindTheBlock());
		server.createContext("/newTransaction",new TransactionReciever());
		Thread t=new Thread(new Miner());
		t.start();
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
			HttpResponse<byte[]> response = client.send(request,
					HttpResponse.BodyHandlers.ofByteArray());
			if(response.statusCode()==404)
				break;
			byte[] binarydata=response.body();
			Block b=new Block(binarydata);
			if(verifyBlock(b)){
				modifyLedger(b,1);
				removeTrans(b);
				removeIaddO(b);
			}
			else {//TODO ADD LOGGER MESSAGE
				System.exit(1);
			}
			ind++;
		}
	}
	protected static synchronized void removeIaddO(Block b) {
		Transaction arr[]=b.getTransactions();
		for(Transaction t:arr) {
			Input inp=t.getInput();
			for(int i=0;i<inp.getNumberofInputs();i++) {
				modifyUO(new Link(inp.getID(i),inp.getOutputIndex(i)),null,-1);
			}
			Output op=t.getOutput();
			for(int i=0;i<op.getOutputs();i++) {
				modifyUO(new Link(t.getTID(),i), op,1);
			}
		}
	}
	protected static void fetchPeers(String s,String add) throws IOException, InterruptedException {
		// Function to add peers quite similiar to dfs :-)
		TreeSet<String> covered=new TreeSet<>();
		TreeSet<String> potential=new TreeSet<>();
		potential.add(s);
		covered.add(s);
		HttpClient client = HttpClient.newHttpClient();
		StringWriter sw=new StringWriter();
		JsonWriter writer=new JsonWriter(sw);
		writer.beginObject();writer.name("url");writer.value(add);writer.endObject();writer.flush();//writing json file to send my own url to peers so he can add my address in her/his own peers list
		String json=sw.toString();
		writer.close();
		sw.close();
		while(!potential.isEmpty()&&Peers.size()<peerLimit) {
			String curr=potential.first();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(curr+"/newPeer"))
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.build();//TODO test this for sending json as String O TEST need t JS AND python code
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString());//TODO check if UTF-8 works
			if(response.statusCode()!=404) {
				modifyPeer(curr,1);
			}
			request=HttpRequest.newBuilder()
					.uri(URI.create(curr+"/getPeers"))
					.build();
			response = client.send(request,
					HttpResponse.BodyHandlers.ofString());
			Gson gson=new Gson();
			Map<? , ?> map=gson.fromJson(response.body(), Map.class);
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				@SuppressWarnings("unchecked")
				ArrayList<String> arr=(ArrayList< String >)(entry.getValue());
				for(int i=0;i<arr.size();i++) {
					String now=arr.get(i);
					if(now.endsWith("/"))now=now.substring(0,now.length()-1);
					if(!covered.contains(now)) {
						potential.add(now);
						covered.add(now);
					}
				}
			}
			potential.remove(curr);
		}
	}
	protected static synchronized boolean verifyTransaction(Transaction t) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException {
		long inputcoins=0;
		long outputcoins=0;//TODO negative number checks
		Input input=t.getInput();
		int n=input.getNumberofInputs();
		TreeSet<Link> s=new TreeSet<>();
		EncryptionClient enc=new EncryptionClient();
		TreeMap<Link,Output> UOcopy=modifyUO(null,null,0);
		byte[] arr=t.getOutput().getData();
		MessageDigest md = MessageDigest.getInstance("SHA-256");  
		byte[] hash=(md.digest(arr));
		for(int i=0;i<n;i++) {
			String key1=input.getID(i);
			int val=input.getOutputIndex(i);
			Link l=new Link(key1,val);
			if(UOcopy.containsKey(l)){
				if(s.contains(l))
					return false;
				else{
					s.add(l);
					Output p=UOcopy.get(l);//TODO take care of memory consumption
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
	protected static synchronized TreeSet<Transaction> modifyPT(Transaction target,int mode){ 
		if(mode==1) {
			pendingTransactions.add(target);
			return null;
		}
		else if(mode==-1){
			pendingTransactions.remove(target);
			return null;
		}
		else{
			TreeSet<Transaction> ts=new TreeSet<Transaction>();
			for(Transaction t:pendingTransactions) {
				ts.add(t);
			}
			return ts;
		}
	}
	protected static synchronized TreeMap<Link, Output> modifyUO(Link l, Output o, int mode){
		if(mode==1) {
			unusedOutputs.put(l, o);
			return null;
		}
		else if(mode==-1) {
			unusedOutputs.remove(l);
			return null;
		}
		else {
			TreeMap<Link, Output> tmp=new TreeMap<Link,Output>();
			for(Map.Entry<Link,Output> entry : unusedOutputs.entrySet()) {
				tmp.put(entry.getKey(),entry.getValue());
			}
			return tmp;
		}
	}
	protected static synchronized Link modifyLedger(Block b,int mode) throws NoSuchAlgorithmException {
		if(mode==1)
		{
			ledger.addBlock(b);
			return null;
		}
		else {
			return new Link(ledger.getTarget()+ledger.getTop().getHash(),ledger.getSize());
		}
	}
	protected static synchronized ArrayList<String > modifyPeer(String target,int mode){
		if(mode==0)
		{
			ArrayList<String> copy=new ArrayList<String>();
			for(String s:Peers) {
				copy.add(s);
			}
			return copy;
		}
		else {
			if(mode==1) {
				if(Peers.size()<peerLimit)
					Peers.add(target);
				return null;
			}
			else {
				Peers.remove(target);
				return null;
			}
		}
	}
	static class Miner implements Runnable{
		@Override
		public void run() {
			try {
				//TODO verify algoritm more
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				EncryptionClient enc=new EncryptionClient();
				while(true) {
					b=true;
					Thread.sleep(2000);//TODO maybe modify this to get more Transactions in
					ArrayList<Transaction> tees=new ArrayList<Transaction>();
					int len=0;
					TreeSet<Link > s=new TreeSet<Link>();
					TreeSet<Transaction> copy=modifyPT(null,0);
					TreeMap<Link,Output> uOcopy=modifyUO(null,null,0);
					for(Transaction t:copy) {
						boolean bob=true;
						TreeSet<Link> s_=new TreeSet<Link>();
						Input inp=t.getInput();
						for(int j=0;j<inp.getNumberofInputs();j++) {
							String TID=inp.getID(j);
							int index=inp.getOutputIndex(j);
							if(!s.contains(new Link(TID,index))&&!s_.contains(new Link(TID,index))&&uOcopy.containsKey(new Link(TID,index))){
								s.add(new Link(TID,index));
								s_.add(new Link(TID,index));//TODO verify if checkTransaction does it job properly
							}
							else{
								bob=false;
								break;
							}
						}
						if(bob==true&&len+t.getSize()<=1e6)
							tees.add(t);
						else {
							for(Link l : s_) {
								s.remove(l);	
							}
						}
					}
					if(tees.size()>0) {//TODO Add Coinbase Transactions
						Transaction param[]=new Transaction[tees.size()];
						for(int i=0;i<tees.size();i++)
							param[i]=tees.get(i);
						Link l=modifyLedger(null,0);
						Block newb=new Block(l.b,l.TID.substring(32,64),param,l.TID.substring(0,32));//TODO Verify if Hash length is 32
						String targ=l.TID.substring(0,32);
						byte head[]=newb.getHeader();
						byte flag=0;
						long non=0;
						while(b==true&&flag==0) {
							Instant now=Instant.now();
							long time=now.getEpochSecond()*1000+((long)now.getNano());
							byte[] curr=Util.toByte(time);
							for(int i=0;i<8;i++)
								head[4+32+32+32+i]=curr[i];
							byte[] nonce=Util.toByte(non);
							for(int i=0;i<8;i++)
								head[4+32+32+32+8+i]=nonce[i];
							byte[] currHash=md.digest(head);
							if(Util.cmpHex(Util.parseByteToHex(currHash),targ)<0)
							{
								newb.setTimestamp(time);
								newb.setNonce(non);
								flag=1;
							}
							non++;
						}
						if(flag==1){  //later add condition for simultaneous mining and recieving
							//TODO verify this part again
							Server.modifyLedger(newb,1);
							Server.removeTrans(newb);
							Server.removeIaddO(newb);
							Server.sendBlock(newb);
						}
					}
				}
			} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	static class TransactionReciever implements HttpHandler{
		@Override
		public void handle(HttpExchange t) throws IOException {
			Gson gson=new Gson();
			String json=new String(ByteStreams.toByteArray(t.getRequestBody()));
			parseTransaction tmp=gson.fromJson(json, parseTransaction.class);
			String response="Transaction Recieved";
			try {
				Transaction t_=tmp.getRealTransaction();
				if(verifyTransaction(t_))
					modifyPT(t_,1);

			} catch (NoSuchAlgorithmException | IOException | InvalidOutputException | InvalidInputException | InvalidKeyException | InvalidAlgorithmParameterException | SignatureException e) {
				e.printStackTrace();
			}//t.getResponseHeaders().add("Content-Type","application/json");//TODO verify if this works without setting Content-Type
			t.sendResponseHeaders(200, response.length());
			OutputStream os=t.getResponseBody();
			os.write(response.getBytes());os.close();
		}
	}
	static class MindTheBlock implements HttpHandler{
		@Override
		public void handle(HttpExchange t) throws IOException{
			try {
				byte[] data=ByteStreams.toByteArray(t.getRequestBody());//Test this guava code
				Block newb=new Block(data);//may throw thousands of Exception but if no exception thrown Block has been parsed properly from data 
				if(verifyBlock(newb)){
					b=false;
					modifyLedger(newb,1);
					removeIaddO(newb);
					removeTrans(newb);
					sendBlock(newb);
					String response="Done";//TODO Check is this works without setting content header
					t.sendResponseHeaders(200, response.length());
					OutputStream os=t.getResponseBody();
					os.write(response.getBytes());os.close();
				}else {
					String response="Jhutaaaaa";
					t.sendResponseHeaders(200, response.length());
					OutputStream os=t.getResponseBody();
					os.write(response.getBytes());os.close();
				}
			} catch (InvalidKeyException | NoSuchAlgorithmException | IndexOutOfBoundsException | SignatureException
					| InvalidAlgorithmParameterException | ConcurrentModificationException | IOException
					| InvalidInputException | InvalidOutputException e) {
				System.out.println("Error Occured while recieving Mined Block.");
				e.printStackTrace();
			}
		}    
	}

	static class PeerSender implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			StringWriter sw=new StringWriter();
			JsonWriter writer=new JsonWriter(sw);
			ArrayList<String> pers=modifyPeer(null,0);
			writer.beginObject();writer.name("peers");writer.beginArray();
			for(int i=0;i<pers.size();i++) {writer.value(pers.get(i));}
			writer.endArray();writer.endObject();writer.flush();sw.flush();
			String s=sw.toString();
			writer.close();sw.close();
			exchange.getResponseHeaders().add("Content-Type","application/json");//TODO check what this does actually
			exchange.sendResponseHeaders(200, s.length());
			OutputStream os=exchange.getResponseBody();
			os.write(s.getBytes());os.close();
		}  
	}
	static class PeerAdder implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			ArrayList<String> pers=modifyPeer(null,0);
			if(pers.size()>=peerLimit) {
				String response="Connection Rejected";
				OutputStream os=exchange.getResponseBody();
				exchange.sendResponseHeaders(500, response.length());
				os.write(response.getBytes());
				os.close();
			}
			else {
				Gson gson = new Gson();
				Reader reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");//TODO why UTF-8
				Map<?, ?> map = gson.fromJson(reader, Map.class);
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					entry.getKey(); 
					String s=(String)entry.getValue();
					if(s.endsWith("/"))
						s=s.substring(0,s.length()-1);
					modifyPeer(s,1);//TODO remove ending 
				}
				reader.close();
				String response="Connection Accepted";
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
			TreeSet<Transaction> PTcopy=modifyPT(null,0);
			writer.beginArray();
			for(Transaction curr:PTcopy) {
				writer.beginObject();writer.name("inputs");writer.beginArray();
				Input curri=curr.getInput();
				for(int j=0;j<curri.getNumberofInputs();j++) {
					writer.beginObject();writer.name("transactionID");writer.value(curri.getID(j));writer.name("index");writer.value(curri.getOutputIndex(j));writer.name("signature");writer.value(curri.getSignature(j));writer.endObject();
				}
				writer.endArray();writer.name("outputs");writer.beginArray();
				Output currop=curr.getOutput();
				for(int j=0;j<currop.getOutputs();j++) {
					writer.beginObject();writer.name("amount");writer.value(currop.getCoins(j));writer.name("recipent");writer.value(currop.KeyToString(j));writer.endObject();
				}
				writer.endObject();
			}
			writer.endArray();writer.flush();
			sw.flush();
			String json=sw.toString();
			sw.close();writer.close();
			t.getResponseHeaders().add("Content-Type", "application/json");
			t.sendResponseHeaders(200, json.length());
			OutputStream os = t.getResponseBody();
			os.write(json.getBytes());os.close();
		} 
	}

	static class BlockSender implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String s=exchange.getRequestURI().toString();
			if(s.endsWith("/"))
				s=s.substring(0,s.length()-1);
			try
			{
				int num=Integer.parseInt(s.substring(s.lastIndexOf("/")+1,s.length()));
				Link l=modifyLedger(null,0);
				if(l.b<=num) {
					exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
					exchange.sendResponseHeaders(404, "Get Lost".length());
					OutputStream os = exchange.getResponseBody();
					os.write("Get Lost".getBytes());os.close();
				}else {
					InputStream reader=new FileInputStream("Block"+num);
					byte[] data=reader.readAllBytes();
					exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
					exchange.sendResponseHeaders(200, data.length);
					OutputStream os = exchange.getResponseBody();
					os.write(data);os.close();
				}
			}
			catch(NumberFormatException | NoSuchAlgorithmException e) {
				exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
				exchange.sendResponseHeaders(404, "Get Lost".length());
				OutputStream os = exchange.getResponseBody();
				os.write("Get Lost".getBytes());os.close();
			}
		}
	}
	protected static synchronized void removeTrans(Block b){
		Transaction[] arr=b.getTransactions(); 
		for(Transaction t:arr) {
			modifyPT(t,-1);
		}
	}
	protected static synchronized void sendBlock(Block b) {
		ArrayList<String> listOfPeers=modifyPeer(null,0);
		HttpClient client=HttpClient.newHttpClient();
		for(String s:listOfPeers) {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(s+"/newBlock"))
					.POST(HttpRequest.BodyPublishers.ofByteArray(b.getData()))
					.build();//TODO test this out
			try {
				HttpResponse<String> response = client.send(request,
						HttpResponse.BodyHandlers.ofString());//TODO Check that here I recieve output as String
				System.out.println(response.body()+": "+s);
			} catch (IOException | InterruptedException e) {
				System.out.println("Connection Interrupted / IOException sending to Peer: "+s);
				e.printStackTrace();
			}
		}
	}
	protected static synchronized boolean verifyBlock(Block b) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
		//TODO separate case of Block 0
		//NOTE any updaes on Block Verification can be made here
		if(!b.getbah())//TODO set bah for direct production
			return false;
		TreeSet<Link> s=new TreeSet<Link>();
		Transaction arr[]=b.getTransactions();
		//TODO verify coinbase transaction
		TreeMap<Link,Output> UOcopy=modifyUO(null,null,0);
		for(int i=1;i<arr.length;i++) {
			Transaction t=arr[i];
			if(!verifyTransaction(t))
				return false;
			Input inp=t.getInput();
			Output op=t.getOutput();
			for(int j=0;j<inp.getNumberofInputs();j++) {
				Link l=new Link(inp.getID(j),inp.getOutputIndex(j));
				if(s.contains(l)||!UOcopy.containsKey(l)) {
					return false;
				}
				else {
					s.add(l);
				}
			}
			//TODO Verify Output
		}
		Link l=modifyLedger(null,0);
		/*
		 * Verify modifyLedger(null,0)
		 * and following part
		 */
		if(b.getIndex()!=l.b)
			return false;
		if(b.getPhash().compareTo(l.TID.substring(64,128))!=0)//TODO check substring size//TODO exception of Block 0
			return false;
		if(b.getTarget().compareTo(l.TID.substring(0,64))!=0)
			return false;
		return true;
	}

}
