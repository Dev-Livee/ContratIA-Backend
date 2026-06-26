package pe.contrataia.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.contrataia.auth.entity.OtpToken;
import pe.contrataia.auth.repository.OtpTokenRepository;
import pe.contrataia.shared.enums.TipoOtp;
import pe.contrataia.shared.exception.BusinessException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${app.otp.expiration-minutes}")
    private int expirationMinutes;

    @Transactional
    public void generarYEnviar(String email, TipoOtp tipo) {
        otpTokenRepository.invalidarTodosPorEmailYTipo(email, tipo);

        String codigo = generarCodigo();
        OtpToken otp = new OtpToken();
        otp.setEmail(email);
        otp.setCodigo(codigo);
        otp.setTipo(tipo);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        otpTokenRepository.save(otp);

        enviarCorreo(email, codigo, tipo);
        log.info("OTP generado para {}: {}", email, codigo);
    }

    @Transactional
    public void verificar(String email, String codigo, TipoOtp tipo) {
        OtpToken otp = otpTokenRepository
                .findTopByEmailAndTipoAndUsadoFalseOrderByCreatedAtDesc(email, tipo)
                .orElseThrow(() -> new BusinessException("Código OTP no encontrado o ya utilizado"));

        if (otp.isExpired()) {
            throw new BusinessException("El código OTP ha expirado");
        }
        if (!otp.getCodigo().equals(codigo)) {
            throw new BusinessException("Código OTP incorrecto");
        }

        otp.setUsado(true);
        otpTokenRepository.save(otp);
    }

    @Async
    protected void enviarCorreo(String email, String codigo, TipoOtp tipo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("ContrataIA Perú - Código de verificación");
            message.setText("""
                    Tu código de verificación es: %s

                    Válido por %d minutos.

                    Si no solicitaste este código, ignora este mensaje.
                    """.formatted(codigo, expirationMinutes));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error enviando OTP a {}: {}", email, e.getMessage());
        }
    }

    private String generarCodigo() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900000) + 100000;
        return String.valueOf(num);
    }
}
