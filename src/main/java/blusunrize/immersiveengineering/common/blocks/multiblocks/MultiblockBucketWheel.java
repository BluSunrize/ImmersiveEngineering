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
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
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

public class MultiblockBucketWheel implements IMultiblock
{
	public static MultiblockBucketWheel instance = new MultiblockBucketWheel();
	static ItemStack[][][] structure = new ItemStack[7][7][1];

	static
	{
		for(int h = 0; h < 7; h++)
			for(int l = 0; l < 7; l++)
			{
				if((h==0||h==6)&&l!=3)
					continue;
				if((l==0||l==6)&&h!=3)
					continue;
				if(l==0||h==0||l==6||h==6||((l==1||l==5)&&(h==1||h==5))||(h==3&&l==3))
					structure[h][l][0] = new ItemStack(IEContent.blockStorage, 1, BlockTypes_MetalsIE.STEEL.getMeta());
				else
					structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
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
	public float getManualScale()
	{
		return 12;
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
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.BUCKET_WHEEL.getMeta());
		GlStateManager.translate(3.5, 3.5, 0.5);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(6.875, 6.875, 6.875);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public String getUniqueName()
	{
		return "IE:BucketWheel";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return Utils.compareToOreName(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), "blockSteel");
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		for(int h = -3; h <= 3; h++)
			for(int w = -3; w <= 3; w++)
			{
				BlockPos pos2 = pos.add((side==EnumFacing.NORTH?w: side==EnumFacing.SOUTH?-w: 0), h, (side==EnumFacing.WEST?w: side==EnumFacing.EAST?-w: 0));

				if((h==-3||h==3)&&w!=0)
					continue;
				if((w==-3||w==3)&&h!=0)
					continue;
				if(w==-3||h==-3||w==3||h==3||((w==-2||w==2)&&(h==-2||h==2))||(h==0&&w==0))
				{
					if(!Utils.isOreBlockAt(world, pos2, "blockSteel"))
						return false;
				}
				else
				{
					if(!Utils.isOreBlockAt(world, pos2, "scaffoldingSteel"))
						return false;
				}
			}
		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled())
			return false;

		IBlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.BUCKET_WHEEL.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, side);
		for(int h = -3; h <= 3; h++)
			for(int w = -3; w <= 3; w++)
			{
				BlockPos pos2 = pos.add((side==EnumFacing.NORTH?w: side==EnumFacing.SOUTH?-w: 0), h, (side==EnumFacing.WEST?w: side==EnumFacing.EAST?-w: 0));

				if((h==-3||h==3)&&w!=0)
					continue;
				if((w==-3||w==3)&&h!=0)
					continue;

				world.setBlockState(pos2, state);
				TileEntity curr = world.getTileEntity(pos2);
				if(curr instanceof TileEntityBucketWheel)
				{
					TileEntityBucketWheel tile = (TileEntityBucketWheel)curr;
					tile.formed = true;
					tile.pos = (w+3)+(h+3)*7;

					tile.offset = new int[]{(side==EnumFacing.NORTH?w: side==EnumFacing.SOUTH?-w: 0), h, (side==EnumFacing.WEST?w: side==EnumFacing.EAST?-w: 0)};
					tile.markDirty();
					world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
				}
			}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("blockSteel", 9),
			new IngredientStack("scaffoldingSteel", 20)};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}