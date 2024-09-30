package pt.unl.fct.di.apresentacoes.api.slots

import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.dto.SlotDTO
import pt.unl.fct.di.apresentacoes.api.dto.SlotsDataDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.services.*

@RestController
class SlotController (val slots: SlotService): SlotAPI {

    override fun addSlots(slotData: SlotsDataDTO) {
        try {
            slots.addSlots(slotData)
        } catch (e: SlotDayAlreadyExists) {
            throw APIConflictException(e.message)
        } catch (e: MaxSlots) {
            throw APIConflictException(e.message)
        }
    }

    override fun addSingleSlot(slotDTO: SlotDTO) {
        try {
            slots.addSingleSlot(slotDTO)
        } catch (e: SlotAlreadyExists) {
            throw APIConflictException(e.message)
        }
    }

    override fun getSlot(id: Long): SlotDTO {
        try{
            return SlotDTO(slots.getOneSlot(id))
        } catch (e: SlotNotFound) {
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllSlots(): Collection<SlotDTO> =
        slots.getAllSlots().map { e -> SlotDTO(e) }

    override fun deleteSlot(id: Long) {
        try {
            slots.deleteOne(id)
        } catch (e: SlotNotFound) {
            throw APINotFoundException(e.message)
        }
    }

}