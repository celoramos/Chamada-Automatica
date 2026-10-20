package com.chamada.Sistema;

import java.time.LocalDateTime;

/**
 * Produto criado por um aluno (produtor) e processado pelo consumidor.
 */
public class SolicitacaoPresenca {
    private final String matricula;
    private final String codigoLido;
    private final LocalDateTime horarioSolicitacao;

    public SolicitacaoPresenca(String matricula, String codigoLido) {
        this.matricula = matricula;
        this.codigoLido = codigoLido;
        this.horarioSolicitacao = LocalDateTime.now();
    }

    public String getMatricula() {
        return matricula;
    }

    public String getCodigoLido() {
        return codigoLido;
    }

    public LocalDateTime getHorarioSolicitacao() {
        return horarioSolicitacao;
    }
}
