package blusunrize.immersiveengineering.api.crafting;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author BluSunrize - 02.02.2016
 * <br>
 * An interface implemented by recipes that can be handled by IE's Metal Multiblocks. <br>
 * This is only used by IE's own machines, it's just in the API because recipes have to implement it.
 */
public interface IMultiblockRecipe
{
	List<IngredientStack> getItemInputs();
	List<FluidStack> getFluidInputs();
	List<ItemStack> getItemOutputs();
	List<FluidStack> getFluidOutputs();
	
	int getTotalProcessTime();
	int getTotalProcessEnergy();
	int getMultipleProcessTicks();
	
	NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound);
}
