# echo-client.py
from encrypt.asymmetric import AsymCipher
from encrypt.symmetric import SymCipher
import json
import socket

HOST = "192.168.0.111"  # The server's hostname or IP address
PORT = 65432  # The port used by the server

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    print('Connecting...')
    s.connect((HOST, PORT))
    print('Sending...')
    s.sendall(b'Hi')
    print('Recieving')
    data = s.recv(1024)

class Frame:

    def __init__(self, msg, keyIv):
        self.msg = None
        self.symCipher = None
        
    def toBytes(self):
        return json.dumps({
            "msg": self.msg,
            "symCipher": self.symCipher
        }).encode()
    
    def fromBytes(bytes):
        frame = Frame()
        objectDict = json.loads(bytes)
        frame.key = objectDict['msg']
        frame.symCipher = objectDict['symCipher']
        
        return frame

class Client:

    def __init__(self, path_to_private_key, path_to_foreing_public_key, password):
        self.asymCipher = AsymCipher()

        self.asymCipher.private_key = AsymCipher.load_private_key(path_to_private_key, password)
        self.asymCipher.foreign_public_key = AsymCipher.load_public_key(path_to_foreing_public_key)

        self.symCipher = SymCipher()


    def send(self, message, socket):
        self.symCipher.gen_key_iv()
        
        encryptedMsg = self.symCipher.encrypt(message)    
        encryptedSymCipher = self.asymCipher.encrypt(self.symCipher.toBytes())
        frame = Frame(encryptedMsg, encryptedSymCipher)
        
        socket.sendall(frame)

    def recieve(self, socket):
        data = socket.recv(1024)
        frame = Frame.fromBytes(data)

        encryptedMsg = frame.msg
        encryptedSymCipher = frame.symCipher
