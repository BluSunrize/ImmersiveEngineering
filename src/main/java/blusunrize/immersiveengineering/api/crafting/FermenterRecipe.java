package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author BluSunrize - 01.03.2016
 *
 * The recipe for the Fermenter
 */
public class FermenterRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;
	
	public final IngredientStack input;
	public final FluidStack fluidOutput;
	public final ItemStack itemOutput;
	public FermenterRecipe(FluidStack fluidOutput, ItemStack itemOutput, Object input, int energy)
	{
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.input = ApiUtils.createIngredientStack(input);
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(80*timeModifier);

		this.inputList = Lists.newArrayList(this.input);
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
		this.outputList = Lists.newArrayList(this.itemOutput);
	}
	public FermenterRecipe setInputSize(int size)
	{
		this.input.inputSize = size;
		return this;
	}
	
	public static ArrayList<FermenterRecipe> recipeList = new ArrayList();
	public static FermenterRecipe addRecipe(FluidStack fluidOutput, ItemStack itemOutput, Object input, int energy)
	{
		FermenterRecipe r = new FermenterRecipe(fluidOutput, itemOutput, input, energy);
		recipeList.add(r);
		return r;
	}
	public static FermenterRecipe findRecipe(ItemStack input)
	{
		if(input==null)
			return null;
		for(FermenterRecipe recipe : recipeList)
			if(recipe.input.matches(input))
				return recipe;
		return null;
	}
//	public static List<SqueezerRecipe> removeRecipes(ItemStack output)
//	{
//		List<SqueezerRecipe> list = new ArrayList();
//		for(ComparableItemStack mold : recipeList.keySet())
//		{
//			Iterator<SqueezerRecipe> it = recipeList.get(mold).iterator();
//			while(it.hasNext())
//			{
//				SqueezerRecipe ir = it.next();
//				if(OreDictionary.itemMatches(ir.output, output, true))
//				{
//					list.add(ir);
//					it.remove();
//				}
//			}
//		}
//		return list;
//	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
		return nbt;
	}
	public static FermenterRecipe loadFromNBT(NBTTagCompound nbt)
	{
		IngredientStack input = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
		for(FermenterRecipe recipe : recipeList)
			if(recipe.input.equals(input))
				return recipe;
		return null;
	}
}