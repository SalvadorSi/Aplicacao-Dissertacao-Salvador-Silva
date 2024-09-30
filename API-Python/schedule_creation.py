from flask import Flask, request, jsonify
import json
import time

from clorm import monkey

monkey.patch()  # must call this before importing clingo

from clorm import IntegerField, Predicate, ConstantField, FactBase
from clorm.clingo import Control

app = Flask(__name__)

# -------------------------------------------------------------------
# Constants
# -------------------------------------------------------------------

PRESENTATION_JURY_KEY = 'presentationJury'
FOLLOWED_SLOTS_KEY = 'followedSlots'
ROOMS_KEY = 'rooms'
USERS_UNAVAILABILITIES_KEY = 'usersUnavailabilities'
PRESENTATIONS_RESTRICTIONS_KEY = 'presentationsRestrictions'
OPTIONALS_KEY = 'optionals'
ALL_PRESENTATIONS_JURIES_KEY = 'allPresentationsJuries'
SLOT_ID_KEY = 'slotID'
SLOTS_ID_KEY = 'slotsID'
ROOM_ID_KEY = 'roomID'
PRESENTATION_ID_KEY = 'presentationID'
ADVISER_ID_KEY = 'adviserID'
ARGUER_ID_KEY = 'arguerID'
USER_ID_KEY = 'userID'
FOLLOWED_PRESENTATIONS_KEY = 'followedPresentations'
FOLLOWED_PRESENTATIONS_IDS_KEY = 'followedPresentationsIDS'

SLOTS_ASSIGNMENT_PROGRAM = "slotsAssignment.lp"
ROOMS_ASSIGNMENT_PROGRAM = "roomsAssignment.lp"
FOLLOWED_PRESENTATIONS_PROGRAM = "followedPresentationsFacts.lp"

TIME_LIMIT = 600 # = 10 min #TODO, METER 600

# -------------------------------------------------------------------
# Classes creation
# -------------------------------------------------------------------

# Input Classes
class NumberRooms(Predicate):
    number = ConstantField()

class Room(Predicate):
    roomID = IntegerField
class Slot(Predicate):
    slotID = IntegerField

class FollowedSlots(Predicate):
    slotID1 = IntegerField
    slotID2 = IntegerField

class Jury(Predicate):
    presentationID = IntegerField
    adviserID = IntegerField
    arguerID = IntegerField

class SessionParticipants(Predicate):
    userID = IntegerField
    presentationID = IntegerField

class Participates(Predicate):
    userID = IntegerField
    slotID = IntegerField

class Assign(Predicate):
    presentationID = IntegerField
    slotID = IntegerField

# Output Class
class Slots(Predicate):
    slotID = IntegerField
    presentationID = IntegerField
    adviserID = IntegerField
    arguerID = IntegerField

class FinalSlots(Predicate):
    roomID = IntegerField
    slotID = IntegerField
    presentationID = IntegerField


# -------------------------------------------------------------------
# Execute Clingo Endpoint
# -------------------------------------------------------------------

@app.route("/executeClingo", methods=["POST"])
def execute_clingo():
    
    # ---------------------Data Process-----------------------------
    data_or_error = process_data(request)
    
    if isinstance(data_or_error, tuple):  # Check if it's an error response
        return data_or_error  # Return the error response
    
    data = data_or_error  # Data successfully parsed
    
    data_keys = {
        PRESENTATION_JURY_KEY: 'allPresentationsJuries',
        FOLLOWED_SLOTS_KEY: 'followedSlots',
        ROOMS_KEY: 'rooms',
        USERS_UNAVAILABILITIES_KEY: 'usersUnavailabilities',
        PRESENTATIONS_RESTRICTIONS_KEY: 'presentationsRestrictions',
        OPTIONALS_KEY: 'optionals',
        FOLLOWED_PRESENTATIONS_KEY:'followedPresentations'
    }
    
    # Extract data using keys
    extracted_data = {value: data[key] for key, value in data_keys.items() if key in data}

    write_followed_presentations_program(extracted_data, FOLLOWED_PRESENTATIONS_PROGRAM)

    # -----------------Geração Factos------------------------

    room_assignment_standard_facts = []
    slot_assignment_facts = []

    # Generate facts
    generate_facts(extracted_data, slot_assignment_facts, room_assignment_standard_facts)
    
    # ---------------------Clorm-----------------------------

    # Create a Control object that will unify models against the appropriate predicates. 
    ctrl = Control(unifier=[Slot, NumberRooms, FollowedSlots, Jury, SessionParticipants, Slots, Participates, Assign])
    # Load the asp file that encodes the problem domain.
    ctrl.load(FOLLOWED_PRESENTATIONS_PROGRAM)
    ctrl.load(SLOTS_ASSIGNMENT_PROGRAM)
    
    # Add the instance data and ground the ASP program
    ctrl.add_facts(FactBase(slot_assignment_facts))
    ctrl.ground([("base", [])])

    result = None
    
    def on_model(model):
        nonlocal result
        result = [(str(model)), model.cost]

    start_time = time.time()
    
    with ctrl.solve(on_model=on_model, async_=True) as handle:
        handle.wait(TIME_LIMIT)
        handle.cancel()

    end_time = time.time()
    
    slots_list = result[0].split(" ")

    print("Time on slot assignment:")
    print(end_time-start_time)
    print("Slots Optimization:")
    print(result[1])

    #writeProgram("slots.lp",slots_list) 

    # Get followed slots to divide by sets
    followed_slots = extracted_data.get(FOLLOWED_SLOTS_KEY, [])
    
    final_rooms_list = []

    # Loop through each set in followed_slots
    for sublist in followed_slots:
        #add standard facts for room assignment
        room_assignment_facts = list(room_assignment_standard_facts)
        
        for item in sublist:
            slot_id = item[SLOT_ID_KEY]
            
            # Find slots that match the slotID and add them into the facts list
            for slots_str in slots_list:
                if int(slots_str.split('(')[1].split(',')[0]) == slot_id:
                    numbers = [int(num) for num in slots_str[6:-1].split(",")]
                    room_assignment_facts.append(Slots(slotID=numbers[0], presentationID=numbers[1], adviserID=numbers[2], arguerID=numbers[3]))
        # -------------------------------
        #       Clorm program 
        # -------------------------------
        ctrl = Control(unifier=[NumberRooms, Room, FollowedSlots, Slots, FinalSlots])
        ctrl.add_facts(FactBase(room_assignment_facts))
        ctrl.load(ROOMS_ASSIGNMENT_PROGRAM)
        ctrl.ground([("base", [])])
        
        start_time = time.time()
        with ctrl.solve(on_model=on_model, async_=True) as handle:
            handle.wait(TIME_LIMIT)
            handle.cancel()
        final_rooms_list.extend(result[0].split(" "))
        end_time = time.time()
        print("Time on room assignment:")
        print(end_time-start_time)
        print("Room Optimization:")
        print(result[1])

    #writeProgram("finalSlots.lp",final_rooms_list) 

    #for element in final_rooms_list:
    #    print(element)
    print(final_rooms_list)
    return jsonify(final_rooms_list)




