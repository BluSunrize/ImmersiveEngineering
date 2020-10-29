/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.items.IEBaseItem;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

// A modified version of RepairItemRecipe that works correctly with items with container items
// (see BluSunrize/ImmersiveEngineering#3994)
public class IERepairItemRecipe extends SpecialRecipe
{
	public IERepairItemRecipe(ResourceLocation name)
	{
		super(name);
	}

	@Override
	public boolean matches(@Nonnull CraftingInventory inv, @Nonnull World worldIn)
	{
		return findInputSlots(inv).isPresent();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull CraftingInventory inv)
	{
		return findInputSlots(inv)
				.map(p -> combineStacks(p.getLeft(), p.getRight()))
				.orElse(ItemStack.EMPTY);
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width*height >= 2;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
	{
		return NonNullList.withSize(inv.getHeight()*inv.getWidth(), ItemStack.EMPTY);
	}

	@Nonnull
	@Override
	public IRecipeSerializer<IERepairItemRecipe> getSerializer()
	{
		return Objects.requireNonNull(RecipeSerializers.IE_REPAIR_SERIALIZER.get());
	}

	private Optional<Pair<ItemStack, ItemStack>> findInputSlots(CraftingInventory inv)
	{
		Optional<ItemStack> first = Optional.empty();
		Optional<ItemStack> second = Optional.empty();
		for(int slot = 0; slot < inv.getSizeInventory(); ++slot)
		{
			ItemStack stack = inv.getStackInSlot(slot);
			if(!stack.isEmpty())
			{
				if(!isValidInput(stack))
					return Optional.empty();
				else if(first.isPresent()&&second.isPresent())
					return Optional.empty();
				else if(first.isPresent())
				{
					ItemStack existing = first.get();
					if(existing.getItem()!=stack.getItem())
						return Optional.empty();
					second = Optional.of(stack);
				}
				else
					first = Optional.of(stack);
			}
		}
		if(first.isPresent()&&second.isPresent())
			return Optional.of(Pair.of(first.get(), second.get()));
		else
			return Optional.empty();
	}

	private boolean isValidInput(ItemStack in)
	{
		return in.getItem() instanceof IEBaseItem&&((IEBaseItem)in.getItem()).isIERepairable(in);
	}

	// Copy of the vanilla logic
	private ItemStack combineStacks(ItemStack a, ItemStack b)
	{
		int remainingA = a.getMaxDamage()-a.getDamage();
		int remainingB = a.getMaxDamage()-b.getDamage();
		int remainingResult = remainingA+remainingB+a.getMaxDamage()*5/100;
		int damageResult = a.getMaxDamage()-remainingResult;
		if(damageResult < 0)
		{
			damageResult = 0;
		}

		ItemStack result = new ItemStack(a.getItem());
		result.setDamage(damageResult);
		return result;
	}
}
