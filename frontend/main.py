from client.client import Client
from encrypt.asymmetric import AsymCipher

def client_one():
    client  = Client('1_private_key.pem', '2_public_key.pem','123')

    return client.get_bytes_to_send(b'hi my name vlad')

def client_two(bytes):
    client  = Client('2_private_key.pem', '1_public_key.pem','321')
    print(client.get_recieved_msg(bytes))

def main():
    
    private_key, public_key = AsymCipher.gen_private_public_key()
    AsymCipher.save_private_key(private_key,'1_private_key.pem', '123')
    AsymCipher.save_public_key(public_key,'1_public_key.pem')

    private_key, public_key = AsymCipher.gen_private_public_key()
    AsymCipher.save_private_key(private_key,'2_private_key.pem', '321')
    AsymCipher.save_public_key(public_key,'2_public_key.pem')

    
    client_two(client_one())

    


if __name__ == '__main__':
    main()