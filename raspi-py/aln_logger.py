import sys
from math import remainder
import selectors, signal, socket, time
from socket import AF_INET, SOCK_DGRAM
from threading import Lock
from aln.tcp_channel import TcpChannel
from aln.router import Router
from aln.packet import Packet

from urllib.parse import urlparse

def main():
    sel = selectors.DefaultSelector()
    router = Router(sel, "python-logger-01") # TODO dynamic address allocation protocol
    router.start()

    def on_log(packet):
        print('log: ' + packet.data.decode('utf-8'))
    router.register_service("log", on_log)
    
    alnUrl = ""

    if len(sys.argv) > 1:
        print("connecting to:", sys.argv[1])
        alnUrl = sys.argv[1]
    else:
        print("listening to port 8082 for UDP broadcast")

        # listen for broadcast
        s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.bind(('', 8082))
        while True:
            m = s.recvfrom(4096)
            alnUrl = m[0].decode('utf-8')
            break
        s.close()

    o = urlparse(alnUrl)
    protocol = o.scheme
    host = o.hostname
    port = o.port
    malnAddr = o.path
    
    print('parsed url params:', protocol, host, port, malnAddr)

    supportedSchemes = ['tcp+aln', 'tcp+maln', 'tls+aln', 'tls+maln']
    if protocol not in supportedSchemes:
        print(protocol, "not supported. supported schemes are", supportedSchemes)
        return

    # connect to an existing node in the network
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))

    # join the network
    ch = TcpChannel(sock)
    if "maln" in protocol: # support multiplexed links
        ch.send(Packet(destAddr=malnAddr))
    router.add_channel(ch)
    
    # listen for ^C
    lock = Lock()
    def signal_handler(signal, frame):
        router.close()
        sel.close()
        lock.release() # release the main thread
    signal.signal(signal.SIGINT, signal_handler)

    # release lock to exit if channel is closed
    ch.on_close(lambda x: lock.release())

    # hang until ^C
    lock.acquire() # take the lock
    lock.acquire() # enqueue a lock request to block the application
    lock.release() # pong response recieved; clear the lock and exit

if __name__ == "__main__":
    main()
