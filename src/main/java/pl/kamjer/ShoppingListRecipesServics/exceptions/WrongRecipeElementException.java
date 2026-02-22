package pl.kamjer.ShoppingListRecipesServics.exceptions;

public class WrongRecipeElementException extends RuntimeException {
    public WrongRecipeElementException(String message) {
        super(message);
    }
}
