/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionRemoveMultipleOutputs;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

import java.util.List;

/**
 * Allows you to add or remove Crops from the Garden Cloche.
 * <p>
 * Cloche Recipes consist of a soil, an input item and output items.
 *
 * @docParam this <recipetype:immersiveengineering:cloche>
 */
@ZenRegister
@Document("mods/immersiveengineering/Cloche")
@ZenCodeType.Name("mods.immersiveengineering.Cloche")
public class ClocheRecipeManager implements IRecipeManager
{

	@Override
	public IRecipeType<ClocheRecipe> getRecipeType()
	{
		return ClocheRecipe.TYPE;
	}

	@Override
	public void removeRecipe(IItemStack output)
	{
		removeRecipe((IIngredient)output);
	}

	/**
	 * Removes the recipe based on its outputs.
	 * Removes the recipe as soon as one of its outputs matches the ingredient given.
	 *
	 * @param output The output to match for
	 * @docParam output <item:minecraft:melon>
	 */
	@ZenCodeType.Method
	public void removeRecipe(IIngredient output)
	{
		CraftTweakerAPI.apply(new AbstractActionRemoveMultipleOutputs<ClocheRecipe>(this, output)
		{
			@Override
			public List<ItemStack> getAllOutputs(ClocheRecipe recipe)
			{
				return recipe.outputs;
			}
		});
	}

	/**
	 * Adds a recipe to the garden Cloche.
	 * <p>
	 * Requires an additional {@link Block} object that should be rendered in the game.<br/>
	 * Also requires a render type that states how the block should "grow" inside the cloche.<br/>
	 * These two parameters are solely for Rendering purposes and don't change what the recipe returns.
	 * <p>
	 * By default these 4 renderers are present:
	 * "crop", can be used for any 1-block crops with an age property
	 * "stacking", can be used for stacking plants like sugarcane or cactus
	 * "stem", can be used for stem-grown plants like melon or pumpkin
	 * "generic", can be used for any block, making it grow in size, like mushrooms
	 *
	 * @param recipePath  recipePath The recipe name, without the resource location
	 * @param seed        The seed that needs to be inserted in the Cloche's gui
	 * @param soil        The soil that this seeds need to grow on
	 * @param time        The time it takes for the crop to mature (without modifiers), in ticks
	 * @param outputs     The outputs this crop produces when it matures
	 * @param renderBlock The block that should be rendered in world
	 * @param renderType  The render type that should be used
	 * @docParam recipePath "bonsai_oak"
	 * @docParam seed <item:minecraft:oak_sapling>
	 * @docParam soil <item:minecraft:dirt>
	 * @docParam time 100
	 * @docParam outputs [<item:minecraft:apple>, <item:minecraft:oak_sapling>, <item:minecraft:oak_wood> * 5]
	 * @docParam renderBlock <blockstate:minecraft:oak_sapling>.block
	 * @docParam renderType "generic"
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredient seed, IIngredient soil, int time, IItemStack[] outputs, Block renderBlock, @ZenCodeType.Optional("\"generic\"") String renderType)
	{
		final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
		final List<ItemStack> outputList = CrTIngredientUtil.getNonNullList(outputs);
		final Ingredient seedIngredient = seed.asVanillaIngredient();
		final Ingredient soilIngredient = soil.asVanillaIngredient();
		if(!ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.containsKey(renderType))
		{
			throw new IllegalArgumentException("Unknown Render Type: "+renderType);
		}

		final ClocheRenderFunction.ClocheRenderReference renderReference = new ClocheRenderFunction.ClocheRenderReference(renderType, renderBlock);
		try
		{
			final ClocheRecipe recipe = new ClocheRecipe(resourceLocation, outputList, seedIngredient, soilIngredient, time, renderReference);
			CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
		} catch(Exception ex)
		{
			CraftTweakerAPI.logThrowing("Could not create Cloche recipe '%s' with renderType '%s': ", ex, recipePath, renderType);
		}
	}
}
