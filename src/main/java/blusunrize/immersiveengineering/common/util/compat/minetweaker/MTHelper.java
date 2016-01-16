package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import static minetweaker.api.minecraft.MineTweakerMC.getItemStack;
import static minetweaker.api.minecraft.MineTweakerMC.getLiquidStack;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import cpw.mods.fml.relauncher.ReflectionHelper;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.MineTweakerImplementationAPI.ReloadEvent;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.item.IngredientStack;
import minetweaker.api.liquid.ILiquidStack;
import minetweaker.api.oredict.IOreDictEntry;
import minetweaker.util.IEventHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class MTHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

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
		MineTweakerAPI.registerClass(BottlingMachine.class);
		MineTweakerAPI.registerClass(MetalPress.class);
		try{
			MineTweakerImplementationAPI.onPostReload(new ExcavatorEventHandler());
		}catch(Exception e)
		{
			IELogger.error("[CRITICAL] YOU ARE USING AN OUTDATED VERSION OF MINETWEAKER");
			IELogger.error("[CRITICAL] IE requires version 3.0.10b or later to function correctly!");
			IELogger.error("[CRITICAL] The use of an outdated version will cause major issues!!!!");
			e.printStackTrace();
		}
	}

	/** Helper Methods */
	public static ItemStack toStack(IItemStack iStack)
	{
		if(iStack==null)
			return null;
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

	public static class ExcavatorEventHandler implements IEventHandler<ReloadEvent>
	{
		@Override
		public void handle(ReloadEvent event)
		{
			ExcavatorHandler.recalculateChances(false);
			if (ManualHelper.ieManualInstance!=null)
				ManualHelper.ieManualInstance.recalculateAllRecipes();
			TileEntityCrusher.recipeCache.clear();
		}
	}

}
