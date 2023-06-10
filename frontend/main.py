from Client.ClientEncryptor import ClientEncryptor
from Encryption.Asymmetric import AsymCipher
from Client.Client import Client
import time

# def client_one():
#     client  = ClientEncryptor('1_private_key.pem', '2_public_key.pem',b'123')

#     return client.get_bytes_to_send(b'hi my name vlad')

# def client_two(bytes):
#     client  = ClientEncryptor('2_private_key.pem', '1_public_key.pem',b'321')
#     print(client.get_recieved_msg(bytes))

def main():
    private_key, public_key = AsymCipher.gen_private_public_key()
    AsymCipher.save_private_key(private_key,'1_private_key.pem', b'123')
    AsymCipher.save_public_key(public_key,'1_public_key.pem')

    private_key, public_key = AsymCipher.gen_private_public_key()
    AsymCipher.save_private_key(private_key,'2_private_key.pem', b'321')
    AsymCipher.save_public_key(public_key,'2_public_key.pem')

    private_key, public_key = AsymCipher.gen_private_public_key()
    AsymCipher.save_private_key(private_key,'server_private_key.pem', b'321')
    AsymCipher.save_public_key(public_key,'server_public_key.pem')
    
    client = Client("192.168.0.111", 65432)
    client.connect()

    client.start_reciever()
    client.start_sender()    
    

    # client_two(client_one())

    


if __name__ == '__main__':
    main()