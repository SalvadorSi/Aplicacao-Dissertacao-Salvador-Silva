package pt.unl.fct.di.apresentacoes.api.unavailabilities

import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeletePresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.UnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.services.*

@RestController
class UnavailabilityController(val unavailabilities: UnavailabilityService,
                               val presentationUnavailabilities: PresentationUnavailabilityService) : UnavailabilityAPI {

    override fun addUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UnavailabilityDTO {
        try{
            return UnavailabilityDTO(unavailabilities.addUnavailability(unavailabilityDTO))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UnavailabilityDTO {
        try{
            return UnavailabilityDTO(unavailabilities.deleteUnavailability(unavailabilityDTO))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }


    override fun getUnavailability(id: Long): UnavailabilityDTO {
        try{
            return UnavailabilityDTO(unavailabilities.getOneUnavailability(id))
        } catch (e : UnavailabilityNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllUnavailabilities(): Collection<UnavailabilityDTO> =
        unavailabilities.getAllUnavailabilities().map { e -> UnavailabilityDTO(e) }

    override fun getUserUnavailabilities(uid: Long): UnavailabilityDTO {
        return UnavailabilityDTO(unavailabilities.getUserUnavailabilities(uid))
    }


    /** PRESENTATION UNAVAILABILITIES - RESTRICTIONS FOR ONLY ONE PRESENTATION **/

    override fun addPresentationUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDTO {
        try{
            return PresentationUnavailabilityDTO(presentationUnavailabilities.addPresentationUnavailability(presentationUnavailabilityDTO))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deletePresentationUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDTO {
        try{
            return PresentationUnavailabilityDTO(presentationUnavailabilities.deletePresentationUnavailability(presentationUnavailabilityDTO))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun getAllPresentationUnavailabilities(): Collection<PresentationUnavailabilityDTO> =
        presentationUnavailabilities.getAllPresentationUnavailabilities().map { e -> PresentationUnavailabilityDTO(e) }

    override fun getOnePresentationUnavailability(puid: Long): PresentationUnavailabilityDTO {
        try{
            return PresentationUnavailabilityDTO(presentationUnavailabilities.getOnePresentationUnavailability(puid))
        } catch (e : PresentationUnavailabilityNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getPresentationUnavailabilities(pid: Long): PresentationUnavailabilityDTO {
        return PresentationUnavailabilityDTO(presentationUnavailabilities.getPresentationUnavailabilities(pid))
    }

}