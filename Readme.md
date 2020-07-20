# IITKBUCKS Application
<p>The repository is a Project I had taken under the Sciency and Technology Coucil,IIT Kanpur.<br>
The aim of the project was to create a functioning Block Chain Network where peers would<br>
be people designing their own nodes under this project<br></p>

## The Model
<p> Our Block Chain model was different from the actual Block Chain in a number of ways. <br>
Out Block Chain would work as follows :-<br>
A transaction would be sent by a user on to any node. This node would then forward it to its peers <br>
and the cycle would continue so that the transaction propagates throught the entire network.<br>
As soon as a transaction is recieved by a node it is put on a queue to be mined by the respective node<br>
Now as many transactions that can be taken in are put in a Block and now the nonce hunting starts. The first node <br>
to find the nonce, send the Block through the network and all nodes would add that Block into their local BlockChain<br>
### Specifics
A transaction is made using Asymmetric Encryption. Each user has his own Public Key and Private Key which they can use <br>  
to make and recieve transactions. The public key is the key that is shared publicly but the private key is kept private<br>
  

