package com.chamada.principal;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import com.chamada.Aluno.Aluno;
import com.chamada.Sistema.Chamada;
import com.chamada.Professor.Professor;

public class Principal {
    private Chamada chamada;
    private final Scanner scanner = new Scanner(System.in);
    private final Professor professor = new Professor("Câmara", "Sistemas Operacionais");
    private final List<Aluno> turma = new ArrayList<>(List.of(
            new Aluno("Marcelo Ramos", "20001234"),
            new Aluno("Joao Danilo", "20005678"),
            new Aluno("Caio Faria", "20009012"),
            new Aluno("Maia", "20003456")));

    public static void main(String[] args) {
        new Principal().exibirMenu();
    }

    public void exibirMenu() {
        System.out.println("\nBem-Vindo " + professor.getNome() + "!");
        String opcao;
        do {
            System.out.println("\n===== CHAMADA: " + professor.getDisciplina() + " =====");
            System.out.println("1 - Abrir chamada (professor)");
            System.out.println("2 - Registrar presença (aluno)");
            System.out.println("3 - Gerar novo QR Code (professor)");
            System.out.println("4 - Encerrar chamada e gerar relatório (professor)");
            System.out.println("0 - Sair");
            opcao = ler("Opção: ");
            switch (opcao) {
                case "1" -> abrirChamada();
                case "2" -> registrarPresenca();
                case "3" -> renovarQrCode();
                case "4" -> encerrarChamada();
                case "0" -> sair();
                default -> System.out.println("Opção inválida.");
            }
        } while (!opcao.equals("0"));
    }

    private void abrirChamada() {
        if (chamada != null) {
            System.out.println("Já existe uma chamada aberta. Encerre-a antes de abrir outra.");
            return;
        }
        chamada = professor.abrirChamada(turma);
        System.out.println("Chamada aberta.\nQR Code da aula:");
        chamada.getQrCode().exibir();
    }

    private void registrarPresenca() {
        if (semChamadaAberta()) {return;}
        String matricula = ler("Matrícula: ");
        String codigo = ler("Código do QR Code: ");
        String erro = chamada.validar(matricula, codigo);
        if (erro != null) {
            System.out.println("Presença NÃO registrada: " + erro);
            return;
        }
        chamada.registrarPresenca(matricula);
    }

    private void renovarQrCode() {
        if (semChamadaAberta()) {return;}
        if (!chamada.renovarQrCode()) {
            System.out.println("Não é possível gerar um novo QR Code para esta chamada.");
            return;
        } System.out.println("Novo QR Code da aula:");
        chamada.getQrCode().exibir();
    }

    private void encerrarChamada() {
        if (semChamadaAberta()) {return;}
        System.out.println("Chamada encerrada.");
        chamada.gerarRelatorio();
        chamada = null;
    }

    private void sair() {
        if (chamada != null) {encerrarChamada();}
        System.out.println("Até logo!");
    }

    private boolean semChamadaAberta() {
        if (chamada == null) {System.out.println("Nenhuma chamada aberta.");}
        return chamada == null;
    }

    private String ler(String rotulo) {
        System.out.print(rotulo);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
    }
}
