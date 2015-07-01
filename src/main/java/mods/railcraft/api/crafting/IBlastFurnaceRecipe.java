/*
 * ******************************************************************************
 *  Copyright 2011-2015 CovertJaguar
 *
 *  This work (the API) is licensed under the "MIT" License, see LICENSE.md for details.
 * ***************************************************************************
 */

package mods.railcraft.api.crafting;

import net.minecraft.item.ItemStack;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IBlastFurnaceRecipe
{

    public int getCookTime();

    public ItemStack getInput();

    public ItemStack getOutput();

    int getOutputStackSize();

    boolean isRoomForOutput(ItemStack outputSlot);
}
