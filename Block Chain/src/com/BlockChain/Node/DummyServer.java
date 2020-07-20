//package com.BlockChain.Node;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.Reader;
//import java.io.StringWriter;
//import java.net.HttpURLConnection;
//import java.net.InetSocketAddress;
//import java.net.URI;
//import java.net.URL;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.file.Paths;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.InvalidKeyException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.security.PublicKey;
//import java.security.SignatureException;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.ConcurrentModificationException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.TreeMap;
//import java.util.TreeSet;
//
//import org.bouncycastle.util.Arrays;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import com.google.gson.Gson;
//import com.google.gson.stream.JsonWriter;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import com.sun.net.httpserver.HttpServer;
//import com.Util;
//import com.google.common.io.ByteStreams;
//
////import App.AddHandler;
////import App.ListHandler;
////import App.PingHandler;
//
////* This class is the one that will receive Transactions
//public class DummyServer {
//	//Big Tests
//	//Test1 try to see if BodyPublishers Will give problem somwhere in sending json as String
//	//Test utf-8 or utf-16
//	protected volatile static TreeMap<Link,Output> unusedOutputs;
//	protected volatile static TreeSet < Transaction > pendingTransactions;
//	protected volatile static BlockChain ledger;
//	protected volatile static ArrayList<String> Peers;
//	protected volatile static int peerLimit=5;
//	protected volatile static boolean pTb=true;
//	protected volatile static boolean uOb=true;
//	protected volatile  static boolean b=true;//Rename this 
//	protected volatile static PublicKey lock;
//	protected volatile static PublicKey unlock;
//	protected volatile static long BLOCK_REWARD;
//	protected volatile static TreeMap<String, TreeSet< Link >  > Mapping;
//	protected volatile static TreeMap<String,String> Alias;
//	public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidInputException, InvalidOutputException, InvalidKeyException, InvalidAlgorithmParameterException, SignatureException {
//		//TODO lead keys from pem file :))
//		Scanner sc=new Scanner(System.in);
//		//TODO should I use UTF-8 OR UTF-16 for pending transaction
//		//Update BlockChain
//		//TODO change backlog value here//Listening to port 8000 on local host
//		String dir=sc.nextLine();
//		String target=sc.nextLine();
//		ledger=new BlockChain(dir,target);
//		pendingTransactions=new TreeSet<Transaction>();
//		unusedOutputs=new TreeMap<Link,Output>();
//		Peers=new ArrayList<String>();
//		Mapping=new TreeMap<String , TreeSet<Link > >();
//		Alias=new TreeMap<String, String>();
//		BLOCK_REWARD=10;
//		String address="";//Input your own address after tunneling via ngrok
//		address=sc.nextLine();
//		String OriginalPeer="";//Input initial peers//TODO add optionally functionality to input more than 1 address
//		OriginalPeer=sc.nextLine();
//		if(OriginalPeer.charAt(OriginalPeer.length()-1)=='/')
//			OriginalPeer=OriginalPeer.substring(0,OriginalPeer.length()-1);
//		fetchPeers(OriginalPeer,address);//Verified
//		if(Peers.size()==0) {
//			System.out.println("Sedlyf u r alone");
//			System.exit(1);
//		}
//		fetchBlocks(OriginalPeer);
//		HttpServer server = HttpServer.create(new InetSocketAddress(8000),10);
//		HttpClient client=HttpClient.newHttpClient();
//		HttpRequest request = HttpRequest.newBuilder()
//				.uri(URI.create(OriginalPeer+"/getPendingTransactions"))
//				.build();
//		HttpResponse<String> response = client.send(request,
//				HttpResponse.BodyHandlers.ofString());
//		parseTransaction[] pendingTemp=(new Gson().fromJson(response.body(), parseTransaction[].class));
//		for(parseTransaction pt:pendingTemp) {
//			Transaction tok=pt.getRealTransaction();
//			if(verifyTransaction(tok))
//				modifyPT(tok,1);
//		}
//		System.out.println("Inititalisation Completed");
//		sc.close();
//		server.createContext("/getPendingTransaction",new PendingHandler());
//		server.createContext("/getBlock/",new BlockSender());
//		server.createContext("/newPeer",new PeerAdder());
//		server.createContext("/getPeers",new PeerSender());
//		server.createContext("/newBlock",new MindTheBlock());
//		server.createContext("/newTransaction",new TransactionReciever());
//		server.createContext("/addAlias",new AliasAdder());
//		server.createContext("/getPublicKey",new PublicKeyFetcher());
//		server.createContext("/getUnusedOutputs",new UnusedOutputsFetcher());
//		Thread t=new Thread(new Miner());
//		t.start();
//	}
//	protected static void fetchBlocks(String s) throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidInputException, InvalidOutputException, InvalidKeyException, InvalidAlgorithmParameterException, SignatureException {
//		HttpClient client = HttpClient.newHttpClient();
//		int ind=0;
//		if(s.endsWith("/"))
//			s=s.substring(0,s.length()-1);
//		while(true) {
//			HttpRequest request = HttpRequest.newBuilder()
//					.uri(URI.create(s+"/getBlock/"+Integer.toString(ind)))
//					.build();
//			HttpResponse<byte[]> response = client.send(request,
//					HttpResponse.BodyHandlers.ofByteArray());
//			if(response.statusCode()!=200)
//				break;
//			byte[] binarydata=response.body();
//			Block b=new Block(binarydata);
//			if(verifyBlock(b)){
//				removeTrans(b);
//				removeIaddO(b);
//				modifyLedger(b,1);
//			}
//			else {//TODO ADD LOGGER MESSAGE
//				removeTrans(b);
//				removeIaddO(b);
//				modifyLedger(b,1);
//			}
//			ind++;
//		}
//	}
//	protected static synchronized void removeIaddO(Block b) throws IOException {
//		Transaction arr[]=b.getTransactions();
//		for(Transaction t:arr) {
//			Input inp=t.getInput();
//			int n=0;
//			if(inp!=null)
//				n=inp.getNumberofInputs();
//			for(int i=0;i<n;i++) {
//				modifyUO(new Link(inp.getID(i),inp.getOutputIndex(i)),null,-1);
//			}
//			Output op=t.getOutput();
//			for(int i=0;i<op.getOutputs();i++) {
//				System.out.print(t.getTID()+" ");
//				System.out.println(i);
//				modifyUO(new Link(t.getTID(),i), op,1);
//			}
//		}
//	}
//	protected static void fetchPeers(String s,String add) throws IOException, InterruptedException {
//		// Function to add peers quite similiar to dfs :-)
//		TreeSet<String> covered=new TreeSet<>();
//		TreeSet<String> potential=new TreeSet<>();
//		potential.add(s);
//		covered.add(s);
//		HttpClient client = HttpClient.newHttpClient();
//		StringWriter sw=new StringWriter();
//		JsonWriter writer=new JsonWriter(sw);
//		writer.beginObject();writer.name("url");writer.value(add);writer.endObject();writer.flush();//writing json file to send my own url to peers so he can add my address in her/his own peers list
//		String json=sw.toString();
//		writer.close();
//		sw.close();
//		while(!potential.isEmpty()&&Peers.size()<peerLimit) {
//			String curr=potential.first();
//			try{
//				if(!curr.contentEquals(add)){
//					HttpRequest request = HttpRequest.newBuilder()
//							.uri(URI.create(curr+"/newPeer"))
//							.header("Content-Type", "application/json")
//							.POST(HttpRequest.BodyPublishers.ofString(json))
//							.build();//TODO test this for sending json as String O TEST need t JS AND python code
//					HttpResponse<String> response = client.send(request,
//							HttpResponse.BodyHandlers.ofString());//TODO check if UTF-8 works
//					if(response.statusCode()==200) {
//						System.out.println("Peers:-"+curr);
//						modifyPeer(curr,1);
//					}
//					request=HttpRequest.newBuilder()
//							.uri(URI.create(curr+"/getPeers"))
//							.build();
//					response = client.send(request,
//							HttpResponse.BodyHandlers.ofString());
//					Gson gson=new Gson();
//					if(response.statusCode()==200)
//					{
//						Map<? , ?> map=gson.fromJson(response.body(), Map.class);
//						for (Map.Entry<?, ?> entry : map.entrySet()) {
//							@SuppressWarnings("unchecked")
//							ArrayList<String> arr=(ArrayList< String >)(entry.getValue());
//							for(int i=0;i<arr.size();i++) {
//								String now=arr.get(i);
//								if(now.endsWith("/"))now=now.substring(0,now.length()-1);
//								if(!covered.contains(now)) {
//									potential.add(now);
//									covered.add(now);
//								}
//							}
//						}
//					}
//				}
//			}
//			catch(Exception e) {
//			}
//			potential.remove(curr);
//		}
//	}
//	protected static synchronized boolean verifyTransaction(Transaction t) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException, IOException {
//		long inputcoins=0;
//		long outputcoins=0;//TODO negative number checks
//		Input input=t.getInput();
//		int n=input.getNumberofInputs();
//		TreeSet<Link> s=new TreeSet<>();
//		EncryptionClient enc=new EncryptionClient();
//		TreeMap<Link,Output> UOcopy=modifyUO(null,null,0);
//		byte[] arr=t.getOutput().getData();
//		MessageDigest md = MessageDigest.getInstance("SHA-256");  
//		byte[] hash=(md.digest(arr));
//		for(int i=0;i<n;i++) {
//			String key1=input.getID(i);
//			int val=input.getOutputIndex(i);
//			Link l=new Link(key1,val);
//			if(UOcopy.containsKey(l)){
//				if(s.contains(l))
//				{
//					System.out.println("here5");
//					return false;
//				}
//				else{
//					s.add(l);
//					Output p=UOcopy.get(l);//TODO take care of memory consumption
//					byte[] signingdata=new byte[68];
//					byte[] hash1=Util.parseHexToByte(key1);
//					for(int j=0;j<32;j++) {
//						signingdata[j]=hash1[j];
//					}
//					byte[] more=Util.toByte(val);
//					for(int j=0;j<4;j++) {
//						signingdata[32+j]=more[j];
//					}
//					for(int j=0;j<32;j++) {
//						signingdata[32+4+j]=hash[j];
//					}
//					if(!enc.verify(input.getSignature(i),Util.parseByteToHex(signingdata) , p.getKey(val))) {
//						System.out.println(Util.parseByteToHex(signingdata));
//						System.out.println(p.getKeyInString(val));
//						System.out.println(input.getSignature(i));
//						System.out.println("here6");
//						return false;
//					}
//
//					inputcoins+=p.getCoins(val);
//				}
//			}
//			else {
//				System.out.println("here4");
//				return false;
//			}
//		}
//		outputcoins+=t.getOutput().getCoins();
//		if(inputcoins<=outputcoins)
//			return true;
//		else 
//		{
//			System.out.println("here3");
//			return false;
//		}
//	}
//	protected static synchronized TreeSet<Transaction> modifyPT(Transaction target,int mode){ 
//		if(mode==1) {
//			pendingTransactions.add(target);
//			return null;
//		}
//		else if(mode==-1){
//			pendingTransactions.remove(target);
//			return null;
//		}
//		else{
//			TreeSet<Transaction> ts=new TreeSet<Transaction>();
//			for(Transaction t:pendingTransactions) {
//				ts.add(t);
//			}
//			return ts;
//		}
//	}
//	protected static synchronized TreeMap<Link, Output> modifyUO(Link l, Output o, int mode) throws IOException{
//		if(mode==1) {
//			String key=o.getKeyInString(l.b);
//			if(!Mapping.containsKey(key))
//				Mapping.put(key, new TreeSet<Link>() );
//			Mapping.get(key).add(l);
//			unusedOutputs.put(l, o);
//			return null;
//		}
//		else if(mode==-1) {
//			String key=unusedOutputs.get(l).getKeyInString(l.b);//fix this calling by null sometimes
//			Mapping.get(key).remove(l);
//			unusedOutputs.remove(l);
//			return null;
//		}
//		else {
//			TreeMap<Link, Output> tmp=new TreeMap<Link,Output>();
//			for(Map.Entry<Link,Output> entry : unusedOutputs.entrySet()) {
//				tmp.put(entry.getKey(),entry.getValue());
//			}
//			return tmp;
//		}
//	}
//	protected static synchronized Link modifyLedger(Block b,int mode) throws NoSuchAlgorithmException {
//		if(mode==1)
//		{
//			ledger.addBlock(b);
//			return null;
//		}
//		else {
//			if(ledger.getSize()==0)
//			{
//				String s="";
//				for(int i=0;i<64;i++) {
//					s+="0";
//				}
//				return new Link(ledger.getTarget()+s,ledger.getSize());
//			}
//			return new Link(ledger.getTarget()+ledger.getTop().getHash(),ledger.getSize());
//
//
//		}
//	}
//	protected static synchronized TreeMap<String, TreeSet< Link > > modifyMapping(String s,Link l,int mode){
//		if(mode==0) {
//			TreeMap<String, TreeSet< Link > > clone=new TreeMap<String, TreeSet< Link > >();
//			for(Map.Entry<String, TreeSet< Link > > entry : Mapping.entrySet()) {
//				@SuppressWarnings("unchecked")
//				TreeSet<Link> cl=(TreeSet<Link>)(entry.getValue().clone());
//				clone.put(entry.getKey(),cl);
//			}
//			return clone;
//		}
//		else {
//			Mapping.get(s).add(l);
//			return null;
//		}
//	}
//	@SuppressWarnings("unchecked")
//	protected static synchronized TreeMap<String, String> modifyAlias(String key,String val, int mode){
//		if(mode==0) {
//			return (TreeMap<String,String>)Alias.clone();
//		}
//		else {
//			if(!Alias.containsKey("key")) {
//				Alias.put(key,val);
//			}
//			return null;
//		}
//	}
//	protected static synchronized ArrayList<String > modifyPeer(String target,int mode){
//		if(mode==0)
//		{
//			ArrayList<String> copy=new ArrayList<String>();
//			for(String s:Peers) {
//				copy.add(s);
//			}
//			return copy;
//		}
//		else {
//			if(mode==1) {
//				if(Peers.size()<peerLimit)
//					Peers.add(target);
//				return null;
//			}
//			else {
//				Peers.remove(target);
//				return null;
//			}
//		}
//	}
//	static class Miner implements Runnable{
//		@Override
//		public void run() {
//			try {
//				//TODO verify algoritm more
//				System.out.println("Mining Begin");
//				MessageDigest md = MessageDigest.getInstance("SHA-256");
//				EncryptionClient enc=new EncryptionClient();
//				while(true) {
//					b=true;
//					Thread.sleep(2000);//TODO maybe modify this to get more Transactions in
//					ArrayList<Transaction> tees=new ArrayList<Transaction>();
//					int len=0;
//					long fees=0;
//					TreeSet<Link > s=new TreeSet<Link>();
//					TreeSet<Transaction> copy=modifyPT(null,0);
//					TreeMap<Link,Output> uOcopy=modifyUO(null,null,0);
//					long inputcoins=0,outputcoins=0;
//					for(Transaction t:copy) {
//						boolean bob=true;
//						outputcoins+=t.getOutput().getCoins();
//						TreeSet<Link> s_=new TreeSet<Link>();
//						Input inp=t.getInput();
//						for(int j=0;j<inp.getNumberofInputs();j++) {
//							String TID=inp.getID(j);
//							int index=inp.getOutputIndex(j);
//							if(!s.contains(new Link(TID,index))&&!s_.contains(new Link(TID,index))&&uOcopy.containsKey(new Link(TID,index))){
//								inputcoins+=uOcopy.get(new Link(TID,index)).getCoins();
//								s.add(new Link(TID,index));
//								s_.add(new Link(TID,index));//TODO verify if checkTransaction does it job properly
//							}
//							else{
//								bob=false;
//								break;
//							}
//						}
//						if(bob==true&&len+t.getSize()<=1e6){
//							tees.add(t);
//							fees+=outputcoins-inputcoins;
//						}
//						else {
//							for(Link l : s_) {
//								s.remove(l);	
//							}
//						}
//					}
//					if(tees.size()>0) {//TODO Add Coinbase Transactions
//						Transaction param[]=new Transaction[tees.size()+1];
//						Transaction mine=new Transaction(null,new Output(1,new long[] {fees+BLOCK_REWARD},new PublicKey[] {lock}));
//						param[0]=mine;
//						for(int i=0;i<tees.size();i++)
//							param[i+1]=tees.get(i);
//						Link l=modifyLedger(null,0);
//						Block newb=new Block(l.b,l.TID.substring(32,64),param,l.TID.substring(0,32));//TODO Verify if Hash length is 32
//						String targ=l.TID.substring(0,32);
//						byte head[]=newb.getHeader();
//						byte flag=0;
//						long non=0;
//						while(b==true&&flag==0) {
//							Instant now=Instant.now();
//							long time=now.getEpochSecond()*1000+((long)now.getNano());
//							byte[] curr=Util.toByte(time);
//							for(int i=0;i<8;i++)
//								head[4+32+32+32+i]=curr[i];
//							byte[] nonce=Util.toByte(non);
//							for(int i=0;i<8;i++)
//								head[4+32+32+32+8+i]=nonce[i];
//							byte[] currHash=md.digest(head);
//							if(Util.cmpHex(Util.parseByteToHex(currHash),targ)<0)
//							{
//								newb.setTimestamp(time);
//								newb.setNonce(non);
//								flag=1;
//							}
//							non++;
//						}
//						if(flag==1){  //later add condition for simultaneous mining and recieving
//							//TODO verify this part again
//							Server.modifyLedger(newb,1);
//							Server.removeTrans(newb);
//							Server.removeIaddO(newb);
//							Server.sendBlock(newb);
//						}
//					}
//				}
//			} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InterruptedException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvalidOutputException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//	static class TransactionReciever implements HttpHandler{
//		@Override
//		public void handle(HttpExchange t) throws IOException {
//			Gson gson=new Gson();
//			String json=new String(ByteStreams.toByteArray(t.getRequestBody()));
//			parseTransaction tmp=gson.fromJson(json, parseTransaction.class);
//			String response="Transaction Recieved";
//			try {
//				Transaction t_=tmp.getRealTransaction();
//				if(verifyTransaction(t_))
//				{
//					modifyPT(t_,1);
//					ArrayList<String > pers=modifyPeer(null,0);
//					for(String s:pers) {
//						
//					}
//				}
//
//			} catch (NoSuchAlgorithmException | IOException | InvalidOutputException | InvalidInputException | InvalidKeyException | InvalidAlgorithmParameterException | SignatureException e) {
//				e.printStackTrace();
//			}
//			t.getResponseHeaders().add("Content-Type","text/plain");//TODO verify if this works without setting Content-Type
//			t.sendResponseHeaders(200, response.length());
//			OutputStream os=t.getResponseBody();
//			os.write(response.getBytes());os.close();
//		}
//	}
//	static class MindTheBlock implements HttpHandler{
//		@Override
//		public void handle(HttpExchange t) throws IOException{
//			try {
//				byte[] data=ByteStreams.toByteArray(t.getRequestBody());//Test this guava code
//				Block newb=new Block(data);//may throw thousands of Exception but if no exception thrown Block has been parsed properly from data 
//				if(verifyBlock(newb)){
//					b=false;
//					modifyLedger(newb,1);
//					removeIaddO(newb);
//					removeTrans(newb);
//					sendBlock(newb);
//					String response="Done";//TODO Check is this works without setting content header
//					t.getResponseHeaders().add("Content-Type","text/plain");
//					t.sendResponseHeaders(200, response.length());
//					OutputStream os=t.getResponseBody();
//					os.write(response.getBytes());os.close();
//				}else {
//					String response="Jhutaaaaa";
//					t.getResponseHeaders().add("Content-Type","text/plain");
//					t.sendResponseHeaders(200, response.length());
//					OutputStream os=t.getResponseBody();
//					os.write(response.getBytes());os.close();
//				}
//			} catch (InvalidKeyException | NoSuchAlgorithmException | IndexOutOfBoundsException | SignatureException
//					| InvalidAlgorithmParameterException | ConcurrentModificationException | IOException
//					| InvalidInputException | InvalidOutputException e) {
//				System.out.println("Error Occured while recieving Mined Block.");
//				String response="GO on";
//				t.getResponseHeaders().add("Content-Type","text/plain");
//				t.sendResponseHeaders(200, response.length());
//				OutputStream os=t.getResponseBody();
//				os.write(response.getBytes());os.close();
//				e.printStackTrace();
//			}
//		}    
//	}
//
//	static class PeerSender implements HttpHandler{
//		@Override
//		public void handle(HttpExchange exchange) throws IOException {
//			StringWriter sw=new StringWriter();
//			JsonWriter writer=new JsonWriter(sw);
//			ArrayList<String> pers=modifyPeer(null,0);
//			writer.beginObject();writer.name("peers");writer.beginArray();
//			for(int i=0;i<pers.size();i++) {writer.value(pers.get(i));}
//			writer.endArray();writer.endObject();writer.flush();sw.flush();
//			String s=sw.toString();
//			writer.close();sw.close();
//			exchange.getResponseHeaders().add("Content-Type","application/json");//TODO check what this does actually
//			exchange.sendResponseHeaders(200, s.length());
//			OutputStream os=exchange.getResponseBody();
//			os.write(s.getBytes());os.close();
//		}  
//	}
//	static class PeerAdder implements HttpHandler{
//		@Override
//		public void handle(HttpExchange exchange) throws IOException {
//			ArrayList<String> pers=modifyPeer(null,0);
//			if(pers.size()>=peerLimit) {
//				String response="Connection Rejected";
//				OutputStream os=exchange.getResponseBody();
//				exchange.sendResponseHeaders(500, response.length());
//				os.write(response.getBytes());
//				os.close();
//			}
//			else {
//				Gson gson = new Gson();
//				Reader reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");//TODO why UTF-8
//				Map<?, ?> map = gson.fromJson(reader, Map.class);
//				for (Map.Entry<?, ?> entry : map.entrySet()) {
//					entry.getKey(); 
//					String s=(String)entry.getValue();
//					if(s.endsWith("/"))
//						s=s.substring(0,s.length()-1);
//					modifyPeer(s,1);
//				}
//				reader.close();
//				String response="Connection Accepted";
//				OutputStream os=exchange.getResponseBody();
//				exchange.sendResponseHeaders(200, response.length());
//				os.write(response.getBytes());
//				os.close();
//			}
//		}
//	}
//
//	static class PendingHandler implements HttpHandler{
//		@Override
//		public void handle(HttpExchange t) throws IOException {
//			StringWriter sw=new StringWriter();
//			JsonWriter writer=new JsonWriter(sw);
//			TreeSet<Transaction> PTcopy=modifyPT(null,0);
//			writer.beginArray();
//			for(Transaction curr:PTcopy) {
//				writer.beginObject();writer.name("inputs");writer.beginArray();
//				Input curri=curr.getInput();
//				for(int j=0;j<curri.getNumberofInputs();j++) {
//					writer.beginObject();writer.name("transactionID");writer.value(curri.getID(j));writer.name("index");writer.value(curri.getOutputIndex(j));writer.name("signature");writer.value(curri.getSignature(j));writer.endObject();
//				}
//				writer.endArray();writer.name("outputs");writer.beginArray();
//				Output currop=curr.getOutput();
//				for(int j=0;j<currop.getOutputs();j++) {
//					writer.beginObject();writer.name("amount");writer.value(currop.getCoins(j));writer.name("recipent");writer.value(currop.KeyToString(j));writer.endObject();
//				}
//				writer.endObject();
//			}
//			writer.endArray();writer.flush();
//			sw.flush();
//			String json=sw.toString();
//			sw.close();writer.close();
//			t.getResponseHeaders().add("Content-Type", "application/json");
//			t.sendResponseHeaders(200, json.length());
//			OutputStream os = t.getResponseBody();
//			os.write(json.getBytes());os.close();
//		} 
//	}
//
//	static class BlockSender implements HttpHandler{
//		@Override
//		public void handle(HttpExchange exchange) throws IOException {
//			String s=exchange.getRequestURI().toString();
//			if(s.endsWith("/"))
//				s=s.substring(0,s.length()-1);
//			try
//			{
//				int num=Integer.parseInt(s.substring(s.lastIndexOf("/")+1,s.length()));
//				Link l=modifyLedger(null,0);
//				if(l.b<=num) {
//					exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
//					exchange.sendResponseHeaders(404, "Get Lost".length());
//					OutputStream os = exchange.getResponseBody();
//					os.write("Get Lost".getBytes());os.close();
//				}else {
//					InputStream reader=new FileInputStream("Block"+num);
//					byte[] data=reader.readAllBytes();
//					exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
//					exchange.sendResponseHeaders(200, data.length);
//					OutputStream os = exchange.getResponseBody();
//					os.write(data);os.close();
//				}
//			}
//			catch(NumberFormatException | NoSuchAlgorithmException e) {
//				exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
//				exchange.sendResponseHeaders(404, "Get Lost".length());
//				OutputStream os = exchange.getResponseBody();
//				os.write("Get Lost".getBytes());os.close();
//			}
//		}
//	}
//	static class AliasAdder implements HttpHandler{
//		@Override
//		public void handle(HttpExchange exchange) throws IOException {
//			Gson gson = new Gson();
//			String json=new String(ByteStreams.toByteArray(exchange.getRequestBody()));
//			@SuppressWarnings("unchecked")
//			Map<String,String > map=gson.fromJson(json, Map.class);
//			String usrname=map.get("alias");
//			String key=map.get("publicKey");
//			TreeMap<String, String> alias=modifyAlias(null,null,0);//TODO fix these. It may lead to a lot of problems.
//			if(usrname!=null&&key!=null) {
//				if(alias.containsKey(usrname))
//				{
//					exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
//					exchange.sendResponseHeaders(400, "Already Done".length());
//					OutputStream os = exchange.getResponseBody();
//					os.write("Already Done".getBytes());os.close();
//				}
//				else {
//					modifyAlias(usrname, key,1);
//					exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
//					exchange.sendResponseHeaders(200, "Accepted".length());
//					OutputStream os = exchange.getResponseBody();
//					os.write("Accepted".getBytes());os.close();
//					ArrayList<String> pers=Server.modifyPeer(null,0);
//					HttpClient client=HttpClient.newHttpClient();
//					for(String s:pers) {
//						HttpRequest request = HttpRequest.newBuilder()
//								.uri(URI.create(s+"/addAlias"))
//								.header("Content-Type", "application/json")
//								.POST(HttpRequest.BodyPublishers.ofString(json))
//								.build();//TODO test this out
//						try {
//							HttpResponse<String> response = client.send(request,
//									HttpResponse.BodyHandlers.ofString());//Test BodyHandlers
//						} catch (IOException e) {
//							e.printStackTrace();
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}	
//					}
//
//				}
//			}
//			else {
//				exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
//				exchange.sendResponseHeaders(404, "Off".length());
//				OutputStream os = exchange.getResponseBody();
//				os.write("Off".getBytes());os.close();
//			}
//		} 
//
//	}
//	static class PublicKeyFetcher implements HttpHandler{
//		@Override
//		public void handle(HttpExchange exchange) throws IOException {
//			Gson gson = new Gson();
//			String json=new String(ByteStreams.toByteArray(exchange.getRequestBody()));
//			Map<String,String > map=gson.fromJson(json, Map.class);
//			String usrname=map.get("alias");//Test this
//			TreeMap<String, String> alias=modifyAlias(null,null,0);
//			if(usrname!=null&&alias.containsKey(usrname)) {
//				StringWriter sw=new StringWriter();
//				JsonWriter writer=new JsonWriter(sw);
//				writer.beginObject();
//				writer.name("PublicKey");
//				writer.value(alias.get(usrname));
//				writer.endObject();
//				writer.flush();
//				sw.flush();
//				String js=sw.toString();
//				writer.close();
//				sw.close();
//				exchange.getResponseHeaders().add("Content-Type", "application/json");
//				exchange.sendResponseHeaders(200,js.length());
//				OutputStream os = exchange.getResponseBody();
//				os.write(js.getBytes());os.close();
//			}
//			else{
//				exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
//				exchange.sendResponseHeaders(404, "Off".length());
//				OutputStream os = exchange.getResponseBody();
//				os.write("Off".getBytes());os.close();
//			}
//		}
//
//	}
//	static class UnusedOutputsFetcher implements HttpHandler{
//		@Override
//		public void handle(HttpExchange exchange) throws IOException {
//			Gson gson = new Gson();
//			String json=new String(ByteStreams.toByteArray(exchange.getRequestBody()));
//			@SuppressWarnings("unchecked")//replace with exception
//			Map<String,String > map=gson.fromJson(json, Map.class);
//			String pkey=map.get("publicKey");
//			String usrname=map.get("alias");
//			TreeMap<String, String> alias=modifyAlias(null,null,0);
//			TreeMap<String, TreeSet<Link> > mapping=modifyMapping(null,null,0);
//			if(pkey!=null||usrname!=null) {
//				int f=1;
//				if(pkey==null) {
//					if(alias.containsKey(usrname))
//						pkey=alias.get(usrname);
//					else
//						f=0;
//					if(!mapping.containsKey(pkey))
//						f=0;
//				}
//				if(f==1) {
//					StringWriter sw=new StringWriter();
//					JsonWriter writer=new JsonWriter(sw);
//					writer.beginObject();
//					writer.name("unusedOutputs");
//					writer.beginArray();
//					TreeSet<Link> ts=mapping.get(pkey);
//					TreeMap<Link,Output> UO=modifyUO(null,null,0);
//					for(Link l:ts) {
//						writer.beginObject();
//						writer.name("transactionId");
//						writer.value(l.TID);
//						writer.name("index");
//						writer.value(l.b);
//						writer.name("amount");
//						writer.value(UO.get(l).getCoins());//Manage Exception here
//						writer.endObject();
//					}
//					writer.endArray();
//					writer.endObject();
//					writer.flush();
//					sw.flush();
//					String js=sw.toString();
//					exchange.getResponseHeaders().add("Content-Type", "application/json");
//					exchange.sendResponseHeaders(200, js.length());
//					OutputStream os=exchange.getResponseBody();
//					os.write(js.getBytes());
//					os.close();
//				}
//				else {
//					exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
//					exchange.sendResponseHeaders(404, "Off".length());
//					OutputStream os = exchange.getResponseBody();
//					os.write("Off".getBytes());os.close();
//				}
//
//			}
//			else {
//				exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
//				exchange.sendResponseHeaders(404, "Off".length());
//				OutputStream os = exchange.getResponseBody();
//				os.write("Off".getBytes());os.close();
//			}
//		}
//	}
//	protected static synchronized void removeTrans(Block b){
//		Transaction[] arr=b.getTransactions(); 
//		for(Transaction t:arr) {
//			modifyPT(t,-1);
//		}
//	}
//	protected static synchronized void sendBlock(Block b) {
//		ArrayList<String> listOfPeers=modifyPeer(null,0);
//		HttpClient client=HttpClient.newHttpClient();
//		for(String s:listOfPeers) {
//			HttpRequest request = HttpRequest.newBuilder()
//					.uri(URI.create(s+"/newBlock"))
//					.header("Content-Type", "application/octet-stream")
//					.POST(HttpRequest.BodyPublishers.ofByteArray(b.getData()))
//					.build();//TODO test this out
//			try {
//				HttpResponse<String> response = client.send(request,
//						HttpResponse.BodyHandlers.ofString());//TODO Check that here I recieve output as String
//				System.out.println(response.body()+": "+s);
//			} catch (IOException | InterruptedException e) {
//				System.out.println("Connection Interrupted / IOException sending to Peer: "+s);
//				e.printStackTrace();
//			}
//		}
//	}
//	protected static synchronized boolean verifyBlock(Block b) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException, IOException {
//		//TODO separate case of Block 0
//		//NOTE any updaes on Block Verification can be made here
//		int ind=b.getIndex();
//		if(!b.getbah())
//			return false;
//		b.getReceipt();
//		System.out.println(b.getIndex());
//		TreeSet<Link> s=new TreeSet<Link>();
//		Transaction arr[]=b.getTransactions();
//		//TODO verify coinbase transaction
//		TreeMap<Link,Output> UOcopy=modifyUO(null,null,0);
//		for(int i=1;i<arr.length;i++) {
//			Transaction t=arr[i];
//			if(!verifyTransaction(t)){
//				System.out.println("here1");
//				return false;
//			}
//			Input inp=t.getInput();
//			Output op=t.getOutput();
//			for(int j=0;j<inp.getNumberofInputs();j++) {
//				Link l=new Link(inp.getID(j),inp.getOutputIndex(j));
//				if(s.contains(l)||!UOcopy.containsKey(l)) {
//					System.out.println("here2");
//					return false;
//				}
//				else {
//					s.add(l);
//				}
//			}
//			//TODO Verify Output
//		}
//		System.out.println("here");
//		Link l=modifyLedger(null,0);
//		/*
//		 * Verify modifyLedger(null,0)
//		 * and following part
//		 */
//		if(b.getIndex()!=l.b)
//			return false;
//		if(b.getPhash().compareTo(l.TID.substring(64,128))!=0)//TODO check substring size//TODO exception of Block 0
//			return false;
//		if(b.getTarget().compareTo(l.TID.substring(0,64))!=0)
//			return false;
//		return true;
//	}
//
//}
package com.BlockChain.Node;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import com.Util;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class DummyServer {
	private volatile static TreeMap<Link,Output> unusedOutputs;
	private volatile static TreeSet < Transaction > pendingTransactions;
	private volatile static BlockChain ledger;
	private volatile static ArrayList<String> peers;
	private static int peerLimit;
	private static PrivateKey lock;
	private static PublicKey unlock;//TODO test if problem does not occur
	private static long blockReward;
	private volatile static TreeMap<String, TreeSet< Link >  > mapping;
	private volatile static TreeMap<String,String> alias;
	private static String target;
	private static String address;
	private static String originalPeer;
	private static String dir;
	private static ArrayList<String > blacklisted;
	private static volatile boolean b=false;
	private static String unlockS;
	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		Gson gson=new Gson();
		String filename="config.json";
		String json="";
		try {
			json=new String(Files.readAllBytes(Paths.get(filename)));
		} catch (IOException e) {
			System.out.println("Config File not found");
			System.exit(1);
		}
		try {
			@SuppressWarnings("unchecked")
			Map<String,String> keyValue=gson.fromJson(json, Map.class);
			peerLimit=Integer.parseInt(keyValue.get("peerLimit"));
			StringReader sr=new StringReader(keyValue.get("publicKey"));
			PemReader r2=new PemReader(sr);
			PemObject pemObject=r2.readPemObject();
			unlock=Util.getPublicKey(pemObject.getContent(),"RSA");
			r2.close();
			StringReader sr2=new StringReader(keyValue.get("privateKey"));
			PEMParser pemParser = new PEMParser(sr2);
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			Object object = pemParser.readObject();
			KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
			unlockS=keyValue.get("publicKey");
			String key2=new String(Files.readAllBytes(Paths.get("public (4).pem")));
			for(int i=0;i<key2.length();i++) {
				if(key2.charAt(i)!=unlockS.charAt(i)) {
					System.out.println(key2.charAt(i));
					System.out.println(unlockS.charAt(i));
					System.exit(1);
				}
			}
			lock = kp.getPrivate();
			pemParser.close();
			blockReward=Long.parseLong(keyValue.get("blockReward"));
			target=keyValue.get("target");
			dir=keyValue.get("dir");
			address=keyValue.get("address");
			originalPeer=keyValue.get("originalPeer");
			System.out.println(unlockS);
		}
		catch(ClassCastException e) {
			System.out.println("Invalid json format");System.exit(1);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Some error occured parsing config file");System.exit(1);
		}
		unusedOutputs=new TreeMap<Link,Output>();
		pendingTransactions=new TreeSet<Transaction>();
		ledger=new BlockChain(dir,target); 
		peers=new ArrayList<String>();
		mapping=new TreeMap<String,TreeSet<Link> >();
		alias=new TreeMap<String,String>();
		blacklisted=new ArrayList<String>();
		blacklisted.add("http://f443e7cb40cc.ngrok.io");
		try {
			if(originalPeer.charAt(originalPeer.length()-1)=='/')
				originalPeer=originalPeer.substring(0,originalPeer.length()-1);
			if(address.charAt(address.length()-1)=='/')
				address=address.substring(0,address.length()-1);
			fetchPeers();
			fetchBlocks();
			fetchPendingTransactions();
			HttpServer server=HttpServer.create(new InetSocketAddress(8000),4);
			System.out.println("Initialisation Successfull");
			server.createContext("/getPendingTransaction",new PendingHandler());
			server.createContext("/getBlock",new BlockSender());
			server.createContext("/newPeer",new PeerAdder());
			server.createContext("/getPeers",new PeerSender());
			server.createContext("/newBlock",new MindTheBlock());
			server.createContext("/newTransaction",new TransactionReciever());
			server.createContext("/addAlias",new AliasAdder());
			server.createContext("/getPublicKey",new PublicKeyFetcher());
			server.createContext("/getUnusedOutputs",new UnusedOutputsFetcher());
			server.start();
			TreeSet<Link> s=null;
			synchronized(mapping) {
				s=mapping.get(unlockS);
			}
			long total=0;
			synchronized(unusedOutputs)
			{
				for(Link l:s) {
					total+=unusedOutputs.get(l).getCoins(l.b);
				}
				System.out.println("My coins"+ total);
			}

			Thread t=new Thread(new Miner());
			t.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	protected static void fetchPendingTransactions() {
		Gson gson=new Gson();
		HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
		HttpRequest request=HttpRequest.newBuilder()
				.uri(URI.create(originalPeer+"/getPendingTransactions"))
				.build();
		try {
			HttpResponse<String> response=client.send(request, HttpResponse.BodyHandlers.ofString());
			String json=response.body();
			System.out.println(json);
			parseTransaction[] pends=gson.fromJson(json, parseTransaction[].class);
			synchronized(pendingTransactions) {
				for(parseTransaction t:pends)     {
					try
					{
						Transaction t_=t.getRealTransaction();
						if(verifyTransaction(t_)) {
							pendingTransactions.add(t_);
						}
					}
					catch(InvalidInputException | NoSuchAlgorithmException | InvalidOutputException e) {

					} 
				}
			}
		} catch (IOException e) {
			System.exit(1);
		} catch (InterruptedException e) {
			System.exit(1);
		} 

	}
	protected static void fetchPeers() {
		// Function to add peers quite similiar to dfs :-)
		try
		{
			TreeSet<String> covered=new TreeSet<>();
			TreeSet<String> potential=new TreeSet<>();
			potential.add(originalPeer);
			covered.add(originalPeer);
			StringWriter sw=new StringWriter();
			JsonWriter writer=new JsonWriter(sw);
			writer.beginObject();writer.name("url");writer.value(address);writer.endObject();writer.flush();//writing json file to send my own url to peers so he can add my address in her/his own peers list
			String json=sw.toString();
			writer.close();
			sw.close();
			HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
			synchronized(peers)
			{
				while(!potential.isEmpty()&&peers.size()<peerLimit) {
					String curr=potential.first();
					try{
						if(!curr.contentEquals(address))
						{
							HttpRequest request = HttpRequest.newBuilder()
									.uri(URI.create(curr+"/newPeer"))
									.header("Content-Type", "application/json")
									.POST(HttpRequest.BodyPublishers.ofString(json))
									.build();//TODO test this for sending json as String O TEST need t JS AND python code
							System.out.println("Trying to add "+curr);
							HttpResponse<String> response = client.send(request,
									HttpResponse.BodyHandlers.ofString());//TODO check if UTF-8 works
							if(response.statusCode()==200) {
								System.out.println("Peers:-"+curr);
								peers.add(curr);
							}
							request=HttpRequest.newBuilder()
									.uri(URI.create(curr+"/getPeers"))
									.build();
							System.out.println("Trying to ask "+curr);
							response = client.send(request,
									HttpResponse.BodyHandlers.ofString());
							Gson gson=new Gson();
							if(response.statusCode()==200)
							{
								@SuppressWarnings("unchecked")
								Map<String , ArrayList<String> > map=gson.fromJson(response.body(), Map.class);
								ArrayList<String> arr=map.get("peers");
								for(int i=0;i<arr.size();i++) {
									String now=arr.get(i);
									if(now.endsWith("/"))now=now.substring(0,now.length()-1);
									if(!covered.contains(now)&&!now.contentEquals(address)&&!blacklisted.contains(now)) {
										potential.add(now);
										covered.add(now);
									}
								}
							}
						}
					}
					catch(InterruptedException e) {
						System.out.println("Connextion Problem to"+curr);
					}
					catch(IOException e) {
						System.out.println("Unexpected Error");
					}
					catch(ClassCastException e) {
						System.out.println("Casting problems occured");
					}
					catch(NullPointerException e) {
						System.out.println("Null Pointer Exception occured");
					}
					catch(IllegalArgumentException e) {

					}
					potential.remove(curr);
				}
			}
		}
		catch(IOException e) {
			System.out.println("Stream problem occured");
			System.exit(1);
		}
	}
	protected static void fetchBlocks() {
		HttpClient client=HttpClient.newHttpClient();
		int ind=0;
		while(true) {
			try
			{
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(originalPeer+"/getBlock/"+Integer.toString(ind)))
						.build();
				HttpResponse<byte[]> response = client.send(request,
						HttpResponse.BodyHandlers.ofByteArray());
				if(response.statusCode()==404)
					break;
				else if(response.statusCode()!=200) {
					System.exit(1);
				}
				else {
					byte binarydata[]=response.body();
					Block b;
					try {
						b = new Block(binarydata);
						if(verifyBlock(b)) {
							removeTrans(b);
							removeIAddO(b);
							synchronized(ledger) {
								ledger.addBlock(b);
							}
						}
						else {
							System.out.println("Origianl Peer has lied");
							System.exit(1);
						}
					} catch (NoSuchAlgorithmException | IndexOutOfBoundsException | InvalidInputException
							| InvalidOutputException e) {

					}
				}
				ind++;
			}
			catch(InterruptedException e) {
				System.out.println("Interrupted connextion Occured");
			}
			catch(IOException e) {
			}
		}
	}
	protected static synchronized boolean verifyBlock(Block b) {
		if(!b.getbah())
			return false;
		TreeSet<Link> s=new TreeSet<Link>();
		Transaction arr[]=b.getTransactions();
		synchronized(unusedOutputs){
			long inputcoins=0;
			long outputcoins=0;
			for(int i=1;i<arr.length;i++) {
				Transaction t=arr[i];
				if(!verifyTransaction(t)) {
					System.out.println("A transaction has failed to be verified.");
					return false; 
				}
				Input inp=t.getInput();
				Output op=t.getOutput();
				for(int j=0;j<inp.getNumberofInputs();j++) {
					Link l=new Link(inp.getID(j),inp.getOutputIndex(j));
					inputcoins+=unusedOutputs.get(l).getCoins(inp.getOutputIndex(j));
					if(s.contains(l)||!unusedOutputs.containsKey(l)) {
						System.out.println("Repeated Output or given output is not unused.");
						return false;
					}
					else {
						s.add(l);
					}
				}
				outputcoins+=op.getCoins();
			}
			//verify coinbase
		}
		synchronized(ledger) {

			if(b.getIndex()!=ledger.getSize()){
				System.out.println("Ledger Index does not match up.");
				return false;
			}
			if(b.getPhash().compareTo(ledger.getTopHash())!=0){
				System.out.println("Ledger Parent Hash does dot match up");
				ledger.getTop().getReceipt();
				return false;
			}
			if(b.getTarget().compareTo(ledger.getTarget())!=0){
				System.out.println("Block target is different from ledger Hash.");
				return false;
			}
		}
		System.out.println("Block passed succesfully");
		System.out.println("Block Index "+b.getIndex());
		return true;
	}
	protected static synchronized  void removeTrans(Block b) {
		synchronized(pendingTransactions) {
			Transaction arr[]=b.getTransactions();
			for(Transaction t:arr) {
				pendingTransactions.remove(t);
			}
		}
	}
	protected static synchronized void sendBlocks(Block b) throws NoSuchAlgorithmException, IndexOutOfBoundsException, InvalidInputException, IOException, InvalidOutputException {
		HttpClient client=HttpClient.newHttpClient();//TODO remove errors
		byte[] bob=Util.append(b.getHeader(), b.getData());
		synchronized(peers)
		{
			for(String s:peers) {

				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(s+"/newBlock"))
						.header("Content-Type", "application/octet-stream")
						.POST(HttpRequest.BodyPublishers.ofByteArray(bob))
						.build();
				try {
					System.out.println("Sending Block to "+s);
					HttpResponse<String> response=client.send(request,
							HttpResponse.BodyHandlers.ofString());//TODO Check that here I recieve output as String
					System.out.println("Response Code :- "+response.statusCode());
				} catch (IOException | InterruptedException e) {
					System.out.println("Connection Interrupted / IOException sending to Peer: "+s);
					e.printStackTrace();
				}
			}
		}

	}
	protected static synchronized void removeIAddO(Block b) {
		Transaction arr[]=b.getTransactions();
		synchronized(unusedOutputs) {
			for(Transaction t:arr) {
				Input inp=t.getInput();
				int n=inp.getNumberofInputs();
				for(int i=0;i<n;i++) {
					Link key=new Link(inp.getID(i), inp.getOutputIndex(i));
					Output op=unusedOutputs.get(key);
					try
					{
						synchronized(mapping) {
							mapping.get(op.getKeyInString(key.b)).remove(key);
							if(op.getKeyInString(key.b).contentEquals(unlockS)) {
								System.out.println("-"+unusedOutputs.get(key).getCoins(key.b));
							}
						}
					}
					catch(IOException e) {
						System.out.println("Application will not work now.Fatal flow dealing with alias");
					}
					unusedOutputs.remove(key);
				}
				Output op=t.getOutput();
				for(int i=0;i<op.getOutputs();i++) {
					synchronized(mapping){
						try
						{
							String key=op.getKeyInString(i);
							if(!mapping.containsKey(key)) {
								mapping.put(op.getKeyInString(i),new TreeSet<Link>());
							}
							mapping.get(key).add(new Link(t.getTID(),i));
							if(key.contentEquals(unlockS)) {
								System.out.println("+"+op.getCoins(i));
							}
						}
						catch(IOException e) {
							System.out.println("Application will not work now.Fatal flow dealing with alias");
						}
					}
					unusedOutputs.put(new Link(t.getTID(),i),op);
				}
			}
		}
	}
	protected static synchronized boolean verifyTransactionl(Transaction t) {
		synchronized(unusedOutputs) {
			return verifyTransaction(t);
		}
	}
	protected static synchronized boolean verifyTransaction(Transaction t) {
		if(t==null)
		{
			return false;
		}
		long inputcoins=0;
		long outputcoins=0;
		try
		{
			MessageDigest md=MessageDigest.getInstance("SHA-256");
			EncryptionClient enc=new EncryptionClient();
			Input input=t.getInput();
			if(input==null)
			{
				System.out.println("Transaction Invalid Input null/");
				return false;
			}
			int n=input.getNumberofInputs();
			TreeSet<Link> s=new TreeSet<>();
			if(t.getOutput()==null)
			{System.out.println("Transactionn Invalid Output null");
			return false;
			}
			byte[] arr=t.getOutput().getData();
			byte[] hash=(md.digest(arr));
			for(int i=0;i<n;i++) {
				String key1=input.getID(i);
				int val=input.getOutputIndex(i);
				Link l=new Link(key1,val);
				if(unusedOutputs.containsKey(l)){
					if(s.contains(l)){
						System.out.println("Repeated Output used");
						return false;
					}
					else{
						s.add(l);
						Output p=unusedOutputs.get(l);//TODO take care of memory consumption
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
						try {
							if(!enc.verify(input.getSignature(i),Util.parseByteToHex(signingdata) , p.getKey(val))) {
								System.out.println("Signature verification failed.");
								return false;
							}
						} catch (InvalidKeyException e) {
							System.out.println("Key is of Inavlaid Format.");
							return false;
						} catch (SignatureException e) {
						} catch (UnsupportedEncodingException e) {
						}
						inputcoins+=p.getCoins(val);
					}
				}
				else {
					System.out.println("Output not present initally in unusedOutputs.");
					return false;
				}
			}
			outputcoins+=t.getOutput().getCoins();
			if(inputcoins>=outputcoins)
				return true;
			else {
				System.out.println("Output coins exceeed Input coins.");
				return false;
			}
		}
		catch(Exception e) {
			System.out.println("unexpected error");
			return false;
		}
	}
	static class TransactionReciever implements HttpHandler{
		@Override
		public void handle(HttpExchange t) throws IOException {
			Gson gson=new Gson();
			try
			{
				System.out.println("new Transaction recieved");
				HttpClient client=HttpClient.newHttpClient();
				String json=new String(ByteStreams.toByteArray(t.getRequestBody()));
				parseTransaction tmp=gson.fromJson(json, parseTransaction.class);
				Transaction t_=tmp.getRealTransaction();
				if(verifyTransactionl(t_)) {
					synchronized(pendingTransactions) {
						System.out.println("Just recieved Incoming Transaction.");
						if(!pendingTransactions.contains(t_))
						{
							pendingTransactions.add(t_);
							synchronized(peers) {
								for(String s:peers) {
									HttpRequest request=HttpRequest.newBuilder()
											.uri(URI.create(s+"/newTransaction"))
											.header("Content-Type", "application/json")
											.POST(HttpRequest.BodyPublishers.ofString(json))
											.build();
									try {
										client.send(request,
												HttpResponse.BodyHandlers.ofString());
									} catch (InterruptedException e) {
										System.out.println("Connection Problem to Peer "+s);
									}
								}
							}
						}
						else {
							System.out.println("Transaction previously present");
						}
					}
				}
			}
			catch(IOException e) {
			}catch(JsonSyntaxException e) {
			} catch (NoSuchAlgorithmException e) {
			} catch (InvalidOutputException e) {
			} catch (InvalidInputException e) {
			}
			String response="Recieved";
			t.getResponseHeaders().add("Content-Type","text/plain");//TODO send  appopriate message here
			t.sendResponseHeaders(200, response.length());
			OutputStream os=t.getResponseBody();
			os.write(response.getBytes());os.close();
		}
	}
	static class AliasAdder implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Gson gson = new Gson();
			String json=new String(ByteStreams.toByteArray(exchange.getRequestBody()));
			try
			{
				@SuppressWarnings("unchecked")
				Map<String,String > map=gson.fromJson(json, Map.class);
				String usrname=map.get("alias");
				String key=map.get("publicKey");
				if(usrname!=null&&key!=null) {
					synchronized(alias)
					{
						if(alias.containsKey(usrname))
						{
							exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
							exchange.sendResponseHeaders(400, "Already Done".length());
							OutputStream os = exchange.getResponseBody();
							os.write("Already Done".getBytes());os.close();
						}
						else {
							System.out.println("Recieved Alias :- "+usrname+":"+key);
							alias.put(usrname,key);
							exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
							exchange.sendResponseHeaders(200, "Accepted".length());
							OutputStream os = exchange.getResponseBody();
							os.write("Accepted".getBytes());os.close();		
						}
					}
				}
				else {
					exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
					exchange.sendResponseHeaders(400, "Off".length());
					OutputStream os = exchange.getResponseBody();
					os.write("Off".getBytes());os.close();
				}
			}
			catch(ClassCastException e) {
				exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
				exchange.sendResponseHeaders(400, "Off".length());
				OutputStream os = exchange.getResponseBody();
				os.write("Off".getBytes());os.close();
			}
		} 

	}
	static class PublicKeyFetcher implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Gson gson = new Gson();
			String json=new String(ByteStreams.toByteArray(exchange.getRequestBody()));
			System.out.println(json);
			try
			{
				@SuppressWarnings("unchecked")
				Map<String,String > map=gson.fromJson(json, Map.class);
				String usrname=map.get("alias");
				synchronized(alias)
				{
					if(usrname!=null&&alias.containsKey(usrname)) {
						StringWriter sw=new StringWriter();
						JsonWriter writer=new JsonWriter(sw);
						writer.beginObject();writer.name("publicKey");writer.value(alias.get(usrname));writer.endObject();writer.flush();sw.flush();
						String js=sw.toString();
						writer.close();
						sw.close();
						exchange.getResponseHeaders().add("Content-Type", "application/json");
						exchange.sendResponseHeaders(200,js.length());
						OutputStream os = exchange.getResponseBody();
						os.write(js.getBytes());os.close();
					}
					else{
						exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
						exchange.sendResponseHeaders(400, "Off".length());
						OutputStream os = exchange.getResponseBody();
						os.write("Off".getBytes());os.close();
					}
				}
			}
			catch(ClassCastException e) {
				exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
				exchange.sendResponseHeaders(400, "Off".length());
				OutputStream os = exchange.getResponseBody();
				os.write("Off".getBytes());os.close();
			}
		}
	}
	static class UnusedOutputsFetcher implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("Here man");
			Gson gson = new Gson();
			String json=new String(ByteStreams.toByteArray(exchange.getRequestBody()));
			System.out.println(json);
			try
			{
				@SuppressWarnings("unchecked")
				Map<String,String > map=gson.fromJson(json, Map.class);
				String pkey=map.get("publicKey");
				String usrname=map.get("alias");
				if(pkey!=null||usrname!=null) {
					int f=1;
					synchronized(alias)
					{
						if(pkey==null) {
							if(alias.containsKey(usrname))
								pkey=alias.get(usrname);
							else
							{
								f=0;
							}
							synchronized(mapping)
							{
								if(!mapping.containsKey(pkey))
									f=0;
							}
						}
					}
					if(f==1) {
						System.out.println("Sending the unusedOutputs");
						StringWriter sw=new StringWriter();
						JsonWriter writer=new JsonWriter(sw);
						writer.beginObject();
						writer.name("unusedOutputs");
						writer.beginArray();
						TreeSet<Link> ts=null;
						synchronized(mapping)
						{
							ts=mapping.get(pkey);
						}
						synchronized(unusedOutputs)
						{
							for(Link l:ts) {
								writer.beginObject();
								writer.name("transactionId");
								writer.value(l.TID);
								writer.name("index");
								writer.value(l.b);
								writer.name("amount");
								writer.value(unusedOutputs.get(l).getCoins(l.b));//Manage Exception here
								writer.endObject();
							}
						}
						writer.endArray();
						writer.endObject();
						writer.flush();
						sw.flush();
						String js=sw.toString();
						writer.close();
						sw.close();
						exchange.getResponseHeaders().add("Content-Type", "application/json");
						exchange.sendResponseHeaders(200, js.length());
						OutputStream os=exchange.getResponseBody();
						os.write(js.getBytes());os.close();
					}
					else {
						System.out.println("Her2 man");
						exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
						exchange.sendResponseHeaders(400, "Off".length());
						OutputStream os = exchange.getResponseBody();
						os.write("Off".getBytes());os.close();
					}
				}
				else {
					System.out.println("Her3 man");
					exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
					exchange.sendResponseHeaders(400, "Off".length());
					OutputStream os = exchange.getResponseBody();
					os.write("Off".getBytes());os.close();
				}
			}
			catch(ClassCastException e) {
				System.out.println("Her4 man");
				exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
				exchange.sendResponseHeaders(400, "Off".length());
				OutputStream os = exchange.getResponseBody();
				os.write("Off".getBytes());os.close();
			}
		}
	}
	static class MindTheBlock implements HttpHandler{
		@Override
		public void handle(HttpExchange t) throws IOException {
			t.getResponseHeaders().add("Content-Type","text/plain");
			String response="";
			byte[] data=ByteStreams.toByteArray(t.getRequestBody());
			try
			{
				Block newb=new Block(data);
				if(verifyBlock(newb)) {
					removeTrans(newb);
					removeIAddO(newb);
					synchronized(ledger) {
						ledger.addBlock(newb);
					}
					sendBlocks(newb);
					b=false;
					response="ok";
					t.sendResponseHeaders(200,response.length());
				}
				else {
					response="Invalid for me Block";
					t.sendResponseHeaders(400,response.length());
				}
			}
			catch(IOException | NoSuchAlgorithmException | IndexOutOfBoundsException | InvalidInputException | InvalidOutputException e) {
				response="Failed";
				t.sendResponseHeaders(404,response.length());
			} 
			OutputStream os=t.getResponseBody();
			os.write(response.getBytes());os.close();
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
				int sz=0;
				synchronized(ledger) {
					sz=ledger.getSize();
				}
				if(sz<=num) {
					exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
					exchange.sendResponseHeaders(400, "Get Lost".length());
					OutputStream os = exchange.getResponseBody();
					os.write("Get Lost".getBytes());os.close();
				}else {
					InputStream reader=new FileInputStream("Block"+num);
					byte[] data=reader.readAllBytes();
					reader.close();
					exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
					exchange.sendResponseHeaders(200, data.length);
					OutputStream os = exchange.getResponseBody();
					os.write(data);os.close();
				}
			}
			catch(NumberFormatException  e) {
				exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
				exchange.sendResponseHeaders(400, "Get Lost".length());
				OutputStream os = exchange.getResponseBody();
				os.write("Get Lost".getBytes());os.close();
			}
		}
	}
	static class PeerSender implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			StringWriter sw=new StringWriter();
			JsonWriter writer=new JsonWriter(sw);
			String s="";
			synchronized(peers){
				writer.beginObject();writer.name("peers");writer.beginArray();
				for(int i=0;i<peers.size();i++) {writer.value(peers.get(i));}
				writer.endArray();writer.endObject();writer.flush();sw.flush();
				s=sw.toString();
				writer.close();sw.close();
			}
			exchange.getResponseHeaders().add("Content-Type","application/json");//TODO check what this does actually
			exchange.sendResponseHeaders(200, s.length());
			OutputStream os=exchange.getResponseBody();
			os.write(s.getBytes());os.close();
		}  
	}
	static class PeerAdder implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			synchronized(peers) {
				if(peers.size()>=peerLimit) {
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
						peers.add(s);
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
	}
	static class PendingHandler implements HttpHandler{
		@Override
		public void handle(HttpExchange t) throws IOException {
			StringWriter sw=new StringWriter();
			JsonWriter writer=new JsonWriter(sw);
			writer.beginArray();
			synchronized(pendingTransactions) {
				for(Transaction curr:pendingTransactions) {
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
	}
	protected static synchronized TreeSet<Transaction > getPending(){
		return pendingTransactions;
	}
	static class Miner implements Runnable{
		@Override
		public void run() {

			//TODO verify algoritm more
			try {
				System.out.println("Mining Begin");
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				while(true) {
					try
					{
						b=true;
						Thread.sleep(2000);//TODO maybe modify this to get more Transactions in
						ArrayList<Transaction> tees=new ArrayList<Transaction>();
						int len=0;
						long fees=0;
						TreeSet<Link > s=new TreeSet<Link>();
						TreeSet<Transaction> copy=new TreeSet<Transaction>();
						synchronized(pendingTransactions)
						{
							copy=getPending();
						}
						synchronized(unusedOutputs)
						{
							for(Transaction t:copy) {
								long inputcoins=0,outputcoins=0;
								boolean bob=true;
								outputcoins=t.getOutput().getCoins();
								TreeSet<Link> s_=new TreeSet<Link>();
								Input inp=t.getInput();
								for(int j=0;j<inp.getNumberofInputs();j++) {
									String TID=inp.getID(j);
									int index=inp.getOutputIndex(j);
									if(!s.contains(new Link(TID,index))&&!s_.contains(new Link(TID,index))&&unusedOutputs.containsKey(new Link(TID,index))){
										inputcoins+=unusedOutputs.get(new Link(TID,index)).getCoins(index);
										s.add(new Link(TID,index));
										s_.add(new Link(TID,index));//TODO verify if checkTransaction does it job properly
									}
									else{
										bob=false;
										break;
									}
								}
								if(bob==true&&len+t.getSize()<=1e6){
									tees.add(t);
									fees+=inputcoins-outputcoins;
								}
								else {
									for(Link l : s_) {
										s.remove(l);	
									}
								}
							}
						}
						if(tees.size()>0) {//TODO Add Coinbase Transactions
							System.out.println("Searching done now nonce finding started");
							Transaction param[]=new Transaction[tees.size()+1];
							System.out.println("Here1");
							Transaction mine=new Transaction(new Input(0,new String[0],new int[0],new String[0]),new Output(1,new long[] {fees+blockReward},new PublicKey[] {unlock},new String[] {unlockS}));
							System.out.println("Here2");
							param[0]=mine;
							for(int i=0;i<tees.size();i++)
								param[i+1]=tees.get(i);
							Block newb=null;
							synchronized(ledger)
							{
								newb=new Block(ledger.getSize(),ledger.getTop().getHash(),param,ledger.getTarget());
							}
							System.out.println("Here3");
							String targ=newb.getTarget();
							byte head[]=newb.getHeader();
							byte flag=0;
							long non=0;
							System.out.println("Here4");
							while(b==true&&flag==0) {
								Instant now=Instant.now();
								long conversionfactor=1000000000;
								long time=now.getEpochSecond()*conversionfactor+((long)now.getNano());
								byte[] curr=Util.toByte(time);
								for(int i=0;i<8;i++)
									head[4+32+32+32+i]=curr[i];
								byte[] nonce=Util.toByte(non);
								for(int i=0;i<8;i++)
									head[4+32+32+32+8+i]=nonce[i];
								byte[] currHash=md.digest(head);
								if(Util.cmpHex(Util.parseByteToHex(currHash),targ)<0){
									newb.setTimestamp(time);
									newb.setNonce(non);
									flag=1;
								}
								non++;
							}
							if(flag==1&&b==true){  
								System.out.println("I mined");
								if(verifyBlock(newb))
								{
									synchronized(ledger){
										ledger.addBlock(newb);
									}
									removeTrans(newb);
									removeIAddO(newb);
									sendBlocks(newb);
								}
							}
						}
					}catch (NoSuchAlgorithmException e) {
					} catch (InvalidInputException e) {
					} catch (InvalidOutputException e) {
					} catch (InterruptedException e) {
					} catch (IOException e) {
					}
					System.out.println("Re serching for pendingTransactions");
				}
			}catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		}
	} 
}

