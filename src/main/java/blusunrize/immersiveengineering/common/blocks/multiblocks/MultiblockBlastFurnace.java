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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockBlastFurnace implements IMultiblock
{

	public static MultiblockBlastFurnace instance = new MultiblockBlastFurnace();

	static ItemStack[][][] structure = new ItemStack[3][3][3];

	static
	{
		for(int h = 0; h < 3; h++)
			for(int l = 0; l < 3; l++)
				for(int w = 0; w < 3; w++)
					structure[h][l][w] = new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.BLASTBRICK.getMeta());
	}

	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}

	@Override
	public float getManualScale()
	{
		return 16;
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
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
	}

	@Override
	public String getUniqueName()
	{
		return "IE:BlastFurnace";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockStoneDecoration&&(state.getBlock().getMetaFromState(state)==BlockTypes_StoneDecoration.BLASTBRICK.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		EnumFacing f = EnumFacing.fromAngle(player.rotationYaw);
		pos = pos.offset(f);

		for(int h = -1; h <= 1; h++)
			for(int xx = -1; xx <= 1; xx++)
				for(int zz = -1; zz <= 1; zz++)
				{
					if(!Utils.isBlockAt(world, pos.add(xx, h, zz), IEContent.blockStoneDecoration, BlockTypes_StoneDecoration.BLASTBRICK.getMeta()))
						return false;
				}
		IBlockState state = IEContent.blockStoneDevice.getStateFromMeta(BlockTypes_StoneDevices.BLAST_FURNACE.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, f.getOpposite());
		for(int h = -1; h <= 1; h++)
			for(int l = -1; l <= 1; l++)
				for(int w = -1; w <= 1; w++)
				{
					int xx = f==EnumFacing.EAST?l: f==EnumFacing.WEST?-l: f==EnumFacing.NORTH?-w: w;
					int zz = f==EnumFacing.NORTH?l: f==EnumFacing.SOUTH?-l: f==EnumFacing.EAST?w: -w;

					BlockPos pos2 = pos.add(xx, h, zz);
					world.setBlockState(pos2, state);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityBlastFurnace)
					{
						TileEntityBlastFurnace currBlast = (TileEntityBlastFurnace)curr;
						currBlast.offset = new int[]{xx, h, zz};
						currBlast.pos = (h+1)*9+(l+1)*3+(w+1);
						currBlast.formed = true;
						currBlast.markDirty();
						world.addBlockEvent(pos2, IEContent.blockStoneDevice, 255, 0);
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{new IngredientStack(new ItemStack(IEContent.blockStoneDecoration, 27, BlockTypes_StoneDecoration.BLASTBRICK.getMeta()))};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}