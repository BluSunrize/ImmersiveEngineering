package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import static minetweaker.api.minecraft.MineTweakerMC.getItemStack;
import static minetweaker.api.minecraft.MineTweakerMC.getLiquidStack;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.item.IngredientStack;
import minetweaker.api.liquid.ILiquidStack;
import minetweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class MTHelper extends IECompatModule
{
	@Override
	public void init()
	{
	}
	
	@Override
	public void postInit()
	{
		MineTweakerAPI.registerClass(BlastFurnace.class);
		MineTweakerAPI.registerClass(CokeOven.class);
		MineTweakerAPI.registerClass(Crusher.class);
		MineTweakerAPI.registerClass(Squeezer.class);
		MineTweakerAPI.registerClass(Fermenter.class);
		MineTweakerAPI.registerClass(Refinery.class);
		MineTweakerAPI.registerClass(ArcFurnace.class);
		MineTweakerAPI.registerClass(Excavator.class);
		MineTweakerAPI.registerClass(Excavator.MTMineralMix.class);
	}

	/** Helper Methods */
	public static ItemStack toStack(IItemStack iStack)
	{
		return getItemStack(iStack);
	}
	public static Object toObject(IIngredient iStack)
	{
		if (iStack == null)
			return null;
		else
		{
			if(iStack instanceof IOreDictEntry)
				return ((IOreDictEntry)iStack).getName();
			else if(iStack instanceof IItemStack)
				return getItemStack((IItemStack) iStack);
			else if(iStack instanceof IngredientStack)
			{
				IIngredient ingr = ReflectionHelper.getPrivateValue(IngredientStack.class, (IngredientStack)iStack, "ingredient");
				return toObject(ingr);
			}
			else
				return null;
		}
	}
	public static Object[] toObjects(IIngredient[] iStacks)
	{
		Object[] oA = new Object[iStacks.length];
		for(int i=0; i<iStacks.length; i++)
			oA[i] = toObject(iStacks[i]);
		return oA;
	}
	
	public static FluidStack toFluidStack(ILiquidStack iStack)
	{
		return getLiquidStack(iStack);
	}
}
