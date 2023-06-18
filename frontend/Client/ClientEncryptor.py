from Encryption.Asymmetric import AsymCipher
from Encryption.Symmetric import SymCipher
from base64 import b64encode, b64decode

class ClientEncryptor:

    def __init__(self, path_to_server_public_key, path_to_my_private_key = '', password = b''):
        self.server_asym_cipher = AsymCipher()
        self.my_asym_cipher = AsymCipher()
        self.sym_cipher = SymCipher()

        if bool(path_to_my_private_key):
            self.my_asym_cipher.load_private_key(path_to_my_private_key, password)
        
        if bool(path_to_server_public_key):
            self.server_asym_cipher.load_public_key(path_to_server_public_key)


    def asym_encrypt(self, data: bytes):
        return bytes(self.server_asym_cipher.encrypt(data))
    
    def asym_decrypt(self, data: bytes):
        return bytes(self.my_asym_cipher.decrypt(data))

    def sym_encrypt(self, message: bytes) -> bytes:
        return bytes(self.sym_cipher.encrypt(message))

    def add_cipher_fields(self, dict): 
        dict['key'] = self.asym_encrypt(self.sym_cipher.key)
        dict['iv'] = self.asym_encrypt(self.sym_cipher.iv)
        dict['encryption_mode'] = self.asym_encrypt(bytes('CBC', 'utf-8'))

        return dict

    def encrypt_dict(self, dict):
        self.sym_cipher.gen_key_iv()

        for key in dict:
            dict[key] = self.sym_encrypt(bytes(dict[key], 'utf-8'))

        dict = self.add_cipher_fields(dict)  

        return dict
    
    def create_sym_cipher(self, dict):

        encryption_mode = self.asym_decrypt(dict['encryption_mode'])
        dict.pop('encryption_mode')
        decr_key = self.asym_decrypt(dict['key'])
        key = b64decode(decr_key)
        dict.pop('key')
        iv = b64decode(self.asym_decrypt(dict['iv']))
        dict.pop('iv')
        
        return (dict, SymCipher(key, iv, encryption_mode).gen_cipher())

    def decrypt_dict_without_cipher_attr(self, sym_cipher, dict):
        for key in dict:
            if type(dict[key]) is list:
                lst = dict[key]
                for el in lst:
                    el = self.decrypt_dict_without_cipher_attr(sym_cipher, el)
            else:
                dict[key] = sym_cipher.decrypt(dict[key]).decode('utf-8')

        return dict

    def decrypt_dict(self, dict: dict[bytes]):
        
        dict, sym_cipher = self.create_sym_cipher(dict)
        dict = self.decrypt_dict_without_cipher_attr(sym_cipher, dict)

        return dict