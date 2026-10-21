package br.com.gimvpark.web.config;

import java.security.MessageDigest;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Dashboard: sessão (login). API: sessão OU header X-API-KEY (app Android). */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final GimvProps props;
    private final AuthTokens tokens;

    public AuthInterceptor(GimvProps props, AuthTokens tokens) {
        this.props = props;
        this.tokens = tokens;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        boolean logado = false;
        if (req.getCookies() != null) {
            for (jakarta.servlet.http.Cookie c : req.getCookies())
                if (AuthTokens.COOKIE.equals(c.getName()) && tokens.valido(c.getValue())) logado = true;
        }
        if (logado) return true;

        if (req.getRequestURI().startsWith("/api/")) {
            String k = req.getHeader("X-API-KEY");
            if (k != null && MessageDigest.isEqual(k.getBytes(), props.apiKey().getBytes())) return true;
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"erro\":\"Não autorizado\"}");
            return false;
        }
        res.sendRedirect("/login");
        return false;
    }
}
