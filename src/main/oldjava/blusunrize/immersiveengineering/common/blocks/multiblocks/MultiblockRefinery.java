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
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import java.util.List;

public class MultiblockRefinery extends TemplateMultiblock
{
	public static MultiblockRefinery instance = new MultiblockRefinery();

	static ItemStack[][][] structure = new ItemStack[3][3][5];

	static
	{
		for(int h = 0; h < 3; h++)
			for(int l = 0; l < 3; l++)
				for(int w = 0; w < 5; w++)
					if(h==0)
					{
						if((l==0||l==2)&&w!=2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						else if(l==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
						else if(l==0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						else if(l==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}
					else if(h==1)
					{
						if(l==0&&w==4)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else if(l==0&&w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						else if(l==2&&w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else if(l > 0&&w!=2)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, EnumMetals.IRON.getMeta());
					}
					else if(h==2)
					{
						if(l > 0&&w!=2)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, EnumMetals.IRON.getMeta());
					}
	}

	@Override
	public List<BlockInfo> getStructureManual()
	{
		return structure;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		if(iterator==5)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("000020");
			return true;
		}
		else if(iterator==9)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("000002");
			return true;
		}
		else if(iterator > 5&&iterator < 9)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("000022");
			return true;
		}
		return false;
	}

	@Override
	public BlockState getBlockstateFromStack(int index, ItemStack stack)
	{
		if(!stack.isEmpty()&&stack.getItem() instanceof BlockItem)
			return ((BlockItem)stack.getItem()).getBlock().getStateFromMeta(stack.getItemDamage());
		return null;
	}

	@Override
	public float getManualScale()
	{
		return 13;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	//@OnlyIn(Dist.CLIENT)
	static ItemStack renderStack = ItemStack.EMPTY;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack.isEmpty())
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.REFINERY.getMeta());
		GlStateManager.translate(1, 1.5, 3);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(5.5, 5.5, 5.5);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return "IE:Refinery";
	}

	@Override
	public boolean isBlockTrigger(BlockState state)
	{
		return state.getBlock()==IEContent.blockMetalDecoration0&&(state.getBlock().getMetaFromState(state)==BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		side = side.getOpposite();
		if(side==Direction.UP||side==Direction.DOWN)
			side = Direction.fromAngle(player.rotationYaw);

		boolean mirror = false;
		boolean b = this.structureCheck(world, pos, side, mirror);
		if(!b)
		{
			mirror = true;
			b = structureCheck(world, pos, side, mirror);
		}
		if(!b)
			return false;

		BlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.REFINERY.getMeta());
		state = state.with(IEProperties.FACING_HORIZONTAL, side);
		for(int h = -1; h <= 1; h++)
			for(int l = 0; l <= 2; l++)
				for(int w = -2; w <= 2; w++)
				{
					if(h==0)
					{
						if(l==0&&(w < 0||w==1))
							continue;
						else if(l==1&&w==0)
							continue;
					}
					if(h==1)
					{
						if(l==0)
							continue;
						else if(w==0)
							continue;
					}
					int ww = mirror?-w: w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					world.setBlockState(pos2, state);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof RefineryTileEntity)
					{
						RefineryTileEntity tile = (RefineryTileEntity)curr;
						tile.formed = true;
						tile.pos = (h+1)*15+l*5+(w+2);
						tile.offset = new int[]{(side==Direction.WEST?-l: side==Direction.EAST?l: side==Direction.NORTH?ww: -ww), h, (side==Direction.NORTH?-l: side==Direction.SOUTH?l: side==Direction.EAST?ww: -ww)};
						tile.mirrored = mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;
	}

	boolean structureCheck(World world, BlockPos startPos, Direction dir, boolean mirror)
	{
		for(int h = -1; h <= 1; h++)
			for(int l = 0; l <= 2; l++)
				for(int w = -2; w <= 2; w++)
				{
					if((h==0&&l==0&&w!=0&&w!=2)||(h==0&&l==0&&w==0)||(h==1&&(l==0||w==0)))
						continue;

					int ww = mirror?-w: w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);

					if(h==-1)
					{
						if(l==1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
								return false;
						}
						else if(l==0&&w==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
								return false;
						}
						else if(l==2&&w==0)
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
						if(l==0&&w==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
								return false;
						}
						if(l==2&&w==0)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
						else if(l==0&&w==2)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
								return false;
						}
						else if(l > 0&&w!=0)
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalIron"))
								return false;
						}
					}
					else if(h==1)
					{
						if(l > 0&&w!=0)
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalIron"))
								return false;
						}
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 8),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 5, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack("blockSheetmetalIron", 16)};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}