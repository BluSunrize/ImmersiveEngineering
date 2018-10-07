/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase.IELadderBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockMetalLadder extends IELadderBlock<BlockTypes_MetalLadder>
{
	protected static final AxisAlignedBB[] LADDER_AABB = {
			new AxisAlignedBB(0, 0, .8125, 1, 1, 1),
			new AxisAlignedBB(0, 0, 0, 1, 1, .1875),
			new AxisAlignedBB(.8125, 0, 0, 1, 1, 1),
			new AxisAlignedBB(0, 0, 0, .1875, 1, 1)
	};
	protected static final AxisAlignedBB[] FRAME_AABB = {
			new AxisAlignedBB(0, 0, .9375, 1, 1, 1),
			new AxisAlignedBB(0, 0, 0, 1, 1, .0625),
			new AxisAlignedBB(.9375, 0, 0, 1, 1, 1),
			new AxisAlignedBB(0, 0, 0, .0625, 1, 1)
	};

	public BlockMetalLadder()
	{
		super("metal_ladder", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalLadder.class), ItemBlockIEBase.class, IEProperties.FACING_HORIZONTAL);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		this.setBlockLayer(BlockRenderLayer.CUTOUT_MIPPED);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side)
	{
		int meta = this.getMetaFromState(state);
		if(meta==0)
			return BlockFaceShape.UNDEFINED;
		else if(side.getAxis()==Axis.Y)
			return BlockFaceShape.BOWL;
		return BlockFaceShape.SOLID;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		int meta = this.getMetaFromState(state);
		return (meta!=0&&side.getAxis()!=Axis.Y)&&super.isSideSolid(state, world, pos, side);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return true;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
	{
		int meta = this.getMetaFromState(state);
		if(meta==0)
			addCollisionBoxToList(pos, entityBox, collidingBoxes, this.getBoundingBox(state, world, pos));
		else
		{
			EnumFacing dir = ((TileEntityLadder)world.getTileEntity(pos)).getFacing();
			for(int i = 0; i < 4; i++)
				if(i==dir.getIndex()-2)
					addCollisionBoxToList(pos, entityBox, collidingBoxes, LADDER_AABB[i]);
				else
					addCollisionBoxToList(pos, entityBox, collidingBoxes, FRAME_AABB[i]);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		if(this.getMetaFromState(state)==0)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof IEBlockInterfaces.IDirectionalTile)
			{
				IEBlockInterfaces.IDirectionalTile directionalTile = (IEBlockInterfaces.IDirectionalTile)tileEntity;
				return LADDER_AABB[directionalTile.getFacing().getIndex()-2];
			}
		}
		return super.getBoundingBox(state, world, pos);
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		if(stack.getMetadata() > 0)
			return true;

		if(this.canAttachTo(world, pos.west(), side))
			return true;
		else if(this.canAttachTo(world, pos.east(), side))
			return true;
		else if(this.canAttachTo(world, pos.north(), side))
			return true;
		else
			return this.canAttachTo(world, pos.south(), side);
	}

	public boolean canAttachTo(World world, BlockPos pos, EnumFacing facing)
	{
		IBlockState state = world.getBlockState(pos);
		boolean flag = isExceptBlockForAttachWithPiston(state.getBlock());
		return !flag&&state.getBlockFaceShape(world, pos, facing)==BlockFaceShape.SOLID&&!state.canProvidePower();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityLadder)
		{
			TileEntityLadder ladder = (TileEntityLadder)te;
			EnumFacing enumfacing = ladder.getFacing();
			if(getMetaFromState(state)==0&&!this.canAttachTo(world, pos.offset(enumfacing.getOpposite()), enumfacing))
			{
				this.dropBlockAsItem(world, pos, state, 0);
				world.setBlockToAir(pos);
			}
		}
		super.neighborChanged(state, world, pos, blockIn, fromPos);
	}

	@Override
	public void onIEBlockPlacedBy(World world, BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer, ItemStack stack)
	{
		TileEntityLadder tile = (TileEntityLadder)world.getTileEntity(pos);
		if(tile!=null)
			tile.setFacing(tile.getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		TileEntityLadder tile = (TileEntityLadder)world.getTileEntity(pos);
		if(tile!=null&&tile.getFacing().getAxis().isHorizontal())
			state = applyProperty(state, IEProperties.FACING_HORIZONTAL, tile.getFacing());
		return state;
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileEntityLadder();
	}
}