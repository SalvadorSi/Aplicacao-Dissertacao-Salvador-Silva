package pt.unl.fct.di.apresentacoes.api.unavailabilities

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeletePresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.UnavailabilityDTO

@RequestMapping("/api/unavailability")
interface UnavailabilityAPI {

    @PostMapping("")
    @CanAddAndDeleteUnavailability
    fun addUnavailability(@RequestBody unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UnavailabilityDTO

    @DeleteMapping("")
    @CanAddAndDeleteUnavailability
    fun deleteUnavailability(@RequestBody unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UnavailabilityDTO

    @GetMapping("/{id}")
    @CanReadOneUnavailability
    fun getUnavailability(@PathVariable id: Long): UnavailabilityDTO

    @GetMapping("")
    @CanReadAllUnavailabilities
    fun getAllUnavailabilities(): Collection<UnavailabilityDTO>

    @GetMapping("/user/{uid}")
    @CanReadUserUnavailabilities
    fun getUserUnavailabilities(@PathVariable uid: Long): UnavailabilityDTO

    /** PRESENTATION UNAVAILABILITIES - RESTRICTIONS FOR ONLY ONE PRESENTATION **/

    @PostMapping("/presentation")
    @CanAddAndDeletePresentationUnavailability
    fun addPresentationUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDTO

    @DeleteMapping("/presentation")
    @CanAddAndDeletePresentationUnavailability
    fun deletePresentationUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDTO

    @GetMapping("/presentation")
    @CanReadAllPresentationUnavailabilities
    fun getAllPresentationUnavailabilities(): Collection<PresentationUnavailabilityDTO>

    @GetMapping("/presentationUnavailability/{puid}")
    @CanReadOnePresentationUnavailability
    fun getOnePresentationUnavailability(@PathVariable puid: Long): PresentationUnavailabilityDTO

    @GetMapping("/presentation/{pid}")
    @CanReadPresentaionUnavailabilities
    fun getPresentationUnavailabilities(@PathVariable pid: Long): PresentationUnavailabilityDTO

}