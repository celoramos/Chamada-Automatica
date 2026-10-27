package com.chamada.principal;

import com.chamada.Aluno.Aluno;
import com.chamada.Professor.Professor;
import com.chamada.Sistema.BufferPresenca;
import com.chamada.Sistema.Chamada;
import com.chamada.Sistema.ConsumidorPresenca;
import com.chamada.Sistema.ProdutorAluno;
import com.chamada.Sistema.SolicitacaoPresenca;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal {
    // Buffer pequeno de propósito para permitir visualizar o bloqueio em sala.
    private static final int CAPACIDADE_BUFFER = 3;

    // Representa o tempo gasto pelo sistema para validar/processar uma solicitação.
    private static final long TEMPO_PROCESSAMENTO_MS = 4_000;

    private final Scanner scanner = new Scanner(System.in);
    private final Professor professor = new Professor("Câmara", "Sistemas Operacionais");

    private final List<Aluno> turma = new ArrayList<>(List.of(
            new Aluno("Marcelo Ramos", "001"),
            new Aluno("Joao Danilo", "002"),
            new Aluno("Caio Faria", "003"),
            new Aluno("Maia", "004"),
            new Aluno("Ana Souza", "005"),
            new Aluno("Paula Oliveira", "006"),
            new Aluno("Pedro Santos", "007"),
            new Aluno("Luis Gabriel", "008"),
            new Aluno("Lara Silva", "009"),
            new Aluno("Maria Oliveira", "010")));

    private final List<ProdutorAluno> produtoresCriados = new ArrayList<>();

    private Chamada chamada;
    private BufferPresenca buffer;
    private ConsumidorPresenca consumidor;

    public static void main(String[] args) {
        new Principal().exibirMenu();
    }

    public void exibirMenu() {
        System.out.println("\nBem-vindo, professor " + professor.getNome() + "!");

        String opcao;

        do {
            System.out.println("\n===== CHAMADA: " + professor.getDisciplina() + " =====");
            System.out.println("1 - Abrir chamada");
            System.out.println("2 - Registrar presenças (modo aluno)");
            System.out.println("3 - Gerar novo QR Code");
            System.out.println("4 - Encerrar chamada e gerar relatório");
            System.out.println("0 - Sair");

            opcao = ler("Opção: ");

            switch (opcao) {
                case "1" -> abrirChamada();
                case "2" -> modoRegistroManual();
                case "3" -> renovarQrCode();
                case "4" -> encerrarChamada();
                case "0" -> sair();
                default -> System.out.println("Opção inválida.");
            }
        } while (!opcao.equals("0"));
    }

    private void abrirChamada() {
        if (chamada != null) {
            System.out.println("Já existe uma chamada aberta.");
            return;
        }

        chamada = professor.abrirChamada(turma);
        buffer = new BufferPresenca(CAPACIDADE_BUFFER);
        consumidor = new ConsumidorPresenca(
                buffer,
                chamada,
                TEMPO_PROCESSAMENTO_MS);

        consumidor.start();

        System.out.println("\nChamada aberta.");
        System.out.println("Buffer criado com capacidade para "
                + CAPACIDADE_BUFFER + " solicitações.");
        System.out.println("Consumidor iniciado em paralelo.");
        System.out.println("Tempo de processamento por solicitação: "
                + (TEMPO_PROCESSAMENTO_MS / 1000) + " segundos.\n");

        System.out.println("QR Code da aula:");
        chamada.getQrCode().exibir();
    }

    /**
     * Não existe opção de simulação automática.
     * Cada matrícula digitada manualmente cria uma thread produtora real.
     * O método NÃO espera o consumidor terminar, permitindo que várias
     * solicitações se acumulem no buffer.
     */
    private void modoRegistroManual() {
        if (semChamadaAberta()) {
            return;
        }

        System.out.println("\n===== MODO ALUNO / PRODUTOR =====");
        System.out.println("Cada matrícula digitada cria uma nova thread produtora.");
        System.out.println("O QR Code atual é considerado como escaneado pelo aluno.");
        System.out.println("Comandos disponíveis durante o registro:");
        System.out.println("  voltar   (ou 9) - retornar ao menu do professor");
        System.out.println("  encerrar (ou 4) - encerrar a chamada e gerar o relatório\n");

        exibirTurma();

        while (true) {
            removerProdutoresFinalizados();

            // Se o QR já expirou antes de uma nova entrada, não faz sentido
            // continuar aceitando matrículas. Voltamos ao menu do professor,
            // onde ele pode renovar o QR ou encerrar a chamada.
            if (chamada.getQrCode().expirou()) {
                System.out.println("\n[QR CODE] O QR Code expirou.");
                System.out.println("Novas presenças não serão aceitas com este código.");
                System.out.println("Retornando ao menu do professor...");
                break;
            }

            String entrada = ler("\nMatrícula (voltar/9 | encerrar/4): ");

            if (entrada.equalsIgnoreCase("voltar") || entrada.equals("9")) {
                break;
            }

            if (entrada.equalsIgnoreCase("encerrar") || entrada.equals("4")) {
                encerrarChamada();
                return;
            }

            if (entrada.isBlank()) {
                System.out.println("Digite uma matrícula.");
                continue;
            }

            // O QR pode expirar enquanto o programa aguardava o usuário digitar.
            // Por isso verificamos novamente depois da leitura.
            if (chamada.getQrCode().expirou()) {
                System.out.println("\n[QR CODE] O QR Code expirou antes do envio da solicitação.");
                System.out.println("A matrícula " + entrada + " não foi enviada.");
                System.out.println("Retornando ao menu do professor...");
                break;
            }

            // Equivale ao aluno ter escaneado o QR Code que está na tela.
            String codigoLido = chamada.getQrCode().getCodigo();

            SolicitacaoPresenca solicitacao =
                    new SolicitacaoPresenca(entrada, codigoLido);

            ProdutorAluno produtor = new ProdutorAluno(buffer, solicitacao);
            produtoresCriados.add(produtor);

            // start(), e não run(): cria execução concorrente.
            produtor.start();

            // Importante: NÃO há join aqui.
            // O usuário já pode digitar a próxima matrícula enquanto
            // produtor e consumidor trabalham em paralelo.
        }
    }

    private void renovarQrCode() {
        if (semChamadaAberta()) {
            return;
        }

        removerProdutoresFinalizados();

        if (existemProdutoresAtivos() || !buffer.estaVazio() || consumidor.isProcessando()) {
            System.out.println("Ainda existem solicitações sendo produzidas/processadas.");
            System.out.println("Aguarde o buffer ficar vazio antes de renovar o QR Code.");
            return;
        }

        chamada.renovarQrCode();

        System.out.println("\nNovo QR Code da aula:");
        chamada.getQrCode().exibir();
    }

    private void encerrarChamada() {
        if (semChamadaAberta()) {
            return;
        }

        System.out.println("\nAguardando todas as solicitações pendentes...");
        aguardarFimDoProcessamento();

        consumidor.encerrar();

        try {
            consumidor.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Chamada encerrada.");
        chamada.gerarRelatorio();

        chamada = null;
        buffer = null;
        consumidor = null;
        produtoresCriados.clear();
    }

    private void aguardarFimDoProcessamento() {
        try {
            // Um produtor pode estar bloqueado em vagas.acquire().
            // O consumidor continuará liberando vagas até todos conseguirem produzir.
            for (ProdutorAluno produtor : produtoresCriados) {
                produtor.join();
            }

            // Depois esperamos a fila esvaziar e o último item terminar de ser processado.
            while (!buffer.estaVazio() || consumidor.isProcessando()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void removerProdutoresFinalizados() {
        produtoresCriados.removeIf(produtor -> !produtor.isAlive());
    }

    private boolean existemProdutoresAtivos() {
        return produtoresCriados.stream().anyMatch(Thread::isAlive);
    }

    private void exibirTurma() {
        System.out.println("Matrículas cadastradas para a demonstração:");
        for (Aluno aluno : turma) {
            System.out.println("  " + aluno.getMatricula() + " - " + aluno.getNome());
        }

        System.out.println("\nPara observar o buffer cheio, envie várias matrículas rapidamente.");
        System.out.println("Capacidade do buffer: " + buffer.getCapacidade());
        System.out.println("O consumidor leva " + (TEMPO_PROCESSAMENTO_MS / 1000)
                + " segundos por solicitação.");
    }

    private boolean semChamadaAberta() {
        if (chamada == null) {
            System.out.println("Nenhuma chamada está aberta.");
            return true;
        }

        return false;
    }

    private void sair() {
        if (chamada != null) {
            encerrarChamada();
        }

        System.out.println("Até logo!");
    }

    private String ler(String rotulo) {
        System.out.print(rotulo);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
    }
}
