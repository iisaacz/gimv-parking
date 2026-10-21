package br.com.gimvpark.web.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gimv")
public record GimvProps(
        String empresa, String unidade, String endereco, String telefone, String cnpj,
        String horario, String tabela, BigDecimal valorPadrao, String apiKey,
        String adminUser, String adminPass, String uploadDir, String aviso) {
}
