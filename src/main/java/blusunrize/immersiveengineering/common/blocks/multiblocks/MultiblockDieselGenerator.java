/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockDieselGenerator implements IMultiblock
{
	public static MultiblockDieselGenerator instance = new MultiblockDieselGenerator();
	static ItemStack[][][] structure = new ItemStack[3][5][3];

	static
	{
		for(int h = 0; h < 3; h++)
			for(int l = 0; l < 5; l++)
				for(int w = 0; w < 3; w++)
				{
					if((h==2&&l==0)||(h==2&&(l==1||l==3)&&w!=1))
						continue;
					if(l==4)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta());
					else if(h==0)
					{
						if(l==0&&w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.GENERATOR.getMeta());
						else if(l==0||w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if(h==1)
					{
						if(l==0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.GENERATOR.getMeta());
						else if(l==2&&w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
					}
					else if(h==2)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
				}
	}

	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		if(iterator==0||iterator==2)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("000000");
			return true;
		}
		if(iterator==4)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("000200");
			return true;
		}
		if(iterator==7)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("002200");
			return true;
		}
		if(iterator==10)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("002000");
			return true;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	//@SideOnly(Side.CLIENT)
	static ItemStack renderStack = ItemStack.EMPTY;

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack.isEmpty())
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.DIESEL_GENERATOR.getMeta());
		GlStateManager.translate(2.5, 1.5, 1.5);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(5.5, 5.5, 5.5);

		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:DieselGenerator";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockMetalDecoration0&&(state.getBlock().getMetaFromState(state)==BlockTypes_MetalDecoration0.GENERATOR.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		side = side.getOpposite();
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		boolean mirror = false;
		boolean b = this.structureCheck(world, pos, side, mirror);
		if(!b)
		{
			mirror = true;
			b = structureCheck(world, pos, side, mirror);
		}
		if(!b)
			return false;
		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled())
			return false;

		IBlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.DIESEL_GENERATOR.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, side);
		for(int l = 0; l < 5; l++)
			for(int w = -1; w <= 1; w++)
				for(int h = -1; h <= 1; h++)
				{
					if((l==0&&h==1)||(h==1&&(l==1||l==3)&&w!=0))
						continue;
					int ww = mirror?-w: w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					world.setBlockState(pos2, state);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityDieselGenerator)
					{
						TileEntityDieselGenerator tile = (TileEntityDieselGenerator)curr;
						tile.formed = true;
						tile.pos = (h+1)*15+l*3+(w+1);
						tile.offset = new int[]{(side==EnumFacing.WEST?-l+2: side==EnumFacing.EAST?l-2: side==EnumFacing.NORTH?ww: -ww), h, (side==EnumFacing.NORTH?-l+2: side==EnumFacing.SOUTH?l-2: side==EnumFacing.EAST?ww: -ww)};
						tile.mirrored = mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
					}
				}
		return b;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
		for(int l = 0; l < 5; l++)
			for(int w = -1; w <= 1; w++)
				for(int h = -1; h <= 1; h++)
				{
					if((l==0&&h==1)||(h==1&&(l==1||l==3)&&w!=0))
						continue;
					int ww = mirror?-w: w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);

					if(l==4)
					{
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RADIATOR.getMeta()))
							return false;
					}
					else if(h==-1)
					{
						if(l==0&&w==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.GENERATOR.getMeta()))
								return false;
						}
						else if(l==0||w==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
					}
					else if(h==0)
					{
						if(l==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.GENERATOR.getMeta()))
								return false;
						}
						else if(l==2&&w==1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
								return false;
						}
					}
					else if(h==1)
					{
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
							return false;
					}

				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 6),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 5, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 13, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 4, BlockTypes_MetalDecoration0.GENERATOR.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 9, BlockTypes_MetalDecoration0.RADIATOR.getMeta()))};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}