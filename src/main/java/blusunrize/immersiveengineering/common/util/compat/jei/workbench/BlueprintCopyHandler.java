package blusunrize.immersiveengineering.common.util.compat.jei.workbench;

import blusunrize.immersiveengineering.common.crafting.BlueprintCopyRecipe;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Size2i;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BlueprintCopyHandler implements ICraftingCategoryExtension
{
	private final BlueprintCopyRecipe recipe;
	private final ResourceLocation id;

	public BlueprintCopyHandler(BlueprintCopyRecipe recipe)
	{
		this.recipe = recipe;
		String category = EngineersBlueprintItem.getCategory(recipe.getRecipeOutput()).toLowerCase(Locale.ROOT);
		ResourceLocation id = recipe.getId();
		this.id = new ResourceLocation(id.getNamespace(), id.getPath() + "/" + category);
	}

	// Convert each of the originals into one recipe for each category.
	public static List<BlueprintCopyRecipe> getSubRecipes()
	{
		// Get all the blueprint subitems.
		NonNullList<ItemStack> blueprints = NonNullList.create();
		Misc.blueprint.fillItemGroup(ItemGroup.SEARCH, blueprints);

		RecipeManager world = Minecraft.getInstance().world.getRecipeManager();

		// Find each original recipe (one, but users could add others),
		// and expand that into a recipe for each specific blueprint.
		return world.getRecipes(IRecipeType.CRAFTING).values().stream()
				.filter(BlueprintCopyRecipe.class::isInstance).map(BlueprintCopyRecipe.class::cast)
				.flatMap(recipe -> blueprints.stream()
						.filter(print -> recipe.getIngredients().stream().anyMatch(ing -> ing.test(print)))
						.map(print -> new BlueprintCopyRecipe(recipe, print)))
				.collect(Collectors.toList());
	}

	public void setIngredients(IIngredients ingredients)
	{
		ItemStack print = new ItemStack(Misc.blueprint);
		// The output is the specific blueprint to show.
		String category = EngineersBlueprintItem.getCategory(recipe.getRecipeOutput());
		if (category.isEmpty()) return;

		ingredients.setInputLists(VanillaTypes.ITEM, recipe.getIngredients().stream()
				.map(item -> {
					// If it's the blueprint ingredient, show only this specific input.
					if(Arrays.stream(item.getMatchingStacks()).anyMatch(stack -> stack.getItem()==Misc.blueprint))
						return Collections.singletonList(recipe.getRecipeOutput());
					else // Otherwise, set to the normal stacks it matches.
						return Arrays.asList(item.getMatchingStacks());
				})
				.collect(Collectors.toList())
		);
		// Use two blueprints in the output, to more clearly show it copies the blueprint.
		ItemStack output = recipe.getRecipeOutput().copy();
		output.setCount(2);
		ingredients.setOutput(VanillaTypes.ITEM, output);
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return id;
	}

	@Override
	public Size2i getSize()
	{
		return new Size2i(recipe.getWidth(), recipe.getHeight());
	}
}
