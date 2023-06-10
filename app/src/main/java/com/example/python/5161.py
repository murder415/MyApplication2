import requests
import json
 
text = '안녕하세요'
source = 'kr'
target = 'en'
 
url = 'https://dapi.kakao.com/v2/translation/translate'
headers = {'Authorization': ' '}
data = {'src_lang': source, 'target_lang': target, 'query': text}
 
response = requests.post(url=url, headers=headers, data=data)
 
if response.status_code == 200:
    result_tmp = response.json()['translated_text']
    result = ''
    for result_x in result_tmp:
        result += result_x[0]
        result += '\n'
    print(result)
else:
    print('Error Code:' + str(response.status_code))
