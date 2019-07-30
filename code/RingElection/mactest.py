import os
import random
import time
import uuid

generatedPort = []


ids = [
    '450AB',
    '3AF02',
    '3A002',
    '3D468',
    '51290',
    '450AC',
    '870DC',
    '12345',
    '2ACDC',
    '7091A',
    '7091C',
    '7092A',
    '7097F',
    '7191A',
    '51280',
    '52290',
    '51390',
    '51C90',
    '512C0',
    '5129C',
    '512F0',
    '51D90',
    '3A003',
    '3AC02',
    ''' '3A902',
    '3A092',
    '3F002',
    '453AC',
    '450FC',
    '490AC',
    '450A9',
    '050A9',
    '6D0A9',
    'AA003',
    'B1C90',
    'C097F',
    'D50AB',
    'E90AC',
    'F129C',
    '9129C' '''
    
]

try:
    os.remove("UUIDs.txt")
except OSError:
    pass

os.system("javac Node.java")

for i in range(0, 24):
    #id = str(uuid.uuid4()).replace('-',"")
    print(id)
    rand = None
    while(rand is None):
        rand = random.randrange(10000, 12000) if rand not in generatedPort else None
    sendPort = 6000
    if len(generatedPort) > 0:
        sendPort = random.choice(generatedPort)
    generatedPort.append(rand)
    script = "osascript -e 'tell app \"Terminal\" \n do script \"java -cp ~/OneDrive/MSc/MODIS/P2P-new/P2P/ Node " + str(rand) + " " + str(sendPort) + " " + ids[i] + "\"\n end tell'"
    print(script)
    os.system(script)
    print("java node " + str(rand) + " " + str(sendPort) + " " + ids[i])
    time.sleep(2)
    #~/OneDrive/MSc/MODIS/P2P/Aal
