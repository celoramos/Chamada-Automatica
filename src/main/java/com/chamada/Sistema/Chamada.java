package com.chamada.Sistema;

import com.chamada.Aluno.Aluno;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Chamada {
    private final String abertaEm;
    private final String disciplina;
    private final List<Aluno> turma;

    // Apenas a thread consumidora registra presenças neste mapa.
    private final Map<Aluno, String> presentes = new LinkedHashMap<>();

    private QRCode qrCode;

    public Chamada(String disciplina, List<Aluno> turma) {
        this.disciplina = disciplina;
        this.turma = turma;
        this.abertaEm = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        this.qrCode = new QRCode();
    }

    public QRCode getQrCode() {
        return qrCode;
    }

    public void renovarQrCode() {
        qrCode = new QRCode();
    }

    private Aluno buscarAluno(String matricula) {
        for (Aluno aluno : turma) {
            if (aluno.getMatricula().equals(matricula)) {
                return aluno;
            }
        }
        return null;
    }

    /**
     * A validação acontece somente no consumidor.
     * O horário usado é o instante em que o produtor criou a solicitação.
     */
    public String validar(SolicitacaoPresenca solicitacao) {
        Aluno aluno = buscarAluno(solicitacao.getMatricula());

        if (aluno == null) {
            return "a matrícula " + solicitacao.getMatricula()
                    + " não pertence a esta turma.";
        }

        if (presentes.containsKey(aluno)) {
            return "a presença de " + aluno.getNome() + " já foi registrada.";
        }

        if (!qrCode.confere(solicitacao.getCodigoLido())) {
            return "o QR Code lido não corresponde ao QR Code atual da aula.";
        }

        if (qrCode.expirouEm(solicitacao.getHorarioSolicitacao())) {
            return "o QR Code já estava expirado quando a solicitação foi enviada.";
        }

        return null;
    }

    public void registrarPresenca(SolicitacaoPresenca solicitacao) {
        Aluno aluno = buscarAluno(solicitacao.getMatricula());

        String horario = solicitacao.getHorarioSolicitacao()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        presentes.put(aluno, horario);

        System.out.println("[REGISTRO] Presença registrada: "
                + aluno.getNome() + " às " + horario + ".");
    }

    public void gerarRelatorio() {
        System.out.println("\n===== RELATÓRIO DE FREQUÊNCIA =====");
        System.out.println("Disciplina: " + disciplina);
        System.out.println("Chamada aberta em: " + abertaEm);
        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-10s %-24s %-10s %s%n",
                "MATRÍCULA", "ALUNO", "STATUS", "HORA");

        for (Aluno aluno : turma) {
            String horario = presentes.get(aluno);

            System.out.printf("%-10s %-24s %-10s %s%n",
                    aluno.getMatricula(),
                    aluno.getNome(),
                    horario != null ? "PRESENTE" : "FALTA",
                    horario != null ? horario : "-");
        }

        System.out.println("---------------------------------------------------------------");
        System.out.println("Presentes: " + presentes.size()
                + " | Faltas: " + (turma.size() - presentes.size()));
    }
}
