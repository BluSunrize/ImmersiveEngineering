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
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.*;
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

public class MultiblockBottlingMachine implements IMultiblock
{
	public static MultiblockBottlingMachine instance = new MultiblockBottlingMachine();
	static ItemStack[][][] structure = new ItemStack[3][2][3];

	static
	{
		for(int h = 0; h < 3; h++)
			for(int l = 0; l < 2; l++)
				for(int w = 0; w < 3; w++)
				{
					if(h==0)
					{
						if(l==0&&w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else if(l==1&&w==0)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
						else if(l==1&&w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if(h==1)
					{
						if(l==0)
							structure[h][l][w] = ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor");
						else if(l==1&&w==0)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
						else if(l==1&&w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.FLUID_PUMP.getMeta());
					}
					else if(h==2)
					{
						if(l==0&&w==1)
							structure[h][l][w] = new ItemStack(Blocks.GLASS);
						else if(l==1&&w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.FLUID_PUMP.getMeta());
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
		if(iterator==13)
		{
			ImmersiveEngineering.proxy.drawFluidPumpTop();
			return true;
		}
		if(iterator >= 6&&iterator <= 8)
			return ImmersiveEngineering.proxy.drawConveyorInGui("immersiveengineering:conveyor", EnumFacing.SOUTH);
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
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.BOTTLING_MACHINE.getMeta());
		GlStateManager.translate(2.1875, 1.125, .8125);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(4, 4, 4);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public float getManualScale()
	{
		return 15;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:BottlingMachine";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockConveyor;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		if(side.getAxis()==Axis.Y)
			side = EnumFacing.fromAngle(player.rotationYaw);
		else
			side = side.getOpposite();


		boolean mirrored = false;
		boolean b = structureCheck(world, pos, side, mirrored);
		if(!b)
		{
			mirrored = true;
			b = structureCheck(world, pos, side, mirrored);
		}
		if(!b)
			return false;
		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled())
			return false;

		IBlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.BOTTLING_MACHINE.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, side);
		for(int l = 0; l < 2; l++)
			for(int w = -1; w <= 1; w++)
				for(int h = -1; h <= 1; h++)
					if(h < 1||w==0)
					{
						int ww = mirrored?-w: w;
						BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

						world.setBlockState(pos2, state);
						TileEntity curr = world.getTileEntity(pos2);
						if(curr instanceof TileEntityBottlingMachine)
						{
							TileEntityBottlingMachine tile = (TileEntityBottlingMachine)curr;
							tile.formed = true;
							tile.pos = (h+1)*6+l*3+(w+1);
							tile.offset = new int[]{(side==EnumFacing.WEST?1-l: side==EnumFacing.EAST?l-1: side==EnumFacing.NORTH?ww: -ww), h, (side==EnumFacing.NORTH?1-l: side==EnumFacing.SOUTH?l-1: side==EnumFacing.EAST?ww: -ww)};
							tile.mirrored = mirrored;
							tile.markDirty();
							world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
						}
					}
		return true;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
		EnumFacing conveyorDir = mirror?dir.rotateYCCW(): dir.rotateY();
		for(int l = 0; l < 2; l++)
			for(int h = -1; h <= 1; h++)
				for(int w = -1; w <= 1; w++)
				{
					BlockPos pos2 = startPos.offset(dir, l).offset(conveyorDir, w).add(0, h, 0);


					if(h==-1)
					{
						if(l==0&&w==0)
						{
							if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
								return false;
						}
						else if(w==-1&&l==1)
						{
							if(!Utils.isOreBlockAt(world, pos2, "blockSheetmetalIron"))
								return false;
						}
						else if(w==1&&l==1)
						{
							if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isOreBlockAt(world, pos2, "scaffoldingSteel"))
								return false;
						}
					}
					else if(h==0)
					{
						if(l==0)
						{
							if(!ConveyorHandler.isConveyor(world, pos2, ImmersiveEngineering.MODID+":conveyor", conveyorDir))
								return false;
						}
						else if(w==-1&&l==1)
						{
							if(!Utils.isOreBlockAt(world, pos2, "blockSheetmetalIron"))
								return false;
						}
						else if(w==1&&l==1)
						{
							if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDevice0, BlockTypes_MetalDevice0.FLUID_PUMP.getMeta()))
								return false;
						}
					}
					else if(h==1&&w==0&&l==0)
						if(!Utils.isOreBlockAt(world, pos2, "blockGlass"))
							return false;

				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 3),
			new IngredientStack("blockSheetmetalIron", 2),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack(Utils.copyStackWithAmount(ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor"), 3)),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.FLUID_PUMP.getMeta())),
			new IngredientStack("blockGlass", 1)};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}