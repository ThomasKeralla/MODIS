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
    '3A902',
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
    '9129C'
]


ids2 = [
    '1ABCD',
    '12342',
    '2BCDE',
    '22222',
    '3CDEF',
    '33333',
    '41234',
    '52345',
    '63456',
    '74567',
    '85678',
    '96789',
    'A789A',
    'B89AB',
    'C9ABC',
    'DABCD',
    'EBCDE',
    'FCDEF',
    '1ABCE',
    '12345',
    '2BCDF',
    '23456',
    '3CDEE',
    '41235',
    '52346',
    '63455',
    '71321'
]

ids3 = [
    '1ABCD',
    '11111',
    '11112',
    '12323',
    '13333',
    '13332',
    '2BCDE',
    '3CDEF',
    '41234',
    '52345',
    '63456',
    '74567',
    '85678',
    '96789',
    'A789A',
    'B89AB',
    'C9ABC',
    'DABCD',
    'EBCDE',
    'FCDEF',
    '1ABCE',
    '12345',
    '2BCDF',
    '23456',
    '3CDEE',
    '41235',
    '52346',
    '63455',
    '71321'
]

def main(list, count):
    if os.system("javac Node.java") is 0:
        for i in range(0, count if len(list) > count else len(list)):
            rand = None
            while(rand is None):
                rand = random.randrange(10000, 12000) if rand not in generatedPort else None
            sendPort = 6000
            if len(generatedPort) > 0:
                sendPort = random.choice(generatedPort)
            generatedPort.append(rand)
            os.system("start cmd /c java Node " + str(rand) + " " + str(sendPort) + " " + list[i])
            print("java Node " + str(rand) + " " + str(sendPort) + " " + list[i])
            time.sleep(1)
    else:
        print("compilation failed")

main(ids, int(input("How many nodes do you want to open? ")))