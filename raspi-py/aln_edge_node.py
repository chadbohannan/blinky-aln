import sys
from math import remainder
import selectors, signal, socket, time
from socket import AF_INET, SOCK_DGRAM
from threading import Lock
from aln.tcp_channel import TcpChannel
from aln.packet import Packet
import time
from urllib.parse import urlparse


def main():    
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
        
    # release lock to exit if channel is closed
    ch.on_close(lambda x: quit())

    while True:
        localtime = time.localtime()
        result = time.strftime("%I:%M:%S %p", localtime)
        print(result)
        ch.send(Packet(service="log", srcAddr="time logger - 01", data=result))
        time.sleep(1)
        


if __name__ == "__main__":
    main()
