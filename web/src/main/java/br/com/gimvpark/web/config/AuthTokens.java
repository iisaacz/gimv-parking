package br.com.gimvpark.web.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

/**
 * Login sem sessao no servidor (stateless): o cookie carrega usuario + validade assinados com HMAC.
 * Funciona em qualquer instancia (necessario em hospedagem serverless como a Vercel).
 */
@Component
public class AuthTokens {

    public static final String COOKIE = "gimv_auth";
    public static final int HORAS = 12;

    private final byte[] chave;

    public AuthTokens(GimvProps p) {
        this.chave = (p.apiKey() + "|" + p.adminUser() + "|" + p.adminPass()).getBytes(StandardCharsets.UTF_8);
    }

    public String emitir(String usuario) {
        long exp = System.currentTimeMillis() / 1000 + HORAS * 3600L;
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((usuario + "|" + exp).getBytes(StandardCharsets.UTF_8));
        return payload + "." + assinar(payload);
    }

    public boolean valido(String token) {
        try {
            if (token == null) return false;
            int i = token.indexOf('.');
            if (i < 1) return false;
            String payload = token.substring(0, i);
            if (!MessageDigest.isEqual(assinar(payload).getBytes(StandardCharsets.UTF_8),
                    token.substring(i + 1).getBytes(StandardCharsets.UTF_8))) return false;
            String[] d = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8).split("\\|");
            return Long.parseLong(d[1]) > System.currentTimeMillis() / 1000;
        } catch (Exception e) {
            return false;
        }
    }

    private String assinar(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(chave, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
