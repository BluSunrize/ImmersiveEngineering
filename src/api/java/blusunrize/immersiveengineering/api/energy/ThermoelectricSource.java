/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class ThermoelectricSource extends IESerializableRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<ThermoelectricSource>> SERIALIZER;

	public static final CachedRecipeList<ThermoelectricSource> ALL_SOURCES = new CachedRecipeList<>(IERecipeTypes.THERMOELECTRIC_SOURCE);

	public final FastEither<TagKey<Block>, List<Block>> blocks;
	public final int temperature;

	public ThermoelectricSource(TagKey<Block> blocks, int temperature)
	{
		this(FastEither.left(blocks), temperature);
	}

	public ThermoelectricSource(List<Block> blocks, int temperature)
	{
		this(FastEither.right(blocks), temperature);
	}

	public ThermoelectricSource(Block block, int temperature)
	{
		this(List.of(block), temperature);
	}

	private ThermoelectricSource(FastEither<TagKey<Block>, List<Block>> blocks, int temperature)
	{
		super(TagOutput.EMPTY, IERecipeTypes.THERMOELECTRIC_SOURCE);
		this.blocks = blocks;
		this.temperature = temperature;
	}

	@Override
	protected IERecipeSerializer<?> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(Provider access)
	{
		return ItemStack.EMPTY;
	}

	public Block getExample()
	{
		return blocks.map(
				tagKey -> BuiltInRegistries.BLOCK.getTag(tagKey)
						.flatMap(t -> t.getRandomElement(ApiUtils.RANDOM_SOURCE))
						.map(Holder::value)
						.orElse(Blocks.AIR),
				l -> l.isEmpty()?Blocks.AIR: l.get(0)
		);
	}

	public List<Block> getMatchingBlocks()
	{
		return blocks.map(t -> TagUtils.elementStream(BuiltInRegistries.BLOCK, t).toList(), Function.identity());
	}

	public int getTemperature()
	{
		return temperature;
	}

	public boolean matches(Block block)
	{
		if(blocks.isLeft())
			return block.defaultBlockState().is(blocks.leftNonnull());
		else
			return blocks.rightNonnull().contains(block);
	}

	@Nullable
	public static ThermoelectricSource getSource(Level level, Block block, @Nullable ThermoelectricSource hint)
	{
		if(hint!=null&&hint.matches(block))
			return hint;
		for(RecipeHolder<ThermoelectricSource> entry : ALL_SOURCES.getRecipes(level))
			if(entry.value().matches(block))
				return entry.value();
		return null;
	}

	public static SortedMap<Component, Integer> getThermalValuesSorted(Level level, boolean inverse)
	{
		SortedMap<Component, Integer> existingMap = new TreeMap<>(
				Comparator.comparing(
						(Function<Component, String>)Component::getString,
						inverse?Comparator.reverseOrder(): Comparator.naturalOrder()
				)
		);
		for(RecipeHolder<ThermoelectricSource> ingr : ALL_SOURCES.getRecipes(level))
		{
			Block example = ingr.value().getExample();
			if(example!=Blocks.AIR)
				existingMap.put(new ItemStack(example).getHoverName(), ingr.value().temperature);
		}
		Fluid[] fluidsToShow = {Fluids.WATER, Fluids.LAVA};
		for(Fluid f : fluidsToShow)
		{
			FluidStack fs = new FluidStack(f, FluidType.BUCKET_VOLUME);
			existingMap.put(fs.getHoverName(), f.getFluidType().getTemperature(fs));
		}
		return existingMap;
	}
}
