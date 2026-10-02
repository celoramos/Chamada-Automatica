package com.chamada.Professor;
import com.chamada.Aluno.Aluno;
import com.chamada.Sistema.Chamada;
import java.util.List;

public class Professor {
    private final String nome;
    private final String disciplina;

    public Professor(String nome, String disciplina) {
        this.nome = nome;
        this.disciplina = disciplina;
    }

    public String getNome() {return nome;}
    public String getDisciplina() {return disciplina;}

    public Chamada abrirChamada(List<Aluno> turma) {
        return new Chamada(disciplina, turma);
    }
}
