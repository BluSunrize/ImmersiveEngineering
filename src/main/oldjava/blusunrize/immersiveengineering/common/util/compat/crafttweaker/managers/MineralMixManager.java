/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.util.random.Percentaged;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Allows you to add or remove Mineral Mix recipes.
 * <p>
 * Mineral Mixes consist of a list of weighted itemstack outputs, a weight for how often the mix is selected, a change of how often the mix should fail, a list of dimensions that the mix can be excavated in and a background used in the gui.
 *
 * @docParam this <recipetype:immersiveengineering:mineral_mix>
 */
@ZenRegister
@Document("mods/immersiveengineering/MineralMix")
@ZenCodeType.Name("mods.immersiveengineering.MineralMix")
public class MineralMixManager implements IRecipeManager<MineralMix>
{

	@Override
	public RecipeType<MineralMix> getRecipeType()
	{
		return IERecipeTypes.MINERAL_MIX.get();
	}

	/**
	 * Adds a Mineral Mix recipe
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param outputs    The WeightedItemStack array outputs
	 * @param spoils     The WeightedItemStack array spoils
	 * @param weight     How often should the Mix be exavated.
	 * @param failChance The chance for the Mix to fail excavation.
	 * @param dimensions The list of dimensions that this Mix can be mined in.
	 * @param background The background block used in samples
	 * @docParam recipePath "sheep_mix"
	 * @docParam outputs [<item:minecraft:white_wool> % 50, <item:minecraft:orange_wool> % 25, <item:minecraft:magenta_wool>]
	 * @docParam weight 50
	 * @docParam failChance 0.5
	 * @docParam dimensions [<resource:minecraft:overworld>]
	 * @docParam background <block:minecraft:white_wool>
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, List<Percentaged<IItemStack>> outputs, List<Percentaged<IItemStack>> spoils, int weight, float failChance, ResourceLocation[] dimensions, Block background)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final StackWithChance[] stacksWithChances = outputs.stream().map(CrTIngredientUtil::getStackWithChance).toArray(StackWithChance[]::new);
		final StackWithChance[] spoilsWithChances = spoils.stream().map(CrTIngredientUtil::getStackWithChance).toArray(StackWithChance[]::new);
		final List<ResourceKey<Level>> dimensionKeys = Arrays.stream(dimensions).map(resourceLocation1 -> ResourceKey.create(Registries.DIMENSION, resourceLocation1)).collect(Collectors.toList());
		final MineralMix mix = new MineralMix(resourceLocation, stacksWithChances, spoilsWithChances, weight, failChance, dimensionKeys, background);
		CraftTweakerAPI.apply(new ActionAddRecipe<>(this, mix, null));
	}

	@Override
	public void remove(IIngredient output)
	{
		throw new UnsupportedOperationException("Mineral Mixes can only be removed by name, not by output!");
	}
}
