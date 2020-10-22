## Improved_DHKE_NIS
what is the problem?  
ans)
fact many of the iot/low energy device compromise on security just 
because the key exchange / session generation is expensive. If majority of the electricity is consumed in session generation 
productivity is dammed!!. 

## solution we propose and how do we solve it?
we are inspired by a research paper in which they introduce to shift all the computation that is possible at the server side,actually shifted to the server even if there are slight pre-requisites and space consumption.  
how do we apply it ?  
there is alice: client.  
there is bob: server.

## client side:
state:  
t is permanent private key  
T which is equal to g^t is calculated  
ephemeral private key x (x is between 1 and p-1)  
compute x+t (mod) p-1  
A is the nonce  
send [A,x+t (mod) p-1, T]   //T is sent on the condition that T is not present in the server (only for first time connection)  
for later connections until T is unchanged only [A,x+t] can be sent

## server side:
choose private key: y.  
Y equal to g^y  
then k(ab) = ((g^(x+t%p))/T)^y  
=> ((g^x.g^t%p)/g^t%p)^y  
=>  g^xy  
then the server sends [B,Y] nonce and B's public key.

## client side revisited:
normal calculation to calculate  
k(ab) = Y^x  
now both server, and the client have the common key.

# observation:

## 1)Authentication 
It is done without any extra steps involved we just evaded man-in-the-middle-attack!! along with extra security  
how?  
ans:  
look T has been sent by the client T is a public counterpart of client's long term private key   
now client sends x+t%p and T = g^(t%p) in order to get g^x the server calculate g^(x+t%p) now divides this by T = g^(t%p) term to get g^x which is clients public key any inconsistency in T can create error and protect the whole system also the inconsistency of client sending its public key again and again makes difficult for man in the middle attack,because 

### {review-3}
how will darth (invader) will get to know clients public key if client does not share it because iot is already stored in the server and if not darth does not know when and how will be public key sent,
will it use another dh key exchanged, or it didn't.

## 2)decreased computational overhead on client side
the client didn't have to do modular multiplication and find powers as the public key is stored at the server and t is a long term private key in contrast to short-lived keys;calculate (g^t%p) it was all taken care at server side

## 3) what is client doing?
the only operation that client[Alice has to do is to]:  
a)save the value of t,T  
b)choose shot lived key 'x'  
c)calculate x+t (mod) p-1  

## 4)serving of (T): to the server:conditions:
the first operation require the client to save the t,T value for future use.

### case1: a new server contacted + first operation cycle of the client too!!
calculation of T,t is required and sending T is also required that happens on the client side. in this first cycle the server stores the T value as key value pair with key being the nonce in a HashMap.

### case2: when client has been connected with the server in past 
as soon as the nonce is received the server searches the hashmap and uses the value of T stored earlier (computation+time + electricity) are saved

### case 3 {review -3}:
in the case when client does not send the T value, and it is not also present ino the hashmap of the server sends acknowledgment to turn on the boolean flag so that the client again sends the T value, and client itself switches the flag after sending T value.

## Implementation and presentation

### option-1
execute on the same system and then compare and analyze the processor and resource consumption process wise

### option 2
execute the dfh after hosting it as an application on the server then,using monitoring the resource consumption 
finally calculating the energy spent in joules in both the systems and then comparing it 
with regular diffie hellman and then finally conclude the project:


# Diffie Hellman Key Exchange for Thin Clients

To run Server
```shell script
java server.ServerDHKE <serverId> <port>
java server.ServerDHKE Server1 9001 
```

To run a Single Client
```shell script
java client.ClientDHKE <clientId> 
java client.ClientDHKE Client1
```


# Flow of the code and algorithm

 To begin: A client and server program to be executed in different terminals  
    - Starting a server opens a new socket for listening a connection  
    - The client application can then connect to server
 - the client creates a new **Session** 
 - client call's **Session::connect**
 - the server waiting for connection accepts the connection from client into socket
 - the server creates a new **ClientThread** object and send it the socket to handle the client
 - the client thread responsible for handling client establishes the I/O to send messages through sockets
 - thread calls the **ClientThread::keyExchange** method to start the key exchange process before any further communication
 - the client thread waits for two minutes to receive a message from the client for key exchange
 - in the meantime client can start the key exchange process by either invoking **Session::fullKeyRequest** or **Session::keyRequest**
 - when client thread accepts the request then three cases as explained above is handled by client thread
 - the client thread if a key exchange request is valid calculates the session key
 - it calls **ClientThread::calcPrivateKey** and **ClientThread::calcPublicKey** and then **ClientThread::calcClientPublicKey**
 - after this client sends back server's public key to client
 - then client thread using the parameters calculated above calculates the **session_key** using **ClientThread::calcSessionKey**
 - in the meantime client wait's for server's public keys
 - client calls **Session::receiveKeys** which receives public keys of server also calculates **session_key** using **Session::calcSessionKey**  








