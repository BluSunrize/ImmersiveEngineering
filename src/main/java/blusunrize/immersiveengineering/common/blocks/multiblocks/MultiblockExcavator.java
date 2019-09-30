/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
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

public class MultiblockExcavator implements IMultiblock
{
	public static MultiblockExcavator instance = new MultiblockExcavator();
	static ItemStack[][][] structure = new ItemStack[3][6][3];

	static
	{
		for(int h = 0; h < 3; h++)
			for(int l = 0; l < 6; l++)
				for(int w = 0; w < 3; w++)
				{
					if(l > 0&&w==1)
						continue;
					if(l==0)
					{
						if(w==0&&h==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else if((w==1&&h==1)||(w==0&&h==2))
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						else if(w==0&&h==0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
					}
					else if(w==0)
					{
						if(l < 3&&h==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta());
						else if(l < 3)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
						else if(h==0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}
					else if(w==2)
					{
						if(l==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else if(l==2)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
						else if(h==0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						else if(h==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
					}
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
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.EXCAVATOR.getMeta());
		GlStateManager.translate(2, 1.5, 2.875);
		GlStateManager.rotate(-225, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(5.25, 5.25, 5.25);

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
		return "IE:Excavator";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockMetalDecoration0&&(state.getBlock().getMetaFromState(state)==BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
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

		IBlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.EXCAVATOR.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, side);
		for(int l = 0; l < 6; l++)
			for(int w = -1; w <= 1; w++)
				for(int h = -1; h <= 1; h++)
				{
					if(l > 0&&w==0)
						continue;
					int ww = mirror?-w: w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					world.setBlockState(pos2, state);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityExcavator)
					{
						TileEntityExcavator tile = (TileEntityExcavator)curr;
						tile.formed = true;
						tile.pos = (h+1)*18+l*3+(w+1);
						tile.offset = new int[]{(side==EnumFacing.WEST?-l: side==EnumFacing.EAST?l: side==EnumFacing.NORTH?ww: -ww), h, (side==EnumFacing.NORTH?-l: side==EnumFacing.SOUTH?l: side==EnumFacing.EAST?ww: -ww)};
						tile.mirrored = mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
					}
				}

		BlockPos wheelPos = pos.offset(side, 4);
		if(MultiblockBucketWheel.instance.isBlockTrigger(world.getBlockState(wheelPos)))
			MultiblockBucketWheel.instance.createStructure(world, wheelPos, side.rotateYCCW(), player);
		return true;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
		for(int l = 0; l < 6; l++)
			for(int w = -1; w <= 1; w++)
				for(int h = -1; h <= 1; h++)
				{
					if(l > 0&&w==0)
						continue;

					int ww = mirror?-w: w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);

					if(l==0)
					{
						if(w==-1&&h==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
								return false;
						}
						else if((w==0&&h==0)||(w==-1&&h==1))
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
								return false;
						}
						else if(w==-1&&h==-1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RADIATOR.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel"))
								return false;
						}
					}
					else if(w==-1)
					{
						if(l < 3&&h==1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RADIATOR.getMeta()))
								return false;
						}
						else if(l < 3)
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel"))
								return false;
						}
						else if(h==-1)
						{
							if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
						else
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
					}
					else if(w==1)
					{
						if(l==1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
						else if(l==2)
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel"))
								return false;
						}
						else if(h==-1)
						{
							if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
						else if(h==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel"))
								return false;
						}
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 6),
			new IngredientStack("blockSheetmetalSteel", 15),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 9, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 5, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 3, BlockTypes_MetalDecoration0.RADIATOR.getMeta()))};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}