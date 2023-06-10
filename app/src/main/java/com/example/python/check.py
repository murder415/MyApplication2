import openai
import time

def set_openai_api_key(api_key):
    openai.api_key = api_key
    
def call_openai_api(retry_count=2):
    print("체크중입니다!!!")
    try:
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[
                    {"role": "system", "content": "You are a helpful assistant."},
                    {"role": "user", "content": "Hello, chatbot!"}
                ]
        )
        print(response)
        print("참입니다!!!")

        return True
    except openai.error.APIError as e:
        if retry_count > 0:
            print("API 호출 오류:", e)
            print("다시 시도합니다...")
            time.sleep(2)
            return call_openai_api(model, msg, retry_count - 1)
        else:
            print("API 호출 오류: 재시도 횟수를 초과했습니다.")
            return False
    except openai.error.RateLimitError as e:
        if retry_count > 0:
            print("API 호출 오류:", e)
            print("다시 시도합니다...")
            time.sleep(20)
            return call_openai_api(model, msg, retry_count - 1)
        else:
            print("API 호출 오류: 재시도 횟수를 초과했습니다.")
            return False
