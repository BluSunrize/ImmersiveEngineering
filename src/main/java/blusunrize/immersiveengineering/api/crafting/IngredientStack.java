package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidRegistry.FluidRegisterEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.oredict.OreDictionary;

public class IngredientStack
{
	public ItemStack stack;
	public List<ItemStack> stackList;
	public String oreName;
	public FluidStack fluid;
	public int inputSize;
	public boolean useNBT;

	public IngredientStack(ItemStack stack)
	{
		this.stack = stack;
		this.inputSize = stack.stackSize;
	}
	public IngredientStack(String oreName, int inputSize)
	{
		this.oreName = oreName;
		this.inputSize = inputSize;
	}
	public IngredientStack(String oreName)
	{
		this(oreName, 1);
	}
	public IngredientStack(List<ItemStack> stackList, int inputSize)
	{
		this.stackList = stackList;
		this.inputSize = inputSize;
	}
	public IngredientStack(List<ItemStack> stackList)
	{
		this(stackList, 1);
	}
	public IngredientStack(FluidStack fluid)
	{
		this.fluid = fluid;
	}


	public IngredientStack setUseNBT(boolean useNBT)
	{
		this.useNBT = useNBT;
		return this;
	}

	public boolean matches(Object input)
	{
		if(input==null)
			return false;
		if(input instanceof IngredientStack)
			return this.equals(input) && this.inputSize <= ((IngredientStack)input).inputSize;
		if(input instanceof ItemStack)
		{
			return matchesItemStack((ItemStack)input);
		}
		else if(input instanceof ItemStack[])
		{
			for(ItemStack iStack : (ItemStack[])input)
				if(matchesItemStack(iStack))
					return true;
		}
		else if(input instanceof List)
		{
			for(Object io : (List)input)
				if(this.matches(io))
					return true;
		}
		else if(input instanceof String)
		{
			if(this.oreName!=null)
				return this.oreName.equals(input);
			return ApiUtils.compareToOreName(stack, (String)input);
		}
		return false;
	}

