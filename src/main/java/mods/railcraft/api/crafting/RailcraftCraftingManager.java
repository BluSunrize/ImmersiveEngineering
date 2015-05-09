/*
 * ******************************************************************************
 *  Copyright 2011-2015 CovertJaguar
 *
 *  This work (the API) is licensed under the "MIT" License, see LICENSE.md for details.
 * ***************************************************************************
 */

package mods.railcraft.api.crafting;

/**
 * These variables are defined during the pre-init phase.
 * Do not attempt to access them during pre-init.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class RailcraftCraftingManager
{

    public static ICokeOvenCraftingManager cokeOven;
    public static IBlastFurnaceCraftingManager blastFurnace;
    public static IRockCrusherCraftingManager rockCrusher;
    public static IRollingMachineCraftingManager rollingMachine;
}
