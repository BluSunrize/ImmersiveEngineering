package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
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

		Item coffeeSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:itemCoffeeSeed"));
		Item coffeeBeans = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:itemCoffeeBeans"));
		Block coffeeBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions:blockCoffee"));
		if(coffeeSeeds!=null && coffeeBeans!=null && coffeeBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(coffeeSeeds), new ItemStack[]{new ItemStack(coffeeBeans,3), new ItemStack(coffeeSeeds)}, new ItemStack(Blocks.DIRT), coffeeBlock.getDefaultState());

		Item riceSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:itemRiceSeed"));
		Item food = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:itemFood"));
		Block riceBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions:blockRice"));
		if(riceSeeds!=null && food!=null && riceBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(riceSeeds), new ItemStack[]{new ItemStack(food,2,16), new ItemStack(riceSeeds)}, new ItemStack(Blocks.DIRT), riceBlock.getDefaultState());

		Item canolaSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:itemCanolaSeed"));
		Item misc = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:itemMisc"));
		Block canolaBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions:blockCanola"));
		if(canolaSeeds!=null && misc!=null && canolaBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(canolaSeeds), new ItemStack[]{new ItemStack(misc,3,13), new ItemStack(canolaSeeds)}, new ItemStack(Blocks.DIRT), canolaBlock.getDefaultState());

		Item flaxSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:itemFlaxSeed"));
		Block flaxBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions:blockFlax"));
		if(flaxSeeds!=null && flaxBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(flaxSeeds), new ItemStack[]{new ItemStack(Items.STRING,4), new ItemStack(flaxSeeds)}, new ItemStack(Blocks.DIRT), flaxBlock.getDefaultState());
	}
}