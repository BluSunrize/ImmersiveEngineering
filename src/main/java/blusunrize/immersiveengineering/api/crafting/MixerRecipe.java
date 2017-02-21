package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author BluSunrize - 20.02.2016
 *
 * The recipe for the Squeezer
 */
public class MixerRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final IngredientStack[] itemInputs;
	public final FluidStack fluidInput;
	public final FluidStack fluidOutput;
	public MixerRecipe(FluidStack fluidOutput, FluidStack fluidInput, Object[] itemInputs, int energy)
	{
		this.fluidOutput = fluidOutput;
		this.fluidInput = fluidInput;
		this.itemInputs = new IngredientStack[itemInputs==null?0:itemInputs.length];
		if(itemInputs!=null)
			for(int i=0; i<itemInputs.length; i++)
				this.itemInputs[i] = ApiUtils.createIngredientStack(itemInputs[i]);
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(fluidOutput.amount*timeModifier);

		this.fluidInputList  = Lists.newArrayList(this.fluidInput);
		this.inputList = Lists.newArrayList(this.itemInputs);
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
	}

	public static ArrayList<MixerRecipe> recipeList = new ArrayList();
	public static MixerRecipe addRecipe(FluidStack fluidOutput, FluidStack fluidInput, Object[] itemInput, int energy)
	{
		MixerRecipe r = new MixerRecipe(fluidOutput, fluidInput, itemInput, energy);
		recipeList.add(r);
		return r;
	}

	public static MixerRecipe findRecipe(FluidStack fluid, ItemStack... components)
	{
		if(fluid==null)
			return null;
		for(MixerRecipe recipe : recipeList)
			if(recipe.matches(fluid, components))
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

	public boolean matches(FluidStack fluid, ItemStack... components)
	{
		if(fluid!=null && fluid.containsFluid(this.fluidInput))
		{

			ArrayList<ItemStack> queryList = new ArrayList<ItemStack>(components.length);
			for(ItemStack s : components)
				if(s!=null)
					queryList.add(s.copy());

			for(IngredientStack add : this.itemInputs)
				if(add!=null)
				{
					int addAmount = add.inputSize;
					Iterator<ItemStack> it = queryList.iterator();
					while(it.hasNext())
					{
						ItemStack query = it.next();
						if(query!=null)
						{
							if(add.matches(query))
								if(query.stackSize > addAmount)
								{
									query.stackSize-=addAmount;
									addAmount=0;
								}
								else
								{
									addAmount-=query.stackSize;
									query.stackSize=0;
								}
							if(query.stackSize<=0)
								it.remove();
							if(addAmount<=0)
								break;
						}
					}
					if(addAmount>0)
						return false;
				}
			return true;
		}
		return false;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("fluidInput", fluidInput.writeToNBT(new NBTTagCompound()));
		if(this.itemInputs.length>0)
		{
			NBTTagList list = new NBTTagList();
			for(IngredientStack add : this.itemInputs)
				list.appendTag(add.writeToNBT(new NBTTagCompound()));
			nbt.setTag("itemInputs", list);
		}
		return nbt;
	}
	public static MixerRecipe loadFromNBT(NBTTagCompound nbt)
	{
		FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fluidInput"));
		IngredientStack[] itemInputs = null;
		if(nbt.hasKey("itemInputs"))
		{
			NBTTagList list = nbt.getTagList("itemInputs", 10);
			itemInputs = new IngredientStack[list.tagCount()];
			for(int i=0; i<itemInputs.length; i++)
				itemInputs[i] = IngredientStack.readFromNBT(list.getCompoundTagAt(i));
		}
		for(MixerRecipe recipe : recipeList)
			if(recipe.fluidInput.equals(fluidInput))
			{
				if(itemInputs==null && recipe.itemInputs.length<1)
					return recipe;
				else if(itemInputs!=null && recipe.itemInputs.length==itemInputs.length)
				{
					boolean b = true;
					for(int i=0; i<itemInputs.length; i++)
						if(!itemInputs[i].equals(recipe.itemInputs[i]))
						{
							b=false;
							break;
						}
					if(b)
						return recipe;
				}
			}
		return null;
	}
}