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
        self.client_encryptor = ClientEncryptor('2_public_key.pem')
        self.server_encryptor = ClientEncryptor('server_nick_public_key.pem')
        self.other_client_encryptor = ClientEncryptor('1_public_key.pem')

    def connect(self):
        self.client_socket.connect((self.host, self.port))

    def disconnect(self):
        self.client_socket.close()

    def send(self, data: bytes):
        self.client_socket.sendall(data)
    
    def asym_encrypt(self, data: str) -> str:
        return b64encode(self.server_encryptor.only_asym_encrypt(bytes(data, 'utf-8'))).decode('utf-8')
    
    def sym_asym_encrypt(self, data: str) -> str:
        return b64encode(self.client_encryptor.get_bytes_to_send(bytes(data, 'utf-8'))).decode('utf-8')

    def create_json_bytes(self, code, msg):
        login = 'vlad'
        password = 'bog'
        chat_id = '26820a39-a764-439b-b1c8-ae78cd7eda04'
        m = {
            'text': self.sym_asym_encrypt(msg),
            'code': self.asym_encrypt(code),
            'login': self.asym_encrypt(login),
            'password': self.asym_encrypt(password),
            'chat_id': self.asym_encrypt(chat_id),
        }

        json_object = json.dumps(m, indent = 4)

        return bytes(json_object, 'utf-8')

    def sender_func(self):
        while True:
            code = input("What code:")
            message = input("Send this to server:")       
            
            self.send(self.create_json_bytes(code, message))

    def reciever_func(self):        
        while True:
            data = self.client_socket.recv(20000)
            data = data.decode('utf-8')
            print(data)
            match data['code']:
                case 'SERVER_NEW_MESSAGE':
                    data = json.loads(data)
                    data = data['text']
                    data = b64decode(data)
                    print(self.other_client_encryptor.get_recieved_msg(data))

    

        
    
    def start_reciever(self):
        thread = threading.Thread(target=self.reciever_func)
        thread.start()

    def start_sender(self):
        thread = threading.Thread(target=self.sender_func)
        thread.start()