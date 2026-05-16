package pl.kamjer.ShoppingListRecipeService.exceptions;

public class WrongRecipeElementException extends RuntimeException {
    public WrongRecipeElementException(String message) {
        super(message);
    }
}
