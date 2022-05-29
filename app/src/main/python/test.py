import sys
import requests
from requests_toolbelt import MultipartEncoder
from os.path import dirname, join
from com.chaquo.python import Python
import uuid
import json
import base64

def translate(target_Language,image_dir,file_name,efile_name):

    image_dirs = str(image_dir)
    filenames = join(dirname(image_dirs),file_name)

    data = {
        'source': target_Language,
        'target': 'ko',
        'image': (filenames, open(filenames, 'rb'), 'application/octet-stream', {'Content-Transfer-Encoding': 'binary'})
    }
    m = MultipartEncoder(data, boundary=uuid.uuid4())

    headers = {
        "Content-Type": m.content_type,
        "X-NCP-APIGW-API-KEY-ID": "w5lgfrssck",
        "X-NCP-APIGW-API-KEY": "tct9yx0oteeuixAnAdIOETTtKiZFhixSLzNw3vvM"
    }

    url = "https://naveropenapi.apigw.ntruss.com/image-to-image/v1/translate"
    res = requests.post(url, headers=headers, data=m.to_string())

    resObj = json.loads(res.text)
    imageStr = resObj.get("data").get("renderedImage")

    return imageStr

