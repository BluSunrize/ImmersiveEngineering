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
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySilo;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
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

public class MultiblockSilo implements IMultiblock
{
	public static MultiblockSilo instance = new MultiblockSilo();

	static ItemStack[][][] structure = new ItemStack[7][3][3];

	static
	{
		for(int h = 0; h < 7; h++)
			for(int l = 0; l < 3; l++)
				for(int w = 0; w < 3; w++)
				{
					if(h==0)
					{
						if((l==0||l==2)&&(w==0||w==2))
							structure[h][l][w] = new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta());
						else if(l==1&&w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
					}
					else if(h < 1||h > 5||w!=1||l!=1)
						structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
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
		return 10;
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
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.SILO.getMeta());
		GlStateManager.translate(2, 2.5, 1);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(8, 8, 8);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

	@Override
	public String getUniqueName()
	{
		return "IE:Silo";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return Utils.compareToOreName(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), "blockSheetmetalIron");
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		EnumFacing f = EnumFacing.fromAngle(player.rotationYaw);
		pos = pos.offset(f);
		if(!(Utils.isOreBlockAt(world, pos.offset(f, -1).offset(f.rotateY()), "fenceTreatedWood")&&Utils.isOreBlockAt(world, pos.offset(f, -1).offset(f.rotateYCCW()), "fenceTreatedWood")))
			for(int i = 0; i < 6; i++)
				if(Utils.isOreBlockAt(world, pos.add(0, -i, 0).offset(f, -1).offset(f.rotateY()), "fenceTreatedWood")&&Utils.isOreBlockAt(world, pos.add(0, -i, 0).offset(f, -1).offset(f.rotateYCCW()), "fenceTreatedWood"))
				{
					pos = pos.add(0, -i, 0);
					break;
				}

		for(int h = 0; h <= 6; h++)
			for(int xx = -1; xx <= 1; xx++)
				for(int zz = -1; zz <= 1; zz++)
					if(h==0)
					{
						if(Math.abs(xx)==1&&Math.abs(zz)==1)
						{
							if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "fenceTreatedWood"))
								return false;
						}
						else if(xx==0&&zz==0)
							if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "blockSheetmetalIron"))
								return false;
					}
					else
					{
						if(h < 6&&xx==0&&zz==0)
						{
							if(!world.isAirBlock(pos.add(xx, h, zz)))
								return false;
						}
						else if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "blockSheetmetalIron"))
							return false;
					}
		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled())
			return false;

		IBlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.SILO.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, f.getOpposite());
		for(int h = 0; h <= 6; h++)
			for(int l = -1; l <= 1; l++)
				for(int w = -1; w <= 1; w++)
				{
					if(h==0&&!((l==0&&w==0)||(Math.abs(l)==1&&Math.abs(w)==1)))
						continue;
					if(h > 0&&h < 6&&l==0&&w==0)
						continue;

					int xx = f==EnumFacing.EAST?l: f==EnumFacing.WEST?-l: f==EnumFacing.NORTH?-w: w;
					int zz = f==EnumFacing.NORTH?l: f==EnumFacing.SOUTH?-l: f==EnumFacing.EAST?w: -w;

					world.setBlockState(pos.add(xx, h, zz), state);
					BlockPos pos2 = pos.add(xx, h, zz);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntitySilo)
					{
						TileEntitySilo currTank = (TileEntitySilo)curr;
						currTank.offset = new int[]{xx, h, zz};
						currTank.pos = h*9+(l+1)*3+(w+1);
						currTank.formed = true;
						currTank.offset = new int[]{xx, h, zz};
						currTank.markDirty();
						world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{new IngredientStack("fenceTreatedWood", 4), new IngredientStack("blockSheetmetalIron", 50)};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}