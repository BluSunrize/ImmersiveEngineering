package blusunrize.immersiveengineering.common.minetweaker;

import static minetweaker.api.minecraft.MineTweakerMC.getItemStack;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.item.IngredientStack;
import minetweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class IEMinetweaker
{
	public static void init()
	{
		MineTweakerAPI.registerClass(BlastFurnace.class);
		MineTweakerAPI.registerClass(CokeOven.class);
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
}
