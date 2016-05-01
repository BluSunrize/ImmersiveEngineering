package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author BluSunrize - 01.05.2015
 *
 * The recipe for the crusher
 */
public class CrusherRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;
	
	public final String oreInputString;
	public final IngredientStack input;
	public final ItemStack output;
	public ItemStack[] secondaryOutput;
	public float[] secondaryChance;

	public CrusherRecipe(ItemStack output, Object input, int energy)
	{
		this.output = output;
		this.input = ApiUtils.createIngredientStack(input);
		this.oreInputString = input instanceof String?(String)input: null;
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(50*timeModifier);

		this.inputList = Lists.newArrayList(this.input);
		this.outputList = Lists.newArrayList(this.output);
	}
	/**
	 * Adds secondary outputs to the recipe. Should the recipe have secondary outputs, these will be added /in addition/<br>
	 * The array should be alternating between Item/Block/ItemStack/ArrayList and a float for the chance
	 */
	public CrusherRecipe addToSecondaryOutput(Object... outputs)
	{
		if(outputs.length%2!=0)
			return this;
		ArrayList<ItemStack> newSecondaryOutput = new ArrayList<ItemStack>();
		ArrayList<Float> newSecondaryChance = new ArrayList<Float>();
		if(secondaryOutput!=null)
			for(int i=0; i<secondaryOutput.length; i++)
			{
				newSecondaryOutput.add(secondaryOutput[i]);
				newSecondaryChance.add(secondaryChance[i]);
			}
		for(int i=0; i<(outputs.length/2); i++)
			if(outputs[i*2]!=null)
			{
				Object o = ApiUtils.convertToValidRecipeInput(outputs[i*2]);
				ItemStack ss = o instanceof ItemStack?(ItemStack)o: o instanceof List?IEApi.getPreferredStackbyMod((List<ItemStack>)o): null;
				if(ss!=null)
				{
					newSecondaryOutput.add(ss);
					newSecondaryChance.add((Float)outputs[i*2+1]);
				}
			}
		secondaryOutput = newSecondaryOutput.toArray(new ItemStack[newSecondaryOutput.size()]);
		secondaryChance = new float[newSecondaryChance.size()];
		int i=0;
		for(Float f : newSecondaryChance)
			secondaryChance[i++] = f;

		this.outputList = Lists.newArrayList(this.secondaryOutput);
		if(this.outputList.isEmpty())
			this.outputList.add(this.output);
		else
			this.outputList.add(0, this.output);

		return this;
	}

	public static ArrayList<CrusherRecipe> recipeList = new ArrayList<CrusherRecipe>();
	public static CrusherRecipe addRecipe(ItemStack output, Object input, int energy)
	{
		CrusherRecipe r = new CrusherRecipe(output, input, energy);
		if(r.input!=null && r.output!=null)
			recipeList.add(r);
		return r;
	}
	public static CrusherRecipe findRecipe(ItemStack input)
	{
		for(CrusherRecipe recipe : recipeList)
			if(recipe.input.matchesItemStack(input))
				return recipe;
		return null;
	}
	public static List<CrusherRecipe> removeRecipes(ItemStack stack)
	{
		List<CrusherRecipe> list = new ArrayList();
		Iterator<CrusherRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			CrusherRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 4;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
		return nbt;
	}
	public static CrusherRecipe loadFromNBT(NBTTagCompound nbt)
	{
		IngredientStack input = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
		for(CrusherRecipe recipe : recipeList)
			if(recipe.input.equals(input))
				return recipe;
		return null;
	}
}