# -------------------------------------------------------------------
# Functions
# -------------------------------------------------------------------

def process_data(request):
    if request.headers['Content-Type'] != 'application/json':
        return jsonify({'error': 'Unsupported Media Type'}), 400
    
    try:
        data = json.loads(request.data)
        return data
    except Exception as e:
        return jsonify({'error': 'Failed to parse JSON data'}), 400

def write_followed_presentations_program(data, program_name):
    followedPresentations = data.get(FOLLOWED_PRESENTATIONS_KEY)

    file = open(program_name,"w")
    for followed in followedPresentations:
        ids = followed[FOLLOWED_PRESENTATIONS_IDS_KEY]
        file.write(f"ok:- assign({ids[0]}, H1), assign({ids[1]}, H2), followedSlots(H1, H2).\n")
        file.write(f"ok:- assign({ids[0]}, H1), assign({ids[1]}, H2), followedSlots(H2, H1).\n")
        file.write(":- not ok.\n")



def generate_facts(data, slot_assignment_facts, room_assignment_standard_facts):
    # Access extracted data
    rooms = data.get(ROOMS_KEY)
    followed_slots = data.get(FOLLOWED_SLOTS_KEY, [])
    all_presentations_juries = data.get(ALL_PRESENTATIONS_JURIES_KEY, [])
    optionals = data.get(OPTIONALS_KEY, [])
    user_unavailabilities = data.get(USERS_UNAVAILABILITIES_KEY)
    presentation_restricitons = data.get(PRESENTATIONS_RESTRICTIONS_KEY)
    
    # Create Rooms predicate     
    slot_assignment_facts.append(NumberRooms(number=str(len(rooms))))
    room_assignment_standard_facts.append(NumberRooms(number=str(len(rooms))))

    for room in rooms:
        room_assignment_standard_facts.append(Room(roomID=room.get(ROOM_ID_KEY)))

    # Create Slot and FollowedSlots predicates 
    for set_of_slots in followed_slots:
        for i, slot in enumerate(set_of_slots):
            slot_id = slot.get(SLOT_ID_KEY)
            slot_assignment_facts.append(Slot(slotID=slot_id))

            if i < len(set_of_slots) - 1:
                next_slot_id = set_of_slots[i + 1].get(SLOT_ID_KEY)
                slot_assignment_facts.append(FollowedSlots(slotID1=slot_id, slotID2=next_slot_id))
                room_assignment_standard_facts.append(FollowedSlots(slotID1=slot_id, slotID2=next_slot_id))

    # Create Jury predicate 
    for presentation_jury in all_presentations_juries:
        slot_assignment_facts.append(Jury(presentationID=presentation_jury[PRESENTATION_ID_KEY],
                          adviserID=presentation_jury[ADVISER_ID_KEY],
                          arguerID=presentation_jury[ARGUER_ID_KEY]))

    # Create OptionalParticipants predicate 
    for item in optionals:
        slot_assignment_facts.append(SessionParticipants(userID=item[USER_ID_KEY],
                                           presentationID=item[PRESENTATION_ID_KEY]))     

    # Create Participates negative predicate 
    for item in user_unavailabilities:
        for slot in item[SLOTS_ID_KEY]:
            slot_assignment_facts.append(Participates(userID=item[USER_ID_KEY],
                                      slotID=slot,
                                      sign=False))
    
    # Create Assign negative predicate 
    for item in presentation_restricitons:
        for slot in item[SLOTS_ID_KEY]:
            slot_assignment_facts.append(Assign(presentationID=item[PRESENTATION_ID_KEY],
                                slotID=slot,
                                sign=False))
    
    # Test the facts created
    #writeProgram("scheduleAssignmentFacts.lp",slot_assignment_facts)
    #writeProgram("roomAssignmentFacts.lp",room_assignment_standard_facts)

def writeProgram(name,program):
    
    file = open(name,"w")
    
    for i in range(len(program)):
        file.write(str(program[i])+".\n")




if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, debug=True)

