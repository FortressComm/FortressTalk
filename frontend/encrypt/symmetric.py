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
    
    def to_bytes(self):
        return json.dumps({
            "symKey": self.key,
            "iv": self.iv
        }).encode()
    
    def from_bytes(bytes):
        sym_cipher = SymCipher()
        objectDict = json.loads(bytes)
        sym_cipher.key = objectDict['symKey']
        sym_cipher.iv = objectDict['iv']
        sym_cipher.gen_cipher()
        
        return sym_cipher