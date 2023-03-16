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
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class ThermoelectricSource extends IESerializableRecipe
{
	public static RegistryObject<IERecipeSerializer<ThermoelectricSource>> SERIALIZER;

	public static final CachedRecipeList<ThermoelectricSource> ALL_SOURCES = new CachedRecipeList<>(IERecipeTypes.THERMOELECTRIC_SOURCE);

	public final FastEither<TagKey<Block>, List<Block>> blocks;
	public final int temperature;

	public ThermoelectricSource(ResourceLocation id, TagKey<Block> blocks, int temperature)
	{
		this(id, FastEither.left(blocks), temperature);
	}

	public ThermoelectricSource(ResourceLocation id, List<Block> blocks, int temperature)
	{
		this(id, FastEither.right(blocks), temperature);
	}

	private ThermoelectricSource(ResourceLocation id, FastEither<TagKey<Block>, List<Block>> blocks, int temperature)
	{
		super(LAZY_EMPTY, IERecipeTypes.THERMOELECTRIC_SOURCE, id);
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
	public ItemStack getResultItem(RegistryAccess access)
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
		for(ThermoelectricSource entry : ALL_SOURCES.getRecipes(level))
			if(entry.matches(block))
				return entry;
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
		for(ThermoelectricSource ingr : ALL_SOURCES.getRecipes(level))
		{
			Block example = ingr.getExample();
			if(example!=Blocks.AIR)
				existingMap.put(new ItemStack(example).getHoverName(), ingr.temperature);
		}
		Fluid[] fluidsToShow = {Fluids.WATER, Fluids.LAVA};
		for(Fluid f : fluidsToShow)
		{
			FluidStack fs = new FluidStack(f, FluidType.BUCKET_VOLUME);
			existingMap.put(fs.getDisplayName(), f.getFluidType().getTemperature(fs));
		}
		return existingMap;
	}
}
