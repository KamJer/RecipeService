package pl.kamjer.ShoppingListRecipesServics.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.kamjer.ShoppingListRecipesServics.exceptions.WrongRecipeElementException;

@RestControllerAdvice
public class Advice {

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<String> handleNoAuth(IllegalAccessException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(WrongRecipeElementException.class)
    public ResponseEntity<String> handleWrongTag(WrongRecipeElementException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
