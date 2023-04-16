from Encryption.Asymmetric import AsymCipher
from Encryption.Symmetric import SymCipher
import pickle

class Frame:

    def __init__(self, msg: bytes, sym_cipher: bytes):
        self.msg = msg
        self.sym_cipher = sym_cipher
        
    
    def to_bytes(self):
        return pickle.dumps(self)
    
    def from_bytes(bytes):
        return pickle.loads(bytes)

class ClientEncryptor:

    def __init__(self, path_to_private_key, path_to_foreing_public_key, password):
        self.asym_cipher = AsymCipher()

        self.asym_cipher.private_key = AsymCipher.load_private_key(path_to_private_key, password)
        self.asym_cipher.foreign_public_key = AsymCipher.load_public_key(path_to_foreing_public_key)
        
        self.sym_cipher = SymCipher()


    def get_bytes_to_send(self, message: bytes) -> bytes:
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
        msg = sym_cipher.decrypt(encrypted_msg)

        return msg
