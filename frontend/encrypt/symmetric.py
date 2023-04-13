import os
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
import json

class SymCipher:

    def __init__(self):
        self.key = None
        self.iv = None
        self.cipher = None

    def gen_key_iv(self):
        self.key = os.urandom(32)
        self.iv = os.urandom(16)
        self.gen_cipher()

    def gen_cipher(self):
        self.cipher = Cipher(algorithms.AES(self.key), modes.CBC(self.iv))

    def encrypt(self, data):
        encryptor = self.cipher.encryptor()

        return encryptor.update(data) + encryptor.finalize()
        

    def decrypt(self, ciphertext):
        decryptor = self.cipher.decryptor()
        
        return decryptor.update(ciphertext) + decryptor.finalize()
    
    def toBytes(self):
        return json.dumps({
            "symKey": self.key,
            "iv": self.iv
        }).encode()
    
    def fromBytes(bytes):
        symCipher = SymCipher()
        objectDict = json.loads(bytes)
        symCipher.key = objectDict['symKey']
        symCipher.iv = objectDict['iv']
        symCipher.gen_cipher()
        return symCipher