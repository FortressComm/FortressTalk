import tkinter as tk
from tkinter import messagebox, filedialog
from Client.ClientEncryptor import ClientEncryptor
from Encryption.Asymmetric import AsymCipher
from Client.Client import Client
import time


class ChatApp:
    def __init__(self, root):
        self.root = root
        self.chats = []
        self.connect()
        self.init_view(root)
        self.root_settings()
        

    def on_closing(self):
        self.client.stop_reciever()
        self.root.destroy()

    def root_settings(self):
        self.root.protocol("WM_DELETE_WINDOW", self.on_closing)
        

    def connect(self):
        self.client = Client("127.0.0.1", 65432, self)
        self.client.connect()
        self.client.start_reciever()

    def init_view(self, root):
        self.root.title("Chat App")
        self.current_user = None
        self.current_chat = None

        # Set window size
        self.root.geometry("400x400")

        # Create top menu
        self.menu_bar = tk.Menu(self.root)
        self.root.config(menu=self.menu_bar)

        # Create File menu
        self.file_menu = tk.Menu(self.menu_bar, tearoff=0)
        self.file_menu.add_command(label="Login", command=self.show_login_page)
        self.file_menu.add_command(label="Register", command=self.show_register_page)
        self.file_menu.add_command(label="Chat", command=self.show_chat_page)
        self.file_menu.add_command(label="Exit", command=self.root.quit)
        self.menu_bar.add_cascade(label="File", menu=self.file_menu)

        # Create chat page
        self.chat_frame = tk.Frame(self.root, width=400, height=400)

        self.chat_name_label = tk.Label(self.chat_frame, text="Chat Name:")
        self.chat_name_label.grid(row=0, column=0, padx=10, pady=10)

        self.chat_name_var = tk.StringVar()
        self.chat_name_dropdown = tk.OptionMenu(self.chat_frame, self.chat_name_var, None,*self.chats, command=self.on_chat_select)
        self.chat_name_dropdown.grid(row=0, column=1, padx=10, pady=10)

        self.chat_area = tk.Text(self.chat_frame, width=40, height=16)
        self.chat_area.configure(state="disabled")  # Set text field as read-only
        self.chat_area.grid(row=1, column=0, columnspan=2, padx=10, pady=10)

        self.scrollbar = tk.Scrollbar(self.chat_frame, width=20)
        self.scrollbar.grid(row=1, column=2, sticky="NS", padx=0, pady=10)
        self.chat_area.config(yscrollcommand=self.scrollbar.set)
        self.scrollbar.config(command=self.chat_area.yview)

        self.entry = tk.Entry(self.chat_frame)
        self.entry.grid(row=2, column=0, padx=10, pady=10, sticky="EW")

        self.send_button = tk.Button(self.chat_frame, text="Send", command=self.send_message)
        self.send_button.grid(row=2, column=1, padx=10, pady=10, sticky="W")

        self.attach_button = tk.Button(self.chat_frame, text="Attach File", command=self.attach_file)
        self.attach_button.grid(row=2, column=1, padx=80, pady=10, sticky="W")

        # Create login page
        self.login_frame = tk.Frame(self.root, width=400, height=400)

        self.login_label = tk.Label(self.login_frame, text="Username:")
        self.login_label.pack()

        self.login_entry = tk.Entry(self.login_frame)
        self.login_entry.pack()

        self.password_label = tk.Label(self.login_frame, text="Password:")
        self.password_label.pack()

        self.password_entry = tk.Entry(self.login_frame, show="*")
        self.password_entry.pack()

        self.login_button = tk.Button(self.login_frame, text="Login", command=self.login)
        self.login_button.pack()

        # Create register page
        self.register_frame = tk.Frame(self.root, width=400, height=400)

        self.register_label = tk.Label(self.register_frame, text="Username:")
        self.register_label.pack()

        self.register_entry = tk.Entry(self.register_frame)
        self.register_entry.pack()

        self.register_password_label = tk.Label(self.register_frame, text="Password:")
        self.register_password_label.pack()

        self.register_password_entry = tk.Entry(self.register_frame, show="*")
        self.register_password_entry.pack()

        self.register_button = tk.Button(self.register_frame, text="Register", command=self.register)
        self.register_button.pack()

        self.show_login_page()

    def on_chat_select(self, value):
        print(value)

    def show_login_page(self):
        self.register_frame.pack_forget()
        self.chat_frame.pack_forget()
        self.login_frame.pack()

    def show_register_page(self):
        self.login_frame.pack_forget()
        self.chat_frame.pack_forget()
        self.register_frame.pack()

    def show_chat_page(self):
        self.login_frame.pack_forget()
        self.register_frame.pack_forget()
        self.chat_frame.pack()
        self.current_chat = self.chat_name_var.get()

    def login(self):
        username = self.login_entry.get()
        password = self.password_entry.get()
        if username and password:
            self.current_user = username
            self.client.send_login(username, password)
            # self.show_messagebox("Login Successful", f"Welcome, {username}!")
            # self.login_frame.pack_forget()
            # self.chat_frame.pack()

    def register(self):
        username = self.register_entry.get()
        password = self.register_password_entry.get()
        if username and password:
            self.current_user = username
            self.client.send_register(username, password)
            

    def send_message(self):
        message = self.entry.get()
        if message:
            chat_info = f"{self.current_user} ({self.current_chat}): {message}"
            self.chat_area.configure(state="normal")  # Set text field as editable temporarily
            self.chat_area.insert(tk.END, chat_info + "\n")
            self.chat_area.configure(state="disabled")  # Set text field as read-only again
            self.entry.delete(0, tk.END)

    def attach_file(self):
        file_path = filedialog.askopenfilename()
        if file_path:
            chat_info = f"{self.current_user} ({self.current_chat}) attached file: {file_path}"
            self.chat_area.configure(state="normal")  # Set text field as editable temporarily
            self.chat_area.insert(tk.END, chat_info + "\n")
            self.chat_area.configure(state="disabled")  # Set text field as read-only again

    def receive_message(self, message):
        self.chat_area.configure(state="normal")  # Set text field as editable temporarily
        self.chat_area.insert(tk.END, message + "\n")
        self.chat_area.configure(state="disabled")  # Set text field as read-only again
        self.chat_area.see(tk.END)  # Scroll to the end of the chat area

    def register_success(self, data):
        self.show_messagebox("SERVER_REGISTRATION", data['text'])
        self.register_frame.pack_forget()
        self.chat_frame.pack()

    def register_failed(self, data):
        self.current_user = None

    def login_failed(self, data):
        self.current_user = None
        self.show_messagebox("SERVER_LOGIN_FAILED", data['text'])

    def login_success(self, data):
        self.show_messagebox("SERVER_LOGIN", data['text'])
        self.login_frame.pack_forget()
        self.chat_frame.pack()

    def show_messagebox(self, title, content):
        messagebox.showinfo(title, content)

def start_ui():
    root = tk.Tk()
    app = ChatApp(root)

    # Bind mouse wheel scrolling to the chat area
    app.chat_area.bind("<MouseWheel>", lambda event: app.chat_area.yview_scroll(int(-1 * (event.delta / 120)), "units"))

    # Configure the scrollbar's appearance
    app.scrollbar.config(troughcolor="#D3D3D3", activebackground="#A9A9A9")

    root.mainloop()



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
    start_ui() 

    


if __name__ == '__main__':
    main()