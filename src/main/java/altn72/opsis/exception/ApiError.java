package altn72.opsis.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        int status,
        String message,
        Map<String, String> erreurs,
        LocalDateTime date
) {}