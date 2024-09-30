package pt.unl.fct.di.apresentacoes.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.UnavailabilityDTO
import pt.unl.fct.di.apresentacoes.domain.UnavailabilityDAO
import pt.unl.fct.di.apresentacoes.presistence.*

private const val UNAVAILABILITY_NOT_FOUND = "There is no unavailability with the id received."


@Service
class UnavailabilityService (val unavailabilities: UnavailabilityRepository,
                             val slots: SlotRepository,
                             val users: UserRepository) {

    @Transactional
    fun addUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UnavailabilityDAO{
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }
        val unavailability = unavailabilities.findByUserId(unavailabilityDTO.userID)

        val slotToAdd = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        unavailability.slots.add(slotToAdd)
        return unavailabilities.save(unavailability)
    }

    @Transactional
    fun deleteUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UnavailabilityDAO{
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }
        val unavailability = unavailabilities.findByUserId(unavailabilityDTO.userID)

        val slotToRemove = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        unavailability.slots.remove(slotToRemove)
        return unavailabilities.save(unavailability)
    }

    fun getOneUnavailability(id:Long): UnavailabilityDAO{
        return unavailabilities.findById(id)
            .orElseThrow { UnavailabilityNotFound() }
    }

    fun getAllUnavailabilities(): Iterable<UnavailabilityDAO> = unavailabilities.findAll()

    fun getUserUnavailabilities(uid: Long): UnavailabilityDAO{
        return unavailabilities.findByUserId(uid)
    }

}

class UnavailabilityNotFound(message: String? = UNAVAILABILITY_NOT_FOUND) : RuntimeException(message)
class UnavailabilityUnprocessable(message: String) : RuntimeException(message)
