package br.com.fiap.rei_dos_piratas.interfaces.exception;

import br.com.fiap.rei_dos_piratas.domain.exceptions.*;
import jakarta.validation.ConstraintDefinitionException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleResourceException(Exception e) {
        e.printStackTrace();
        final ErrorResponse errorResponse = new ErrorResponse("Ops.. ocorreu um error inesperado!");
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    @Getter
    public static class ErrorResponse {

        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }


}
