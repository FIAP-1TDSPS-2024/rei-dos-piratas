package br.com.fiap.rei_dos_piratas.interfaces.exception;

import br.com.fiap.rei_dos_piratas.domain.exceptions.*;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.fallback.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResourceFoundException(NoResourceFoundException e) {}

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<Map<String, String>> handleValidacaoException(ValidacaoException e) {
        return ResponseEntity.badRequest().body(e.getErros());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        final ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(UniqueKeyDuplicatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleUniqueKeyDuplicatedException(UniqueKeyDuplicatedException e) {
        final ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String,String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        final Map<String, String> errors = new HashMap<>();
        e.getBindingResult()
                .getFieldErrors()
                .stream().parallel()
                .forEach( error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleEstoqueInsuficienteException(EstoqueInsuficienteException e) {
        final ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErrorResponse> handleRegraDeNegocioException(RegraDeNegocioException e) {
        final ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(WrongStatusException.class)
    public ResponseEntity<ErrorResponse> handleWrongStatusException(WrongStatusException e) {
        final ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ApiExternaException.class)
    public ResponseEntity<ErrorResponse> handleApiExternaException(ApiExternaException e) {
        final ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Circuit Breaker OPEN — serviço externo bloqueado enquanto o auto-scaling sobe (~120s).
     * Retorna 503 Service Unavailable com header Retry-After de 130s.
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCallNotPermittedException(CallNotPermittedException e) {
        final ErrorResponse errorResponse = new ErrorResponse(
                "Serviço externo temporariamente indisponível. O sistema está se recuperando. Tente novamente em instantes.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "130")
                .body(errorResponse);
    }

    /**
     * Fallback de circuito aberto para operações que não podem ser degradadas.
     * Retorna 503 com Retry-After de 130s.
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException e) {
        final ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "130")
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleResourceException(Exception e) {
        e.printStackTrace();
        final ErrorResponse errorResponse = new ErrorResponse("Ops.. ocorreu um error inesperado!");
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String,String>> handleBadCredentialException(BadCredentialsException e) {
        Map<String, String> errorResult = Map.of("error", "Usuário e/ou senha inválidos");
        return ResponseEntity.badRequest().body(errorResult);
    }

    @Getter
    public static class ErrorResponse {

        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }


}
