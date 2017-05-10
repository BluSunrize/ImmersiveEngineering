package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author BluSunrize - 22.02.2017
 */
public class MixerRecipePotion extends MixerRecipe
{
	public final PotionType inputPotionType;
	public MixerRecipePotion(PotionType inputType)
	{
		super(new FluidStack(IEContent.fluidPotion,1000), getFluidStackForType(inputType,1000), new Object[0], 6400);
		this.inputPotionType = inputType;
	}

	public static FluidStack getFluidStackForType(PotionType type, int amount)
	{
		if(type==PotionTypes.WATER || type==null)
			return new FluidStack(FluidRegistry.WATER,amount);
		FluidStack stack = new FluidStack(IEContent.fluidPotion,amount);
		stack.tag = new NBTTagCompound();
		stack.tag.setString("Potion", PotionType.REGISTRY.getNameForObject(type).toString());
		return stack;
	}

	@Override
	public FluidStack getFluidOutput(FluidStack input, NonNullList<ItemStack> components)
	{
		if(components.size()!=1)
			return input;
		if(input!=null)
			for(PotionHelper.MixPredicate<PotionType> mixPredicate : PotionHelper.POTION_TYPE_CONVERSIONS)
				if(mixPredicate.input==this.inputPotionType&&mixPredicate.reagent.apply(components.get(0)))
					return getFluidStackForType(mixPredicate.output, input.amount);
		return input;
	}

	@Override
	public boolean matches(FluidStack fluid, NonNullList<ItemStack> components)
	{
		if(fluid!=null && fluid.containsFluid(this.fluidInput))
			for(PotionHelper.MixPredicate<PotionType> mixPredicate : PotionHelper.POTION_TYPE_CONVERSIONS)
				if(mixPredicate.input==this.inputPotionType)
					for(ItemStack stack : components)
						if(mixPredicate.reagent.apply(stack))
							return true;
		return false;
	}

	@Override
	public int[] getUsedSlots(FluidStack fluid, NonNullList<ItemStack> components)
	{
		for(int i = 0; i< components.size(); i++)
			if(!components.get(i).isEmpty() && PotionHelper.isReagent(components.get(i)))
				return new int[]{i};
		return new int[0];
	}
}
