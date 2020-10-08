![GitHub Logo](/images/logo.png)
Format: ![Alt Text](url)
# Improved_DHKE_NIS

whats the problem ?

ans)

fact many of the iot/low energy device compromise on security just 
because the key exchange / session generation is expensive .

if majority of the electricity is consumed in session generation 
productivity is dammed!!.

solution we propose and how do we solve it?

we get inspired by a research paper in which they introduce to 
shift all the computation that is possible at the server side,actually shifted 
to the server even if there are slight pre-requisites and space consumption.

how do we apply it ?

there is alice :client

there is bob   :server


# client side:

state:

private key  = t

T is calculated when required

private key x (x is between 1 and p-1)

compute (x+tmodp)

A is the nonce

send (A,x+t%p,T)   //T is sent on the condition that T is not present in the server(first time conection)

# server side:

choose private key :y

Y = g^y

then k(ab) = [(g^(x+t%p))/T]^y

=>   ((g^x.g^t%p)/g^t%p)^y

=>   g^xy

then the server sends 

(B,Y)

nonce,B's public key

# client side:

normal calculation to calculate 

k(ab) = Y^x

now both of the server and the client have the common key.

# observation:

# 1)Authentication 

is done without any extra steps involved we just evaded man-in-the-middle-attack!! 

along with extra security

![GitHub Logo](/images/logo.png)
Format: ![Alt Text](url)
how?

ans:

look T has been sent by the client 

T is made by client long term private key

now 

client sends x+t%p

and T = g^(t%p)

inoreder to get g^x

the server calculate g^(x+t%p)

now divite this by T = g^(t%p) term

to get [g^x] which is client's public key

any inconsistency in T can create error and protect the whole system

also the inconsistency of client sending it's public key again and again 

makes difficult for man in the middle attack,because 

### {review-3}
how will darth(envader) will get to know clients public key if client does not shares
it because iot is alredy stored in the server.

and if not darth does not know when and how will be public key sent,
will it use another dh key exchange or not
### {review-3}

# 2)decreased computational overhead on client side

the client didnt had to do modular multiplcation and find powers as 

the public key is stored at the server and t is a long term private key in contrast to short lived keys

;calculate (g^t%p) it was all taken care at server side

# 3) whats client doin?

there fore the only operation that client[Alice has to do is to]:
![GitHub Logo](/images/logo.png)
Format: ![Alt Text](url)

a)save the value of t,T

b)choose shot lived key 'x'

# 4)serving of (T): to the server:conditions:

the first opertion require the client to save the t,T value for future use.

#### case1: new server contacted + first operation cycle of the client too!!

calculation of T,t is required and also sending T is also required that happens on the client side.

in this first cycle the server stores the T value as key value pair 

with key being the nonce in a hashmap{review-3)

#### case2: when client has been connected with the server in past 

,as soon as the nonce is recieved 
the server searches the hashmap and uses the value of T stored earlier (computation+time + electricity) are saved

#### case 3 {review -3}:
in the case when client does not sends the T value,

and it is not also present ino the hashmap of the server

server sends acknoledgment to turn on the boolean flag so that the client again sends the T value,

and client itself switches the flag after sending T value.

# Implementation and presentation

#### option-1
execute on the same system and then compare and analyze the processor and 

resource consumption process wise

#### option 2
execute the dfh after hosting it as an application on the server
then,using monitoring the resource consumption 

##### finally calculating the energy spent in joules 

in both the systems and then comparing it 

with regular diffie hellman 

and then finally conclude the project:






