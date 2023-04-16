import socket
import threading
from Client.ClientEncryptor import ClientEncryptor
import json
from base64 import b64encode, b64decode

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
    
    def create_json_bytes(self, code, msg):
        login = 'vlad'
        password = 'bog'
        chat_id = '26820a39-a764-439b-b1c8-ae78cd7eda04'
        bytes_to_send = self.client_encryptor.get_bytes_to_send(bytes(msg, 'utf-8'))
        m = {
            'text': b64encode(bytes_to_send).decode('utf-8'),
            'code': code,
            'login': login,
            'password': password,
            'chat_id': chat_id,
        }

        json_object = json.dumps(m, indent = 4)

        return bytes(json_object, 'utf-8')

    def sender_func(self):
        while True:
            code = input("What code:")
            message = input("Send this to server:")       
            
            self.send(self.create_json_bytes(code, message))

    def reciever_func(self):
        client2  = ClientEncryptor('2_private_key.pem', '1_public_key.pem',b'321')
        
        while True:
            data = self.client_socket.recv(20000)
            data = data.decode('utf-8')
            print(data)
            data = json.loads(data)
            data = data['text']
            data = b64decode(data)
            print(client2.get_recieved_msg(data))
        
    
    def start_reciever(self):
        thread = threading.Thread(target=self.reciever_func)
        thread.start()

    def start_sender(self):
        thread = threading.Thread(target=self.sender_func)
        thread.start()