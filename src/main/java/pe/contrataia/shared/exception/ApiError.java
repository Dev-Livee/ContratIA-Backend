package pe.contrataia.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> errors
) {
    public static ApiError of(int status, String message) {
        return new ApiError(status, message, LocalDateTime.now(), null);
    }

    public static ApiError of(int status, String message, Map<String, String> errors) {
        return new ApiError(status, message, LocalDateTime.now(), errors);
    }
}
