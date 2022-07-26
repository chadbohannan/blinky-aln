from math import remainder
import selectors, signal, socket, time
from socket import AF_INET, SOCK_DGRAM
from threading import Lock
from aln.tcpchannel import TcpChannel
from aln.router import Router
from aln.packet import Packet

def main():
    sel = selectors.DefaultSelector()
    router = Router(sel, "python-client-1") # TODO dynamic address allocation protocol
    router.start()

    s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('', 8282))
    while True:
        m=s.recvfrom(4096)
        print(m[0])
        parts = m[0].decode('utf-8').split('://')
        if len(parts) != 2 :
            print('failed to parse:', m[0])
            continue
        protocol = parts[0]
        remainder = parts[1]
        parts = remainder.split('/')
        if len(parts) != 2:
            print('failed to parse ', remainder)
            continue
        hostParts = parts[0].split(':')
        if len(hostParts) != 2:
            print('failed to parse:', parts[0])
        host = hostParts[0]
        port = int(hostParts[1])
        malnAddr = parts[1]
        break
    s.close()
    
    print('parsed host:', protocol, host, port, malnAddr)
    

    def on_chat(packet):
        print('chat: ' + packet.data.decode('utf-8'))
    router.register_service("chat", on_chat)
    
    def on_led_control(packet):
        # import pdb; pdb.set_trace()
        data = bytearray(packet.data).decode('utf-8')
        print('led control: ' + data)

    router.register_service("8x8_led_control", on_led_control)

    # connect to an existing node in the network
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))

    # join the network
    ch = TcpChannel(sock)
    time.sleep(0.2)
    # ch.send(Packet(destAddr="da96e57a-6b2a-46d1-9469-4629ddf37c1d"))
    ch.send(Packet(destAddr=malnAddr))
    time.sleep(0.2)
    router.add_channel(TcpChannel(sock))

    # listen for ^C
    lock = Lock()
    def signal_handler(signal, frame):
        router.close()
        sel.close()
        lock.release() # release the main thread
    signal.signal(signal.SIGINT, signal_handler)

    # def on_pong(packet):
    #     print('received:', str(bytes(packet.data)))
    #     # no more packets expected for this context
    #     router.release_context(packet.contextID)         

    # ctxID = router.register_context_handler(on_pong)
    # msg = router.send(Packet(service="name", contextID=ctxID))
    # if msg: print("on send:", msg)

    # hang until ^C
    lock.acquire() # take the lock
    lock.acquire() # enqueue a lock request to block the application
    lock.release() # pong response recieved; clear the lock and exit

if __name__ == "__main__":
    main()
