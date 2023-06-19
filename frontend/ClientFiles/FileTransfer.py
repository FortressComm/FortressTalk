class FileSaveSession:

    FILES_CATALOG = './'

    def __init__(self, file_name):
        self.file_name = file_name
        self.chunks: list[tuple[int, bytes]] = list()

    def add_bytes(self, chunk: tuple[int, bytes]):
        self.chunks.append(chunk)

    def save_bytes_to_file(self):
        sorted_chunks = sorted(self.chunks, key=lambda x: x[0])
        buffer = b"".join(item[1] for item in sorted_chunks)

        with open(FileSaveSession.FILES_CATALOG + self.file_name, 'wb') as file:
            file.write(buffer)
