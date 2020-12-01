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
import net.minecraft.nbt.ByteNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Size2i;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	// Find all the original BlueprintCopyRecipe recipes.
	public static Stream<BlueprintCopyRecipe> getOriginals()
	{
		RecipeManager world = Minecraft.getInstance().world.getRecipeManager();
		return world.getRecipes(IRecipeType.CRAFTING).values().stream()
				.filter(BlueprintCopyRecipe.class::isInstance).map(BlueprintCopyRecipe.class::cast);
	}

	// Convert each of the originals into one recipe for each category.
	public static List<BlueprintCopyRecipe> getSubRecipes()
	{
		// Get all the kinds of blueprints.
		NonNullList<ItemStack> blueprints = NonNullList.create();
		Misc.blueprint.fillItemGroup(ItemGroup.SEARCH, blueprints);

		return getOriginals()
				.flatMap(recipe -> blueprints.stream()
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
				.map(item -> item.test(print) ? Collections.singletonList(recipe.getRecipeOutput()): Arrays.asList(item.getMatchingStacks()))
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
