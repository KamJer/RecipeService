echo "fetching recipeService"

git fetch origin main

if ! git diff --quiet HEAD origin/main; then
        git checkout main
        git pull origin main
        chmod +x mvnw
        ./mvnw clean package && cp target/*.jar ~/services_shopping_list/
        sudo systemctl daemon-reload
        sudo systemctl restart shopping-list-recipe.service
fi
