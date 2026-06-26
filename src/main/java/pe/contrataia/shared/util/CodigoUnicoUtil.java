package pe.contrataia.shared.util;

import java.security.SecureRandom;

public final class CodigoUnicoUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private CodigoUnicoUtil() {}

    public static String generar() {
        // Formato: OBR-XXXX-XXXX  (OBR = Obra)
        StringBuilder sb = new StringBuilder("OBR-");
        for (int i = 0; i < 4; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        sb.append("-");
        for (int i = 0; i < 4; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        return sb.toString();
    }
}
