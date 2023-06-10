import openai
import time
import re
import ast


global answer
global turn_num
global attributes
global messages

turn_num = 0

def set_openai_api_key(api_key):
    openai.api_key = api_key

def find_attr(attributes):
   missing_attributes = False
   expected_attributes = []

   for attr in ['Turn number', 'Difficulty', 'Time period of the day', 'Current day number', 'Weather',
                'Health', 'XP', 'AC', 'Level', 'Location', 'Description','Gold','Inventory','Abilities', 'Quest','Possible Commands','Story']:

       if attr not in attributes:
           expected_attributes.append(attr)

           missing_attributes = True
           break
       elif 'Possible Commands' in attributes:
                value = attributes[attr]
                if attr in ['Inventory', 'Abilities'] and (not value or (isinstance(value, list) and not value) or (isinstance(value, dict) and not value)):
                    missing_attributes = True
                    expected_attributes.append(attr)
                possible_commands = attributes['Possible Commands']

                if not possible_commands or ('1' not in possible_commands.keys() or '2' not in possible_commands.keys()):
                    expected_attributes.append('Possible Commands')
                    missing_attributes = True
                    break

       elif 'Abilities' in attributes:


                Abilities = attributes['Abilities']

                if len(Abilities)<5:
                    expected_attributes.append('Abilities')
                    missing_attributes = True
                    break

       elif attributes['Inventory'] == '':

            missing_attributes = True
            expected_attributes.append('Inventory')
            break

       else:

           value = attributes[attr]
           if attr in ['Inventory', 'Abilities'] and (not value or (isinstance(value, list) and not value) or (isinstance(value, dict) and not value)):
               missing_attributes = True
               expected_attributes.append(attr)
               break


   print("***************************************")
   print(expected_attributes)
   print("***************************************")

   return missing_attributes



