import socket
import threading
from Client.ClientEncryptor import ClientEncryptor
import json
from base64 import b64encode, b64decode
import os
import uuid

class Client:

    def __init__(self, host, port, app):
        self.app = app
        self.host = host
        self.port = port
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        
        self.client_encryptor = ClientEncryptor('server_nick_public_key.pem', '1_private_key.pem', b'123')

    def connect(self):
        self.client_socket.connect((self.host, self.port))

    def disconnect(self):
        self.client_socket.close()

    def send(self, data: bytes):
        self.client_socket.sendall(data)
            
    def file_transfer(self, file_path, chat_id):
        chunk_size = 10240
        file_size = str(os.path.getsize(file_path))
        file_name = str(uuid.uuid1()) + os.path.splitext(os.path.basename(file_path))[1]
        self.send_transfer_start(file_size, file_name, chat_id)
        chunk_number = 0
        with open(file_path, 'rb') as file:
            chunk = file.read(chunk_size)
          
            while chunk:
                self.send_transfer_chunk(chunk, str(chunk_number), chat_id)
                chunk_number += 1
                chunk = file.read(chunk_size)
        
        self.send_transfer_end(chat_id)

    def dict_to_json_bytes(self, dict):
        for key in dict:
            dict[key] = b64encode(dict[key]).decode('utf-8')

        return bytes(json.dumps(dict, indent = 4), 'utf-8')
    
    def decode_dict(self, dict):

        for key in dict:
            if type(dict[key]) is list:
                lst = dict[key]
                for el in lst:
                    el = self.decode_dict(el)
            else:
                dict[key] = b64decode(bytes(dict[key], 'utf-8'))
        
        return dict

    def send_json_bytes(self, dict):
        self.send(self.dict_to_json_bytes(self.client_encryptor.encrypt_dict(dict)))

    def send_register(self, login, password):
        # pk = self.client_encryptor.my_asym_cipher.public_key_to_string()
        # print(pk)
        self.send_json_bytes({
            'code': 'REGISTER',
            'login': login,
            'password': password,
            'client_public_key': self.client_encryptor.my_asym_cipher.public_key_to_string(),
        })

    def send_login(self, login, password):
        self.send_json_bytes({
            'code': 'LOGIN',
            'login': login,
            'password': password,
            'client_public_key': self.client_encryptor.my_asym_cipher.public_key_to_string(),
        })

    def send_msg(self, msg, chat_id):
        self.send_json_bytes({
            'text': msg,
            'code': 'WRITE_TO_CHAT',
            'chat_id': chat_id,
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
    
    def send_get_msg(self, chat_id):
        self.send_json_bytes({
            'code': 'GET_CHAT_MESSAGES',
            'chat_id': chat_id,
        })

    def send_get_chats(self):
        self.send_json_bytes({
            'code': 'GET_CHATS',
        })

    def send_transfer_start(self, expected_size, file_name, chat_id):
        self.send_json_bytes({
            'code': 'START_SEND',
            'expected_size': expected_size,
            'file_name': file_name,
            'chat_id': chat_id,
        })

    def send_transfer_chunk(self, chunk, chunk_number, chat_id):
        self.send_json_bytes({
            'code': 'SEND',
            'chunk': chunk,
            'chunk_number': chunk_number,
            'chat_id': chat_id,
        })

    def send_transfer_end(self, chat_id):
        self.send_json_bytes({
            'code': 'END_SEND',
            'chat_id': chat_id,
        })

    def print_dict(dict):
        for key in dict:
            print(f'{key}: {dict[key]}')

    def reciever_func(self):        
        while True:
            try:
                data = self.client_socket.recv(20000)
            except:
                break
            data = data.decode('utf-8')
            dict = json.loads(data)
            dict = self.decode_dict(dict)
            dict = self.client_encryptor.decrypt_dict(dict)
            print('response decrypted:')
            Client.print_dict(dict)
            match dict['code']:
                case 'SERVER_NEW_MESSAGE':
                    self.app.new_msg_response(dict)    
                case 'SERVER_REGISTRATION':
                    self.app.register_success(dict)
                case 'SERVER_LOGIN':
                    self.app.login_success(dict)
                case 'SERVER_LOGIN_FAILED':
                    self.app.login_failed(dict)
                case 'SERVER_CHATS':
                    self.app.chats_response(dict)
                case 'SERVER_CHAT_ID':
                    self.app.create_chat_response(dict)
                case 'SERVER_MSG_CHAT':
                    self.app.send_msg_response(dict)
                case 'SERVER_MSG_ALL':
                    self.app.all_mgs_response(dict)
                case 'SERVER_JOINED_CHAT':
                    self.app.joined_chat_response(dict)
                case 'SERVER_FILE_PROGRESS':
                    self.app.transfer_progress(dict)
                case _:
                    self.app.print_error(dict['code'], dict['text'])  
    
    def start_reciever(self):
        self.thread = threading.Thread(target=self.reciever_func)
        self.thread.start()

    def stop_reciever (self):
        self.disconnect()
        self.thread.join()

    # def start_sender(self):
    #     thread = threading.Thread(target=self.sender_func)
    #     thread.start()