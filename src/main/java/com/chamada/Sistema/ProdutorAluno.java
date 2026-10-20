package com.chamada.Sistema;

/**
 * Thread produtora. Cada envio de presença cria uma thread desta classe.
 */
public class ProdutorAluno extends Thread {
    private final BufferPresenca buffer;
    private final SolicitacaoPresenca solicitacao;

    public ProdutorAluno(BufferPresenca buffer, SolicitacaoPresenca solicitacao) {
        this.buffer = buffer;
        this.solicitacao = solicitacao;
        setName("Produtor-" + solicitacao.getMatricula());
    }

    @Override
    public void run() {
        try {
            System.out.println("[PRODUTOR] Matrícula " + solicitacao.getMatricula()
                    + " produziu uma solicitação de presença.");

            buffer.produzir(solicitacao);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[PRODUTOR] Envio interrompido para a matrícula "
                    + solicitacao.getMatricula() + ".");
        }
    }
}
