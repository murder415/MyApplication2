import openai
import time

global attributes


def set_openai_api_key(api_key):
    openai.api_key = api_key

def find_attr(attributes):
    missing_attributes = False
    expected_attributes = []

    if 'Gender' in attributes:
        gender = attributes['Gender'].lower()
        if gender != 'female' and gender != 'male':
            missing_attributes = True
            expected_attributes.append('Gender')

    if 'Age' in attributes:
        try:
            age = int(attributes['Age'])
        except ValueError:
            missing_attributes = True
            expected_attributes.append('Age')

    for attr in ['Name', 'Residence', 'Hobbies', 'Personality', 'Role']:
        if attr not in attributes:
            expected_attributes.append(attr)
            missing_attributes = True
            
    print("+++++++++++++++++++++++++")
    print(expected_attributes)
    print("+++++++++++++++++++++++++")
    
    print(missing_attributes)
    return missing_attributes


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

def extract_attributes_from_answer(answer):
    attributes = {}
    lines = answer.split("\n")
    parts = []
    for line in lines:
        if ":" in line:
            if "**" in line:
               line = line.replace("**","")
               parts = line.split(": ", 1)
               if len(parts) >= 2:
                    key = parts[0].strip()
                    value = ':'.join(parts[1:]).strip()
                    attributes[key] = value
            elif "'" in line:
               line = line.replace("'","")
               parts = line.split(": ", 1)
               if len(parts) >= 2:
                    key = parts[0].strip()
                    value = ':'.join(parts[1:]).strip()
                    attributes[key] = value
            else:
                parts = line.split(": ", 1)
                if len(parts) >= 2:
                    key = parts[0].strip()
                    value = ':'.join(parts[1:]).strip()
                    attributes[key] = value
            
        elif ":" not in line:
             if "**" in line:
                  line = line[2:]
                  parts = line.split("**", 1)
                  if len(parts) >= 2:
                    key = parts[0].strip()
                    value = '**'.join(parts[1:]).strip()
                    attributes[key] = value
             elif "'" in line:
                   line = line[1:]
                   parts = line.split("'", 1)
                   if len(parts) >= 2:
                        key = parts[0].strip()
                        value = "'".join(parts[1:]).strip()
                        attributes[key] = value

             
    return attributes





def return_attributes():
   global attributes
   return attributes

              
        
     

def main(topic):
   
   global attributes
   

  

   # 모델 - GPT 3.5 Turbo 선택
   model = "gpt-3.5-turbo"

   # 질문 작성하기
   query = "make character"

   original_prompt = """
   Please creation character, following the rules listed below:

presentation rules:

1.The person information output will always show 'name', 'age', 'residence', 'gender', 'hobbies', 'personality' ,'role' and 'etc'.

2. Wrap all person information output in code blocks.

3. The character lives in the {change_topic} world. Enter the character's personal information so that the character feels like he is living in the world.

4. write down "personality" and "role" in more detail."""

   original_prompt = original_prompt.replace('{change_topic}', topic)
   print(original_prompt)

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
       answer = response['choices'][0]['message']['content']
       print(answer)

   print("-----------------------------")
   attributes = extract_attributes_from_answer(answer)
   print(attributes)
   print("-----------------------------")
   
   missing_attributes = find_attr(attributes)

   # Handle missing attributes
   if missing_attributes:
       print("inner loop")
       #print("Some attributes are missing. Sending original prompt again.")
       time.sleep(20)
       messages = main(topic)  # Send the original prompt again
       return messages
       




