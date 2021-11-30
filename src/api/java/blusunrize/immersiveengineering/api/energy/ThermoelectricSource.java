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
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.utils.FastEither;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class ThermoelectricSource extends IESerializableRecipe
{
	public static RecipeType<ThermoelectricSource> TYPE;
	public static RegistryObject<IERecipeSerializer<ThermoelectricSource>> SERIALIZER;

	public static Collection<ThermoelectricSource> ALL_SOURCES = new ArrayList<>();

	public final FastEither<Tag<Block>, List<Block>> blocks;
	public final int temperature;

	public ThermoelectricSource(ResourceLocation id, Tag<Block> blocks, int temperature)
	{
		this(id, FastEither.left(blocks), temperature);
	}

	public ThermoelectricSource(ResourceLocation id, List<Block> blocks, int temperature)
	{
		this(id, FastEither.right(blocks), temperature);
	}

	private ThermoelectricSource(ResourceLocation id, FastEither<Tag<Block>, List<Block>> blocks, int temperature)
	{
		super(ItemStack.EMPTY, TYPE, id);
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
	public ItemStack getResultItem()
	{
		return ItemStack.EMPTY;
	}

	public Block getExample()
	{
		return blocks.map(
				t -> t.getRandomElement(ApiUtils.RANDOM),
				l -> l.isEmpty()?Blocks.AIR: l.get(0)
		);
	}

	public List<Block> getMatchingBlocks()
	{
		return blocks.map(Tag::getValues, Function.identity());
	}

	public int getTemperature()
	{
		return temperature;
	}

	public boolean matches(Block block)
	{
		if(blocks.isLeft())
			return blocks.leftNonnull().contains(block);
		else
			return blocks.rightNonnull().contains(block);
	}

	@Nullable
	public static ThermoelectricSource getSource(Block block, @Nullable ThermoelectricSource hint)
	{
		if(hint!=null&&hint.matches(block))
			return hint;
		for(ThermoelectricSource entry : ALL_SOURCES)
			if(entry.matches(block))
				return entry;
		return null;
	}

	public static SortedMap<Component, Integer> getThermalValuesSorted(boolean inverse)
	{
		SortedMap<Component, Integer> existingMap = new TreeMap<>(
				Comparator.comparing(
						(Function<Component, String>)Component::getString,
						inverse?Comparator.reverseOrder(): Comparator.naturalOrder()
				)
		);
		for(ThermoelectricSource ingr : ALL_SOURCES)
		{
			Block example = ingr.getExample();
			if(example!=Blocks.AIR)
				existingMap.put(new ItemStack(example).getHoverName(), ingr.temperature);
		}
		return existingMap;
	}
}
