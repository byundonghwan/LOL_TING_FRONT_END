import os
import sys
import cv2
import io
import base64
import np
import requests
import json
from PIL import Image

from flask import Flask, request,jsonify
app = Flask(__name__)

@app.route('/img_data',methods=['POST'])
def face_cascade():
    params = request.get_json()
    #print(params['nameValuePairs']['data'])
    
    if(cv_scan_face(params['nameValuePairs']['data'])!=0):
        return jsonify({"img_byte": "1"})
    elif(naver_scan_face(params['nameValuePairs']['data'])!=0):
        return jsonify({"img_byte": "1"})
    else:
        return jsonify({"img_byte": "0"})

    

   
# Take in base64 string and return cv image
def stringToRGB(base64_string):
    imgdata = base64.b64decode(base64_string)
    dataBytesIO = io.BytesIO(imgdata)
    image = Image.open(dataBytesIO)
    return cv2.cvtColor(np.array(image), cv2.COLOR_BGR2RGB)

def cv_scan_face(base64_string):
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_alt2.xml')

    img = stringToRGB(base64_string) # 이미지 파일 경로

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    faces = face_cascade.detectMultiScale(gray, 1.3, 5)
    return len(faces)

def naver_scan_face(base64_string):
   
    # Decode base64 String Data
    decodedData = base64.b64decode((base64_string))
  
    # Write Image from Base64 File
    imgFile = open('image.jpeg', 'wb')
    imgFile.write(decodedData)
    imgFile.close()
    
    imgFile = Image.open('images.jpeg')
    img_resize = imgFile.resize((256, 256))
    img_resize.save('/home/side/img_server/image.jpeg')
    print(imgFile.size)
    #imgFile.save(os.path.join("/home/side/img_server", 'image.jpeg'), 'JPEG', qualty=10)
    imgFile.close()
        
    client_id = "yFoACaV8ws608Xswckpa"
    client_secret = "LVr6f3L4t8"
    url = "https://openapi.naver.com/v1/vision/face"

    files = {'image': open('/home/side/img_server/image.jpeg', 'rb')}
    
    headers = {'X-Naver-Client-Id': client_id, 'X-Naver-Client-Secret': client_secret }
    response = requests.post(url,  files=files, headers=headers)
    rescode = response.status_code
    

    if(rescode==200):
        print (response.text)
        jsonObject = json.loads(response.text)
        return jsonObject.get('info').get('faceCount')
    else:
        print("Error Code:" + str(rescode))
    
if __name__ == '__main__':
    app.run(use_reloader=True)
