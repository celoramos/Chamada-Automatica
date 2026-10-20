package com.chamada.Sistema;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * Buffer limitado do problema Produtor/Consumidor.
 *
 * vagas -> quantidade de posições livres no buffer.
 * itens  -> quantidade de solicitações disponíveis para consumo.
 * mutex  -> exclusão mútua para que apenas uma thread altere a fila por vez.
 */
public class BufferPresenca {
    private final Queue<SolicitacaoPresenca> fila = new ArrayDeque<>();
    private final int capacidade;

    private final Semaphore vagas;
    private final Semaphore itens;
    private final Semaphore mutex;

    public BufferPresenca(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("A capacidade do buffer deve ser maior que zero.");
        }

        this.capacidade = capacidade;
        this.vagas = new Semaphore(capacidade, true);
        this.itens = new Semaphore(0, true);
        this.mutex = new Semaphore(1, true);
    }

    /**
     * Chamado pelas threads produtoras.
     */
    public void produzir(SolicitacaoPresenca solicitacao) throws InterruptedException {
        if (vagas.availablePermits() == 0) {
            System.out.println("[VAGAS] Buffer cheio. Matrícula "
                    + solicitacao.getMatricula() + " aguardando uma vaga...");
        }

        // Se não houver vaga, o produtor fica bloqueado aqui.
        vagas.acquire();

        boolean mutexAdquirido = false;
        try {
            // Região crítica: somente uma thread pode alterar a fila por vez.
            mutex.acquire();
            mutexAdquirido = true;

            fila.add(solicitacao);
            System.out.println("[PRODUTOR] " + solicitacao.getMatricula()
                    + " adicionou uma solicitação ao buffer. "
                    + "Fila: " + fila.size() + "/" + capacidade);
        } catch (InterruptedException e) {
            // A vaga já havia sido reservada; devolvemos se não houve inserção.
            vagas.release();
            throw e;
        } finally {
            if (mutexAdquirido) {
                mutex.release();
            }
        }

        // Agora existe um novo item disponível para o consumidor.
        itens.release();
    }

    /**
     * Chamado pela thread consumidora.
     */
    public SolicitacaoPresenca consumir() throws InterruptedException {
        // Se não houver itens, o consumidor fica bloqueado aqui.
        itens.acquire();

        boolean mutexAdquirido = false;
        try {
            // Região crítica da fila.
            mutex.acquire();
            mutexAdquirido = true;

            SolicitacaoPresenca solicitacao = fila.remove();
            System.out.println("[CONSUMIDOR] Retirou " + solicitacao.getMatricula()
                    + " do buffer. Fila: " + fila.size() + "/" + capacidade);

            // Ao retirar um item, uma posição livre volta a existir.
            vagas.release();
            return solicitacao;
        } catch (InterruptedException e) {
            // O item já havia sido reservado por itens.acquire().
            itens.release();
            throw e;
        } finally {
            if (mutexAdquirido) {
                mutex.release();
            }
        }
    }

    public boolean estaVazio() {
        mutex.acquireUninterruptibly();
        try {
            return fila.isEmpty();
        } finally {
            mutex.release();
        }
    }

    public int tamanhoAtual() {
        mutex.acquireUninterruptibly();
        try {
            return fila.size();
        } finally {
            mutex.release();
        }
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getVagasDisponiveis() {
        return vagas.availablePermits();
    }

    public int getItensDisponiveis() {
        return itens.availablePermits();
    }
}
