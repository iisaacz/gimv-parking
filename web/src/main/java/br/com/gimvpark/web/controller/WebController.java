package br.com.gimvpark.web.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import br.com.gimvpark.web.config.GimvProps;
import br.com.gimvpark.web.model.TicketDto;
import br.com.gimvpark.web.service.TicketService;
import br.com.gimvpark.web.config.AuthTokens;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class WebController {

    private final TicketService service;
    private final GimvProps props;
    private final AuthTokens tokens;

    public WebController(TicketService service, GimvProps props, AuthTokens tokens) {
        this.service = service;
        this.props = props;
        this.tokens = tokens;
    }

    private static void cookie(HttpServletRequest req, HttpServletResponse res, String valor, int maxAge) {
        boolean https = req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
        res.addHeader("Set-Cookie", AuthTokens.COOKIE + "=" + valor + "; Path=/; Max-Age=" + maxAge
                + "; HttpOnly; SameSite=Lax" + (https ? "; Secure" : ""));
    }

    @ModelAttribute("app")
    public GimvProps app() { return props; }

    private static String base() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }

    @GetMapping("/")
    public String dashboard(Model m) {
        m.addAttribute("app", props);
        return "dashboard";
    }

    @GetMapping("/login")
    public String login(Model m) {
        m.addAttribute("app", props);
        return "login";
    }

    @PostMapping("/login")
    public String entrar(@RequestParam String u, @RequestParam String s,
                         HttpServletRequest req, HttpServletResponse res, Model m) {
        boolean ok = MessageDigest.isEqual(u.getBytes(), props.adminUser().getBytes())
                && MessageDigest.isEqual(s.getBytes(), props.adminPass().getBytes());
        if (ok) {
            cookie(req, res, tokens.emitir(u), AuthTokens.HORAS * 3600);
            return "redirect:/";
        }
        m.addAttribute("app", props);
        m.addAttribute("erro", "Usuário ou senha inválidos");
        return "login";
    }

    @GetMapping("/logout")
    public String sair(HttpServletRequest req, HttpServletResponse res) {
        cookie(req, res, "", 0);
        return "redirect:/login";
    }

    /** Comprovante online do cliente (público, acessado pelo QR/token). */
    @GetMapping("/ticket")
    public String ticket(@RequestParam("t") String token, Model m) {
        m.addAttribute("app", props);
        m.addAttribute("t", TicketDto.de(service.buscarPorToken(token), base()));
        return "ticket";
    }

    /** Comprovante físico (80 mm) para impressão pelo navegador. */
    @GetMapping("/imprimir")
    public String imprimir(@RequestParam("t") String token, Model m) {
        m.addAttribute("app", props);
        m.addAttribute("t", TicketDto.de(service.buscarPorToken(token), base()));
        m.addAttribute("agora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return "imprimir";
    }

    /** QR Code (PNG) apontando para o comprovante online. */
    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] qr(@RequestParam("t") String token) throws Exception {
        BitMatrix mx = new QRCodeWriter().encode(base() + "/ticket?t=" + token, BarcodeFormat.QR_CODE, 300, 300);
        BufferedImage img = MatrixToImageWriter.toBufferedImage(mx);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bo);
        return bo.toByteArray();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(TicketService.NaoEncontrado.class)
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> naoEncontrado(TicketService.NaoEncontrado e) {
        return org.springframework.http.ResponseEntity.status(404).body("Ticket não encontrado");
    }
}
