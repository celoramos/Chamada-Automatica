# Chamada Automática — Produtor/Consumidor

Projeto didático de **Sistemas Operacionais** em Java 21 para demonstrar o problema clássico **Produtor/Consumidor** usando `Thread`, `Semaphore` e uma fila compartilhada limitada.

## Fluxo

`Aluno → Produtor → Buffer → Consumidor → Validação → Registro`

- **Aluno / Produtor:** cada matrícula enviada cria uma thread `ProdutorAluno`.
- **Buffer:** `BufferPresenca` guarda solicitações em uma fila de capacidade 3.
- **Consumidor:** uma thread `ConsumidorPresenca` retira as solicitações da fila.
- **Validação:** verifica matrícula, QR Code, validade e presença duplicada.
- **Registro:** presença é guardada em memória e aparece no relatório final.

## Semáforos

O buffer usa três semáforos:

- `vagas`: começa em 3 e representa as posições livres.
- `itens`: começa em 0 e representa quantas solicitações podem ser consumidas.
- `mutex`: começa em 1 e garante exclusão mútua na fila.

Quando o buffer está cheio, um produtor bloqueia em `vagas.acquire()`. Quando o buffer está vazio, o consumidor bloqueia em `itens.acquire()`.

## Demonstração sem simulação automática

Não existe opção de "simular vários alunos". A concorrência é criada pelos próprios registros manuais.

1. Abra a chamada.
2. Entre em **Registrar presenças (modo aluno)**.
3. Digite várias matrículas rapidamente.
4. Cada matrícula cria uma thread produtora e o programa imediatamente aceita a próxima.
5. O consumidor leva 4 segundos para processar cada solicitação.
6. Com várias entradas, o buffer chega a 3/3 e o próximo produtor fica bloqueado aguardando `vagas`.
7. Quando o consumidor retira um item, uma vaga é liberada e o produtor bloqueado continua.

Matrículas já cadastradas:

- `20001234` — Marcelo Ramos
- `20005678` — Joao Danilo
- `20009012` — Caio Faria
- `20003456` — Maia
- `20007890` — Ana Souza
- `20001111` — Lucas Lima
- `20002222` — Pedro Santos
- `20003333` — Maria Oliveira

Durante o modo aluno, você pode usar:

- `voltar` ou `9` — voltar ao menu do professor.
- `encerrar` ou `4` — encerrar a chamada e gerar o relatório imediatamente após processar as solicitações pendentes.

Quando o QR Code expira, o sistema deixa de aceitar novas matrículas e retorna ao menu do professor. Solicitações enviadas antes do vencimento continuam válidas mesmo que sejam processadas depois.

## Estrutura

```text
src/main/java/com/chamada/
├── Aluno/
│   └── Aluno.java
├── Professor/
│   └── Professor.java
├── Sistema/
│   ├── BufferPresenca.java
│   ├── Chamada.java
│   ├── ConsumidorPresenca.java
│   ├── ProdutorAluno.java
│   ├── QRCode.java
│   └── SolicitacaoPresenca.java
└── principal/
    └── Principal.java
```

## Dependências

O projeto foi simplificado: **Spring Boot e Gson foram removidos**. Maven é usado apenas para Java 21 e ZXing, biblioteca responsável pela geração do QR Code.

## Execução

Abra o projeto em uma IDE com Java 21 e carregue o projeto Maven. Execute:

`com.chamada.principal.Principal`

O QR Code também é salvo como `qrcode.png` na pasta em que o programa estiver sendo executado.