def extract_attributes_from_answer(answer):


    answer = answer.replace("<br>", '')
    answer = answer.replace("<br/>", '')
    answer = re.sub(r"`+|\'+", "", answer)




    attributes = {}
    lines = answer.replace('\\', '').split('\n')
    lines = [line for line in lines if line.strip()]
    lines = [line.strip() for line in lines]


    if lines[0]=='```' and lines[-1]=='```':
        lines.pop(0)
        lines.pop(-1)
    elif lines[0]=='`' and lines[-1]=='`':
        lines.pop(0)
        lines.pop(-1)
    elif lines[0]=='``' and lines[-1]=='``':
        lines.pop(0)
        lines.pop(-1)
    '''
    print('==================================================')

    print(lines)
    print('==================================================')
    '''
    story = []
    before_turn_number = False
    possible_commands = {}
    abilities = {}
    inventory = []

    for line in lines:
        line = line.strip()
        line = line.replace('\n','')
        line = line.replace('\n\n','')


        #print(line)
        if line.startswith('Turn number'):
            before_turn_number = True

        if before_turn_number:
            if ':' in line:
                parts = line.split(':')
                if len(parts) >= 2:
                    key = parts[0].strip()
                    value = ':'.join(parts[1:]).strip()
                    attributes[key] = value
            else:
                parts = line.split('.')
                if len(parts) >= 2:
                    key = parts[0].strip()
                    value = '.'.join(parts[1:]).strip()
                    attributes[key] = value
        else:
            story.append(line.strip())

    if story:
        if story == "":
            attributes['Story'] = None
        else:
            attributes['Story'] = '\n'.join(story)
    else:
        attributes['Story'] = None



    abilities_started = False


    for line in lines:
        if line.startswith('Possible Commands') or line.startswith('Commands'):
            break

        if line.startswith('Abilities'):
            abilities_started = True

        if abilities_started:
            if "{" in line:
                #print(line)
                abilities_str = line[line.index('{')+1 : line.index('}')]
                ability_parts = [ability.strip() for ability in abilities_str.split(',')]
                for ability in ability_parts:
                    if ':' in ability:
                        ability_key, ability_value = ability.split(':')
                        abilities[ability_key.strip()] = ability_value.strip()
                        #print("Ability extracted in loop 1")


    #print('Loop1')
    #print(abilities)

    if len(abilities) != 5 or next(iter(abilities)) != 'Persuasion':
        abilities = {}


    abilities_started = False

    if len(abilities) < 5:  # 1번 루프에서 가져오지 못한 경우에만 2번 루프 실행
        #print("Loop 2")
        for line in lines:
            if line.startswith('Possible Commands') or line.startswith('Commands'):
                break

            if line.startswith('Abilities'):
                abilities_started = True

            if abilities_started:
                if "(" in line:
                    #print("line")
                    #print(line)

                    pattern = r"(\w+)\s*\((\d+)\)"
                    matches = re.findall(pattern, line)

                    for match in matches:
                        ability_name = match[0]
                        ability_value = match[1]
                        abilities[ability_name] = ability_value
                    #print("Ability extracted in loop 2")



    if len(abilities) != 5 or next(iter(abilities)) != 'Persuasion':
        abilities = {}

    abilities_started = False

    if len(abilities) < 5:  # 1번, 2번 루프에서 가져오지 못한 경우에만 3번 루프 실행
        #print("Loop 3")
        for line in lines:
            if line.startswith('Possible Commands') or line.startswith('Commands'):
                break

            if line.startswith('Abilities'):
                abilities_started = True

            if abilities_started:
                if "Persuasion" in line:
                    ability_parts = line.split(":")
                    if len(ability_parts) == 2:
                        ability = ability_parts[0].strip()
                        value = ability_parts[1].strip()
                        abilities[ability] = value
                        #print("Ability extracted in loop 3")
                        break

    if 'Abilities' in abilities and isinstance(abilities['Abilities'], str):
        #print("Loop3-1")
        if abilities['Abilities'].endswith('.'):
            abilities['Abilities'] = abilities['Abilities'].rstrip('.')
        ability_values = abilities['Abilities'].split(',')
        for ability_value in ability_values:
            ability_value = ability_value.strip()
            ability_value_parts = ability_value.split()
            if len(ability_value_parts) == 2:
                ability, val = ability_value_parts
                abilities[ability] = val
            else:
                print("", ability_value)
        abilities.pop('Abilities')
        #print("검사 통과: abilities 딕셔너리 생성 완료")
    else:
        print("")

    for key, value in abilities.items():
        if '(' in value and ')' in value:
            value = value.replace('(', '').replace(')', '')  # 괄호 제거
            abilities[key] = value

    #print(abilities)

    if len(abilities) != 5 or next(iter(abilities)) != 'Persuasion':
        if 'Abilities' in abilities and isinstance(abilities['Abilities'], str):
            #print("Loop3-2")
            abilities_text = abilities.replace("Abilities:", "").strip()  # Abilities: 제거 후 공백 제거
            abilities = abilities_text.split(",")  # `,`를 구분자로 각 항목 분리

            ability_dict = {}
            for ability in abilities:
                ability = ability.strip()  # 공백 제거
                # 영단어와 값 분리
                parts = ability.split("(")
                attribute = parts[0].strip()
                value = parts[1].replace(")", "").strip()
                ability_dict[attribute] = value
                abilities = ability_dict
            #print("검사 통과: abilities 딕셔너리 생성 완료")
            abilities.pop('Abilities')
        else:
            print("")



    #print(abilities)


    if len(abilities) != 5 or next(iter(abilities)) != 'Persuasion':
        abilities = {}

    if len(abilities) < 5:  # 1번, 2번, 3번 루프에서 가져오지 못한 경우에만 4번 루프 실행
        #print("Loop 4")
        abilities_started = False

        for line in lines:
            if line.startswith('Possible Commands') or line.startswith('Commands'):
                break

            if line.startswith('Abilities'):
                abilities_started = True

            if abilities_started:
                abilities_str = line.replace("Abilities:", "").strip()
                ability_parts = [ability.strip() for ability in abilities_str.split(',')]
                for ability in ability_parts:
                    if ':' in ability:
                        ability_key = ability.split(':')[0]
                        ability_value = ':'.join(ability.split(':')[1:])
                        abilities[ability_key.strip()] = ability_value.strip()
                        if len(abilities) >= 5:
                            break
                        #print("Ability extracted in loop 4")


    #print(abilities)

    new_abilities = {}

    for key, value in abilities.items():
        if key.startswith('- '):
            new_key = key[2:]  # '- ' 제거
            new_abilities[new_key] = value
        elif key.startswith('-'):
            new_key = key[1:]  # '- ' 제거
            new_abilities[new_key] = value
        elif key.startswith(' '):
            new_key = key[1:]  # '- ' 제거
            new_abilities[new_key] = value
        elif key.endswith(' '):
            new_key =  key.rstrip()# '- ' 제거
            new_abilities[new_key] = value
        elif key.endswith('- '):
            new_key = key[:-2]  # '- ' 제거
            new_abilities[new_key] = value
        elif key.startswith('-'):
            new_key = key[:-1]  # '- ' 제거
            new_abilities[new_key] = value
        else:
            new_abilities[key] = value

    abilities = new_abilities

    if len(abilities) != 5 or next(iter(abilities)) != 'Persuasion':
        abilities = {}

    if len(abilities) < 5:  # 1번, 2번, 3번, 4번 루프에서 가져오지 못한 경우에만 5번 루프 실행
        #print("Loop 5")
        abilities_started = False

        for line in lines:
            if line.startswith('Possible Commands') or line.startswith('Commands'):
                break

            if line.startswith('Abilities'):
                abilities_started = True

            if abilities_started:
                if "Abilities:" not in line:
                    abilities_parts = line.strip().split(',')
                    for ability_part in abilities_parts:
                        if ':' in ability_part:
                            ability_key, ability_value = ability_part.split(':')
                            abilities[ability_key.strip()] = ability_value.strip()
                            #print("Ability extracted in loop 5")
                else:
                    abilities_started = False

    #print(abilities)

    if len(abilities) != 5 or next(iter(abilities)) != 'Persuasion':
        abilities = {}

    if len(abilities) < 5:  # 1번, 2번, 3번, 4번 루프에서 가져오지 못한 경우에만 5번 루프 실행
        #print("Loop 6")
        abilities_started = False

        for line in lines:
            if line.startswith('Possible Commands') or line.startswith('Commands'):
                break

            if line.startswith('Abilities'):
                abilities_started = True

            if abilities_started:
                if '-' in line:
                    ability_parts = line.split(':')
                    if len(ability_parts) == 2:
                        ability_key = ability_parts[0].strip()[1:]
                        ability_value = ability_parts[1].strip()
                        ability_key = ability_key.strip()  # 공백 제거
                        abilities[ability_key] = ability_value
                        #print("Ability extracted in loop 6")

                elif line.strip() == '':
                    abilities_started = False

    #print(abilities)

    if len(abilities) != 5 or next(iter(abilities)) != 'Persuasion':
        abilities = {}

    if len(abilities) < 5:  # 1번, 2번, 3번, 4번 루프에서 가져오지 못한 경우에만 5번 루프 실행
        #print("Loop 7")
        abilities_started = False

        for line in lines:
            if line.startswith('Possible Commands') or line.startswith('Commands'):
                break

            if line.startswith('Abilities'):
                abilities_started = True

            if abilities_started:
                if '-' in line:
                    ability_parts = line.split('-')
                    if len(ability_parts) == 2:
                        ability_key = ability_parts[0].strip()
                        ability_value = ability_parts[1].strip()
                        abilities[ability_key] = ability_value
                        #print("Ability extracted in loop 7")

                elif line.strip() == '':
                    abilities_started = False

    #print(abilities)



    attributes['Abilities'] = abilities

    # Extracting inventory
    inventory_started = False
    for line in lines:
        if line.startswith('Inventory'):
            inventory_started = True

        if inventory_started:
            if line.startswith('Wearing') or line.startswith('Wielding') or line.startswith('Quest') or line.startswith('Worn') or line.startswith('Abilities'):
                break
            elif line.strip():
                items = line.strip().replace('-', '').split(',')
                for item in items:
                    item = item.strip()
                    inventory.append(item)

    inventory_started = False
    if len(inventory) < 1 or inventory[0] == '':
        for line in lines:
            if line.startswith('Inventory'):
                inventory_started = True

            if inventory_started:
                if line.startswith('Wearing') or line.startswith('Wielding') or line.startswith('Quest') or line.startswith('Worn') or line.startswith('Abilities'):
                    break
                inventory_items = line.replace('Inventory:', '').strip().split('\n')
                if len(inventory_items) == 1:
                    items = inventory_items[0].split(',')
                    for item in items:
                        item = item.strip()
                        inventory.append(item)
                else:
                    for item in inventory_items:
                        item = item.strip()
                        inventory.append(item)


    if inventory and inventory[0].startswith('Inventory: '):
        inventory[0] = inventory[0].replace('Inventory: ', '')
    elif inventory and inventory[0].startswith('Inventory:'):
        inventory[0] = inventory[0].replace('Inventory:', '')
    elif inventory and inventory[0].startswith('Inventory'):
            inventory[0] = inventory[0].replace('Inventory', '')

    if inventory and inventory[0] == '':
        inventory.pop(0)
    attributes['Inventory'] = inventory

    #print("Commands Loop 1")

    # Extracting possible commands
    for key, value in attributes.items():
        if key.isdigit():
            possible_commands[key] = value

    #print(possible_commands)
    #print(possible_commands.keys())
    #print(list(possible_commands.keys()))
    key = []
    key = list(possible_commands.keys())

    if len(key) > 3:
        if key[0] != '1' and key[1] != '2':

            possible_commands = {}

    if len(possible_commands) == 0:
        for line in lines:
            if line.startswith('Possible Commands'):
                pattern = r"(\d+)\)\s*(.+?)(?=(\d+\)|Other\.))"
                matches = re.findall(pattern, line)

                for match in matches:
                    command_number = match[0]
                    command_description = match[1]
                    possible_commands[command_number] = command_description.strip()


    if len(possible_commands) == 0:  # 예외 사항: Possible Commands가 없는 경우
        #print("Commands Loop 2")
        for line in lines:
            if line.startswith('Possible Commands'):
                commands_str = line.replace('Possible Commands:', '').strip()
                commands_list = [command.strip() for command in commands_str.split(',')]
                for i, command in enumerate(commands_list, start=1):
                    possible_commands[str(i)] = command
                break  # 2번 루프에서 가져오면 다음 루프를 건너뛰기 위해 break


    key = []
    key = list(possible_commands.keys())


    if len(key) > 3:
        if key[0] != '1' and key[1] != '2':

            possible_commands = {}

    if len(possible_commands) == 0:  # 예외 사항: Possible Commands가 없는 경우
        #print("Commands Loop 3")

        for line in lines:
            if line.startswith('Possible Commands'):
                commands_list = line.replace('Possible Commands:', '').strip().split('-')
                for i, command in enumerate(commands_list, start=1):
                    possible_commands[str(i)] = command.strip()
                break  # 3번 루프에서 가져오면 다음 루프를 건너뛰기 위해 brea

    key = []
    key = list(possible_commands.keys())


    if len(key) > 3:
        if key[0] != '1' and key[1] != '2':

            possible_commands = {}



    command_dict = {}
    command_list = []  # command_list 변수 초기화
    if len(possible_commands) == 0:
        for line in lines:
            if line.startswith('Possible Commands'):
                # ':' 기준으로 명령어 분리
                command_list = line.split(':')

        if len(command_list) > 1:
            if 'or' in command_list[1]:
                command_options = command_list[1].split('or')

            for option in command_options:
                option = option.strip()

                # 번호와 명령어 분리
                num, command = option.split(')')
                num = num.strip()
                command = command.strip()

                # 딕셔너리에 추가
                command_dict[num] = command

        possible_commands = command_dict

    key = []
    key = list(possible_commands.keys())

    if len(key) > 3:
        if key[0] != '1' and key[1] != '2':

            possible_commands = {}

    attributes['Possible Commands'] = possible_commands
    attributes.pop('1', None)
    attributes.pop('2', None)
    attributes.pop('3', None)
    attributes.pop('Other', None)
    attributes.pop('4', None)


    attributes.pop('- Persuasion', None)
    attributes.pop('- Strength', None)

    attributes.pop('- Intelligence', None)

    attributes.pop('- Dexterity', None)

    attributes.pop('- Luck', None)

    if 'Time period of day' in attributes:
        attributes['Time period of the day'] = attributes.pop('Time period of day')

    if 'Time of day' in attributes:
        attributes['Time period of the day'] = attributes.pop('Time of day')

    if 'Time period of the Day' in attributes:
        attributes['Time period of the day'] = attributes.pop('Time period of the Day')

    if 'Time period' in attributes:
        attributes['Time period of the day'] = attributes.pop('Time period')

    if 'Quests' in attributes:
        attributes['Quest'] = attributes.pop('Quests')

    if 'Quest' not in attributes:
        attributes['Quest'] = None
    return attributes





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


