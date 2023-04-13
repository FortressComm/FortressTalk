from encrypt.asymmetric import AsymCipher
from encrypt.symmetric import SymCipher
import json
# import socket

# HOST = "192.168.0.111"  # The server's hostname or IP address
# PORT = 65432  # The port used by the server

# with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
#     print('Connecting...')
#     s.connect((HOST, PORT))
#     print('Sending...')
#     s.sendall(b'Hi')
#     print('Recieving')
#     data = s.recv(1024)

class Frame:

    def __init__(self, msg: bytes, sym_cipher: bytes):
        self.msg = msg
        self.sym_cipher = sym_cipher
        
    
    def to_bytes(self):
        return json.dumps({
            "msg": str(self.msg),
            "symCipher": str(self.sym_cipher)
        }).encode()
    
    def from_bytes(bytes):
        object_dict = json.loads(bytes)
        frame = Frame(object_dict['msg'], object_dict['symCipher'])
        
        return frame

class Client:

    def __init__(self, path_to_private_key, path_to_foreing_public_key, password):
        self.asym_cipher = AsymCipher()

        self.asym_cipher.private_key = AsymCipher.load_private_key(path_to_private_key, bytes(password, 'utf-8'))
        self.asym_cipher.foreign_public_key = AsymCipher.load_public_key(path_to_foreing_public_key)
        
        self.sym_cipher = SymCipher()


    def get_bytes_to_send(self, message: bytes):
        self.sym_cipher.gen_key_iv()
        
        encrypted_msg = self.sym_cipher.encrypt(message)    
        encrypted_sym_cipher = self.asym_cipher.encrypt(self.sym_cipher.to_bytes())
        frame = Frame(encrypted_msg, encrypted_sym_cipher)
        
        return frame.to_bytes()

    def get_recieved_msg(self, bytes):
        frame = Frame.from_bytes(bytes)

        encrypted_msg = frame.msg
        encrypted_sym_cipher = frame.sym_cipher

        sym_cipher = SymCipher.from_bytes(self.asym_cipher.decrypt(encrypted_sym_cipher))
        msg = sym_cipher.decrypt(encrypted_msg).decode()

        return msg
