/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMetalDevice0 extends BlockIETileProvider<BlockTypes_MetalDevice0>
{
	public BlockMetalDevice0()
	{
		super("metal_device0", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDevice0.class), ItemBlockIEBase.class, IEProperties.MULTIBLOCKSLAVE, IEProperties.SIDECONFIG[0], IEProperties.SIDECONFIG[1], IEProperties.SIDECONFIG[2], IEProperties.SIDECONFIG[3], IEProperties.SIDECONFIG[4], IEProperties.SIDECONFIG[5]);
		setHardness(3.0F);
		setResistance(15.0F);
		this.setMetaBlockLayer(BlockTypes_MetalDevice0.FLUID_PLACER.getMeta(), BlockRenderLayer.CUTOUT);
		this.setNotNormalBlock(BlockTypes_MetalDevice0.FLUID_PUMP.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice0.FLUID_PLACER.getMeta());
		this.setMetaMobilityFlag(BlockTypes_MetalDevice0.FLUID_PUMP.getMeta(), EnumPushReaction.BLOCK);
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(BlockTypes_MetalDevice0.values()[meta]==BlockTypes_MetalDevice0.FLUID_PUMP)
			return "fluid_pump";
		return null;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		if(stack.getItemDamage()==BlockTypes_MetalDevice0.FLUID_PUMP.getMeta())
		{
			BlockPos above = pos.up();
			return !world.isOutsideBuildHeight(above)&&world.getBlockState(above).getBlock().isReplaceable(world, above);
		}
		return true;
	}


	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		return state;
	}


	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityFluidPump)
			return ((TileEntityFluidPump)te).dummy==false||side==EnumFacing.UP;
		return true;
	}

	@Override
	public TileEntity createBasicTE(World world, BlockTypes_MetalDevice0 type)
	{
		switch(type)
		{
			case CAPACITOR_LV:
				return new TileEntityCapacitorLV();
			case CAPACITOR_MV:
				return new TileEntityCapacitorMV();
			case CAPACITOR_HV:
				return new TileEntityCapacitorHV();
			case CAPACITOR_CREATIVE:
				return new TileEntityCapacitorCreative();
			case BARREL:
				return new TileEntityMetalBarrel();
			case FLUID_PUMP:
				return new TileEntityFluidPump();
			case FLUID_PLACER:
				return new TileEntityFluidPlacer();
		}
		return null;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
}