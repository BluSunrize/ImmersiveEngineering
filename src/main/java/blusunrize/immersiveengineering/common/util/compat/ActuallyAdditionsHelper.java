/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.common.IEContent;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.farmer.FarmerResult;
import de.ellpeck.actuallyadditions.api.farmer.IFarmerBehavior;
import de.ellpeck.actuallyadditions.api.internal.IFarmer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
	public void registerRecipes()
	{
		Fluid canolaOil = FluidRegistry.getFluid("canolaoil");
		if(canolaOil!=null)
			SqueezerRecipe.addRecipe(new FluidStack(canolaOil, 80), ItemStack.EMPTY, "cropCanola", 6400);
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
		Item coffeeSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "item_coffee_seed"));
		Item coffeeBeans = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "item_coffee_beans"));
		Block coffeeBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "block_coffee"));
		if(coffeeSeeds!=null&&coffeeBeans!=null&&coffeeBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(coffeeSeeds), new ItemStack[]{new ItemStack(coffeeBeans, 3), new ItemStack(coffeeSeeds)}, new ItemStack(Blocks.DIRT), coffeeBlock.getDefaultState());

		Item riceSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "item_rice_seed"));
		Item food = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "item_food"));
		Block riceBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "block_rice"));
		if(riceSeeds!=null&&food!=null&&riceBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(riceSeeds), new ItemStack[]{new ItemStack(food, 2, 16), new ItemStack(riceSeeds)}, new ItemStack(Blocks.DIRT), riceBlock.getDefaultState());

		Item canolaSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "item_canola_seed"));
		Item misc = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "item_misc"));
		Block canolaBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "block_canola"));
		if(canolaSeeds!=null&&misc!=null&&canolaBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(canolaSeeds), new ItemStack[]{new ItemStack(misc, 3, 13), new ItemStack(canolaSeeds)}, new ItemStack(Blocks.DIRT), canolaBlock.getDefaultState());

		Item flaxSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "item_flax_seed"));
		Block flaxBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "block_flax"));
		if(flaxSeeds!=null&&flaxBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(flaxSeeds), new ItemStack[]{new ItemStack(Items.STRING, 4), new ItemStack(flaxSeeds)}, new ItemStack(Blocks.DIRT), flaxBlock.getDefaultState());

		Item fertilizer = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions", "item_fertilizer"));
		if(fertilizer!=null)
			BelljarHandler.registerBasicItemFertilizer(new ItemStack(fertilizer), 1.25f);

		ActuallyAdditionsAPI.addFarmerBehavior(new HempFarmBehavior());
	}

	private static class HempFarmBehavior implements IFarmerBehavior
	{
		@Override
		public FarmerResult tryPlantSeed(ItemStack seed, World world, BlockPos pos, IFarmer farmer)
		{
			int use = 250;
			if(farmer.getEnergy() >= use&&seed.getItem()==IEContent.itemSeeds)
			{
				if(IEContent.blockCrop.canPlaceBlockAt(world, pos))
				{
					world.setBlockState(pos, IEContent.blockCrop.getDefaultState(), 2);
					farmer.extractEnergy(use);
					return FarmerResult.SUCCESS;
				}
				return FarmerResult.STOP_PROCESSING;
			}
			return FarmerResult.FAIL;
		}

		@Override
		public FarmerResult tryHarvestPlant(World world, BlockPos pos, IFarmer farmer)
		{
			int use = 250;
			if(farmer.getEnergy() >= use)
			{
				IBlockState state = world.getBlockState(pos);
				BlockPos up = pos.up();
				IBlockState stateUp = world.getBlockState(up);
				if(IEContent.blockCrop==state.getBlock()&&state.getBlock().getMetaFromState(state)==4&&stateUp.getBlock()==IEContent.blockCrop)//Fully Grown
				{
					NonNullList<ItemStack> drops = NonNullList.create();
					stateUp.getBlock().getDrops(drops, world, up, stateUp, 0);
					NonNullList<ItemStack> seeds = NonNullList.create();
					for(ItemStack stack : drops)
						if(stack.getItem()==IEContent.itemSeeds)
						{
							seeds.add(new ItemStack(IEContent.itemSeeds));
							stack.shrink(1);
						}
					
					if(!seeds.isEmpty()&&farmer.canAddToSeeds(seeds))
					{
						farmer.addToSeeds(seeds);
						seeds.clear();
					}

					if(!seeds.isEmpty()) drops.addAll(seeds);

					if(!drops.isEmpty()&&farmer.canAddToOutput(drops))
					{
						if(IEContent.blockCrop==stateUp.getBlock())
						{
							world.playEvent(2001, up, Block.getStateId(state));
							world.setBlockToAir(up);
						}
						farmer.extractEnergy(use);
						farmer.addToOutput(drops);
					}
					return FarmerResult.SUCCESS;
				}
			}
			return FarmerResult.FAIL;
		}

		//I don't know what this does, but ExtraUtils had 10 and I want to be as popular as Tema
		@Override
		public int getPriority()
		{
			return 10;
		}
	}
}