package com.hepdd.easytech.api.enums;

import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;

public final class ETHRecipeMaps {

    public static final RecipeMap<RecipeMapBackend> bigCokeOvenRecipe = RecipeMapBuilder.of("gt.recipe.bigcokeOven")
        .maxIO(1, 1, 0, 1)
        .minInputs(1, 0)
        .build();
}
