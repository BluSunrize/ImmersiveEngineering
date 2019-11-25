/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.data.RecipeSerializers;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class SpeedloaderRecipe extends SpecialRecipe {
	private final byte[] offsetPattern = {0, 1, 1, 1, 0, -1, -1, -1};

	public SpeedloaderRecipe(ResourceLocation ressourceLocation) {
		super(ressourceLocation);
	}

	@Override
	public boolean matches(CraftingInventory inv, @Nonnull World world) {
		ItemStack stackInSlot;
		int speedloaderX = -1;
		int speedloaderY = -1;
		boolean hasSpeedLoader = false;
		boolean hasBullets = false;
		int width = inv.getWidth();
		//for bullets found before the speedloader was located. could maybe limit it to half the crafting grids size,
		//but I can't come up with a smart way to prevent out-of-bounds exceptions for mismatching inventories at the moment
		boolean[][] blindBulletGrid = new boolean[inv.getHeight()][width];
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			stackInSlot = inv.getStackInSlot(i);
			if (!stackInSlot.isEmpty()) {
				if (stackInSlot.getItem() instanceof SpeedloaderItem) {
					if (hasSpeedLoader || !((SpeedloaderItem) stackInSlot.getItem()).isEmpty(stackInSlot))
						return false;
					speedloaderX = i % width;
					speedloaderY = i / width;
					for (int j = 0; j < inv.getHeight(); j++) {
						for (int k = 0; k < width; k++) {
							if (j >= speedloaderY && k >= speedloaderX)
								break;
							if (blindBulletGrid[j][k] && (Math.abs(j - speedloaderY) > 1 || Math.abs(k - speedloaderX) > 1))
								return false;
						}
						if (j >= speedloaderY)
							break;
					}
					hasSpeedLoader = true;
				} else if (stackInSlot.getItem() instanceof BulletItem) {
					hasBullets = true;
					if (!hasSpeedLoader)
						blindBulletGrid[i / width][i % width] = true;
					else if (Math.abs((i / width) - speedloaderY) > 1 || Math.abs((i % width) - speedloaderX) > 1)
						return false;
				} else
					return false;
			}
		}
		return hasSpeedLoader && hasBullets;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		ItemStack speedloader = null;
		int speedloaderX = -1;
		int speedloaderY = -1;
		int width = inv.getWidth();
		int height = inv.getHeight();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i).getItem() instanceof SpeedloaderItem) {
				speedloader = inv.getStackInSlot(i);
				speedloaderX = i % width;
				speedloaderY = i / width;
				break;
			}
		}

		ItemStack out = speedloader.copy();
		NonNullList<ItemStack> fill = NonNullList.withSize(8, ItemStack.EMPTY);
		for (int i = 0; i < 8; i++) { //8 == offsetPattern.length == # of Revolver Slots
			int curY = speedloaderY + offsetPattern[(i + 6) % 8];
			if (curY < 0 || curY >= height)
				continue;
			int curX = speedloaderX + offsetPattern[i];
			if (curX < 0 || curX >= width)
				continue;
			ItemStack curBullet = inv.getStackInSlot(width * curY + curX);
			if (!curBullet.isEmpty())
				fill.set(i, Utils.copyStackWithAmount(curBullet, 1));
		}
		((SpeedloaderItem) out.getItem()).setContainedItems(out, fill);
		return out;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width >= 2 || height >= 2; //assumes the minimum value for width and height is 1, so this isn't checked for
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer() {
		return RecipeSerializers.SPEEDLOADER_LOAD;
	}

}