package com.eduardo.maintenancerequests.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler{

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handlerValidException(MethodArgumentNotValidException exception, 
			HttpServletRequest request){
		
		Map<String, String> errorFields = new HashMap<String, String>();
		
		exception.getFieldErrors().forEach(
				(field) -> errorFields.put(field.getField(), field.getDefaultMessage())
				);

		ApiError error = new ApiError(
				LocalDateTime.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				exception.getMessage(),
				request.getRequestURI(),
				errorFields
				);
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
	
	@ExceptionHandler(MaintenanceRequestNotFoundException.class)
	public ResponseEntity<ApiError> handlerMaintenanceRequestException(MaintenanceRequestNotFoundException exception,
			HttpServletRequest request){
		
		ApiError error = new ApiError(
				LocalDateTime.now(),
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				exception.getMessage(),
				request.getRequestURI(),
				Map.of()
				);
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "El cuerpo JSON no tiene un formato válido o contiene un valor no permitido",
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.badRequest().body(error);
    }
	/**
	 * Control del error, para el apartado opcional de filtrar por categoría. Capta el error de una categoria
	 * que no existe.
	 * @param exception
	 * @param request
	 * @return
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handlerMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request){
		
		ApiError error = new ApiError(
				LocalDateTime.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"La valor especificada no es correcta",
				request.getRequestURI(),
				Map.of());
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
}
