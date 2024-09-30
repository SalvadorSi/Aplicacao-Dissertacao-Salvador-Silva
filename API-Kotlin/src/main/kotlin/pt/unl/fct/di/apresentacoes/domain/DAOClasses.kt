package pt.unl.fct.di.apresentacoes.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name="users")
data class UserDAO(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,

    val name: String,
    val email: String,
    var role: UserRoles,
    var password: String,

    @OneToMany(mappedBy = "adviser", orphanRemoval = true, cascade = [CascadeType.PERSIST])
    val adviserPresentations: MutableCollection<PresentationDAO>,
    @OneToMany(mappedBy = "arguer", orphanRemoval = true, cascade = [CascadeType.PERSIST])
    val arguerPresentations: MutableCollection<PresentationDAO>,
    @OneToMany(mappedBy = "optionalParticipant1")
    val optional1Presentations: MutableCollection<PresentationDAO>,
    @OneToMany(mappedBy = "optionalParticipant2")
    val optional2Presentations: MutableCollection<PresentationDAO>,
    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = [CascadeType.PERSIST])
    val followedPresentations: MutableCollection<FollowedPresentationsDAO>,
    @OneToMany(mappedBy = "user")
    val presentationUnavailabilities: MutableCollection<PresentationUnavailabilityDAO>,
    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade = [CascadeType.PERSIST])
    var unavailability: UnavailabilityDAO?,
) {
constructor(id: Long,name: String,email: String,role: UserRoles,password: String) :
        this(id,name, email, role, password, mutableListOf(),mutableListOf(),mutableListOf(),mutableListOf(),mutableListOf(),mutableListOf(),null)
}

@Entity
@Table(name="slots")
data class SlotDAO(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,

    val date: LocalDate,
    val startingHour: LocalTime,

    @ManyToMany(mappedBy = "slots")
    val unavailabilities: MutableCollection<UnavailabilityDAO>,
    @ManyToMany(mappedBy = "slots")
    val presentationUnavailabilities: MutableCollection<PresentationUnavailabilityDAO>,
    @OneToMany(mappedBy = "slot")
    val presentations: MutableCollection<PresentationDAO>
)

@Entity
@Table(name="rooms")
data class RoomDAO(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,

    var roomNr:String,
    var building:String,

    @OneToMany(mappedBy = "room")
    val presentations: MutableCollection<PresentationDAO>
)

@Entity
@Table(name="presentations")
data class PresentationDAO(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)//not auto generated because it has to have the same id as the one in the Excel
    val id: Long,

    val studentNumber: String,
    val studentName: String,
    val thesisTitle: String,
    @ManyToOne  // one presentation has only one room, but one room has several presentations
    var room: RoomDAO?,
    @ManyToOne  // one presentation has only one slot, but one slot has several presentations
    var slot: SlotDAO?,  // hour, date
    @ManyToOne
    val adviser: UserDAO,
    @ManyToOne
    val arguer: UserDAO,
    @ManyToOne
    var optionalParticipant1: UserDAO?, //added by the thesis adviser
    @ManyToOne
    var optionalParticipant2: UserDAO?, // add by an organizer/administrator of the app

    @ManyToMany(mappedBy = "presentations")
    val followedPresentations: MutableCollection<FollowedPresentationsDAO>,
    @OneToOne(mappedBy = "presentation", orphanRemoval = true, cascade = [CascadeType.ALL])
    var presentationUnavailability: PresentationUnavailabilityDAO?
) { /** Default, put room slot and optionals with null**/
    constructor(id: Long,studentNumber: String,studentName: String,thesisTitle: String,adviser: UserDAO,arguer: UserDAO) :
            this(id,studentNumber, studentName, thesisTitle, room=null, slot=null, adviser, arguer, optionalParticipant1=null, optionalParticipant2=null, mutableListOf(),
                null
            )
}

@Entity
@Table(name="unavailabilities")
data class UnavailabilityDAO(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,

    @ManyToMany
    var slots: MutableCollection<SlotDAO>,

    @OneToOne
    val user: UserDAO
)

@Entity
@Table(name="presentationUnavailabilities")
data class PresentationUnavailabilityDAO(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,

    @ManyToMany
    var slots: MutableCollection<SlotDAO>,

    @OneToOne
    val presentation: PresentationDAO,
    @ManyToOne
    val user: UserDAO
)

@Entity
@Table(name="followedPresentations")
data class FollowedPresentationsDAO(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,
    @ManyToOne
    var user: UserDAO,
    @ManyToMany
    var presentations: MutableCollection<PresentationDAO>
)

enum class UserRoles {
    ADMIN,
    ORGANIZER,
    USER
}