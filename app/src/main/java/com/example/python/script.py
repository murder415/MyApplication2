import openai
from PyKakao import Karlo
from PIL import Image
import time
import base64
import requests
from io import BytesIO


def set_openai_api_key(api_key):
    openai.api_key = api_key


def call_openai_api(model, msg):
   try:
       response = openai.ChatCompletion.create(
           model=model,
           messages=msg
       )
       return response
   except openai.error.APIError as e:
       print("API 호출 오류:", e)
       print("다시 시도합니다...")
       time.sleep(2)
       return call_openai_api(model, msg)
   except openai.error.RateLimitError as e:
        print("API 호출 오류:", e)
        print("다시 시도합니다...")
        time.sleep(20)  # 20초 대기
        return call_openai_api(model, msg)
   except openai.APIConnectionError as e:
           print("API 호출 오류:", e)
           print("다시 시도합니다...")
           time.sleep(20)  # 20초 대기
           return call_openai_api(model, msg)




def image_to_bytes(image_data):
    try:
        image_bytes = base64.b64decode(image_data)
        return image_bytes
    except Exception as e:
        print("Error converting image to bytes:", str(e))
        return None

    return title_line



def main(worldview):
   KAKAO_API_KEY = "9e1f646d2210318f877f3459be8d1efd"
   global title_line
   # 발급받은 API 키 설정



   # Karlo API 인스턴스 생성
   karlo = Karlo(service_key = KAKAO_API_KEY)

   # 모델 - GPT 3.5 Turbo 선택
   model = "gpt-3.5-turbo"

   # 질문 작성하기
   query = worldview

   # 메시지 설정하기
   messages = [
       {
           "role": "system",
           "content": "You are a helpful assistant who is good at detailing."
       },
       {
           "role": "user",
           "content": query
       }
   ]

   response = call_openai_api(model, messages)
   answer = response['choices'][0]['message']['content']




   messages.append(
       {
           "role": "assistant",
           "content": answer
       },
   )

   # 사용자 메시지 추가
   messages.append(
       {
           "role": "user",
           "content": "위 내용을 바탕으로 모습을 더 자세히 상상해서 묘사해주세요."
       }
   )

   # ChatGPT API 호출하기
   response = call_openai_api(model, messages)
   answer2 = response['choices'][0]['message']['content']

   # 새 메시지 구성
   messages = [
       {
           "role": "system",
           "content": "You are a helpful assistant who is good at translating."
       },
       {
           "role": "assistant",
           "content": answer2
       }
   ]

   # 사용자 메시지 추가
   messages.append(
       {
           "role": "user",
           "content": "영어로 번역해주세요."
       }
   )

   # ChatGPT API 호출하기
   response = call_openai_api(model, messages)
   answer3 = response['choices'][0]['message']['content']

   # 새 메시지 구성
   messages = [
       {
           "role": "system",
           "content": "You are an assistant who is good at creating prompts for image creation."
       },
       {
           "role": "assistant",
           "content": answer3
       }
   ]

   # 사용자 메시지 추가
   messages.append(
       {
           "role": "user",
           "content": "Make sure to express it within 1-2 lines. Condense up to 4 outward description to focus on nouns and adjectives separated by ,"
       }
   )

   # ChatGPT API 호출하기
   response = call_openai_api(model, messages)
   answer4 = response['choices'][0]['message']['content']


   messages = [
         {
            "role": "system",
            "content": "You are an assistant who is good at creating prompts for image creation."
         },
         {
            "role": "assistant",
            "content": worldview
         }
   ]
   # 사용자 메시지 추가
   messages.append(
         {
            "role": "user",
            "content": "Make sure to express it within 1-2 lines, Express the given description with a maximum of 10 nouns and adjectives"
         }
   )

   response = call_openai_api(model, messages)
   answer10 = response['choices'][0]['message']['content']
   bytes_text = answer10.encode('utf-8')
   byte_count = len(bytes_text)
   while True:
       if 'sorry' in answer10 or byte_count > 170:
           response = call_openai_api(model, messages)
           answer10 = response['choices'][0]['message']['content']
           bytes_text = answer10.encode('utf-8')
           byte_count = len(bytes_text)
       else:
           break



   # ChatGPT API 호출하기

   #print("실행")


   # 이미지 생성을 위한 프롬프트
   params = ", concept art, realistic lighting, ultra-detailed, 8K, photorealism, digital art"
   prompt = f"{answer10}, {answer4}{params}"
   pmt = f"{answer10}, {answer4}"

   img_dict = karlo.text_to_image(prompt, 1)

   """print(prompt)

   while True:

       try:

           response = openai.Image.create(
               prompt=prompt,
               n=1,
               size="1024x1024"
           )
           image_url = response['data'][0]['url']
           break
       except openai.error.RateLimitError as e:
           print("Rate limit reached. Waiting for 20 seconds...")
           time.sleep(20)

   return image_url
   """

   if img_dict.get("images") is None:
      while True:
         #print("---------------1-1-1-1--1-1-1-1-1-1--")


         # 새 메시지 구성
         messages = [
             {
                 "role": "system",
                 "content": "You are an assistant who is good at creating prompts for image creation."
             },
             {
                 "role": "assistant",
                 "content": pmt
             }
         ]

         # 사용자 메시지 추가
         messages.append(
             {
                 "role": "user",
                 "content": "Make sure to express it within 1-2 lines, Condense up to 160 bytes outward description to focus on nouns and adjectives separated by ,"
             }
         )

         response = call_openai_api(model, messages)
         answer20 = response['choices'][0]['message']['content']

         while True:
             if 'sorry' in answer20 or byte_count > 170:
                     if "Condensed" in answer20:
                         break
                     else:
                         response = call_openai_api(model, messages)
                         answer20 = response['choices'][0]['message']['content']
             else:
                 break


         # ChatGPT API 호출하기


         lines = answer20.replace('\\', '').split('\n')
         #print(lines)
         for line in lines:
             if "Condensed" in answer20:
                 start_index = answer20.find("Condensed")
                 if start_index == -1:
                    continue
                 start_index += len("Condensed")
                 end_index = answer20.find("\n", start_index)
                 if end_index == -1:
                    continue
                 answer20 = answer20[start_index:end_index].strip()

         prompt = f"{answer20},{params}"
         #print("+++++++++++++++++++++++++++++++++++++++")
         #print(prompt)
         #print("+++++++++++++++++++++++++++++++++++++++")

         img_dict = karlo.text_to_image(prompt, 1)
         #print(img_dict.get("images"))
         if img_dict.get("images") is not None:
            break



   # 생성된 이미지 정보
   img_str = img_dict.get("images")[0].get('image')
   #print(img_str)



   '''img = image_to_bytes(img_str)
   #print(img)



   # base64 string을 이미지로 변환
   img = karlo.string_to_image(base64_string = img_str, mode = 'RGBA')
   img

   #print(imageToS)

   #print(type(img))'''
   print(img_str)

   return img_str


