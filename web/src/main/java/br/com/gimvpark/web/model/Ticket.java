package br.com.gimvpark.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets", indexes = {@Index(columnList = "status"), @Index(columnList = "placa")})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Integer numero;

    @Column(unique = true, nullable = false)
    public String token;

    @Column(nullable = false)
    public String placa;

    public String motorista;

    @Column(nullable = false)
    public LocalDateTime entradaEm;

    public LocalDateTime saidaEm;

    public String tabela;

    @Column(precision = 10, scale = 2)
    public BigDecimal valor;

    public String formaPagamento;

    @Column(nullable = false)
    public String status = "ABERTO";   // ABERTO | PAGO

    public String foto;

    public long atualizadoEm;
}
