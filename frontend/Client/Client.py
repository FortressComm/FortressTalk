import socket
import threading
from Client.ClientEncryptor import ClientEncryptor

class Client:

    def __init__(self, host, port):
        self.host = host
        self.port = port
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client_encryptor = ClientEncryptor('1_private_key.pem', '2_public_key.pem',b'123')

    def connect(self):
        self.client_socket.connect((self.host, self.port))

    def disconnect(self):
        self.client_socket.close()

    def send(self, data: bytes):
        self.client_socket.sendall(data)
    
    def sender_func(self):
        while True:
            input1 = input()
            bytes_to_send = self.client_encryptor.get_bytes_to_send(bytes(input1, 'utf-8'))
            print('send len: ', len(bytes_to_send))
            self.send(bytes_to_send)

    def reciever_func(self):
        client2  = ClientEncryptor('2_private_key.pem', '1_public_key.pem',b'321')
        
        while True:
            data = self.client_socket.recv(20000)
            print('recv len: ', len(data))
            print(client2.get_recieved_msg(data))
    
    def start_reciever(self):
        thread = threading.Thread(target=self.reciever_func)
        thread.start()

    def start_sender(self):
        thread = threading.Thread(target=self.sender_func)
        thread.start()