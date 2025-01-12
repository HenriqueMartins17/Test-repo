package com.apitable.enterprise.ai.server;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * call AI server exception.
 *
 * @author Shawn Deng
 */
public class AiServerException extends ResponseStatusException {

    public AiServerException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public AiServerException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }

    public AiServerException(String reason) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, reason);
    }

    public AiServerException(String reason, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
    }
}
