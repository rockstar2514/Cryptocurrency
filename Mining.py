import hashlib
s=input()
result = hashlib.sha256(s.encode()) 
for i in range (65536000):
    curr=s+str(i)
    newres=hashlib.sha256(curr.encode())
    ans=newres.hexdigest()
    if(int(ans,16)<0x0000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF):
        print (curr)
