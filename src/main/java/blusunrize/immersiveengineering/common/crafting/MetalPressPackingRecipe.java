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
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;

/**
 * @author BluSunrize - 12.09.2017
 */
public class MetalPressPackingRecipe extends MetalPressRecipe
{
	private final int gridSize;
	private final int totalAmount;
	private final int baseEnergy;
	private HashMap<ComparableItemStack, PackedDelegate> cache = new HashMap();

	public MetalPressPackingRecipe(ComparableItemStack mold, int energy, int gridSize)
	{
		super(ItemStack.EMPTY, ItemStack.EMPTY, mold, energy);
		this.gridSize = gridSize;
		this.totalAmount = gridSize*gridSize;
		this.baseEnergy = energy;

		MetalPressRecipe.deserializers.put("packing"+totalAmount, nbt -> {
			ComparableItemStack comp = ComparableItemStack.readFromNBT(nbt.getCompoundTag("mapKey"));
			if(cache.containsKey(comp))
				return cache.get(comp);
			PackedDelegate delegate = new PackedDelegate(comp, new ItemStack(nbt.getCompoundTag("output")), IngredientStack.readFromNBT(nbt.getCompoundTag("input")), ComparableItemStack.readFromNBT(nbt.getCompoundTag("mold")), nbt.getInteger("energy"));
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
	public boolean matches(ItemStack mold, ItemStack input)
	{
		return input.getCount() >= this.totalAmount&&getOutputCached(input)!=null;
	}

	@Override
	public MetalPressRecipe getActualRecipe(ItemStack mold, ItemStack input)
	{
		return getOutputCached(input);
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
		public NBTTagCompound writeToNBT(NBTTagCompound nbt)
		{
			nbt.setString("type", "packing"+input.inputSize);
			nbt.setTag("mapKey", mapKey.writeToNBT(new NBTTagCompound()));
			nbt.setTag("output", output.writeToNBT(new NBTTagCompound()));
			nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
			nbt.setTag("mold", mold.writeToNBT(new NBTTagCompound()));
			nbt.setInteger("energy", (int)(getTotalProcessEnergy()/energyModifier));
			return nbt;
		}
	}

	private PackedDelegate getOutputCached(ItemStack input)
	{
		ComparableItemStack comp = new ComparableItemStack(input, true, false);
		if(this.cache.containsKey(comp))
			return this.cache.get(comp);

		comp.copy();
		ItemStack out = getPackedOutput(this.gridSize, this.totalAmount, input);
		if(out.isEmpty())
		{
			this.cache.put(comp, null);
			return null;
		}
		PackedDelegate delegate = new PackedDelegate(comp, out, Utils.copyStackWithAmount(input, this.totalAmount), this.mold, this.baseEnergy);
		this.cache.put(comp, delegate);
		return delegate;
	}

	public static ItemStack getPackedOutput(int gridSize, int totalAmount, ItemStack stack)
	{
		InventoryCrafting invC = Utils.InventoryCraftingFalse.createFilledCraftingInventory(gridSize, gridSize, NonNullList.withSize(totalAmount, stack.copy()));
		IRecipe recipe = Utils.findRecipe(invC, FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
		if(recipe!=null)
			return recipe.getCraftingResult(invC);
		return ItemStack.EMPTY;
	}
}
