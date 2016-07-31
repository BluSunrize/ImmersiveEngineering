package blusunrize.immersiveengineering.api.crafting;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IJEIRecipe
{
	List<ItemStack> getJEITotalItemInputs();
	List<ItemStack> getJEITotalItemOutputs();
	List<FluidStack> getJEITotalFluidInputs();
	List<FluidStack> getJEITotalFluidOutputs();
}