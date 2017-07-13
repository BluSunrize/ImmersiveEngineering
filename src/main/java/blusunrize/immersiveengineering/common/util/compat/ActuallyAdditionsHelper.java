package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ActuallyAdditionsHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		Fluid canolaOil = FluidRegistry.getFluid("canolaoil");
		if(canolaOil!=null)
			SqueezerRecipe.addRecipe(new FluidStack(canolaOil,80), null, "cropCanola", 6400);
	}

	@Override
	public void postInit()
	{
		Fluid fluidEthanol = FluidRegistry.getFluid("ethanol");
		Fluid fluidBiodiesel = FluidRegistry.getFluid("biodiesel");

		Item coffeeSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:item_coffee_seed"));
		Item coffeeBeans = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:item_coffee_beans"));
		Block coffeeBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions:block_coffee"));
		if(coffeeSeeds!=null && coffeeBeans!=null && coffeeBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(coffeeSeeds), new ItemStack[]{new ItemStack(coffeeBeans,3), new ItemStack(coffeeSeeds)}, new ItemStack(Blocks.DIRT), coffeeBlock.getDefaultState());

		Item riceSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:item_rice_seed"));
		Item food = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:item_food"));
		Block riceBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions:block_rice"));
		if(riceSeeds!=null && food!=null && riceBlock!=null) {
			BelljarHandler.cropHandler.register(new ItemStack(riceSeeds), new ItemStack[]{new ItemStack(food,2,16), new ItemStack(riceSeeds)}, new ItemStack(Blocks.DIRT), riceBlock.getDefaultState());
			FermenterRecipe.addRecipe(new FluidStack(fluidEthanol,40), ItemStack.EMPTY, new ItemStack(food, 1, 16), 6400);
		}

		Item canolaSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:item_canola_seed"));
		Item misc = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:item_misc"));
		Block canolaBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions:block_canola"));
		if(canolaSeeds!=null && misc!=null && canolaBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(canolaSeeds), new ItemStack[]{new ItemStack(misc,3,13), new ItemStack(canolaSeeds)}, new ItemStack(Blocks.DIRT), canolaBlock.getDefaultState());

		Item flaxSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:item_flax_seed"));
		Block flaxBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions:block_flax"));
		if(flaxSeeds!=null && flaxBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(flaxSeeds), new ItemStack[]{new ItemStack(Items.STRING,4), new ItemStack(flaxSeeds)}, new ItemStack(Blocks.DIRT), flaxBlock.getDefaultState());
		
		Fluid canolaOil = FluidRegistry.getFluid("canolaoil");
		if(canolaOil!=null) {
			RefineryRecipe.addRecipe(new FluidStack(fluidBiodiesel,16), new FluidStack(canolaOil,8),new FluidStack(fluidEthanol,8), 80);
		}
		
		Fluid crystaloil = FluidRegistry.getFluid("crystaloil");
		if( crystaloil != null){
			
		}
		
		
		//Add whatever fuels AA burns to the Diesel Generator
		for (de.ellpeck.actuallyadditions.api.recipe.OilGenRecipe recipe 
			: de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI.OIL_GENERATOR_RECIPES)
		{
			IELogger.debug("Found "+ recipe.fluidName +" fuel from AA.");
			// AA burns oil in batches of 50mb.
			// We want power for 1 bucket. 1000mb/50mb = 20
			int totalPower = recipe.genAmount*recipe.genTime*20;
			
			// Diesel Generator always generates 4096/t
			Fluid fuel = FluidRegistry.getFluid(recipe.fluidName);
			DieselHandler.registerFuel(fuel, totalPower/4096);
			
			// If it's at least as energy dense as BioDiesel it should work in the drill.
			if(totalPower > 512000){
				DieselHandler.registerDrillFuel(fuel);
			}
			
			// Have it work as flammable in the chemthrower with duration relative to it's genTime.
			// and amplifier relative to it's genAmount. 
			ChemthrowerHandler.registerEffect(fuel, new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,recipe.genTime,recipe.genAmount/100));
			ChemthrowerHandler.registerFlammable(fuel);
			
		}
		
	}
	
			
	
}