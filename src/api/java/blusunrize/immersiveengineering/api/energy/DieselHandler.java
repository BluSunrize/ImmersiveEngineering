/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;

/**
 * @author BluSunrize - 23.04.2015
 * <p>
 * The Fuel Handler for the Diesel Generator. Use this to register custom fuels
 */
public class DieselHandler
{
	private static final List<Pair<Tag<Fluid>, Integer>> dieselGenBurnTime = new ArrayList<>();
	private static final Set<Tag<Fluid>> drillFuel = new HashSet<>();

	static
	{
		drillFuel.add(IETags.drillFuel);
	}

	/**
	 * @param fuel the fluidtag to be used as fuel
	 * @param time the total burn time gained from one bucket
	 * @deprecated use JSON recipes instead
	 */
	@Deprecated
	public static void registerFuel(Tag<Fluid> fuel, int time)
	{
		if(fuel!=null)
			dieselGenBurnTime.add(Pair.of(fuel, time));
	}

	@Deprecated
	public static int getBurnTime(Fluid fuel)
	{
		if(fuel!=null)
		{
			GeneratorFuel recipe = GeneratorFuel.getRecipeFor(fuel, null);
			if(recipe!=null)
				return recipe.getBurnTime();
		}
		return 0;
	}

	@Deprecated
	public static boolean isValidFuel(Fluid fuel)
	{
		if(fuel!=null)
			return GeneratorFuel.getRecipeFor(fuel, null)!=null;
		return false;
	}

	@Deprecated
	public static List<Pair<Tag<Fluid>, Integer>> getFuelValues()
	{
		List<Pair<Tag<Fluid>, Integer>> values = new ArrayList<>(dieselGenBurnTime);
		for(GeneratorFuel recipe : GeneratorFuel.ALL_FUELS)
			values.add(Pair.of(
					recipe.fluids.map(Function.identity(), DieselHandler::makeFakeTag), recipe.getBurnTime()
			));
		return values;
	}

	private static <T> Tag<T> makeFakeTag(List<T> elements)
	{
		return new Tag<T>()
		{
			@Override
			public boolean contains(T object)
			{
				return elements.contains(object);
			}

			@Override
			public List<T> getValues()
			{
				return Collections.unmodifiableList(elements);
			}
		};
	}

	@Deprecated
	public static void registerDrillFuel(Tag<Fluid> fuel)
	{
		if(fuel!=null)
			drillFuel.add(fuel);
	}

	public static boolean isValidDrillFuel(Fluid fuel)
	{
		return fuel!=null&&drillFuel.stream().anyMatch(fluidTag -> fluidTag.contains(fuel));
	}

	@Deprecated
	public static List<GeneratorFuel> getLegacyRecipes()
	{
		List<GeneratorFuel> list = new ArrayList<>();
		int i = 0;
		for(Pair<Tag<Fluid>, Integer> p : dieselGenBurnTime)
		{
			list.add(new GeneratorFuel(new ResourceLocation(Lib.MODID, "legacy_"+i), p.getLeft(), p.getRight()));
			++i;
		}
		return list;
	}
}