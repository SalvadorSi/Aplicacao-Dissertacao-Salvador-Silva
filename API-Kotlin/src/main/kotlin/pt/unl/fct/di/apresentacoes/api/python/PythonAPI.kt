package pt.unl.fct.di.apresentacoes.api.python

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/python")
interface PythonAPI {

    @PostMapping("/execute")
    @CanExecuteClingo
    fun executeClingo() : String

}