def main(topic):
   global answer
   global turn_num
   global attributes
   global messages


   # 모델 - GPT 3.5 Turbo 선택
   model = "gpt-3.5-turbo"

   # 질문 작성하기
   query = "start the game"

   original_prompt = """
   Please perform the function of a text adventure game, following the rules listed below:

Presentation Rules:

1. Play the game in turns, starting with you.

2. The game output will always show 'Turn number', 'Difficulty', 'Time period of the day', 'Current day numbegr', 'Weather', 'Health', 'XP', ‘AC’, 'Level’, Location', 'Description', ‘Gold’, 'Inventory', 'Quest', 'Abilities', and 'Possible Commands'.

3. Always wait for the player’s next command.

4. Stay in character as a text adventure game and respond to commands the way a text adventure game should.

5. Wrap all game output in code blocks.

6. The ‘Description’ must stay between 10 to 20 sentences.

7. For each 'Possible Commands', please write down more than 2 sentences in more detail.

8. Increase the value for ‘Turn number’ by +1 every time it’s your turn.

9. ‘Time period of day’ must progress naturally after a few turns.

10. Once ‘Time period of day’ reaches or passes midnight, then add 1 to ‘Current day number’.

11. Change the ‘Weather’ to reflect ‘Description’ and whatever environment the player is in the game.


Fundamental Game Mechanics:

1. Determine ‘AC’ using Dungeons and Dragons 5e rules.

2. Generate ‘Abilities’ before the game starts. ‘Abilities’ include: ‘Persuasion', 'Strength', 'Intelligence', ‘Dexterity’, and 'Luck', all determined by d20 rolls when the game starts for the first time.

3. Start the game with 20/20 for ‘Health’, with 20 being the maximum health. Eating food, drinking water, or sleeping will restore health.

4. Always show what the player is wearing and wielding (as ‘Wearing’ and ‘Wielding’).

5. Display ‘Game Over’ if ‘Health’ falls to 0 or lower.

6. The player must choose all commands, and the game will list 3 of them at all times under ‘Commands’, and assign them a number 1-3 that I can type to choose that option, and vary the possible selection depending on the actual scene and characters being interacted with.

7. The 3th command should be ‘Other’, which allows me to type in a custom command.

8. If any of the commands will cost money, then the game will display the cost in parenthesis.

9. Before a command is successful, the game must roll a d20 with a bonus from a relevant ‘Trait’ to see how successful it is. Determine the bonus by dividing the trait by 3.

10. If an action is unsuccessful, respond with a relevant consequence.

11. Always display the result of a d20 roll before the rest of the output.

12. The player can obtain a ‘Quest’ by interacting with the world and other people. The ‘Quest’ will also show what needs to be done to complete it.

13. The only currency in this game is Gold.

14. The value of ‘Gold’ must never be a negative integer.

15. The player can not spend more than the total value of ‘Gold’.

16. The difficulty level ranges from 1 to 10, and the lower the number, the easier the difficulty. Contents change according to difficulty. When HP is continuously maintained at 20 or less attacks are received, the difficulty level is changed to a higher level than the previous level. When HP is less than 5 or close to GAME OVER, the difficulty level is changed to a lower level than the previous level.

Rules for Setting:

1. Use the world of {change_topic}.

2. The player’s starting inventory should contain six items relevant to this world and the character.

3. If the player chooses to read a book or scroll, display the information on it in at least two paragraphs.

4. The game world will be populated by interactive NPCs. Whenever these NPCs speak, put the dialogue in quotation marks.

5. Completing a quest adds to my XP.

Combat Rules:

1. Battles can only begin when players have combat items in their inventory.

2. If 'dexterity' is low or injured, there is a less than 40% chance of losing HP while using combat items.

4. Combat should be handled in rounds, roll attacks for the NPCs each round.

5. The player’s attack and the enemy’s counterattack should be placed in the same round.

6. Always show how much damage is dealt when the player receives damage.

7. Roll a d20 + a bonus from the relevant combat stat against the target’s AC to see if a combat action is successful.

8. Who goes first in combat is determined by initiative. Use D&D 5e initiative rules.

9. Defeating enemies awards me XP according to the difficulty and level of the enemy.

Refer back to these rules after every prompt.

Start Game."""

   original_prompt = original_prompt.replace('{change_topic}', topic)

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
       #print(answer)

   print("-----------------------------")
   attributes = extract_attributes_from_answer(answer)
   print(attributes)
   print("-----------------------------")

   missing_attributes = find_attr(attributes)

   # Handle missing attributes
   if missing_attributes:
       #print("Some attributes are missing. Sending original prompt again.")
       time.sleep(20)
       messages = main(topic)  # Send the original prompt again
       return messages

   # Extract specific attributes
   """

   time_period = attributes.get('Time period of the day', '')

   weather = attributes.get('Weather', '')
   health = attributes.get('Health', '')
   location = attributes.get('Location', '')
   description = attributes.get('Description', '')
   quest = attributes.get('Quest', '')

   turn_number = 0
   if 'Turn number' in attributes:
       turn_number_str = attributes.get('Turn number', '')
       if turn_number_str.isdigit():
           turn_number = int(turn_number_str)

   difficulty = 0
   if 'Difficulty' in attributes:
       difficulty_str = attributes.get('Difficulty', '')
       if difficulty_str.isdigit():
           difficulty = int(difficulty_str)

   day_number = 0
   if 'Current day number' in attributes:
       day_number_str = attributes.get('Current day number', '')
       if day_number_str.isdigit():
           day_number = int(day_number_str)

   xp = 0
   if 'XP' in attributes:
       xp_str = attributes.get('XP', '')
       if xp_str.isdigit():
           xp = int(xp_str)

   ac = 0
   if 'AC' in attributes:
       ac_str = attributes.get('AC', '')
       if ac_str.isdigit():
           ac = int(ac_str)

   level = 0
   if 'Level' in attributes:
       level_str = attributes.get('Level', '')
       if level_str.isdigit():
           level = int(level_str)
    """
   #print(attributes)

   # Print extracted attributes
   """print("Turn number:", turn_number)
   print("Difficulty:", difficulty)
   print("Time period of the day:", time_period)
   print("Current day number:", day_number)
   print("Weather:", weather)
   print("Health:", health)
   print("XP:", xp)
   print("AC:", ac)
   print("Level:", level)
   print("Location:", location)
   print("Description:", description)
   print("Quest:", quest)"""
   return messages

