import openai
import time
import re

# OpenAI API 키 설정
def set_openai_api_key(api_key):
    openai.api_key = api_key


def check_korean_ratio(text):
    korean_pattern = re.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]")
    special_pattern = re.compile("[\W]")
    numeric_pattern = re.compile("[\d]")

    korean_count = 0
    special_count = 0
    numeric_count = 0
    total_count = len(text)

    for char in text:
        if korean_pattern.match(char):
            korean_count += 1
        elif special_pattern.match(char):
            special_count += 1
        elif numeric_pattern.match(char):
            numeric_count += 1

    korean_ratio = korean_count / total_count
    special_ratio = special_count / total_count
    numeric_ratio = numeric_count / total_count
    total_ratio = korean_ratio + special_ratio + numeric_ratio

    return total_ratio


def check_english_ratio(text):
    english_pattern = re.compile("[A-Za-z]")
    special_pattern = re.compile("[\W]")
    numeric_pattern = re.compile("[\d]")

    english_count = 0
    special_count = 0
    numeric_count = 0
    total_count = len(text)

    for char in text:
        if english_pattern.match(char):
            english_count += 1
        elif special_pattern.match(char):
            special_count += 1
        elif numeric_pattern.match(char):
            numeric_count += 1

    english_ratio = english_count / total_count
    special_ratio = special_count / total_count
    numeric_ratio = numeric_count / total_count

    total_ratio = english_ratio +  special_ratio +  numeric_ratio
    print(total_ratio)

    return total_ratio


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

def get_response(input_text):
   model = "gpt-3.5-turbo"

   #print("123456789")
   #print(input_text)

   english_pattern = re.compile("[A-Za-z]+")
   if check_korean_ratio(input_text) > 0.75:






      input_pmt = f"translate {input_text} into english, Show only translated results. No korean in the result"
      print(input_pmt)

      messages = [
         {
               "role": "system",
               "content": "You are a helpful assistant who is good at translating."
         },
         {
               "role": "user",
               "content": input_pmt
         }

      ]


      #print("1")
      #print(input_pmt)

      korean_pattern = re.compile("[\uac00-\ud7a3]+")

      while True:
          try:
              response = call_openai_api(model, messages)

              translate_pmt = response['choices'][0]['message']['content']
              print(translate_pmt)

              if check_english_ratio(translate_pmt) > 0.75:
                 break
              else:
                 print("한글이 탐지되었습니다.")
          except openai.error.RateLimitError as e:
              print("Rate limit reached. Waiting for 20 seconds...")
              time.sleep(20)
   else:
      #print("2")
      translate_pmt = input_text

   #print(translate_pmt)

   time.sleep(20)


   # 원래 내용
   original_prompt = """Please create a text adventure story, following the rules listed below:

presentation rules:

1. The story output will always show 'title', 'theme' ,'world view', 'story summary' and 'etc'

2. Wrap all story output in code blocks.

3. The subject is {translate_pmt}, please write the subject in a little more detail you want so that it can be used as a text adventure game story.

4. The world view of the topic must be written within 2 lines

5. The Story Summary of the topic must be written within 6 lines"""

   original_prompt = original_prompt.replace('{translate_pmt}', translate_pmt)

   messages = [
       {
           "role": "system",
           "content": original_prompt
       },
       {
           "role": "user",
           "content": "start create."
       }
   ]

   #print("2")
   # OpenAI API 호출
   while True:
       try:
           response = call_openai_api(model, messages)

           return_response = response['choices'][0]['message']['content']
           #print(return_response)
           break
       except openai.error.RateLimitError as e:
           print("Rate limit reached. Waiting for 20 seconds...")
           time.sleep(20)
       except openai.error.APIError as e:
          print("API 호출 오류:", e)
          print("다시 시도합니다...")
          time.sleep(2)



   if not('Story Summary' in return_response or 'STORY SUMMARY' in return_response):
      print("No Summary. Sending original prompt again.")
      time.sleep(20)
      return_response = get_response(translate_pmt)
      return return_response

   if not('Theme' in return_response or 'THEME' in return_response):
      print("No Theme. Sending original prompt again.")
      time.sleep(20)
      return_response = get_response(translate_pmt)
      return return_response

   if not('Worldview' not in return_response or 'WORLDVIEW' not in return_response):
      print("No Worldview. Sending original prompt again.")
      time.sleep(20)
      return_response = get_response(translate_pmt)
      return return_response





   #print("3")
   return return_response

def categorize_text(text):
    # #, :, 등의 특수문자 제거
    text = text.replace('#', '')

    categorized_dict = {}

    # Theme 추출
    theme_start = text.lower().find("theme")
    theme_end = text.lower().find("world view")
    theme = text[theme_start + len("theme"):theme_end].strip()
    theme = theme.replace(':', '').strip()
    categorized_dict["Theme"] = theme

    # World View 추출
    worldview_start = text.lower().find("world view")
    worldview_end = text.lower().find("story summary")
    worldview = text[worldview_start + len("world view"):worldview_end].strip()
    worldview = worldview.replace(':', '').strip()
    categorized_dict["World View"] = worldview

    # Story Summary 추출
    summary_start = text.lower().find("story summary")
    if 'etc'in text.lower():
        summary_end = text.lower().find("etc")
        summary = text[summary_start + len("story summary"):summary_end].strip()
    else:
        summary = text[summary_start + len("story summary"):].strip()



    categorized_dict["Sample Story"] = summary

    return categorized_dict




