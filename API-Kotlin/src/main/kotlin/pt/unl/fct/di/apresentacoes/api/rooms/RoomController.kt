package pt.unl.fct.di.apresentacoes.api.rooms

import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.dto.RoomDTO
import pt.unl.fct.di.apresentacoes.api.dto.SlotDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.services.*

@RestController
class RoomController(val rooms: RoomService) : RoomAPI {

    override fun addInitialRooms() {
        try{
            rooms.addInitialRooms()
        } catch (e: ThereAreRooms){
            throw APIConflictException(e.message)
        }
    }

    override fun addRoom(room: RoomDTO): RoomDTO{
        try{
            return RoomDTO(rooms.addRoom(room))
        } catch (e: RoomAlreadyExists){
            throw APIConflictException(e.message)
        }
    }

    override fun getRoom(id: Long): RoomDTO {
        try {
            return RoomDTO(rooms.getOneRoom(id))
        } catch (e: RoomNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllRooms(): Collection<RoomDTO> =
        rooms.getAllRooms().map { e -> RoomDTO(e) }

    override fun deleteRoom(id: Long) {
        try {
            rooms.deleteOneRoom(id)
        } catch (e: RoomNotFound){
            throw APINotFoundException(e.message)
        }
    }

}