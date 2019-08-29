/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PotionBulletRecipe implements ICraftingRecipe
{
	public static final IRecipeSerializer<PotionBulletRecipe> SERIALIZER = IRecipeSerializer.register(
			ImmersiveEngineering.MODID+":potion_bullets", new SpecialRecipeSerializer<>(PotionBulletRecipe::new)
	);

	private final ResourceLocation id;

	public PotionBulletRecipe(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean matches(CraftingInventory inv, @Nonnull World world)
	{
		ItemStack bullet = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(bullet.isEmpty()&&Weapons.bullet.equals(stackInSlot.getItem())&&"potion".equals(ItemNBTHelper.getString(stackInSlot, "bullet")))
					bullet = stackInSlot;
				else if(potion.isEmpty()&&stackInSlot.getItem() instanceof PotionItem)
					potion = stackInSlot;
				else
					return false;
		}
		return !bullet.isEmpty()&&!potion.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		ItemStack bullet = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(bullet.isEmpty()&&Weapons.bullet.equals(stackInSlot.getItem())&&"potion".equals(ItemNBTHelper.getString(stackInSlot, "bullet")))
					bullet = stackInSlot;
				else if(potion.isEmpty()&&stackInSlot.getItem() instanceof PotionItem)
					potion = stackInSlot;
		}
		ItemStack newBullet = Utils.copyStackWithAmount(bullet, 1);
		ItemNBTHelper.setItemStack(newBullet, "potion", potion.copy());
		return newBullet;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput()
	{
		return BulletHandler.getBulletStack("potion");
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}

	@Nonnull
	@Override
	public ResourceLocation getId()
	{
		return id;
	}
}