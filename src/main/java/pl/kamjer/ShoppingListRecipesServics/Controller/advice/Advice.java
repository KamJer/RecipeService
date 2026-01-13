package pl.kamjer.ShoppingListRecipesServics.Controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Advice {

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<String> handleNoAuth(IllegalAccessException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }
}
