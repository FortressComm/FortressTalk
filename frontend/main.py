from encrypt.asymmetric import AsymmCipher
from encrypt.symmetric import SymmCipher


def reciev(encryptedPacket, my_private_key):
    cp, cpSymmKey = encryptedPacket
    

def main():
    asymCipher = AsymmCipher()
    asymCipher.gen_private_public_key()
    symCipher = SymmCipher()
    symCipher.gen_key_iv()
    message = b'hi Vlad'
    cp = symCipher.encrypt(message)
    cpSymmKey = asymCipher.encrypt(symCipher.key)
    encryptedPacket = (cp, cpSymmKey)
    reciev(encryptedPacket, asymCipher.private_key)
    


if __name__ == '__main__':
    main()