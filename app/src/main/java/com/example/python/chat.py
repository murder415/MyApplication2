import openai
import time
import ast

global attributes
global answer


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
       #print("API 호출 오류:", e)
       #print("다시 시도합니다...")
       time.sleep(2)
       return call_openai_api(model, msg)
   except openai.error.RateLimitError as e:
        #print("API 호출 오류:", e)
        #print("다시 시도합니다...")
        time.sleep(20)  # 20초 대기
        return call_openai_api(model, msg)




def return_answer():
     global answer
     return answer
        
     

def main():
   
   global attributes
   global answer
   

  

   # 모델 - GPT 3.5 Turbo 선택
   model = "gpt-3.5-turbo"

   # 질문 작성하기
   query = "hello!"

   original_prompt = """
   Please You have to act like you're the following person

1. Talk like a veteran in his 60s who survived zombie apocalypse water.

2. You have to speak in a way that sounds like you know the main character well

3. Wrap all conversation output in code blocks."""


   # 메시지 설정하기
   messages = [
       {
           "role": "system",
           "content": original_prompt
       },
       {
           "role": "user",
           "content": query
       }
   ]

   response = call_openai_api(model, messages)

   if 'choices' in response and len(response['choices']) > 0:
       answer2 = response['choices'][0]['message']['content']
       print(answer2)

   answer = answer2

   return messages

def get_input(user_input, msg):
   global attributes
   global answer
   global turn_num
   messages = msg

   model = "gpt-3.5-turbo"


   print("====================")
   print(type(messages))
   print("====================")

   if isinstance(messages, list):
         for message in messages:
              if not isinstance(message, dict):
                  for i in range(len(messages)):
                      message = ast.literal_eval(messages[i])
                      messages[i] = message

         messages = messages
   else:
         messages = list(messages)
         for i in range(len(messages)):
             message = ast.literal_eval(messages[i])
             messages[i] = message

   print(type(messages))

   messages.append(
       {
           "role": "assistant",
           "content": answer
       }
   )

   messages.append(
       {
           "role": "user",
           "content": user_input
       }
   )

   response = call_openai_api(model, messages)
   answer = response['choices'][0]['message']['content']

       

   return messages
'''
set_openai_api_key("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")
messages = main()
while True:
     name = input("대화를 계속하세요: ")

     messages = get_input(name, messages)
     print(return_answer())
'''


