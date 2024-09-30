package pt.unl.fct.di.apresentacoes.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeletePresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.domain.PresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.presistence.PresentationRepository
import pt.unl.fct.di.apresentacoes.presistence.PresentationUnavailabilityRepository
import pt.unl.fct.di.apresentacoes.presistence.SlotRepository
import pt.unl.fct.di.apresentacoes.presistence.UserRepository

private const val PRESENTATION_UNAVAILABILITY_NOT_FOUND = "There is no presentationUnavailability with the id received."

@Service
class PresentationUnavailabilityService (val presentationUnavailabilities: PresentationUnavailabilityRepository,
                                         val presentations: PresentationRepository,
                                         val slots: SlotRepository,
                                         val users: UserRepository
) {

    @Transactional
    fun addPresentationUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDAO {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }

        val presentationUnavailability = presentationUnavailabilities.findByPresentationId(presentationUnavailabilityDTO.presentationID)

        val slotToAdd = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        presentationUnavailability.slots.add(slotToAdd)
        return presentationUnavailabilities.save(presentationUnavailability)
    }
    @Transactional
    fun deletePresentationUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDAO{
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }

        val presentationUnavailability = presentationUnavailabilities.findByPresentationId(presentationUnavailabilityDTO.presentationID)

        val slotToRemove = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        presentationUnavailability.slots.remove(slotToRemove)
        return presentationUnavailabilities.save(presentationUnavailability)
    }

    fun getAllPresentationUnavailabilities(): Iterable<PresentationUnavailabilityDAO> = presentationUnavailabilities.findAll()

    fun getOnePresentationUnavailability(puid:Long): PresentationUnavailabilityDAO{
        return presentationUnavailabilities.findById(puid)
            .orElseThrow { PresentationUnavailabilityNotFound() }
    }

    fun getPresentationUnavailabilities(pid: Long): PresentationUnavailabilityDAO{
        return presentationUnavailabilities.findByPresentationId(pid)
    }
}

class PresentationUnavailabilityNotFound(message: String? = PRESENTATION_UNAVAILABILITY_NOT_FOUND) : RuntimeException(message)
class PresentationUnavailabilityUnprocessable(message: String) : RuntimeException(message)

