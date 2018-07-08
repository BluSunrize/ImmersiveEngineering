/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockCrusher implements IMultiblock
{
	public static MultiblockCrusher instance = new MultiblockCrusher();
	static ItemStack[][][] structure = new ItemStack[3][3][5];

	static
	{
		for(int h = 0; h < 3; h++)
			for(int l = 0; l < 3; l++)
				for(int w = 0; w < 5; w++)
				{
					if((w==0&&h==2)||(w==4&&h==2)||(w==4&&l==2&&h > 0))
						continue;
					if(w==0)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					else if(w==4)
					{
						if(h < 1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						else if(h < 2&&l==0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else if(h < 2&&l==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}
					else if(h==0)
					{
						if(w==2&&(l==0||l==1))
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if(h==1)
					{
						if(l==1&&w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
					}
					else if(h==2)
						structure[h][l][w] = new ItemStack(Blocks.HOPPER);
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
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.CRUSHER.getMeta());
		GlStateManager.translate(1.5, 1.5, 2.5);
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
		return "IE:Crusher";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockMetalDecoration1&&(state.getBlock().getMetaFromState(state)==BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		if(side.getAxis()==Axis.Y)
			return false;
		BlockPos startPos = pos;
		side = side.getOpposite();

		if(Utils.isOreBlockAt(world, startPos.add(0, -1, 0), "scaffoldingSteel")
				&&Utils.isBlockAt(world, startPos.offset(side, 2).add(0, -1, 0), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
		{
			startPos = startPos.offset(side, 2);
			side = side.getOpposite();
		}

		boolean mirrored = false;
		boolean b = structureCheck(world, startPos, side, mirrored);
		if(!b)
		{
			mirrored = true;
			b = structureCheck(world, startPos, side, mirrored);
		}

		if(b)
		{
			IBlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.CRUSHER.getMeta());
			state = state.withProperty(IEProperties.FACING_HORIZONTAL, side);
			for(int l = 0; l < 3; l++)
				for(int w = -2; w <= 2; w++)
					for(int h = -1; h <= 1; h++)
					{
						if((w==-2&&h==1)||(w==2&&h==1)||(w==2&&l==2&&h > -1))
							continue;
						int ww = mirrored?-w: w;
						BlockPos pos2 = startPos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

						world.setBlockState(pos2, state);
						TileEntity curr = world.getTileEntity(pos2);
						if(curr instanceof TileEntityCrusher)
						{
							TileEntityCrusher tile = (TileEntityCrusher)curr;
							tile.formed = true;
							tile.pos = (h+1)*15+l*5+(w+2);
							tile.offset = new int[]{(side==EnumFacing.WEST?-l+1: side==EnumFacing.EAST?l-1: side==EnumFacing.NORTH?ww: -ww), h, (side==EnumFacing.NORTH?-l+1: side==EnumFacing.SOUTH?l-1: side==EnumFacing.EAST?ww: -ww)};
							tile.mirrored = mirrored;
							tile.markDirty();
							world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
						}
					}
		}
		return b;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
		for(int l = 0; l < 3; l++)
			for(int w = -2; w <= 2; w++)
				for(int h = -1; h <= 1; h++)
				{
					if((w==-2&&h==1)||(w==2&&h==1)||(w==2&&l==2&&h > -1))
						continue;
					int ww = mirror?-w: w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);

					if(w==-2)
					{
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
							return false;
					}
					else if(w==2&&h==0)
					{
						if(l==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
					}
					else if(h==-1)
					{
						if(w==0&&l < 2)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
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
						if(w==0&&l==1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isOreBlockAt(world, pos, "fenceSteel"))
								return false;
						}
					}
					else if(h==1)
					{
						if(!Utils.isBlockAt(world, pos, Blocks.HOPPER, -1))
							return false;
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 10),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 10, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack("fenceSteel", 8),
			new IngredientStack(new ItemStack(Blocks.HOPPER, 9))};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}