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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	@OnlyIn(Dist.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
	}

	@Override
	public String getUniqueName()
	{
		return "IE:BlastFurnace";
	}

	@Override
	public boolean isBlockTrigger(BlockState state)
	{
		return state.getBlock()==IEContent.blockStoneDecoration&&(state.getBlock().getMetaFromState(state)==BlockTypes_StoneDecoration.BLASTBRICK.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		Direction f = Direction.fromAngle(player.rotationYaw);
		pos = pos.offset(f);

		for(int h = -1; h <= 1; h++)
			for(int xx = -1; xx <= 1; xx++)
				for(int zz = -1; zz <= 1; zz++)
				{
					if(!Utils.isBlockAt(world, pos.add(xx, h, zz), IEContent.blockStoneDecoration, BlockTypes_StoneDecoration.BLASTBRICK.getMeta()))
						return false;
				}
		BlockState state = IEContent.blockStoneDevice.getStateFromMeta(BlockTypes_StoneDevices.BLAST_FURNACE.getMeta());
		state = state.with(IEProperties.FACING_HORIZONTAL, f.getOpposite());
		for(int h = -1; h <= 1; h++)
			for(int l = -1; l <= 1; l++)
				for(int w = -1; w <= 1; w++)
				{
					int xx = f==Direction.EAST?l: f==Direction.WEST?-l: f==Direction.NORTH?-w: w;
					int zz = f==Direction.NORTH?l: f==Direction.SOUTH?-l: f==Direction.EAST?w: -w;

					BlockPos pos2 = pos.add(xx, h, zz);
					world.setBlockState(pos2, state);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityBlastFurnace)
					{
						TileEntityBlastFurnace currBlast = (TileEntityBlastFurnace)curr;
						currBlast.offset = new int[]{xx, h, zz};
						currBlast.posInMultiblock = (h+1)*9+(l+1)*3+(w+1);
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