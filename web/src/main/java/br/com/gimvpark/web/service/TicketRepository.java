package br.com.gimvpark.web.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gimvpark.web.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByToken(String token);

    boolean existsByPlacaAndStatus(String placa, String status);

    List<Ticket> findTop500ByOrderByIdDesc();

    List<Ticket> findTop500ByStatusOrderByIdDesc(String status);

    long countByStatus(String status);

    long countByEntradaEmGreaterThanEqualAndEntradaEmLessThan(LocalDateTime a, LocalDateTime b);

    long countBySaidaEmGreaterThanEqualAndSaidaEmLessThan(LocalDateTime a, LocalDateTime b);

    @Query("select coalesce(sum(t.valor), 0) from Ticket t where t.saidaEm >= :a and t.saidaEm < :b")
    BigDecimal faturamento(@Param("a") LocalDateTime a, @Param("b") LocalDateTime b);
}
