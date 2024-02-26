# blinky-aln
Multi-platform mesh networked blinky LED controller


sudo nano /etc/systemd/system/dbus-org.bluez.service
ExecStart=/usr/lib/bluetooth/bluetoothd -C

sudo sdptool add SP
sudo chmod o+rw /var/run/sdp


sudo usermod -G bluetooth -a pi
sudo chgrp bluetooth /var/run/sdp