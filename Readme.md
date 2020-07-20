# IITKBUCKS Application
<p>The repository is a Project I had taken under the Science and Technology Coucil,IIT Kanpur.<br>
The aim of the project was to create a functioning Block Chain Network where peers would<br>
be people designing their own nodes under this project<br></p>

## The Model
Out Block Chain would work as follows :-<br>
A transaction would be sent by a user on to any node. This node would then forward it to its peers <br>
and the peer forwarding would continue so that the transaction propagates throught the entire network.<br>
As soon as a transaction is recieved by a node it is put on a queue to be mined by the respective node<br>
Now as many transactions that can be taken in are put in a Block and now the nonce hunting starts. The <br>
  first node to find the nonce sends the Block through the network and all nodes would add that Block <br>
  into their local BlockChain. This cycle would repeat again and again.
  
### Specifics
A transaction is made using Asymmetric Encryption. Each user has his own public key and private key which <br>
they can use to make and recieve transactions. The public key is the key that is shared publicly but the <br>
private key is kept private. In order to make transaction to a person, the sender attaches the recipient's <br>
public key in the transaction. This signifies that the transaction is made to a particular person. However,<br>
anyone can claim that this key is theirs,but in order to use the money attached to a particular public key,<br>
the corresponding private key is needed. Again, revealing private key can then be a risk because then other <br>
people who have seen the revealed private key can say that they have the private key too. For this purpose<br>
,signatures come into play. The basic idea is that if you encrypt a string with a private key ,you can decrypt<br>
it only with the corresponding public key. Hence for this purpose we have used RSA-PSS Signatures with 32 Salt<br>
length.So, we need a string (bytes in fact) that is encrypted by the private key of a recipient and the signature<br>
generated by the recipient. However, if the string to be signed is fixed and the signature has been revealed then<br>
it can be used by anybody .For this purpose we design the string to be signed in a way that this string is used <br>
only once. This way a recipient can use his money securely and anonymously. For mining we use the SHA-256 hash of <br>
the binary data of the block which must be less than a particular target ( proof of work). In order to introduce <br>
psuedo randomness, we attach the timestamp of the instant at which a nonce is being tried.

## Dependencies
A number of Java Dependencies were used
<ul>
<li>Bouncy Castle Provider </li>
<li> Google Guava V 29.0 </li>
<li> Google Guava V 21.0 </li>
<li> Gson 2.8.5 </li>
</ul>

## To run as a node
Requirements
JDK ( latest version possible )
The program functions on the Node currently being run at <https://iitkbucks.pclub.in>
Our node initialises with Blocks from that Node

### Instructions
1. Hover to the Block Chain folder and import BundledNode.jar , bc.dat and config.json
2. First tunnel port 8000 of your local server via ngrok or any other service.
3. On ngrok you, will recieve a URL which will show your public URL where you have tunneled your port 8000 to. Copy that address.
2. Configure config.json with this address against the address field.
3. Also, copy paste your public and private key in the config.json
3. Ensure the bc.dat file is present in the directory (wheather empty or not).
3. Now simply cd to this directory and run java -jar BundledNode.jar . Remember to give permissions to make changes in the directory
4. Soon, u will start to see logs coming up of wheather a transaction is being searched for, a block has been recieved, a block is being mined ,a transaction is recieved and others





  








  

