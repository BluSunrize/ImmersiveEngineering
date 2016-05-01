package blusunrize.immersiveengineering.api.crafting;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IJEIRecipe
{
	public List<ItemStack> getJEITotalItemInputs();
	public List<ItemStack> getJEITotalItemOutputs();
	public List<FluidStack> getJEITotalFluidInputs();
	public List<FluidStack> getJEITotalFluidOutputs();
}