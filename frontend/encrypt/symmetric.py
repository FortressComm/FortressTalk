import os
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding
import json

class SymCipher:

    def __init__(self):
        self.key = None
        self.iv = None
        self.cipher = None
        self.block_size = None

    def gen_key_iv(self):
        self.key = os.urandom(32)
        self.iv = os.urandom(16)
        self.gen_cipher()

    def gen_cipher(self):
        self.cipher = Cipher(algorithms.AES(self.key), modes.CBC(self.iv))
        self.block_size = algorithms.AES.block_size

    def encrypt(self, data: bytes):
        padder = padding.PKCS7(self.block_size).padder()
        padded_data = padder.update(data) + padder.finalize()
        
        encryptor = self.cipher.encryptor()

        return encryptor.update(padded_data) + encryptor.finalize()
        

    def decrypt(self, ciphertext: bytes):
        decryptor = self.cipher.decryptor()
        data = decryptor.update(ciphertext) + decryptor.finalize()
        unpadder = padding.PKCS7(self.block_size).unpadder()

        return unpadder.update(data) + unpadder.finalize()
    
    def to_bytes(self):
        return json.dumps({
            "symKey": str(self.key),
            "iv": str(self.iv)
        }).encode()
    
    def from_bytes(bytes):
        sym_cipher = SymCipher()
        objectDict = json.loads(bytes)
        sym_cipher.key = objectDict['symKey']
        sym_cipher.iv = objectDict['iv']
        sym_cipher.gen_cipher()
        
        return sym_cipher