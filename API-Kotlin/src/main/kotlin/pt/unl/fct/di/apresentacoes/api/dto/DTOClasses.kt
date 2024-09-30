package pt.unl.fct.di.apresentacoes.api.dto

import pt.unl.fct.di.apresentacoes.domain.*
import java.time.LocalDate
import java.time.LocalTime

data class CreateUserDTO(
    val id: Long,
    val name: String,
    val email: String
) {
    constructor(userDAO: UserDAO) : this(
        userDAO.id,
        userDAO.name,
        userDAO.email
    )
}

data class FullUserDTO(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRoles
) {
    constructor(userDAO: UserDAO) : this(
        userDAO.id,
        userDAO.name,
        userDAO.email,
        userDAO.role
    )
}

data class UserNameDTO(
    val name: String,
) {
    constructor(userDAO: UserDAO) : this(
        userDAO.name,
    )
}

data class SlotDTO(
    val id: Long,
    val date: LocalDate,
    val startingHour: LocalTime
) {
    constructor(slotDAO: SlotDAO) : this(
        slotDAO.id,
        slotDAO.date,
        slotDAO.startingHour
    )
}

data class SlotsDataDTO(
    val date: LocalDate,
    val numberOfSlots: Long
)

data class RoomDTO(
    val id: Long,

    val roomNr:String,
    val building:String
) {
    constructor(roomDAO: RoomDAO) : this(
        roomDAO.id,
        roomDAO.roomNr,
        roomDAO.building
    )
}

data class UnavailabilityDTO(
    val id: Long,

    val slotsID: Collection<Long>,
    val userID: Long
) {
    constructor(unavailabilityDAO: UnavailabilityDAO) : this(
        unavailabilityDAO.id,
        unavailabilityDAO.slots.map { e -> e.id },
        unavailabilityDAO.user.id
    )
}

data class AddOrDeleteUnavailabilityDTO(
    val slotID: Long,
    val userID: Long
)

data class PresentationUnavailabilityDTO(
    val id: Long,

    val slotsID: Collection<Long>,
    val presentationID: Long,
    val userID: Long
) {
    constructor(presentationUnavailabilityDAO: PresentationUnavailabilityDAO) : this(
        presentationUnavailabilityDAO.id,
        presentationUnavailabilityDAO.slots.map { e -> e.id },
        presentationUnavailabilityDAO.presentation.id,
        presentationUnavailabilityDAO.user.id
    )
}

data class AddOrDeletePresentationUnavailabilityDTO(
    val slotID: Long,
    val presentationID: Long
)

data class PresentationDTO(
    val id: Long,

    val studentNumber: String,
    val studentName: String,
    val thesisTitle: String,
    val roomNumber: String?,
    val roomBuilding: String?,
    val startingHour: LocalTime?,
    val date: LocalDate?,
    val adviserName: String,
    val arguerName: String,
    val optionalParticipant1Name: String?,
    val optionalParticipant2Name: String?
) {
    constructor(presentationDAO: PresentationDAO) : this(
        presentationDAO.id,
        presentationDAO.studentNumber,
        presentationDAO.studentName,
        presentationDAO.thesisTitle,
        presentationDAO.room?.roomNr,
        presentationDAO.room?.building,
        presentationDAO.slot?.startingHour,
        presentationDAO.slot?.date,
        presentationDAO.adviser.name,
        presentationDAO.arguer.name,
        presentationDAO.optionalParticipant1?.name,
        presentationDAO.optionalParticipant2?.name
    )
}

data class FollowedPresentationsDTO(
    val id: Long,

    val userID: Long,
    val presentationsIDS: Collection<Long>
) {
    constructor(followedPresentationsDAO: FollowedPresentationsDAO) : this(
        followedPresentationsDAO.id,
        followedPresentationsDAO.user.id,
        followedPresentationsDAO.presentations.map { e -> e.id }
    )
}


data class ChangePasswordDTO(
    val oldPassword: String,
    val newPassword: String
)

data class ChangeRoleDTO(
    val userID: Long,
    val newRole: UserRoles
)

data class AddOptionalDTO(
    val name: String
)

data class LoginDTO(
    val email: String,
    var password: String
)

data class AuthResponseDTO(
    val token: String
)

data class ForgotPasswordDTO(
    val email: String
)


/** PYTHON DTOs **/

data class PythonDataDTO(
    val presentationJury: Collection<PresentationJuryDTO>,
    val followedSlots :Collection<Collection<SlotIDDTO>>,
    val rooms: Collection<RoomIDDTO>,
    val usersUnavailabilities: Collection<ReducedUnavailabilityDTO>,
    val presentationsRestrictions: Collection<ReducedPresentationUnavailabilityDTO>,
    val optionals: Collection<OptionalViewersDTO>,
    val followedPresentations: Collection<ReducedFollowedPresentationsDTO>
)

data class PresentationJuryDTO(
    val presentationID: Long,
    val adviserID: Long,
    val arguerID: Long
)

data class SlotIDDTO(
    val slotID: Long
)

data class RoomIDDTO(
    val roomID: Long
)

data class ReducedUnavailabilityDTO(
    val userID:Long,
    val slotsID: Collection<Long>
)

data class ReducedFollowedPresentationsDTO(
    val followedPresentationsIDS: Collection<Long>
)

data class ReducedPresentationUnavailabilityDTO(
    val presentationID: Long,
    val slotsID: Collection<Long>
)

data class OptionalViewersDTO(
    val userID: Long,
    val presentationID: Long
)