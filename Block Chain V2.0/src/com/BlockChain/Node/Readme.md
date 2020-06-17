Transaction Verification

I have assumed that our Node sofware would be either the Server class or a program that uses it. The Server class is commented code of the Web Server implemented in Assignment 6. I will build it further upon that. The Server class contains a TreeMap of unused Outputs which is declared protected and can be accessed by the Transaction class.

Verification is carried out by the verifyExists() function of Tranasaction class same as the mentor has suggested
 
However, this is the second step of verfication I have currently planned to implement. An extra check will be placed in the Input and Output class to avoid other exceptions.

TODO - i can reduce memory consumption by replacing by TreeMap from < pair <string , int> , Output > to < string , Output >  

Part 2
For the Server part of the code I have created a class ( static informally ) , the Server class that will connect to the other node. When the program is run our node comes into action. But currently I have done the psuedo Code of the recieving part. The endpoints are created in the main() function and are a memmber of class BlockSender and PendingTransactions
Part 3 

The next part is also added into Server class
