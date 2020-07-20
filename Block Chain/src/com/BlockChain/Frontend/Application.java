package com.BlockChain.Frontend;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.time.Instant;
import java.util.Map;
import java.util.Scanner;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import com.Util;
import com.BlockChain.Node.EncryptionClient;
import com.BlockChain.Node.Inputi;
import com.BlockChain.Node.InvalidOutputException;
import com.BlockChain.Node.Output;
import com.BlockChain.Node.Outputo;
import com.BlockChain.Node.parseTransaction;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class Application {
	public static void main(String[] args) throws IOException {
		Security.addProvider(new BouncyCastleProvider());
		Gson gson=new Gson();
		String filename="config.json";
		String js=new String(Files.readAllBytes(Paths.get(filename)));
		@SuppressWarnings("unchecked")
		Map<String,String > keyValue=gson.fromJson(js,Map.class);
		String address=keyValue.get("address");
		Scanner sc=new Scanner(System.in);
		System.out.println("Welcome to rockstar Mining Node");
		HttpClient client=HttpClient.newHttpClient();
		int choice=-1;
		do {
			System.out.println("Press 1 for Checking Balance");
			System.out.println("Press 2 for creating account");
			System.out.println("Press 3 to make a new Transaction");
			System.out.println("Press 4 for making an alias");
			System.out.println("Pressing anything else will lead to termination");
			choice=Integer.parseInt(sc.nextLine());
			switch(choice) {
			case 1:
				try
				{
					int ch=-1;
					System.out.println("Press 1 for Alias");
					System.out.println("Press 2 for entering PublicKey");
					ch=Integer.parseInt(sc.nextLine());
					if(ch==1) {
						String alias="";
						System.out.println("Enter Alias");
						alias=sc.nextLine();
						StringWriter sw=new StringWriter();
						JsonWriter writer=new JsonWriter(sw);
						writer.beginObject();
						writer.name("alias");
						writer.value(alias);
						writer.endObject();
						writer.flush();
						sw.flush();
						String json=sw.toString();
						sw.close();
						writer.close();
						HttpRequest request = HttpRequest.newBuilder()
								.uri(URI.create(address+"/getUnusedOutputs"))
								.header("Content-Type", "application/json")
								.POST(HttpRequest.BodyPublishers.ofString(json))
								.build();
						HttpResponse<String> response=client.send(request, BodyHandlers.ofString());
						if(response.statusCode()==200) {
							Auxiliary s=gson.fromJson(response.body(), Auxiliary.class);
							long total=0;
							for(int i=0;i<s.unusedOutputs.length;i++) {
								Tuple curr=s.unusedOutputs[i];
								total+=curr.amount;
							}
							System.out.println("You have "+total+" money left.");
						}
						else {
							System.out.println("Unable to make request at the moment");
						}
					}
					else if(ch==2) {
						System.out.println("Enter directory of pem file");
						String dir=sc.nextLine();
						String key=new String(Files.readAllBytes(Paths.get(dir)));
						StringWriter sw=new StringWriter();
						JsonWriter writer=new JsonWriter(sw);
						writer.beginObject();
						writer.name("publicKey");
						writer.value(key);
						writer.endObject();
						writer.flush();
						sw.flush();
						String json=sw.toString();
						sw.close();
						writer.close();
						HttpRequest request = HttpRequest.newBuilder()
								.uri(URI.create(address+"/getUnusedOutputs"))
								.header("Content-Type", "application/json")
								.POST(HttpRequest.BodyPublishers.ofString(json))
								.build();
						HttpResponse<String> response=client.send(request, BodyHandlers.ofString());
						if(response.statusCode()==200) {
							Auxiliary s=gson.fromJson(response.body(), Auxiliary.class);
							long total=0;
							for(int i=0;i<s.unusedOutputs.length;i++) {
								Tuple curr=s.unusedOutputs[i];
								total+=curr.amount;
							}
							System.out.println("You have "+total+" money left.");
						}
						else {
							System.out.println("Unable to make request at the moment");
						}
					}
					else {
						System.out.println("Invalid Choice");
					}
				}
				catch(IOException e) {
					System.out.println("Unexpected Error occured.");
				} catch (InterruptedException e) {
					System.out.println("Interuppted");
				}
				break;
			case 2:
				try
				{
					System.out.println("We are going to provide two files in home directory the private and the public key.");
					SecureRandom random=new SecureRandom();
					long time=Instant.now().getEpochSecond();
					Signature signee = Signature.getInstance("SHA256withRSA/PSS");
					signee.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
					KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
					generator.initialize(4096, random);
					KeyPair pair = generator.generateKeyPair();
					PublicKey pubKey = pair.getPublic();
					PrivateKey privKey = pair.getPrivate();
					FileWriter publicf=new FileWriter("public"+time+".pem");
					PemWriter real=new PemWriter(publicf);
					real.writeObject(new PemObject("PUBLIC KEY",pubKey.getEncoded()));
					real.close();
					FileWriter privatef=new FileWriter("private"+time+".pem");
					real=new PemWriter(privatef);
					real.writeObject(new PemObject("RSA PRIVATE KEY",privKey.getEncoded()));
					real.close();
					System.out.println("Keys created with name public"+time+".pem and private"+time+".pem");

				}
				catch(IOException e) {

				} catch (NoSuchAlgorithmException e) {
				} catch (InvalidAlgorithmParameterException e) {
				} catch (NoSuchProviderException e) {
				}
				break;
			case 3:
				try
				{
					System.out.println("Enter directory of public key");
					String dir1=sc.nextLine();
					System.out.println("Enter directory of private key");
					String dir2=sc.nextLine();
					PrivateKey privkey=Util.getPrivateKey(dir2);
					String key=new String(Files.readAllBytes(Paths.get(dir1)));
					StringWriter sw=new StringWriter();
					JsonWriter writer=new JsonWriter(sw);
					writer.beginObject();
					writer.name("publicKey");
					writer.value(key);
					writer.endObject();
					writer.flush();
					sw.flush();
					String json=sw.toString();
					sw.close();
					writer.close();
					HttpRequest request = HttpRequest.newBuilder()
							.uri(URI.create(address+"/getUnusedOutputs"))
							.header("Content-Type", "application/json")
							.POST(HttpRequest.BodyPublishers.ofString(json))
							.build();
					HttpResponse<String> response=client.send(request, BodyHandlers.ofString());
					if(response.statusCode()==200) {
						Auxiliary s=gson.fromJson(response.body(), Auxiliary.class);
						long total=0;
						for(int i=0;i<s.unusedOutputs.length;i++) {
							Tuple curr=s.unusedOutputs[i];
							total+=curr.amount;
						}
						System.out.println("You have "+total+" money left.");
						if(total>0) {
							String pkey=null;
							System.out.println("Enter Amount to transfer");
							long value=0;
							value=Long.parseLong(sc.nextLine());
							if(value<total)
							{
								System.out.println("Enter 1 to enter alias of recipient or 2 to enter public key of recipient");
								int ch2=Integer.parseInt(sc.nextLine());
								if(ch2==1) {
									System.out.println("Enter alias");
									String alias=sc.nextLine();
									StringWriter sw2=new StringWriter();
									JsonWriter writer2=new JsonWriter(sw2);
									writer2.beginObject();
									writer2.name("alias");
									writer2.value(alias);
									writer2.endObject();
									writer2.flush();
									sw2.flush();
									String json2=sw2.toString();
									sw2.close();
									writer2.close();
									HttpRequest request2 = HttpRequest.newBuilder()
											.uri(URI.create(address+"/getPublicKey"))
											.header("Content-Type", "application/json")
											.POST(HttpRequest.BodyPublishers.ofString(json2))
											.build();
									HttpResponse<String> response2=client.send(request2, BodyHandlers.ofString());
									@SuppressWarnings("unchecked")
									Map<String,String> map=gson.fromJson(response2.body(), Map.class);
									pkey=map.get("publicKey");
								}
								if(ch2==2) {
									System.out.println("Enter directory of public key");
									String dir=sc.nextLine();
									pkey=new String(Files.readAllBytes(Paths.get(dir)));
								}
								if(pkey!=null) {
									System.out.println("Enter money to give to miner>=1");
									long val=Long.parseLong(sc.nextLine());
									if(val>=1)
									{
										long back=total-val-value;
										Outputo arr1[]=null;
										if(back>0) {
											arr1=new Outputo[2];
											arr1[0]=new Outputo(back,key);
											arr1[1]=new Outputo(value,pkey);
										}
										else {
											arr1=new Outputo[] {new Outputo(value,pkey)};
										}
										Output op=Output.parseOutput(arr1);
										byte[] opdata=op.getData();
										MessageDigest md=MessageDigest.getInstance("SHA-256");
										Inputi arr2[]=new Inputi[s.unusedOutputs.length];
										for(int i=0;i<s.unusedOutputs.length;i++) {
											arr2[i]=new Inputi();
											arr2[i].transactionId=s.unusedOutputs[i].transactionId;
											arr2[i].index=s.unusedOutputs[i].index;
											byte[] signingdata=new byte[68];
											byte[] c=Util.parseHexToByte(arr2[i].transactionId);
											for(int j=0;j<32;j++) {
												signingdata[j]=c[j];
											}
											c=Util.toByte(arr2[i].index);
											for(int j=0;j<4;j++) {
												signingdata[32+j]=c[j];
											}
											c=md.digest(opdata);
											for(int j=0;j<32;j++) {
												signingdata[32+4+j]=c[j];
											}
											arr2[i].signature=new EncryptionClient().sign(Util.parseByteToHex(signingdata),privkey);

										}
										parseTransaction t=new parseTransaction(null,arr2,arr1);
										String jsonfinal=gson.toJson(t, parseTransaction.class);
										System.out.println(jsonfinal);
										HttpRequest requestf = HttpRequest.newBuilder()
												.uri(URI.create(address+"/newTransaction"))
												.header("Content-Type", "application/json")
												.POST(HttpRequest.BodyPublishers.ofString(jsonfinal))
												.build();
										HttpResponse<String> responsef=client.send(requestf, BodyHandlers.ofString());
										System.out.println(responsef.statusCode());
										System.out.println(responsef.body());
									}
								}
							}
						}
						else {

						}
					}
				}
				catch(IOException e) {
				} catch (InterruptedException e) {
				} catch (InvalidOutputException e) {
				} catch (NoSuchAlgorithmException e) {
				} catch (InvalidKeyException e) {
				} catch (SignatureException e) {
				} catch (InvalidAlgorithmParameterException e) {
				} 
				break;
			case 4:
				try
				{
					System.out.println("Enter alias");
					String alias=sc.nextLine();
					System.out.println("Enter directory of public file");
					String pKey=new String(Files.readAllBytes(Paths.get(sc.nextLine())));
					StringWriter sw=new StringWriter();
					JsonWriter writer=new JsonWriter(sw);
					writer.beginObject();
					writer.name("alias");
					writer.value(alias);
					writer.name("publicKey");
					writer.value(pKey);
					writer.endObject();
					writer.flush();
					sw.flush();
					String json=sw.toString();
					sw.close();
					writer.close();
					HttpRequest request= HttpRequest.newBuilder()
							.uri(URI.create(address+"/addAlias"))
							.header("Content-Type", "application/json")
							.POST(HttpRequest.BodyPublishers.ofString(json))
							.build();
					HttpResponse<String > response = client.send(request, BodyHandlers.ofString());	
					System.out.println(response.statusCode());
					System.out.println(response.body());
				}
				catch(IOException e) {

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;

			default:
				System.out.println("Incorrect Input. Terminating");
				break;
			}
		}
		while(choice<=4&&choice>=1);
		sc.close();
	}
} 
