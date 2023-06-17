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
        self.client_encryptor = ClientEncryptor('1_public_key.pem')
        self.server_encryptor = ClientEncryptor('server_nick_public_key.pem')

    def connect(self):
        self.client_socket.connect((self.host, self.port))

    def disconnect(self):
        self.client_socket.close()

    def send(self, data: bytes):
        self.client_socket.sendall(data)
            
    def dict_to_json_bytes(self, dict):
        for key in dict:
            dict[key] = b64encode(dict[key]).decode('utf-8')

        return bytes(json.dumps(dict, indent = 4), 'utf-8')

    def send_json_bytes(self, dict):
        self.send(self.dict_to_json_bytes(self.server_encryptor.encrypt_dict(dict)))

    def send_register(self, login, password):
        self.send_json_bytes({
            'code': 'REGISTER',
            'login': login,
            'password': password,
            'client_public_key': 'self.client_encryptor.asym_cipher.public_key_to_string()',
        })

    def send_login(self, login, password):
        self.send_json_bytes({
            'code': 'LOGIN',
            'login': login,
            'password': password,
        })

    def send_msg(self, msg, chat_id):
        self.send_json_bytes({
            'text': self.sym_asym_encrypt(msg),
            'code': self.asym_encrypt('WRITE_TO_CHAT'),
            'chat_id': self.asym_encrypt(chat_id),
        })

    def send_join_chat(self, chat_id):
        self.send_json_bytes({
            'code': 'JOIN_CHAT',
            'chat_id': chat_id,
        })

    def send_create_chat(self):
        self.send_json_bytes({
            'code': 'CREATE_CHAT',
        })
    
    def send_get_msg(self):
        self.send_json_bytes({
            'code': 'GET_CHAT_MESSAGES',
        })

    def send_get_chats(self):
        self.send_json_bytes({
            'code': 'GET_CHATS',
        })

    def reciever_func(self):        
        while True:
            try:
                data = self.client_socket.recv(20000)
            except:
                break

            data = data.decode('utf-8')
            data = json.loads(data)
            print(data)
            match data['code']:
                case 'SERVER_NEW_MESSAGE':
                    text = data['text']
                    text = b64decode(text)
                    # print(self.other_client_encryptor.get_recieved_msg(text))    
                case 'SERVER_REGISTRATION':
                    self.app.register_success(data)
                case 'SERVER_LOGIN':
                    self.app.login_success(data)
                case 'SERVER_LOGIN_FAILED':
                    self.app.login_failed(data)
                case 'SERVER_CHATS':
                    self.app.chats_response(data)
                case 'SERVER_CHAT_ID':
                    self.app.create_chat_resoponse(data)
                case 'SERVER_MSG_CHAT':
                    self.app.send_msg_response(data)
                case _:
                    self.app.show_messagebox("Error", data['code'])  
    
    def start_reciever(self):
        self.thread = threading.Thread(target=self.reciever_func)
        self.thread.start()

    def stop_reciever (self):
        self.disconnect()
        self.thread.join()

    # def start_sender(self):
    #     thread = threading.Thread(target=self.sender_func)
    #     thread.start()