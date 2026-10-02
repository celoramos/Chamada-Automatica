package com.chamada.Sistema;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QRCode {
    private static final int VALIDADE_SEGUNDOS = 10;
    private static final int TAMANHO_IMAGEM = 300;
    private static final Path ARQUIVO_IMAGEM = Path.of("qrcode.png");
    private final String codigo;
    private final LocalDateTime expiraEm;

    public QRCode() {
        this.codigo = "AULA-" + (1000 + new Random().nextInt(9000));
        this.expiraEm = LocalDateTime.now().plusSeconds(VALIDADE_SEGUNDOS);
    }

    public boolean confere(String codigoLido) {
        return codigo.equalsIgnoreCase(codigoLido.trim());
    }
    public boolean expirou() {
        return LocalDateTime.now().isAfter(expiraEm);
    }

    // Texto que o celular lê ao escanear o QR Code
    private String conteudo() {
        return codigo;
    }

    // Tamanho 0 pede ao ZXing a menor matriz possível: 1 célula por módulo
    private BitMatrix gerarMatriz(int tamanho, int margem) throws WriterException {
        return new QRCodeWriter().encode(conteudo(), BarcodeFormat.QR_CODE, tamanho, tamanho,
                Map.of(EncodeHintType.MARGIN, margem));
    }

    private void salvarImagem() throws WriterException, IOException {
        MatrixToImageWriter.writeToPath(gerarMatriz(TAMANHO_IMAGEM, 4), "PNG", ARQUIVO_IMAGEM);
        System.out.println("Imagem salva em " + ARQUIVO_IMAGEM.toAbsolutePath());
    }

    private void desenharNoTerminal() throws WriterException {
        BitMatrix matriz = gerarMatriz(0, 2);
        StringBuilder desenho = new StringBuilder();
        for (int y = 0; y < matriz.getHeight(); y++) {
            for (int x = 0; x < matriz.getWidth(); x++) {
                desenho.append(matriz.get(x, y) ? "██" : "  ");
            }
            desenho.append('\n');
        }
        System.out.print(desenho);
    }

    public void exibir() {
        try {
            desenharNoTerminal();
            salvarImagem();
        } catch (WriterException | IOException e) {
            System.out.println("Não foi possível gerar a imagem do QR Code: " + e.getMessage());
        }
        System.out.println(codigo);
        System.out.println("Válido até " + expiraEm.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
