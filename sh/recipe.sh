cd ./RecipeService

echo "fetching recipeService"

git fetch origin main

if ! git diff --quiet HEAD origin/main; then
        git checkout main
        git pull origin main
        ./mvnw clean package
        cp target/*.jar ~/services_shopping_list
        systemctl daemon-reload
        systemctl restart shopping-list-recipe.service
fi
