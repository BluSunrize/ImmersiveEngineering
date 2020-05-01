/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;

import java.util.HashMap;

/**
 * @author BluSunrize - 12.09.2017
 */
public class MetalPressUnpackingRecipe extends MetalPressRecipe
{
	private final int baseEnergy;
	private HashMap<ComparableItemStack, PackedDelegate> cache = new HashMap<>();

	public MetalPressUnpackingRecipe(ComparableItemStack mold, int energy)
	{
		//TODO: this needs an id
		super(null, ItemStack.EMPTY, new IngredientWithSize(Ingredient.EMPTY), mold, energy);
		this.baseEnergy = energy;
	}

	@Override
	public boolean listInJEI()
	{
		return false;
	}

	@Override
	public boolean matches(ItemStack mold, ItemStack input, World world)
	{
		return getOutputCached(input, world)!=null;
	}

	@Override
	public MetalPressRecipe getActualRecipe(ItemStack mold, ItemStack input, World world)
	{
		return getOutputCached(input, world);
	}

	public static class PackedDelegate extends MetalPressRecipe
	{
		private final ComparableItemStack mapKey;

		public PackedDelegate(ComparableItemStack mapKey, ItemStack output, ItemStack input, ComparableItemStack mold, int energy)
		{
			//TODO: this needs an id
			super(null, output, IngredientWithSize.of(input), mold, energy);
			this.mapKey = mapKey;
		}

		@Override
		public boolean listInJEI()
		{
			return false;
		}

	}

	private PackedDelegate getOutputCached(ItemStack input, World world)
	{
		ComparableItemStack comp = new ComparableItemStack(input, false);
		if(this.cache.containsKey(comp))
			return this.cache.get(comp);

		comp.copy();
		ItemStack out = MetalPressPackingRecipe.getPackedOutput(1, 1, input, world);
		int count = out.getCount();

		if(count!=4&&count!=9)
		{
			this.cache.put(comp, null);
			return null;
		}

		ItemStack rePacked = MetalPressPackingRecipe.getPackedOutput(count==4?2: 3, count, out, world);
		if(rePacked.isEmpty()||!ItemStack.areItemStacksEqual(input, rePacked))
		{
			this.cache.put(comp, null);
			return null;
		}

		PackedDelegate delegate = new PackedDelegate(comp, out, Utils.copyStackWithAmount(input, 1), this.mold, this.baseEnergy);
		this.cache.put(comp, delegate);
		return delegate;
	}
}
