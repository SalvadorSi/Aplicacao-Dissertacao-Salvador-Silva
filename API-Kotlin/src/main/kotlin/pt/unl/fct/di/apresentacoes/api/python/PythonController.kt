package pt.unl.fct.di.apresentacoes.api.python

import com.google.gson.Gson
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.reflect.TypeToken
import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.dto.*
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.services.*
import java.time.Duration

private const val DEFAULT_TIME_DIFF_TO_FOLLOWED_SLOTS: Long = 60 // 1 hour
private const val PYTHON_URL: String = "http://python-backend:5000/executeClingo"
private const val DEFAULT_ROOM_ID_POSITION = 0
private const val DEFAULT_SLOT_ID_POSITION = 1
private const val DEFAULT_PRESENTATION_ID_POSITION = 2
private const val DEFAULT_PYTHON_VALUE_FOR_ITERATIONS = 600000

@RestController
class PythonController(val presentations: PresentationService,
                       val slots: SlotService,
                       val rooms: RoomService,
                       val unavailabilities: UnavailabilityService,
                       val presentationUnavailabilities: PresentationUnavailabilityService,
                       val followedPresentations: FollowedPresentationsService) : PythonAPI {

    override fun executeClingo() : String {
        val jury = getJury()
        val followedSlots = getFollowedSlots()
        val rooms = getRooms()
        val unavailabilities = getUnavailabilities()
        val presentationUnavailabilities = getPresentationsUnavailabilities()
        val optionals = getOptionals()
        val followedPresentations = getFollowedPresentations()

        val numberSlots = followedSlots.sumOf { it.size }

        if(numberSlots == 0 || rooms.isEmpty())
            throw APIUnprocessableEntityException("Please insert rooms and/or slots.")

        if(jury.size > rooms.size * numberSlots )
            throw APIUnprocessableEntityException("The number of presentations exceeds the available slots and rooms. Please add more slots or more rooms.")

        var started = true
        // one value for slots assignment and another one for each roomAssignment in each set of followed slots
        val timeoutRead = DEFAULT_PYTHON_VALUE_FOR_ITERATIONS + DEFAULT_PYTHON_VALUE_FOR_ITERATIONS*followedSlots.size

        val data = PythonDataDTO(jury, followedSlots, rooms, unavailabilities, presentationUnavailabilities, optionals, followedPresentations)
        val gson = Gson()
        val jsonData = gson.toJson(data)
        //println(data)

        Fuel.post(PYTHON_URL)
            .header("Content-Type" to "application/json")
            .body(jsonData)
            .timeoutRead(timeoutRead)
            .response { result ->
                val (bytes, error) = result
                if (bytes != null) {
                    val responseString = String(bytes)
                    println("[Response]: $responseString")

                    // Parse the JSON array directly into a list of strings
                    val gson = Gson()
                    val finalSlotsList: List<String> = gson.fromJson(responseString, object : TypeToken<List<String>>() {}.type)

                    for (item in finalSlotsList) {
                        val values = item.substringAfter('(').substringBeforeLast(')').split(',')
                        if(values.all { it.isBlank() }) {
                            continue
                        }
                        val roomID = values[DEFAULT_ROOM_ID_POSITION].toLong()
                        val slotID = values[DEFAULT_SLOT_ID_POSITION].toLong()
                        val presentationID = values[DEFAULT_PRESENTATION_ID_POSITION].toLong()
                        try {
                            presentations.addRoomAndSlot(presentationID, roomID, slotID)
                        } catch (e:PresentationNotFound){
                            throw APINotFoundException(e.message)
                        } catch (e:PresentationUnprocessable){
                            throw APIUnprocessableEntityException(e.message)
                        }
                    }

                } else {
                    if (error != null) {
                        println("[Error]: ${error.exception.message}")
                        started = false
                        throw APIUnprocessableEntityException("There was an error in the server side, please try again later.")
                    } else {
                        println("[Error]: Unknown error occurred")
                        started = false
                        throw APIUnprocessableEntityException("There was an error in the server side, please try again later.")
                    }
                }
            }

        //return jsonData
        if(started)
            return "Python started"
        else
            return "Not started"
    }

    private fun getJury(): Collection<PresentationJuryDTO> {
        val allPresentations = presentations.getAllPresentations().iterator()
        val res = mutableListOf<PresentationJuryDTO>()

        while (allPresentations.hasNext()){
            val onePresentation = allPresentations.next()
            res.add(PresentationJuryDTO(
                onePresentation.id,
                onePresentation.adviser.id,
                onePresentation.arguer.id))
        }

        return res
    }

    private fun getFollowedSlots(): Collection<Collection<SlotIDDTO>> {
        val allSlots = slots.getAllSlots().toList().listIterator() //toList so the iterator can be bidirectional
        val followedSlots = mutableListOf<MutableList<SlotIDDTO>>() //list of lists so there is a list of theh slots that are right after each other

        while (allSlots.hasNext()){
            val setOfFollowedSlots = mutableListOf<SlotIDDTO>()

            var previousSlot = allSlots.next()

            //add first slot to the list
            setOfFollowedSlots.add(SlotIDDTO(previousSlot.id))

            while(allSlots.hasNext()){
                val nextSlot = allSlots.next()

                val duration = Duration.between(nextSlot.startingHour, previousSlot.startingHour).abs()

                if(duration.toMinutes() > DEFAULT_TIME_DIFF_TO_FOLLOWED_SLOTS || previousSlot.date != nextSlot.date)
                    break

                previousSlot = nextSlot
                setOfFollowedSlots.add(SlotIDDTO(nextSlot.id))
            }
            if(allSlots.hasNext()) //just so there is no slot ignored, since in the loop before i already do the .next to verify the time and day
                allSlots.previous()

            followedSlots.add(setOfFollowedSlots)
        }
        return followedSlots
    }

    private fun getRooms(): Collection<RoomIDDTO> {
        val res = mutableListOf<RoomIDDTO>()

        val rooms = rooms.getAllRooms().iterator()
        while(rooms.hasNext()){
            res.add(RoomIDDTO(rooms.next().id))
        }
        return res
    }

    private fun getUnavailabilities(): Collection<ReducedUnavailabilityDTO>{
        val res = mutableListOf<ReducedUnavailabilityDTO>()

        val usersUnavailabilitiesIterator = unavailabilities.getAllUnavailabilities().iterator()
        while(usersUnavailabilitiesIterator.hasNext()){
            val unavailability = usersUnavailabilitiesIterator.next()
            res.add(ReducedUnavailabilityDTO(unavailability.user.id,unavailability.slots.map { e -> e.id }))
        }
        return res
    }

    private fun getPresentationsUnavailabilities(): Collection<ReducedPresentationUnavailabilityDTO> {
        val res = mutableListOf<ReducedPresentationUnavailabilityDTO>()

        val presentationsUnavailabilitiesIterator = presentationUnavailabilities.getAllPresentationUnavailabilities().iterator()
        while(presentationsUnavailabilitiesIterator.hasNext()){
            val presentationUnavailability = presentationsUnavailabilitiesIterator.next()
            res.add(ReducedPresentationUnavailabilityDTO(presentationUnavailability.presentation.id,presentationUnavailability.slots.map { e -> e.id }))
        }
        return res
    }

    private fun getOptionals(): Collection<OptionalViewersDTO>{
        val res = mutableListOf<OptionalViewersDTO>()
        val presentationsWithOptionalsIterator = presentations.getPresentationsWithOptionals().iterator()
        while (presentationsWithOptionalsIterator.hasNext()){
            val presentationUnavailabilityWithOptionals = presentationsWithOptionalsIterator.next()
            if(presentationUnavailabilityWithOptionals.optionalParticipant1 != null)
                res.add(OptionalViewersDTO(presentationUnavailabilityWithOptionals.optionalParticipant1!!.id, presentationUnavailabilityWithOptionals.id))
            if(presentationUnavailabilityWithOptionals.optionalParticipant2 != null)
                res.add(OptionalViewersDTO(presentationUnavailabilityWithOptionals.optionalParticipant2!!.id, presentationUnavailabilityWithOptionals.id))
        }
        return res
    }

    private fun getFollowedPresentations():  Collection<ReducedFollowedPresentationsDTO>{
        val res = mutableListOf<ReducedFollowedPresentationsDTO>()

        val followed = followedPresentations.getAllFollowedPresentations().iterator()
        while(followed.hasNext()){
            res.add(ReducedFollowedPresentationsDTO(followed.next().presentations.map { e -> e.id }))
        }
        return res
    }

}
