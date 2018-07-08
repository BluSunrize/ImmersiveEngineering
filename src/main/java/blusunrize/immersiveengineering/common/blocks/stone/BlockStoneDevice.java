/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStoneDevice extends BlockIEMultiblock<BlockTypes_StoneDevices>
{
	public BlockStoneDevice()
	{
		super("stone_device", Material.ROCK, PropertyEnum.create("type", BlockTypes_StoneDevices.class), ItemBlockIEBase.class, IEProperties.BOOLEANS[0]);
		setHardness(2.0F);
		setResistance(20f);
		this.setAllNotNormalBlock();
		this.setMetaMobilityFlag(BlockTypes_StoneDevices.COKE_OVEN.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_StoneDevices.BLAST_FURNACE.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_StoneDevices.BLAST_FURNACE_ADVANCED.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_StoneDevices.ALLOY_SMELTER.getMeta(), EnumPushReaction.BLOCK);
		this.hasMultiblockTile[BlockTypes_StoneDevices.CONCRETE_SHEET.getMeta()] = false;
		this.hasMultiblockTile[BlockTypes_StoneDevices.CONCRETE_QUARTER.getMeta()] = false;
		this.hasMultiblockTile[BlockTypes_StoneDevices.CONCRETE_THREEQUARTER.getMeta()] = false;
		this.hasMultiblockTile[BlockTypes_StoneDevices.CORESAMPLE.getMeta()] = false;
		lightOpacity = 0;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBlastFurnaceAdvanced)
			return ((TileEntityBlastFurnaceAdvanced)te).pos==1||((TileEntityBlastFurnaceAdvanced)te).pos==4||((TileEntityBlastFurnaceAdvanced)te).pos==7||(((TileEntityBlastFurnaceAdvanced)te).pos==31);
		return true;
	}

	private static final AxisAlignedBB AABB_CARPET = new AxisAlignedBB(0, 0, 0, 1, .0625, 1);
	private static final AxisAlignedBB AABB_QUARTER = new AxisAlignedBB(0, 0, 0, 1, .25, 1);
	private static final AxisAlignedBB AABB_THREEQUARTER = new AxisAlignedBB(0, 0, 0, 1, .75, 1);
	private static final AxisAlignedBB AABB_CORESAMPLE_X = new AxisAlignedBB(0, 0, .28125f, 1, 1, .71875f);
	private static final AxisAlignedBB AABB_CORESAMPLE_Z = new AxisAlignedBB(.28125f, 0, 0, .71875f, 1, 1);

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		BlockTypes_StoneDevices meta = state.getValue(getMetaProperty());
		if(meta==BlockTypes_StoneDevices.CONCRETE_SHEET)
			return AABB_CARPET;
		else if(meta==BlockTypes_StoneDevices.CONCRETE_QUARTER)
			return AABB_QUARTER;
		else if(meta==BlockTypes_StoneDevices.CONCRETE_THREEQUARTER)
			return AABB_THREEQUARTER;
		else if(meta==BlockTypes_StoneDevices.CORESAMPLE)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te!=null&&te instanceof TileEntityCoresample)
				return ((TileEntityCoresample)te).facing.getAxis()==Axis.Z?AABB_CORESAMPLE_Z: AABB_CORESAMPLE_X;
		}
		return super.getBoundingBox(state, world, pos);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		super.getSubBlocks(tab, list);
	}

	@Override
	public TileEntity createBasicTE(World world, BlockTypes_StoneDevices type)
	{
		switch(type)
		{
			case COKE_OVEN:
				return new TileEntityCokeOven();
			case BLAST_FURNACE:
				return new TileEntityBlastFurnace();
			case BLAST_FURNACE_ADVANCED:
				return new TileEntityBlastFurnaceAdvanced();
			case CORESAMPLE:
				return new TileEntityCoresample();
			case ALLOY_SMELTER:
				return new TileEntityAlloySmelter();
		}
		return null;
	}
}