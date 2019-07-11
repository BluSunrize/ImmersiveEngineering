/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
		super(ItemStack.EMPTY, ItemStack.EMPTY, mold, energy);
		this.baseEnergy = energy;

		MetalPressRecipe.deserializers.put("unpacking", nbt -> {
			ComparableItemStack comp = ComparableItemStack.readFromNBT(nbt.getCompound("mapKey"));
			if(cache.containsKey(comp))
				return cache.get(comp);
			PackedDelegate delegate = new PackedDelegate(comp, ItemStack.read(nbt.getCompound("output")),
					IngredientStack.readFromNBT(nbt.getCompound("input")),
					ComparableItemStack.readFromNBT(nbt.getCompound("mold")), nbt.getInt("energy"));
			cache.put(comp, delegate);
			return delegate;
		});
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

		public PackedDelegate(ComparableItemStack mapKey, ItemStack output, Object input, ComparableItemStack mold, int energy)
		{
			super(output, input, mold, energy);
			this.mapKey = mapKey;
		}

		@Override
		public boolean listInJEI()
		{
			return false;
		}

		@Override
		public CompoundNBT writeToNBT(CompoundNBT nbt)
		{
			nbt.putString("type", "unpacking");
			nbt.put("mapKey", mapKey.writeToNBT(new CompoundNBT()));
			nbt.put("output", output.write(new CompoundNBT()));
			nbt.put("input", input.writeToNBT(new CompoundNBT()));
			nbt.put("mold", mold.writeToNBT(new CompoundNBT()));
			nbt.putInt("energy", (int)(getTotalProcessEnergy()/energyModifier));
			return nbt;
		}
	}

	private PackedDelegate getOutputCached(ItemStack input, World world)
	{
		ComparableItemStack comp = new ComparableItemStack(input, true, false);
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
