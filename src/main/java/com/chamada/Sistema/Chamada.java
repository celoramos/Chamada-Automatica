package com.chamada.Sistema;

import com.chamada.Aluno.Aluno;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chamada {
    private final String abertaEm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    private final String disciplina;
    private final List<Aluno> turma;
    private final Map<Aluno, String> presentes = new HashMap<>();
    private QRCode qrCode = new QRCode();

    public Chamada(String disciplina, List<Aluno> turma) {
        this.disciplina = disciplina;
        this.turma = turma;
    }

    public QRCode getQrCode() {
        return qrCode;
    }

    public boolean renovarQrCode() {
        qrCode = new QRCode();
        return true;
    }

    private Aluno buscarAluno(String matricula) {
        for (Aluno aluno : turma) {
            if (aluno.getMatricula().equals(matricula)) {
                return aluno;
            }
        }
        return null;
    }

    public String validar(String matricula, String codigoLido) {
        Aluno aluno = buscarAluno(matricula);
        if (aluno == null) {return "a matrícula " + matricula + " não pertence a esta turma.";}
        if (presentes.containsKey(aluno)) {return "a presença de " + aluno.getNome() + " já foi registrada.";}
        if (!qrCode.confere(codigoLido)) {return "o QR Code lido não é o desta aula.";}
        if (qrCode.expirou()) {return "o QR Code expirou (fora do horário da chamada).";}
        return null;
    }

    public void registrarPresenca(String matricula) {
        Aluno aluno = buscarAluno(matricula);
        presentes.put(aluno, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        System.out.println("PRESENÇA registrada: " + aluno.getNome());
    }

    // Quem está na turma e não registrou presença fica com FALTA
    public void gerarRelatorio() {
        System.out.println("\n===== RELATÓRIO DE FREQUÊNCIA =====");
        System.out.println("Disciplina: " + disciplina);
        System.out.println("Chamada aberta em: " + abertaEm);
        System.out.println("-----------------------------------------------------");
        System.out.printf("%-10s %-22s %-9s %s%n", "MATRÍCULA", "ALUNO", "STATUS", "HORA");
        for (Aluno aluno : turma) {
            String hora = presentes.get(aluno);
            System.out.printf("%-10s %-22s %-9s %s%n", aluno.getMatricula(), aluno.getNome(),
                    hora != null ? "PRESENÇA" : "FALTA", hora != null ? hora : "-");
        }
        System.out.println("-----------------------------------------------------");
        System.out.println("Presentes: " + presentes.size() + " | Faltas: " + (turma.size() - presentes.size()));
    }
}
