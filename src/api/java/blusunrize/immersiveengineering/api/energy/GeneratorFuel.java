/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class GeneratorFuel extends IESerializableRecipe
{
	public static RegistryObject<IERecipeSerializer<GeneratorFuel>> SERIALIZER;

	public static final CachedRecipeList<GeneratorFuel> RECIPES = new CachedRecipeList<>(IERecipeTypes.GENERATOR_FUEL);

	private final FastEither<TagKey<Fluid>, List<Fluid>> fluids;
	private final int burnTime;

	public GeneratorFuel(ResourceLocation id, TagKey<Fluid> fluids, int burnTime)
	{
		super(LAZY_EMPTY, IERecipeTypes.GENERATOR_FUEL, id);
		this.fluids = FastEither.left(fluids);
		this.burnTime = burnTime;
	}

	public GeneratorFuel(ResourceLocation id, List<Fluid> fluids, int burnTime)
	{
		super(LAZY_EMPTY, IERecipeTypes.GENERATOR_FUEL, id);
		this.fluids = FastEither.right(fluids);
		this.burnTime = burnTime;
	}

	public List<Fluid> getFluids()
	{
		return fluids.map(t -> TagUtils.elementStream(BuiltInRegistries.FLUID, t).toList(), Function.identity());
	}

	public int getBurnTime()
	{
		return burnTime;
	}

	@Override
	protected IERecipeSerializer<?> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(RegistryAccess access)
	{
		return ItemStack.EMPTY;
	}

	public boolean matches(Fluid in)
	{
		if(this.fluids.isLeft())
			return in.is(this.fluids.leftNonnull());
		else
			return this.fluids.rightNonnull().contains(in);
	}

	public static GeneratorFuel getRecipeFor(Level level, Fluid in, @Nullable GeneratorFuel hint)
	{
		if(hint!=null&&hint.matches(in))
			return hint;
		for(GeneratorFuel fuel : RECIPES.getRecipes(level))
			if(fuel.matches(in))
				return fuel;
		return null;
	}

	public static SortedMap<Component, Integer> getManualFuelList(Level level)
	{
		SortedMap<Component, Integer> map = new TreeMap<>(
				Comparator.comparing(Component::getString, Comparator.naturalOrder())
		);
		for(GeneratorFuel recipe : RECIPES.getRecipes(level))
			for(Fluid f : recipe.getFluids())
				map.put(f.getFluidType().getDescription(), recipe.getBurnTime());
		return map;
	}
}