	public ItemStack getExampleStack()
	{
		ItemStack ret = stack;
		if(ret==null&&stackList!=null&&stackList.size()>0)
			ret = stackList.get(0);
		if(ret==null&&oreName!=null)
		{
			List<ItemStack> ores = OreDictionary.getOres(oreName);
			if(ores!=null&&ores.size()>0)
				ret = ores.get(0);
		}
		if(ret==null&&fluid!=null&&ForgeModContainer.getInstance().universalBucket!=null)
			ret = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluid.getFluid());
		return ret;
	}
	public Object getShapedRecipeInput()
	{
		Object ret = stack;
		if(ret==null&&stackList!=null&&stackList.size()>0)
			ret = stackList;
		if(ret==null&&oreName!=null)
			ret = OreDictionary.getOres(oreName);
		if(ret==null&&fluid!=null&&ForgeModContainer.getInstance().universalBucket!=null)
			ret = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluid.getFluid());
		return ret;
	}

	public boolean matchesItemStack(ItemStack input)
	{
		if(input==null)
			return false;
		if(this.fluid!=null)
		{
			FluidStack fs = FluidUtil.getFluidContained(input);
			if(fs!=null && fs.containsFluid(fluid))
				return true;
		}
		if(this.oreName!=null)
			return ApiUtils.compareToOreName(input, oreName) && this.inputSize <= input.stackSize;
		if(this.stackList!=null)
		{
			for(ItemStack iStack : this.stackList)
				if(OreDictionary.itemMatches(iStack, input, false) && this.inputSize <= input.stackSize)
					return true;
		}
		if(!OreDictionary.itemMatches(stack,input, false) ||  this.inputSize > input.stackSize)
			return false;
		if(this.useNBT)
		{
			if(this.stack.hasTagCompound() != input.hasTagCompound())
				return false;
			if(!this.stack.hasTagCompound() && !input.hasTagCompound())
				return true;
			if(!this.stack.getTagCompound().equals(input.getTagCompound()))
				return false;
		}
		return true;
	}

	public boolean matchesItemStackIgnoringSize(ItemStack input)
	{
		if(this.fluid!=null)
		{
			FluidStack fs = FluidUtil.getFluidContained(input);
			if(fs!=null && fs.containsFluid(fluid))
				return true;
		}
		if(this.oreName!=null)
			return ApiUtils.compareToOreName(input, oreName);
		if(this.stackList!=null)
		{
			for(ItemStack iStack : this.stackList)
				if(OreDictionary.itemMatches(iStack, input, false))
					return true;
		}
		if(!OreDictionary.itemMatches(stack,input, false))
			return false;
		if(this.useNBT)
		{
			if(this.stack.hasTagCompound() != input.hasTagCompound())
				return false;
			if(!this.stack.hasTagCompound() && !input.hasTagCompound())
				return true;
			if(!this.stack.getTagCompound().equals(input.getTagCompound()))
				return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof IngredientStack))
			return false;
		if(this.fluid!=null && ((IngredientStack)object).fluid!=null)
			return this.fluid.equals(((IngredientStack)object).fluid);
		if(this.oreName!=null && ((IngredientStack)object).oreName!=null)
			return this.oreName.equals(((IngredientStack)object).oreName);
		if(this.stackList!=null)
		{
			if(((IngredientStack)object).stack!=null)
			{
				for(ItemStack iStack : this.stackList)
					if(OreDictionary.itemMatches(iStack, ((IngredientStack)object).stack, false))
						return true;
			}
			else
			{
				for(ItemStack iStack : this.stackList)
					for(ItemStack iStack2 : ((IngredientStack)object).stackList)
						if(OreDictionary.itemMatches(iStack, iStack2, false))
							return true;
			}
			return false;
		}
		if(this.stack!=null && ((IngredientStack)object).stack!=null)
		{
			ItemStack otherStack = ((IngredientStack)object).stack;
			if(!OreDictionary.itemMatches(stack,otherStack, false))
				return false;
			if(this.useNBT)
			{
				if(this.stack.hasTagCompound() != otherStack.hasTagCompound())
					return false;
				if(!this.stack.hasTagCompound() && !otherStack.hasTagCompound())
					return true;
				if(!this.stack.getTagCompound().equals(otherStack.getTagCompound()))
					return false;
			}
			return true;
		}
		return false;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		if(this.fluid!=null)
		{
			nbt.setString("fluid", FluidRegistry.getFluidName(fluid));
			nbt.setInteger("fluidAmount", fluid.amount);
			nbt.setInteger("nbtType", 3);
		}
		else if(this.oreName!=null)
		{
			nbt.setString("oreName", oreName);
			nbt.setInteger("nbtType", 2);
		}
		else if(this.stackList!=null)
		{
			NBTTagList list = new NBTTagList();
			for(ItemStack stack : stackList)
				if(stack!=null)
					list.appendTag(stack.writeToNBT(new NBTTagCompound()));
			nbt.setTag("stackList", list);
			nbt.setInteger("nbtType", 1);
		}
		else
		{
			nbt.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
			nbt.setInteger("nbtType", 0);
			nbt.setBoolean("useNBT", useNBT);
		}
		nbt.setInteger("inputSize", inputSize);
		return nbt;
	}
	public static IngredientStack readFromNBT(NBTTagCompound nbt)
	{
		if(nbt.hasKey("nbtType"))
			switch(nbt.getInteger("nbtType"))
			{
				case 0:
					ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
					stack.stackSize = nbt.getInteger("inputSize");
					IngredientStack ingr = new IngredientStack(stack);
					ingr.useNBT = nbt.getBoolean("useNBT");
					return ingr;
				case 1:
					NBTTagList list = nbt.getTagList("stackList", 10);
					List<ItemStack> stackList = new ArrayList();
					for(int i=0; i<list.tagCount(); i++)
						stackList.add(ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
					return new IngredientStack(stackList, nbt.getInteger("inputSize"));
				case 2:
					return new IngredientStack(nbt.getString("oreName"), nbt.getInteger("inputSize"));
				case 3:
					FluidStack fs = new FluidStack(FluidRegistry.getFluid(nbt.getString("fluid")),nbt.getInteger("fluidAmount"));
					return new IngredientStack(fs);
			}
		return null;
	}
}