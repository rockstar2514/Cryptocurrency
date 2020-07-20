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
import java.net.http.HttpTimeoutException;
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

public class Server {
	private volatile static TreeMap<Link,Output> unusedOutputs;
	private volatile static TreeSet < Transaction > pendingTransactions;
	private volatile static BlockChain ledger;
	private volatile static ArrayList<String> peers;
	private static int peerLimit;
	private static PrivateKey lock;
	private static PublicKey unlock;
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
			unlockS=keyValue.get("publicKey");
			r2.close();
			sr.close();
			StringReader sr2=new StringReader(keyValue.get("privateKey"));
			PEMParser pemParser = new PEMParser(sr2);
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			Object object = pemParser.readObject();
			KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
			lock = kp.getPrivate();
			pemParser.close();
			sr2.close();
			blockReward=Long.parseLong(keyValue.get("blockReward"));
			target=keyValue.get("target");
			dir=keyValue.get("dir");
			address=keyValue.get("address");
			originalPeer=keyValue.get("originalPeer");
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
		try {
			if(originalPeer.charAt(originalPeer.length()-1)=='/')
				originalPeer=originalPeer.substring(0,originalPeer.length()-1);
			if(address.charAt(address.length()-1)=='/')
				address=address.substring(0,address.length()-1);
			System.out.println("Fetching Peers");
			fetchPeers();
			System.out.println("Fetching Blocks");
			fetchBlocks();
			System.out.println("Fetching Pending Transactions");
			fetchPendingTransactions();
			HttpServer server=HttpServer.create(new InetSocketAddress(8000),20);
			System.out.println("Initialisation Successfull");
			server.createContext("/getPendingTransactions",new PendingHandler());
			server.createContext("/getBlock",new BlockSender());
			server.createContext("/newPeer",new PeerAdder());
			server.createContext("/getPeers",new PeerSender());
			server.createContext("/newBlock",new MindTheBlock());
			server.createContext("/newTransaction",new TransactionReciever());
			server.createContext("/addAlias",new AliasAdder());
			server.createContext("/getPublicKey",new PublicKeyFetcher());
			server.createContext("/getUnusedOutputs",new UnusedOutputsFetcher());
			server.start();
			Thread t=new Thread(new Miner());
			t.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	protected static void fetchPendingTransactions() {
		Gson gson=new Gson();
		HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		HttpRequest request=HttpRequest.newBuilder()
				.uri(URI.create(originalPeer+"/getPendingTransactions"))
				.timeout(Duration.ofSeconds(10))
				.GET()
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
						synchronized(unusedOutputs)
						{
							if(verifyTransaction(t_)) {
								pendingTransactions.add(t_);
							}
						}
					}
					catch(InvalidInputException | NoSuchAlgorithmException | InvalidOutputException e) {
						//TODO ADD later after defining when to throw InvalidInputEXCPETION;
					} 
				}
			}
		} 
		catch (IOException e) {
			System.exit(1);
		} catch (InterruptedException e) {
			System.out.println("Internet issue");
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
			HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
			synchronized(peers)
			{
				while(!potential.isEmpty()&&peers.size()<peerLimit) {
					String curr=potential.first();
					try{
						if(!curr.contentEquals(address))
						{
							HttpRequest request = HttpRequest.newBuilder()
									.uri(URI.create(curr+"/newPeer"))
									.timeout(Duration.ofSeconds(10))
									.header("Content-Type", "application/json")
									.POST(HttpRequest.BodyPublishers.ofString(json))
									.build();
							System.out.println("Trying to add "+curr);
							HttpResponse<String> response = client.send(request,
									HttpResponse.BodyHandlers.ofString());
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
						System.out.println("Connection Problem to"+curr);
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
		HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		int ind=0;
		while(true) {
			try
			{
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(originalPeer+"/getBlock/"+Integer.toString(ind)))
						.timeout(Duration.ofSeconds(10))
						.GET()
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
							System.out.println("Block with index "+b.getIndex()+" added.");
							removeTrans(b);
							removeIAddO(b);
							synchronized(ledger) {
								ledger.addBlock(b);
							}
						}
						else {
							System.out.println("Original Peer has lied");
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
				System.out.println("Timeout error Probably occured");
			}
		}
	}
	protected static boolean verifyBlock(Block b) {
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
			//TODO verify coinbase
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
		return true;
	}
	protected static void removeTrans(Block b) {
		synchronized(pendingTransactions) {
			Transaction arr[]=b.getTransactions();
			for(Transaction t:arr) {
				pendingTransactions.remove(t);
			}
		}
	}
	protected static void sendBlocks(Block b) throws NoSuchAlgorithmException, IndexOutOfBoundsException, InvalidInputException, IOException, InvalidOutputException {
		HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();//TODO remove errors
		byte[] bob=Util.append(b.getHeader(), b.getData());
		synchronized(peers)
		{
			for(String s:peers) {
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(s+"/newBlock"))
						.timeout(Duration.ofSeconds(10))
						.header("Content-Type", "application/octet-stream")
						.POST(HttpRequest.BodyPublishers.ofByteArray(bob))
						.build();
				try {
					System.out.println("Sending Block to "+s);
					HttpResponse<String> response=client.send(request,
							HttpResponse.BodyHandlers.ofString());//TODO Check that here I recieve output as String
					System.out.println("Response Code :- "+response.statusCode());
					System.out.println(response.body());
				} catch (IOException | InterruptedException e) {
					System.out.println("Connection Interrupted / IOException sending to Peer: "+s);
					e.printStackTrace();
				}
			}
		}
	}
	protected static void removeIAddO(Block b) {
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
	protected static boolean verifyTransaction(Transaction t) {
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
			{
				System.out.println("Transaction Invalid Output null");
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
						Output p=unusedOutputs.get(l);
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
							if(p.getKey(val)!=null&&!enc.verify(input.getSignature(i),Util.parseByteToHex(signingdata) , p.getKey(val))) {
								System.out.println("Signature verification failed.");
								return false;
							}
						} catch (InvalidKeyException e) {
							System.out.println("Key is of Invalid Format.");
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
			boolean bah=false;
			String dat=null;
			Gson gson=new Gson();
			try
			{
				boolean b=false;

				String json=new String(ByteStreams.toByteArray(t.getRequestBody()));
				parseTransaction tmp=gson.fromJson(json, parseTransaction.class);
				Transaction t_=tmp.getRealTransaction();

				synchronized(unusedOutputs) {
					b=verifyTransaction(t_);
				}
				if(b) {
					synchronized(pendingTransactions) {
						System.out.println("Just recieved Incoming Transaction.");
						if(!pendingTransactions.contains(t_))
						{
							System.out.println("Transaction has been verified");
							t_.getReceipt();
							pendingTransactions.add(t_);
							bah=true;
							dat=json;
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
			HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
			if(bah) {
				synchronized(peers) {
					for(String s:peers) {
						HttpRequest request=HttpRequest.newBuilder()
								.uri(URI.create(s+"/newTransaction"))
								.timeout(Duration.ofSeconds(10))
								.header("Content-Type", "application/json")
								.POST(HttpRequest.BodyPublishers.ofString(dat))
								.build();
						try {
							System.out.println("Sending Transaction to "+s);
							client.send(request,
									HttpResponse.BodyHandlers.ofString());
							System.out.println("Sent Succcessfully");
						} catch (InterruptedException|IOException e) {
							System.out.println("Connection Problem to Peer "+s);
						}
					}
				}
			}
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
			Gson gson = new Gson();
			String json=new String(ByteStreams.toByteArray(exchange.getRequestBody()));
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
						exchange.getResponseHeaders().add("Content-Type", "application/text-plain");
						exchange.sendResponseHeaders(400, "Off".length());
						OutputStream os = exchange.getResponseBody();
						os.write("Off".getBytes());os.close();
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
	static class MindTheBlock implements HttpHandler{
		@Override
		public void handle(HttpExchange t) throws IOException {
			Block cop=null;
			boolean check=false;
			t.getResponseHeaders().add("Content-Type","text/plain");
			String response="";
			byte[] data=ByteStreams.toByteArray(t.getRequestBody());
			try
			{
				Block newb=new Block(data);
				if(verifyBlock(newb)) {
					System.out.println("Block with index "+newb.getIndex()+"added.");
					b=false;
					removeTrans(newb);
					removeIAddO(newb);
					synchronized(ledger) {
						ledger.addBlock(newb);
					}
					check=true;
					cop=newb;
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
			if(check) {
				try {
					sendBlocks(cop);
				} catch (NoSuchAlgorithmException e) {
				} catch (IndexOutOfBoundsException e) {
				} catch (InvalidInputException e) {
				} catch (IOException e) {
				} catch (InvalidOutputException e) {
				}
			}
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
					exchange.sendResponseHeaders(404, "Get Lost".length());
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
					writer.endArray();
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
	protected static TreeSet<Transaction > getPending(){
		return pendingTransactions;
	}
	static class Miner implements Runnable{
		@Override
		public void run() {
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
										s_.add(new Link(TID,index));
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
						if(tees.size()>0) {
							Transaction param[]=new Transaction[tees.size()+1];
							Transaction mine=new Transaction(new Input(0,new String[0],new int[0],new String[0]),new Output(1,new long[] {fees+blockReward},new PublicKey[] {unlock},new String[] {unlockS}));
							param[0]=mine;
							for(int i=0;i<tees.size();i++)
								param[i+1]=tees.get(i);
							Block newb=null;
							synchronized(ledger)
							{
								newb=new Block(ledger.getSize(),ledger.getTop().getHash(),param,ledger.getTarget());
							}
							String targ=newb.getTarget();
							byte head[]=newb.getHeader();
							byte flag=0;
							long non=0;
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
								System.out.println("Block with index "+newb.getIndex()+ " passed.");
								synchronized(ledger){
									ledger.addBlock(newb);
								}
								removeTrans(newb);
								removeIAddO(newb);
								sendBlocks(newb);
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
				e1.printStackTrace();
			} 
		}
	} 
}