def return_attributes():
   global attributes
   return attributes

def return_answer():
   global attributes
   return attributes

def return_message():
   global messages
   return messages


def parse_attributes(answer):
   global attributes
   attributes = re.findall(r"(\w+):\s(.+)", answer)
   return dict(attributes)


def parse_inventory(inventory_str):
   inventory_items = inventory_str.split(", ")
   return inventory_items


def get_input(user_input, msg):

   messages = msg
   print(type(messages))
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
   model = "gpt-3.5-turbo"



   global attributes
   global answer
   global turn_num

   messages.append(
       {
           "role": "assistant",
           "content": answer
       }
   )

   print("551")

   # 사용자 메시지 추가
   messages.append(
       {
           "role": "user",
           "content": user_input
       }
   )
   print("552")
   print(messages)
   print(user_input)


   response = call_openai_api(model, messages)


   print("553")
   if 'choices' in response and len(response['choices']) > 0:
       answer = response['choices'][0]['message']['content']
       print(answer)

       # 게임 오버인지 확인
       if 'Game Over' in answer:
           #print("Game Over")
           answer = None
           return

       # 속성 추출
       attributes = extract_attributes_from_answer(answer)
       #print(attributes)

       missing_attributes = find_attr(attributes)

       # Handle missing attributes
       if missing_attributes:
           #print("Some attributes are missing. Sending original prompt again.")
           time.sleep(20)
           messages = get_input(user_input, msg)  # Send the original prompt again
           return messages


       #if 'Turn number' in attributes:
       #    turn_num = int(attributes['Turn number'])

       # Inventory 아이템 추출
       #inventory = []
       #if 'Inventory' in attributes:
           #inventory = parse_inventory(attributes['Inventory'])

       #print("Attributes:", attributes)
       #print("Inventory:", inventory)

   return messages
'''
set_openai_api_key("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")
messages = main("zombie")
print(type(messages))



while True:
        print(messages)

        user_input = input("Enter your input: ")
        messages = get_input(user_input, messages)
        print(type(messages))
'''

