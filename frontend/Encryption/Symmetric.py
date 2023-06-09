import os
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding
import pickle

class SymCipher:

    def __init__(self, key = None, iv = None, encryption_mode = None):
        self.key = key
        self.iv = iv
        self.encryption_mode = encryption_mode
        self.cipher = None
        self.block_size = None

    def gen_key_iv(self):
        self.key = os.urandom(32)
        self.iv = os.urandom(16)
        self.gen_cipher()

    def gen_cipher(self):
        self.cipher = Cipher(algorithms.AES(self.key), modes.CBC(self.iv))
        self.block_size = algorithms.AES.block_size

        return self

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
        return pickle.dumps(self)
    
    def from_bytes(bytes):
        return pickle.loads(bytes)