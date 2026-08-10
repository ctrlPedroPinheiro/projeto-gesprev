package iff.edu.br.gesprev.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.openai.errors.RateLimitException;

import iff.edu.br.gesprev.dto.ApiErrorDTO;
import iff.edu.br.gesprev.exception.ChatIndisponivelException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiErrorDTO> handleAiRateLimit(RateLimitException ex, HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS,
                "Cota do provedor de IA excedida; verifique creditos, faturamento e limites do projeto",
                request);
    }

    @ExceptionHandler(ChatIndisponivelException.class)
    public ResponseEntity<ApiErrorDTO> handleChatIndisponivel(ChatIndisponivelException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorDTO> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = statusFromMessage(ex.getMessage());
        return build(status, ex.getMessage(), request);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorDTO> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, mensagem(ex), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .distinct()
                .reduce((a, b) -> a + "; " + b)
                .orElse("Dados invalidos");

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleConflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Registro viola uma regra de integridade do banco de dados", request);
    }

    @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ApiErrorDTO> handleMultipart(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Erro ao receber arquivo: " + mensagem(ex), request);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorDTO> handleIo(IOException ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar arquivo: " + mensagem(ex), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorDTO> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Autenticacao necessaria ou invalida", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorDTO> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP nao permitido para este endpoint", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Endpoint nao encontrado", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request);
    }

    private ResponseEntity<ApiErrorDTO> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorDTO body = new ApiErrorDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message == null || message.isBlank() ? "Erro nao informado" : message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus statusFromMessage(String message) {
        String texto = message == null ? "" : message.toLowerCase();

        if (texto.contains("nao encontrado") || texto.contains("não encontrado")
                || texto.contains("nao encontrada") || texto.contains("não encontrada")) {
            return HttpStatus.NOT_FOUND;
        }

        if (texto.contains("ja cadastrado") || texto.contains("já cadastrado")
                || texto.contains("ja existe") || texto.contains("já existe")) {
            return HttpStatus.CONFLICT;
        }

        if (texto.contains("precisa") || texto.contains("deve")
                || texto.contains("invalido") || texto.contains("inválido")
                || texto.contains("nao possui") || texto.contains("não possui")) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.BAD_REQUEST;
    }

    private String mensagem(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? ex.getClass().getSimpleName()
                : ex.getMessage();
    }

    private String formatFieldError(FieldError error) {
        String defaultMessage = error.getDefaultMessage() == null || error.getDefaultMessage().isBlank()
                ? "valor invalido"
                : error.getDefaultMessage();

        return error.getField() + ": " + defaultMessage;
    }
}
