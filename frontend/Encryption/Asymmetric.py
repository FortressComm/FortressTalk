from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives import serialization
from base64 import b64encode, b64decode

class AsymCipher:       

    def __init__(self):
        self.private_key = None
        self.public_key = None
        self.password = None
        self.padd = padding.OAEP(
                mgf=padding.MGF1(algorithm=hashes.SHA256()),
                algorithm=hashes.SHA256(),
                label=None
            )      
    
    def encrypt(self, data: bytes):
        ciphertext = self.public_key.encrypt(
            data,
            self.padd
        )

        return ciphertext
    
    def decrypt(self, ciphertext: bytes):
        plaintext = self.private_key.decrypt(
            ciphertext,
            self.padd
        )
        
        return plaintext

    def gen_private_public_key():
        private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=4096,
        )

        return (private_key, private_key.public_key())

    def load_public_key(self, path_to_public_key):
        with open(path_to_public_key, "rb") as key_file:
            self.public_key =  serialization.load_pem_public_key(key_file.read())

            return self.public_key

    def public_key_to_string(self):
        return b64encode(self.public_key.public_bytes(serialization.Encoding.Raw, format=serialization.PublicFormat.Raw)).decode('utf-8');

    def save_public_key(public_key, path_to_public_key):
        pem = public_key.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo
        )
        with open(path_to_public_key, 'wb') as file:
            file.write(pem)

    def load_private_key(self, path_to_private_key, password):
        with open(path_to_private_key, "rb") as key_file:
            self.private_key = serialization.load_pem_private_key(
            key_file.read(),
            password,
            )
            return self.private_key
            
    def save_private_key(private_key, path_to_private_key, password):
        pem = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=serialization.BestAvailableEncryption(password)
        )
        with open(path_to_private_key, 'wb') as file:
            file.write(pem)
    
