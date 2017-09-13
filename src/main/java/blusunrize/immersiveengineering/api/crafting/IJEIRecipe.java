package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IJEIRecipe
{
	default boolean listInJEI() { return true; }
	List<ItemStack> getJEITotalItemInputs();
	List<ItemStack> getJEITotalItemOutputs();
	List<FluidStack> getJEITotalFluidInputs();
	List<FluidStack> getJEITotalFluidOutputs();
}