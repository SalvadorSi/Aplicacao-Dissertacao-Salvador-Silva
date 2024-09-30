package pt.unl.fct.di.apresentacoes.api.presentations

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pt.unl.fct.di.apresentacoes.api.dto.AddOptionalDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.services.PresentationNotFound
import pt.unl.fct.di.apresentacoes.services.PresentationService
import pt.unl.fct.di.apresentacoes.services.PresentationUnprocessable

@RestController
class PresentationController(val presentations: PresentationService) : PresentationAPI{

    override fun add(presentationDTO: PresentationDTO): PresentationDTO {
        try{
            return PresentationDTO(presentations.add(presentationDTO))
        } catch (e: PresentationUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun uploadData(excelFile: MultipartFile) {
        try{
            presentations.uploadData(excelFile)
        } catch (e:PresentationUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun getPresentation(pid: Long): PresentationDTO {
        try{
            return PresentationDTO(presentations.getOnePresentation(pid))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllPresentations(): Collection<PresentationDTO> =
        presentations.getAllPresentations().map{ e -> PresentationDTO(e) }

    override fun deletePresentation(pid: Long) {
        try{
            presentations.deleteOnePresentation(pid)
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getUserPresentations(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentations(uid).map { e -> PresentationDTO(e) }
    }

    override fun getUserPresentationsAsAdviserAndAsArguer(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentationsAsAdviserAndAsArguer(uid).map { e -> PresentationDTO(e) }
    }

    override fun getUserPresentationsAsAdviser(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentationsAsAdviser(uid).map{ e -> PresentationDTO(e) }
    }

    override fun getUserPresentationsAsArguer(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentationsAsArguer(uid).map{ e -> PresentationDTO(e) }
    }

    override fun getUserPresentationsAsOptional(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentationsAsOptional(uid).map{ e -> PresentationDTO(e) }
    }

    override fun addOptional1(pid: Long, addOptionalDTO: AddOptionalDTO): PresentationDTO {
        try{
            return PresentationDTO(presentations.addOptional(pid,addOptionalDTO, true))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        } catch (e:PresentationUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addOptional2(pid: Long, addOptionalDTO: AddOptionalDTO): PresentationDTO {
        try{
            return PresentationDTO(presentations.addOptional(pid,addOptionalDTO, false))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        } catch (e:PresentationUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteOptional1(pid: Long): PresentationDTO {
        try{
            return PresentationDTO(presentations.deleteOptional(pid, true))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun deleteOptional2(pid: Long): PresentationDTO {
        try{
            return PresentationDTO(presentations.deleteOptional(pid, false))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        }
    }

}