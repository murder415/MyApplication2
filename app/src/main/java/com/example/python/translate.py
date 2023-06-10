import openai
import time
import re
# OpenAI API 키 설정
def set_openai_api_key(api_key):
    openai.api_key = api_key



def call_openai_api(model, msg):
   model = "gpt-3.5-turbo"

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

def en2ko(input):
    input_pmt = f"translate {input} into korean, Show only translated results. No English in the result"
    model = "gpt-3.5-turbo"


    print("1")
    print(input_pmt)

    while True:
        try:
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
            response = call_openai_api(model, messages)


            result = response['choices'][0]['message']['content']

            korean_pattern = re.compile("[\uac00-\ud7a3]+")
            print(result)
            threshold = 0.75


            # 결과가 한국어인지 확인
            if result:
                if check_korean_ratio(result) >= threshold:
                    return result
                else:
                    if "(" in result and ")" in  result:

                        while '(' in  result and ')' in result:
                            start =  result.find('(')
                            end =  result.find(')')
                            result =  result[:start] +  result[end + 1:]
                        return result

                    print("Empty translation. Retrying...")
                    time.sleep(1)
                    continue

        # 한도 에러가 발생하면 잠시 대기한 후 다시 시도
        except openai.error.RateLimitError:
            print("Rate limit exceeded. Waiting for 20 seconds...")
            time.sleep(20)
            continue

        # 다른 오류가 발생하면 잠시 대기한 후 다시 시도
        except Exception as e:
            print(f"Error: {e}. Retrying...")
            time.sleep(1)
            continue

def ko2en(input):
    input_pmt = f"translate {input} into english, Show only translated results. No korean in the result"
    model = "gpt-3.5-turbo"


    print("1")
    print(input_pmt)

    while True:
        try:
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
            response = call_openai_api(model, messages)
            result = response['choices'][0]['message']['content']

            english_pattern = re.compile("[A-Za-z]+")
            print(result)

            threshold = 0.75

            # 결과가 영어인지 확인
            if result:
                if check_english_ratio(result) >= threshold:
                    return result

                else:
                    if "(" in result and ")" in  result:

                        while '(' in  result and ')' in result:
                            start =  result.find('(')
                            end =  result.find(')')
                            result =  result[:start] +  result[end + 1:]
                        return result
                    print(check_english_ratio(result) )
                    print("Empty translation. Retrying...")
                    time.sleep(1)
                    continue

        # 한도 에러가 발생하면 잠시 대기한 후 다시 시도
        except openai.error.RateLimitError:
            print("Rate limit exceeded. Waiting for 20 seconds...")
            time.sleep(20)
            continue

        # 다른 오류가 발생하면 잠시 대기한 후 다시 시도
        except Exception as e:
            print(f"Error: {e}. Retrying...")
            time.sleep(1)
            continue


