package ru.mephi.cloud.ml.deploying.filter;

import java.io.IOException;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.GenericFilterBean;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

public class JwtFilter extends GenericFilterBean {


    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ServletException("Missing or invalid Authorization header.");
        }

        final String token = authHeader.substring(7); // The part after "Bearer "
        try {
            final Claims claims = Jwts.parser().setSigningKey("TimoFeevKey1")
                    .parseClaimsJws(token).getBody();
            if (claims.getExpiration().before(new Date())) {
                throw new ServletException("TOKEN WAS EXPIRED");
            } else {
                if (claims.get("token_type").toString().equals("refresh token")) {
                    throw new ServletException("Authorization with REFRESH TOKEN IMPOSSIBLE");
                }

                request.setAttribute("claims", claims);
            }
        } catch (final SignatureException e) {
            throw new ServletException("Invalid token.");
        }

        chain.doFilter(req, res);
    }

}