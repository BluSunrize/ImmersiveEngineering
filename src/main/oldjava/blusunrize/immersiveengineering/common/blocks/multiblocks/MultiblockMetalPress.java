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
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import java.util.List;

public class MultiblockMetalPress extends TemplateMultiblock
{
	public static MultiblockMetalPress instance = new MultiblockMetalPress();

	static ItemStack[][][] structure = new ItemStack[3][3][1];

	static
	{
		for(int h = 0; h < 3; h++)
			for(int l = 0; l < 3; l++)
				if(h==0)
				{
					if(l==1)
						structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
					else
						structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
				}
				else if(h==1)
				{
					if(l==1)
						structure[h][l][0] = new ItemStack(Blocks.PISTON, 1, 0);
					else
						structure[h][l][0] = ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor");
				}
				else if(h==2&&l==1)
					structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
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
		if(iterator==3||iterator==5)
			return ImmersiveEngineering.proxy.drawConveyorInGui("immersiveengineering:conveyor", Direction.EAST);
		return false;
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
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.METAL_PRESS.getMeta());
		GlStateManager.scale(4, 4, 4);
		GlStateManager.translate(.375, .375, .125f);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return "IE:MetalPress";
	}

	@Override
	public boolean isBlockTrigger(BlockState state)
	{
		return state.getBlock()==Blocks.PISTON&&(state.getBlock().getMetaFromState(state)==0);
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		if(side==Direction.UP||side==Direction.DOWN)
			side = Direction.fromAngle(player.rotationYaw);

		Direction dir = side.rotateY();
		if(world.getTileEntity(pos.offset(dir)) instanceof ConveyorBeltTileEntity)
			dir = ((ConveyorBeltTileEntity)world.getTileEntity(pos.offset(dir))).getFacing();

		for(int l = -1; l <= 1; l++)
			for(int h = -1; h <= 1; h++)
			{
				if(h==1&&l!=0)
					continue;
				BlockPos pos2 = pos.offset(dir, l).add(0, h, 0);

				if(h==-1)
				{
					if(l==0)
					{
						if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
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
						if(!Utils.isBlockAt(world, pos2, Blocks.PISTON, 0))
							return false;
					}
					else
					{
						if(!ConveyorHandler.isConveyor(world, pos2, ImmersiveEngineering.MODID+":conveyor", dir))
							return false;
					}
				}
				else
				{
					if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
						return false;
				}
			}
		BlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.METAL_PRESS.getMeta());
		state = state.with(IEProperties.FACING_HORIZONTAL, dir);
		for(int l = -1; l <= 1; l++)
			for(int h = -1; h <= 1; h++)
			{
				if(h==1&&l!=0)
					continue;
				BlockPos pos2 = pos.offset(dir, l).add(0, h, 0);
				world.setBlockState(pos2, state);
				TileEntity curr = world.getTileEntity(pos2);
				if(curr instanceof MetalPressTileEntity)
				{
					MetalPressTileEntity tile = (MetalPressTileEntity)curr;
					tile.formed = true;
					tile.pos = (h+1)*3+(l+1);
					tile.offset = new int[]{(dir==Direction.WEST?-l: dir==Direction.EAST?l: 0), h, (dir==Direction.NORTH?-l: dir==Direction.SOUTH?l: 0)};
					tile.markDirty();
					world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
				}
			}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 2),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(Blocks.PISTON)),
			new IngredientStack(Utils.copyStackWithAmount(ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor"), 2)),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}