SERVICES=(
  "RecipeService|ShoppingListRecipeService-0.0.1-SNAPSHOT.jar|shopping-list-recipe.service"
  "shopping-list-service|ShoppingListService-3.0.jar|shopping-list.service"
  "Shopping-security-service|ShoppingSecService-0.0.1-SNAPSHOT.jar|shopping-list-user.service"
)

for SERVICE in "${SERVICES[@]}"; do
  REPO_DIR=$(echo "$SERVICE" | cut -d'|' -f1)
  JAR=$(echo "$SERVICE" | cut -d'|' -f2)
  SVC_NAME=$(echo "$SERVICE" | cut -d'|' -f3)

  cd "$REPO_DIR" || { echo "$REPO_DIR not found, skipping"; continue; }

  echo "fetching $REPO_DIR"
  git fetch origin main

  LATEST=$(git rev-parse origin/main)
  DEPLOYED=$(cat ~/.".last_deployed_${REPO_DIR}" 2>/dev/null || echo "")

  if [ "$DEPLOYED" != "$LATEST" ]; then
    echo "new commit detected for $REPO_DIR"
    git checkout main
    git pull origin main
    echo "building $REPO_DIR"
    chmod 777 ./mvnw
    ./mvnw clean package
    cp "target/$JAR" ~/services_shopping_list/
    echo "restarting $SVC_NAME"
    sudo systemctl daemon-reload
    sudo systemctl restart "$SVC_NAME"
    echo "$LATEST" > ~/.".last_deployed_${REPO_DIR}"
  else
    echo "$REPO_DIR is up to date"
  fi

  cd ..
done
