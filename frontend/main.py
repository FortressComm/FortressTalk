from Client.ClientEncryptor import ClientEncryptor
from Encryption.Asymmetric import AsymCipher
from Client.Client import Client
import time


def main():
    # private_key, public_key = AsymCipher.gen_private_public_key()
    # AsymCipher.save_private_key(private_key,'1_private_key.pem', b'123')
    # AsymCipher.save_public_key(public_key,'1_public_key.pem')

    # private_key, public_key = AsymCipher.gen_private_public_key()
    # AsymCipher.save_private_key(private_key,'2_private_key.pem', b'321')
    # AsymCipher.save_public_key(public_key,'2_public_key.pem')

    # private_key, public_key = AsymCipher.gen_private_public_key()
    # AsymCipher.save_private_key(private_key,'server_private_key.pem', b'321')
    # AsymCipher.save_public_key(public_key,'server_public_key.pem')
    
    client = Client("192.168.0.111", 65432)
    client.connect()

    client.start_reciever()
    client.start_sender()    

    


if __name__ == '__main__':
    main()