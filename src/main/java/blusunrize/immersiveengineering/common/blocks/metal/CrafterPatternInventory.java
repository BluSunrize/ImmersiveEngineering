/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.assembler.RecipeQuery;
import blusunrize.immersiveengineering.common.util.InventoryCraftingFalse;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CrafterPatternInventory
{
	private static final int NUM_SLOTS = 10;

	public final NonNullList<ItemStack> inv = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	public Recipe<CraftingInput> recipe;

	public void recalculateOutput(@Nullable Level level)
	{
		if(level==null)
			return;
		CraftingInput invC = InventoryCraftingFalse.createFilledCraftingInventory(3, 3, inv);
		this.recipe = Utils.findCraftingRecipe(invC, level).map(RecipeHolder::value).orElse(null);
		this.inv.set(9, recipe!=null?recipe.assemble(invC, level.registryAccess()): ItemStack.EMPTY);
	}

	public ListTag writeToNBT(Provider provider)
	{
		ListTag list = new ListTag();
		for(int i = 0; i < this.inv.size(); i++)
			if(!this.inv.get(i).isEmpty())
			{
				CompoundTag itemTag = new CompoundTag();
				itemTag.putByte("Slot", (byte)i);
				list.add(this.inv.get(i).save(provider, itemTag));
			}
		return list;
	}

	public void readFromNBT(ListTag list, Provider provider)
	{
		Collections.fill(this.inv, ItemStack.EMPTY);
		for(int i = 0; i < list.size(); i++)
		{
			CompoundTag itemTag = list.getCompound(i);
			int slot = itemTag.getByte("Slot")&255;
			if(slot < NUM_SLOTS)
				this.inv.set(slot, ItemStack.parseOptional(provider, itemTag));
		}
	}

	@Nullable
	public List<RecipeQuery> getQueries(Level level)
	{
		if(recipe==null)
			recalculateOutput(level);
		if(recipe==null)
			return null;
		return getQueriesGeneric(recipe, inv, level);
	}

	@Nullable
	private static <R extends Recipe<CraftingInput>>
	List<RecipeQuery> getQueriesGeneric(R recipe, NonNullList<ItemStack> inv, Level level)
	{
		AssemblerHandler.IRecipeAdapter<? super R> adapter = AssemblerHandler.findAdapter(recipe);
		return adapter.getQueriedInputs(recipe, inv, level);
	}
}
