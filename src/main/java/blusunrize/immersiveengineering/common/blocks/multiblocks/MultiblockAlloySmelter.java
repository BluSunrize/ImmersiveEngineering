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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityAlloySmelter;
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

public class MultiblockAlloySmelter implements IMultiblock
{
	public static MultiblockAlloySmelter instance = new MultiblockAlloySmelter();

	static ItemStack[][][] structure = new ItemStack[3][3][3];

	static
	{
		for(int h = 0; h < 2; h++)
			for(int l = 0; l < 2; l++)
				for(int w = 0; w < 2; w++)
					structure[h][l][w] = new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.ALLOYBRICK.getMeta());
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
		return 20;
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
		return "IE:AlloySmelter";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockStoneDecoration&&state.getBlock().getMetaFromState(state)==BlockTypes_StoneDecoration.ALLOYBRICK.getMeta();
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		EnumFacing f = EnumFacing.fromAngle(player.rotationYaw);

		if(Utils.isBlockAt(world, pos.down(), IEContent.blockStoneDecoration, BlockTypes_StoneDecoration.ALLOYBRICK.getMeta()))
			pos = pos.down();
		if(!Utils.isBlockAt(world, pos.offset(f.rotateY()), IEContent.blockStoneDecoration, BlockTypes_StoneDecoration.ALLOYBRICK.getMeta()))
			pos = pos.offset(f.rotateYCCW());

		for(int h = 0; h <= 1; h++)
			for(int l = 0; l <= 1; l++)
				for(int w = 0; w <= 1; w++)
				{
					BlockPos pos2 = pos.up(h).offset(f, l).offset(f.rotateY(), w);
					if(!Utils.isBlockAt(world, pos2, IEContent.blockStoneDecoration, BlockTypes_StoneDecoration.ALLOYBRICK.getMeta()))
						return false;
				}
		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled())
			return false;

		IBlockState state = IEContent.blockStoneDevice.getStateFromMeta(BlockTypes_StoneDevices.ALLOY_SMELTER.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, f.getOpposite());
		for(int h = 0; h <= 1; h++)
			for(int l = 0; l <= 1; l++)
				for(int w = 0; w <= 1; w++)
				{
					BlockPos pos2 = pos.up(h).offset(f, l).offset(f.rotateY(), w);
					world.setBlockState(pos2, state);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityAlloySmelter)
					{
						TileEntityAlloySmelter currBlast = (TileEntityAlloySmelter)curr;
						currBlast.offset = new int[]{pos2.getX()-pos.getX(), pos2.getY()-pos.getY(), pos2.getZ()-pos.getZ()};
						currBlast.pos = (h+1)*9+(l+1)*3+(w+1);
						currBlast.formed = true;
						currBlast.markDirty();
						world.addBlockEvent(pos2, IEContent.blockStoneDevice, 255, 0);
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{new IngredientStack(new ItemStack(IEContent.blockStoneDecoration, 8, BlockTypes_StoneDecoration.ALLOYBRICK.getMeta()))};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}