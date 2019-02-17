/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ForestryHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
		Fluid fluidHoney = FluidRegistry.getFluid("for.honey");
		if(fluidHoney!=null)
		{
			SqueezerRecipe.addRecipe(new FluidStack(fluidHoney, 100), ItemStack.EMPTY, "dropHoney", 6400);
			SqueezerRecipe.addRecipe(new FluidStack(fluidHoney, 100), ItemStack.EMPTY, "dropHoneydew", 6400);
		}
	}

	@Override
	public void init()
	{
		FMLInterModComms.sendMessage("forestry", "add-backpack-items", "forestry.forester@"+ImmersiveEngineering.MODID+":seed:*");
	}

	@Override
	public void postInit()
	{
		ChemthrowerHandler.registerFlammable("bio.ethanol");
		ChemthrowerHandler.registerEffect("for.honey", new ChemthrowerEffect_Potion(null, 0, IEPotions.sticky, 60, 1));
		ChemthrowerHandler.registerEffect("juice", new ChemthrowerEffect_Potion(null, 0, IEPotions.sticky, 40, 0));
		Item fertilizer = Item.REGISTRY.getObject(new ResourceLocation("forestry", "fertilizer_compound"));
		if(fertilizer!=null)
			BelljarHandler.registerBasicItemFertilizer(new ItemStack(fertilizer), 1.5f);
	}
}