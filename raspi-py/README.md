# Setting up python client

```
apt install python-bluez bluetooth libbluetooth-dev
python3 -m pip install pybluez
```

Problem:
```
Traceback (most recent call last):
  File "rfcomm-server.py", line 20, in <module>
    profiles = [ SERIAL_PORT_PROFILE ],
  File "/usr/lib/python2.7/site-packages/bluetooth/bluez.py", line 176, in advertise_service
    raise BluetoothError (str (e))
bluetooth.btcommon.BluetoothError: (2, 'No such file or directory')
```
Solution:
https://stackoverflow.com/questions/36675931/bluetooth-btcommon-bluetootherror-2-no-such-file-or-directory

Run bluetooth in compatibility mode, by modifying `/etc/systemd/system/dbus-org.bluez.service`

Change

```
ExecStart=/usr/lib/bluetooth/bluetoothd
```
into
```
ExecStart=/usr/lib/bluetooth/bluetoothd -C
```

Then reload the deam on and add the Serial Port Profile

```
sudo systemctl daemon-reload
sudo systemctl restart bluetooth
sudo sdptool add SP
```

make accessible without running apps as sudo
```
sudo chmod o+rw /var/run/sdp
```
