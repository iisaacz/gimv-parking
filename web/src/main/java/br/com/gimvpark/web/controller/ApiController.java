package br.com.gimvpark.web.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gimvpark.web.model.TicketDto;
import br.com.gimvpark.web.service.TicketService;

/** WEBSERVICES REST (JSON) usados pelo dashboard e pelo app Android. */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final TicketService service;

    public ApiController(TicketService service) {
        this.service = service;
    }

    private static String base() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }

    @GetMapping("/tickets")
    public Map<String, Object> listar(@RequestParam(defaultValue = "todos") String status) {
        String b = base();
        List<TicketDto> l = service.listar("patio".equals(status)).stream().map(t -> TicketDto.de(t, b)).toList();
        return Map.of("agora", System.currentTimeMillis() / 1000, "tickets", l);
    }

    @GetMapping("/tickets/{id}")
    public TicketDto buscar(@PathVariable Long id) {
        return TicketDto.de(service.buscar(id), base());
    }

    @GetMapping("/tickets/token/{token}")
    public TicketDto porToken(@PathVariable String token) {
        return TicketDto.de(service.buscarPorToken(token), base());
    }

    public record NovoTicket(String placa, String motorista, String foto) { }

    @PostMapping("/tickets")
    public ResponseEntity<TicketDto> criar(@RequestBody NovoTicket b) throws Exception {
        var t = service.criar(b.placa(), b.motorista(), b.foto());
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketDto.de(t, base()));
    }

    public record Saida(String forma_pagamento, BigDecimal valor) { }

    @PostMapping("/tickets/{id}/saida")
    public TicketDto saida(@PathVariable Long id, @RequestBody Saida b) {
        return TicketDto.de(service.saida(id, b.forma_pagamento(), b.valor()), base());
    }

    @GetMapping("/resumo")
    public Map<String, Object> resumo() {
        return service.resumo();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalido(IllegalArgumentException e) {
        return ResponseEntity.status(422).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(TicketService.NaoEncontrado.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(TicketService.NaoEncontrado e) {
        return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erro(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("erro", "Erro interno"));
    }
}
