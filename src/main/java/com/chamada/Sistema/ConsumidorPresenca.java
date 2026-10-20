package com.chamada.Sistema;

/**
 * Única thread consumidora do sistema.
 * Retira solicitações do buffer, simula o tempo de processamento,
 * valida os dados e registra a presença.
 */
public class ConsumidorPresenca extends Thread {
    private final BufferPresenca buffer;
    private final Chamada chamada;
    private final long tempoProcessamentoMs;

    private volatile boolean ativo = true;
    private volatile boolean processando = false;

    public ConsumidorPresenca(BufferPresenca buffer, Chamada chamada, long tempoProcessamentoMs) {
        this.buffer = buffer;
        this.chamada = chamada;
        this.tempoProcessamentoMs = tempoProcessamentoMs;
        setName("Consumidor-Presenca");
    }

    @Override
    public void run() {
        System.out.println("[CONSUMIDOR] Thread consumidora iniciada.");

        while (ativo) {
            try {
                SolicitacaoPresenca solicitacao = buffer.consumir();
                processando = true;

                System.out.println("[CONSUMIDOR] Processando matrícula "
                        + solicitacao.getMatricula() + "...");

                // Representa o tempo que um sistema real gastaria para validar/processar.
                Thread.sleep(tempoProcessamentoMs);

                String erro = chamada.validar(solicitacao);

                if (erro != null) {
                    System.out.println("[VALIDAÇÃO] Solicitação rejeitada: " + erro);
                } else {
                    System.out.println("[VALIDAÇÃO] Solicitação válida para "
                            + solicitacao.getMatricula() + ".");
                    chamada.registrarPresenca(solicitacao);
                }

                processando = false;
            } catch (InterruptedException e) {
                processando = false;

                if (!ativo) {
                    break;
                }
            }
        }

        System.out.println("[CONSUMIDOR] Thread consumidora encerrada.");
    }

    public void encerrar() {
        ativo = false;
        interrupt();
    }

    public boolean isProcessando() {
        return processando;
    }
}
