package br.com.gimvpark.web.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gimvpark.web.config.GimvProps;
import br.com.gimvpark.web.model.Ticket;

@Service
public class TicketService {

    private static final Set<String> FORMAS = Set.of("DINHEIRO", "CREDITO", "DEBITO", "PIX");
    private static final SecureRandom RND = new SecureRandom();

    private final TicketRepository repo;
    private final GimvProps props;

    public TicketService(TicketRepository repo, GimvProps props) {
        this.repo = repo;
        this.props = props;
    }

    public static String limparPlaca(String p) {
        return p == null ? "" : p.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    @Transactional
    public synchronized Ticket criar(String placaBruta, String motorista, String fotoBase64) throws Exception {
        String placa = limparPlaca(placaBruta);
        if (placa.length() < 7) throw new IllegalArgumentException("Placa inválida");
        if (repo.existsByPlacaAndStatus(placa, "ABERTO")) throw new IllegalArgumentException("Veículo já está no pátio");

        byte[] r = new byte[12];
        RND.nextBytes(r);
        String token = HexFormat.of().formatHex(r);

        Ticket t = new Ticket();
        t.token = token;
        t.placa = placa;
        t.motorista = motorista == null ? "" : motorista.trim();
        t.entradaEm = LocalDateTime.now().withNano(0);
        t.tabela = props.tabela();
        t.valor = props.valorPadrao();
        t.foto = salvarFoto(token, fotoBase64);
        t.atualizadoEm = System.currentTimeMillis() / 1000;
        t = repo.save(t);
        t.numero = 1000 + t.id.intValue();
        return repo.save(t);
    }

    private String salvarFoto(String token, String b64) throws Exception {
        if (b64 == null || b64.isBlank()) return null;
        byte[] bin;
        try {
            bin = Base64.getMimeDecoder().decode(b64.replaceFirst("^data:image/\\w+;base64,", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bin));
        if (img == null) return null;
        Path dir = Path.of(props.uploadDir());
        Files.createDirectories(dir);
        String nome = token + ".jpg";
        Files.write(dir.resolve(nome), bin);
        return nome;
    }

    @Transactional
    public Ticket saida(Long id, String forma, BigDecimal valor) {
        Ticket t = repo.findById(id).orElseThrow(() -> new NaoEncontrado("Ticket não encontrado"));
        if ("PAGO".equals(t.status)) throw new IllegalArgumentException("Ticket já finalizado");
        String f = forma == null ? "" : forma.toUpperCase();
        if (!FORMAS.contains(f)) throw new IllegalArgumentException("Forma de pagamento inválida");
        t.saidaEm = LocalDateTime.now().withNano(0);
        t.formaPagamento = f;
        if (valor != null) t.valor = valor;
        t.status = "PAGO";
        t.atualizadoEm = System.currentTimeMillis() / 1000;
        return repo.save(t);
    }

    public Ticket buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> new NaoEncontrado("Não encontrado"));
    }

    public Ticket buscarPorToken(String token) {
        return repo.findByToken(token).orElseThrow(() -> new NaoEncontrado("Não encontrado"));
    }

    public List<Ticket> listar(boolean soPatio) {
        return soPatio ? repo.findTop500ByStatusOrderByIdDesc("ABERTO") : repo.findTop500ByOrderByIdDesc();
    }

    public Map<String, Object> resumo() {
        LocalDateTime ini = LocalDate.now().atStartOfDay(), fim = ini.plusDays(1);
        return Map.of(
                "no_patio", repo.countByStatus("ABERTO"),
                "entradas_hoje", repo.countByEntradaEmGreaterThanEqualAndEntradaEmLessThan(ini, fim),
                "saidas_hoje", repo.countBySaidaEmGreaterThanEqualAndSaidaEmLessThan(ini, fim),
                "faturamento_hoje", repo.faturamento(ini, fim).doubleValue());
    }

    public static class NaoEncontrado extends RuntimeException {
        public NaoEncontrado(String m) { super(m); }
    }
}
