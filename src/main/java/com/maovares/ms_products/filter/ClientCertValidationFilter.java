package com.maovares.ms_products.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
public class ClientCertValidationFilter extends OncePerRequestFilter {

    @Value("${CLIENT_CERT_THUMBPRINT:}")
    private String expectedThumbprint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Read the header X-ARR-ClientCert
        String clientCertHeader = request.getHeader("X-ARR-ClientCert");

        // If the header is missing, or expectedThumbprint is not configured, we might want to reject it
        // Depending on requirements, we can reject or allow. Usually if validation is enabled, we require it.
        if (clientCertHeader == null || clientCertHeader.isEmpty()) {
            // For endpoints that don't need cert validation, this should be adjusted.
            // Assuming the whole app needs it based on the instructions.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing client certificate");
            return;
        }

        try {
            // 2. Transform the base64 value on a X509Certificate
            byte[] decodedCert = Base64.getDecoder().decode(clientCertHeader);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(decodedCert));

            // 3. Calculates the thumbprint (SHA-1) of the received certificate
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] der = certificate.getEncoded();
            md.update(der);
            byte[] digest = md.digest();
            String thumbprint = bytesToHex(digest);

            // 4. Compares with the expected thumbprint of your certificate
            if (expectedThumbprint == null || !expectedThumbprint.equalsIgnoreCase(thumbprint)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid client certificate thumbprint");
                return;
            }

        } catch (Exception e) {
            logger.error("Error validating client certificate", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Error validating client certificate");
            return;
        }

        // Proceed if validation succeeds
        filterChain.doFilter(request, response);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
