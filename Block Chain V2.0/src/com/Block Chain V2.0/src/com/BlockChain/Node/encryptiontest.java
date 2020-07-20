package com.BlockChain.Node;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import com.Util;

public class encryptiontest {
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, SignatureException {
		Security.addProvider(new BouncyCastleProvider());
		PEMParser pemParser = new PEMParser(new FileReader("a_private.pem"));
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		Object object = pemParser.readObject();
		KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
		PrivateKey privateKey = kp.getPrivate();
		PublicKey publicKey =Util.readPublicKeyFromFile("a_public.pem", "RSA");
		pemParser.close();
		EncryptionClient enc=new EncryptionClient();
		String s="38496351961367153969753643587563419584753478319734504357053898";
		String encr=enc.sign(s, privateKey);
		System.out.println(enc.verify(encr, s, publicKey));
		
	}
}
