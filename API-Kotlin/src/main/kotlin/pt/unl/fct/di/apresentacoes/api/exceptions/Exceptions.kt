package pt.unl.fct.di.apresentacoes.api.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.NOT_FOUND)
class APINotFoundException(message: String? = "NOT FOUND") : Exception(message)

@ResponseStatus(code = HttpStatus.CONFLICT)
class APIConflictException(message: String? = "CONFLICT") : Exception(message)

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
class APIUnprocessableEntityException(message: String? = "UNPROCESSABLE ENTITY") : Exception(message)