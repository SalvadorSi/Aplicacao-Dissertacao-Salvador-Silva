package pt.unl.fct.di.apresentacoes.api.followedPresentations

import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.dto.FollowedPresentationsDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.services.FollowedPesentationsNotFound
import pt.unl.fct.di.apresentacoes.services.FollowedPresentationsService
import pt.unl.fct.di.apresentacoes.services.FollowedPresentationsUnprocessable

@RestController
class FollowedPresentationsController(val followedPresentations: FollowedPresentationsService): FollowedPresentationsAPI {

    override fun addFollowedPresentations(presentations: FollowedPresentationsDTO): FollowedPresentationsDTO {
        try{
            return FollowedPresentationsDTO(followedPresentations.add(presentations))
        } catch (e: FollowedPresentationsUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun getAllFollowedPresentations(): Collection<FollowedPresentationsDTO> =
        followedPresentations.getAllFollowedPresentations().map { e -> FollowedPresentationsDTO(e) }

    override fun getUserFollowedPresentations(uid: Long): Collection<FollowedPresentationsDTO> {
        return followedPresentations.getUserFollowedPresentations(uid).map { e -> FollowedPresentationsDTO(e) }
    }

    override fun deleteFollowedPresentations(fpid: Long) {
        try{
            followedPresentations.deleteOneFollowedPresentations(fpid)
        } catch (e: FollowedPesentationsNotFound){
            throw APINotFoundException(e.message)
        }
    }


}