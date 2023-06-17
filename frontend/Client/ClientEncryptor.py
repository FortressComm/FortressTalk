from Encryption.Asymmetric import AsymCipher
from Encryption.Symmetric import SymCipher
from base64 import b64encode, b64decode

class ClientEncryptor:

    def __init__(self, path_to_foreing_public_key, path_to_private_key = '', password = b''):
        self.asym_cipher = AsymCipher()
        self.sym_cipher = SymCipher()

        if bool(path_to_private_key):
            self.asym_cipher.private_key = AsymCipher.load_private_key(path_to_private_key, password)
        
        if bool(path_to_foreing_public_key):
            self.asym_cipher.public_key = AsymCipher.load_public_key(path_to_foreing_public_key)


    def asym_encrypt(self, data: bytes):
        return bytes(self.asym_cipher.encrypt(data))

    def sym_encrypt(self, message: bytes) -> bytes:
        
        return bytes(self.sym_cipher.encrypt(message))

    def add_cipher_fields(self, dict):
        dict['key'] = self.asym_encrypt(self.sym_cipher.key)
        dict['iv'] = self.asym_encrypt(self.sym_cipher.iv)
        dict['encryption_mode'] = self.asym_encrypt('CBC')

        return dict

    def sym_decrypt(self, bytes):
        # frame = Frame.from_bytes(bytes)

        # encrypted_msg = frame.msg
        # encrypted_sym_cipher = frame.sym_cipher

        # sym_cipher = SymCipher.from_bytes(self.asym_cipher.decrypt(encrypted_sym_cipher))
        msg = self.sym_cipher.decrypt(bytes)

        return msg

    def encrypt_dict(self, dict):
        self.sym_cipher.gen_key_iv()

        for key in dict:
            dict[key] = self.sym_encrypt(bytes(dict[key], 'utf-8'))

        dict = self.add_cipher_fields(dict)

        return dict