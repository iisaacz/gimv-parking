package br.com.gimvpark.web.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Formato JSON consumido pelo app Android (nomes em snake_case). */
public record TicketDto(
        Long id,
        Integer numero,
        String token,
        String placa,
        @JsonProperty("placa_fmt") String placaFmt,
        String motorista,
        @JsonProperty("entrada_em") String entradaEm,
        @JsonProperty("saida_em") String saidaEm,
        String tabela,
        double valor,
        @JsonProperty("forma_pagamento") String formaPagamento,
        String status,
        @JsonProperty("foto_url") String fotoUrl,
        @JsonProperty("url_comprovante") String urlComprovante,
        @JsonProperty("atualizado_em") long atualizadoEm) {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static TicketDto de(Ticket t, String base) {
        return new TicketDto(t.id, t.numero, t.token, t.placa, formatarPlaca(t.placa), t.motorista,
                t.entradaEm.format(ISO), t.saidaEm == null ? null : t.saidaEm.format(ISO),
                t.tabela, t.valor == null ? 0 : t.valor.doubleValue(), t.formaPagamento, t.status,
                t.foto == null ? null : base + "/uploads/" + t.foto,
                base + "/ticket?t=" + t.token, t.atualizadoEm);
    }

    public static String formatarPlaca(String p) {
        return p != null && p.length() == 7 ? p.substring(0, 3) + "-" + p.substring(3) : p;
    }

    // ---- auxiliares para os templates (não vão para o JSON) ----
    public boolean pago() { return "PAGO".equals(status); }
    public String entradaBr() { return br(entradaEm); }
    public String saidaBr() { return br(saidaEm); }
    public String valorBr() { return String.format(new java.util.Locale("pt", "BR"), "%,.2f", valor); }

    private static String br(String iso) {
        return iso == null ? "—" : LocalDateTime.parse(iso, ISO).format(BR);
    }
}
