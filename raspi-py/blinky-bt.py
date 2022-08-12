import bluetooth
import selectors, signal, socket, time
from socket import AF_INET, SOCK_DGRAM
from threading import Lock
from aln.btchannel import BtChannel
from aln.router import Router
from aln.packet import Packet

def main():
    sel = selectors.DefaultSelector()
    router = Router(sel, "python-client-1") # TODO dynamic address allocation protocol
    router.start()


    server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    server_sock.bind(("", bluetooth.PORT_ANY))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]

    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

    bluetooth.advertise_service(server_sock, "AlnServer", service_id=uuid,
                                service_classes=[uuid, bluetooth.SERIAL_PORT_CLASS],
                                profiles=[bluetooth.SERIAL_PORT_PROFILE])

    print("Waiting for connection on RFCOMM channel", port)


    def on_chat(packet):
        print('chat: ' + packet.data.decode('utf-8'))
    router.register_service("chat", on_chat)
    
    def on_led_control(packet):
        # import pdb; pdb.set_trace()
        data = packet.data.decode('utf-8')
        print('led control: ' + data)

    router.register_service("8x8_led_control", on_led_control)


    # listen for ^C
    lock = Lock()
    def signal_handler(signal, frame):
        lock.release() # release the main thread
    signal.signal(signal.SIGINT, signal_handler)

    while True:
        sock, client_info = server_sock.accept()
        print("Accepted connection from", client_info)

        # join the network
        router.add_channel(BtChannel(sock))


    # # hang until ^C
    # lock.acquire() # take the lock
    # lock.acquire() # enqueue a lock request to block the application
    # lock.release() # pong response recieved; clear the lock and exit

    router.close()
    sel.close()

    bluetooth.stop_advertising(server_sock)
    server_sock.close()


if __name__ == "__main__":
    main()
