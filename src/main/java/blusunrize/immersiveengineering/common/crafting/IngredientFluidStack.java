package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.UniversalBucket;

import javax.annotation.Nullable;

/**
 * @author BluSunrize - 03.07.2017
 */
public class IngredientFluidStack extends Ingredient
{
	private final FluidStack fluid;

	public IngredientFluidStack(FluidStack fluid)
	{
		this.fluid = fluid;
	}

	ItemStack[] cachedStacks;
	@Override
	public ItemStack[] getMatchingStacks()
	{
		if(cachedStacks==null)
		{
			cachedStacks = new ItemStack[]{UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluid.getFluid())};
		}
		return this.cachedStacks;
	}

	@Override
	public boolean apply(@Nullable ItemStack stack)
	{
		if (stack == null)
		{
			return false;
		}
		else
		{
			FluidStack fs = FluidUtil.getFluidContained(stack);
			return fs == null && this.fluid == null || fs != null && fs.containsFluid(fluid);
		}
	}
}
