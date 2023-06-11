import socket
import threading
from Client.ClientEncryptor import ClientEncryptor
import json
from base64 import b64encode, b64decode

class Client:

    def __init__(self, host, port, app):
        self.app = app
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

    def encrypt_dict(self, dict):
        for key in dict:
            dict[key] = self.asym_encrypt(dict[key])
        return dict
            
    def get_dict_bytes(self, dict):
        return bytes(json.dumps(dict, indent = 4), 'utf-8')

    def send_register(self, login, password):
        return self.send(self.get_dict_bytes(self.encrypt_dict({
            'code': 'REGISTER',
            'login': login,
            'password': password,
        })))

    def send_login(self, login, password):
        return self.send(self.get_dict_bytes(self.encrypt_dict({
            'code': 'LOGIN',
            'login': login,
            'password': password,
        })))

    def send_msg(self, msg, chat_id):
        return self.send(self.get_dict_bytes({
            'text': self.sym_asym_encrypt(msg),
            'code': self.asym_encrypt('WRITE_TO_CHAT'),
            'chat_id': self.asym_encrypt(chat_id),
        }))

    def send_join_chat(self, chat_id):
        return self.send(self.get_dict_bytes(self.encrypt_dict({
            'code': 'JOIN_CHAT',
            'chat_id': chat_id,
        })))

    def send_create_chat(self):
        return self.send(self.get_dict_bytes(self.encrypt_dict({
            'code': 'CREATE_CHAT',
        })))
    
    def send_get_msg(self):
        return self.send(self.get_dict_bytes(self.encrypt_dict({
            'code': 'GET_CHAT_MESSAGES',
        })))

    # def create_json_bytes(self, code, msg):
    #     login = 'vlad'
    #     password = 'bog'
    #     chat_id = '26820a39-a764-439b-b1c8-ae78cd7eda04'
    #     m = {
    #         'text': self.sym_asym_encrypt(msg),
    #         'code': self.asym_encrypt(code),
    #         'login': self.asym_encrypt(login),
    #         'password': self.asym_encrypt(password),
    #         'chat_id': self.asym_encrypt(chat_id),
    #     }

    #     json_object = json.dumps(m, indent = 4)

    #     return bytes(json_object, 'utf-8')

    # def sender_func(self):
    #     while True:
    #         code = input("What code:")
    #         message = input("Send this to server:")       
            
    #         self.send(self.create_json_bytes(code, message))

    def reciever_func(self):        
        while True:
            data = self.client_socket.recv(20000)
            data = data.decode('utf-8')
            data = json.loads(data)
            print(data)
            match data['code']:
                case 'SERVER_NEW_MESSAGE':
                    text = data['text']
                    text = b64decode(text)
                    print(self.other_client_encryptor.get_recieved_msg(text))    
                case 'SERVER_REGISTRATION':
                    self.app.register_success(data)
                case 'SERVER_LOGIN':
                    self.app.login_success(data)
                case 'SERVER_LOGIN_FAILED':
                    self.app.login_failed(data)
                case _:
                    self.app.show_messagebox("Error", data['code'])  
    
    def start_reciever(self):
        thread = threading.Thread(target=self.reciever_func)
        thread.start()

    # def start_sender(self):
    #     thread = threading.Thread(target=self.sender_func)
    #     thread.start()