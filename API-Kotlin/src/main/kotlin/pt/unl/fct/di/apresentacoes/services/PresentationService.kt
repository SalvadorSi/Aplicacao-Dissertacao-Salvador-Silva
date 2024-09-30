package pt.unl.fct.di.apresentacoes.services

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import pt.unl.fct.di.apresentacoes.api.dto.AddOptionalDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationDTO
import pt.unl.fct.di.apresentacoes.domain.PresentationDAO
import pt.unl.fct.di.apresentacoes.domain.PresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.presistence.*

private const val PRESENTATION_NOT_FOUND = "This presentations does not exist."
private const val DEFAULT_ID_IN_EXCEL = 0
private const val DEFAULT_STUDENT_NUMBER_IN_EXCEL = 1
private const val DEFAULT_STUDENT_NAME_IN_EXCEL = 2
private const val DEFAULT_THESIS_TITLE_IN_EXCEL = 3
private const val DEFAULT_ADVISER_IN_EXCEL = 4
private const val DEFAULT_ARGUER_IN_EXCEL = 5
private const val DEFAULT_ROW_LENGTH_IN_EXCEL = 6


@Service
class PresentationService(val presentations: PresentationRepository,
                          val users: UserRepository,
                          val slots: SlotRepository,
                          val rooms: RoomRepository,
                          val presentationUnavailabilities: PresentationUnavailabilityRepository,
                          val followedPresentations: FollowedPresentationsRepository) {

    @Transactional
    fun add(presentationDTO: PresentationDTO):PresentationDAO{
        val adviserName = presentationDTO.adviserName
        val arguerName = presentationDTO.arguerName
        val adviser = users.findByName(adviserName).orElseThrow { throw PresentationUnprocessable("Aviser: \"$adviserName\" does not exist.") }
        val arguer = users.findByName(arguerName).orElseThrow { throw PresentationUnprocessable("Arguer: \"$arguerName\" does not exist.") }
        if(presentations.findById(presentationDTO.id).isPresent){
            throw PresentationUnprocessable("There is already a presentations with this id.")
        }
        if(adviser.id == arguer.id)
            throw PresentationUnprocessable("User can not be adviser and arguer.")

        val studentNumber = presentationDTO.studentNumber
        val studentName = presentationDTO.studentName
        if(presentations.existsByStudentNumber(studentNumber))
            throw PresentationUnprocessable("The student with the number \"$studentNumber\" already has one presentation.")
        if(presentations.existsByStudentName(studentName))
            throw PresentationUnprocessable("The student \"$studentName\" already has one presentation.")

        val presentationDAO = PresentationDAO(
            presentationDTO.id,
            studentNumber,
            studentName,
            presentationDTO.thesisTitle,
            adviser,
            arguer
        )
        val presentationUnavailabilityDAO = PresentationUnavailabilityDAO(0, mutableListOf(),presentationDAO,adviser)
        presentationUnavailabilities.save(presentationUnavailabilityDAO)
        presentationDAO.presentationUnavailability = presentationUnavailabilityDAO
        return presentations.save(presentationDAO)
    }

    @Transactional
    fun uploadData(excelFile: MultipartFile){
        val wb = WorkbookFactory.create(excelFile.inputStream)
        val sheet = wb.getSheetAt(0) //1st sheet
        val rowIt = sheet.rowIterator()

        //1st row, is the one with the types (ex: studentNr, studentName, etc.)
        rowIt.next()

        while (rowIt.hasNext()){
            val currentRow = rowIt.next()
            var presentationData = mutableListOf<String>()
            for(i in 0 until DEFAULT_ROW_LENGTH_IN_EXCEL) {
                val cellValue = currentRow.getCell(i).toString()
                presentationData.add(cellValue)
            }

            val adviserName = presentationData[DEFAULT_ADVISER_IN_EXCEL]
            val arguerName = presentationData[DEFAULT_ARGUER_IN_EXCEL]
            val adviser = users.findByName(adviserName).orElseThrow { throw PresentationUnprocessable("Aviser: \"$adviserName\" does not exist.") }
            val arguer = users.findByName(arguerName).orElseThrow { throw PresentationUnprocessable("Arguer: \"$arguerName\" does not exist.") }

            val studentNumber = presentationData[DEFAULT_STUDENT_NUMBER_IN_EXCEL].replace(".0", "")
            val studentName = presentationData[DEFAULT_STUDENT_NAME_IN_EXCEL]

            if(presentations.existsByStudentNumber(studentNumber))
                throw PresentationUnprocessable("The student with the number \"$studentNumber\" already has one presentation.")
            if(presentations.existsByStudentName(studentName))
                throw PresentationUnprocessable("The student \"$studentName\" already has one presentation.")

            val presentationDAO = PresentationDAO(
                //numbers come with .0 from Excel. replace only eliminates te .0 so it can become a number
                0,
                studentNumber,
                studentName,
                presentationData[DEFAULT_THESIS_TITLE_IN_EXCEL],
                adviser,
                arguer
            )
            val presentationUnavailabilityDAO = PresentationUnavailabilityDAO(0, mutableListOf(),presentationDAO,adviser)
            presentationUnavailabilities.save(presentationUnavailabilityDAO)
            presentationDAO.presentationUnavailability = presentationUnavailabilityDAO
            presentations.save(presentationDAO)
        }
    }

    fun getOnePresentation(pid:Long): PresentationDAO{
        return presentations.findById(pid)
            .orElseThrow { PresentationNotFound() }
    }

    fun getAllPresentations(): Iterable<PresentationDAO> = presentations.findAll()

    fun deleteOnePresentation(pid:Long){
        val presentation = presentations.findById(pid)
            .orElseThrow { PresentationNotFound() }
        for(followed in presentation.followedPresentations){
            followedPresentations.delete(followed)
        }
        presentations.delete(presentation)
    }

    fun getUserPresentations(uid:Long): Iterable<PresentationDAO>{
        val adviserPresentations = presentations.findByAdviserId(uid)
        val arguerPresentations = presentations.findByArguerId(uid)
        val optional1Presentations = presentations.findByOptionalParticipant1Id(uid)
        val optional2Presentations = presentations.findByOptionalParticipant2Id(uid)
        return adviserPresentations + arguerPresentations + optional1Presentations + optional2Presentations
    }

    fun getUserPresentationsAsAdviserAndAsArguer(uid:Long): Iterable<PresentationDAO>{
        val adviserPresentations = presentations.findByAdviserId(uid)
        val arguerPresentations = presentations.findByArguerId(uid)
        return adviserPresentations + arguerPresentations
    }

    fun getUserPresentationsAsAdviser(uid: Long): Iterable<PresentationDAO> {
        return presentations.findByAdviserId(uid)
    }

    fun getUserPresentationsAsArguer(uid: Long): Iterable<PresentationDAO> {
        return presentations.findByArguerId(uid)
    }

    fun getUserPresentationsAsOptional(uid: Long): Iterable<PresentationDAO> {
        val optional1Presentations = presentations.findByOptionalParticipant1Id(uid)
        val optional2Presentations = presentations.findByOptionalParticipant2Id(uid)
        return optional1Presentations + optional2Presentations
    }

    @Transactional
    fun addOptional(pid:Long, optional: AddOptionalDTO, optional1: Boolean): PresentationDAO{
        val presentation = presentations.findById(pid)
            .orElseThrow { PresentationNotFound() }
        val user = users.findByName(optional.name)
            .orElseThrow { PresentationUnprocessable("User with the name: ${optional.name} does not exist.") }
        if(presentation.adviser.name == optional.name || presentation.arguer.name == optional.name)
            throw PresentationUnprocessable("This user is the arguer or the adviser of the presentation. Can not add this user as optional participant.")
        if(optional1) {
            presentation.optionalParticipant1 = user
        }
        else {
            presentation.optionalParticipant2 = user
        }
        return presentations.save(presentation)
    }

    @Transactional
    fun deleteOptional(pid:Long, optional1: Boolean): PresentationDAO{
        val presentation = presentations.findById(pid)
            .orElseThrow { PresentationNotFound() }
        if(optional1)
            presentation.optionalParticipant1 = null
        else
            presentation.optionalParticipant2=null
        return presentations.save(presentation)
    }

    fun getPresentationsWithOptionals(): Iterable<PresentationDAO> = presentations.findByOptionalParticipant1NotNullOrOptionalParticipant2NotNull()

    //Exceptions should not be necessary because all the values received are in database, but just in case something happens
    fun addRoomAndSlot(pid:Long, roomID:Long, slotID:Long){
        val presentation = presentations.findById(pid)
            .orElseThrow { PresentationNotFound("This presentations $pid") }
        val slot = slots.findById(slotID)
            .orElseThrow { PresentationUnprocessable("Slot with the id: $slotID does not exist.") }
        val room = rooms.findById(roomID)
            .orElseThrow { PresentationUnprocessable("Room with the id: $roomID does not exist.") }
        presentation.slot = slot
        presentation.room = room
        presentations.save(presentation)
    }
}

class PresentationNotFound(message: String? = PRESENTATION_NOT_FOUND) : RuntimeException(message)
class PresentationUnprocessable(message: String): RuntimeException(message)