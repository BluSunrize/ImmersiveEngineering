/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.INeighbourChangeTile;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class BlockConveyorProxy extends BlockIETileProvider<BlockTypes_ConveyorProxy>
{
	public BlockConveyorProxy()
	{
		super("conveyor_proxy", Material.AIR, PropertyEnum.create("type", BlockTypes_ConveyorProxy.class), ItemBlockIEBase.class);
		setAllNotNormalBlock();
		setOpaque(false);
		setLightOpacity(0);
	}

	@Override
	public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return true;
	}

	@Override
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
	{
		return null;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		return null;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
	}

	@Override
	public boolean canCollideCheck(IBlockState state, boolean b)
	{
		return false;
	}

	@Override
	public boolean isCollidable()
	{
		return false;
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World par1World, BlockPos pos, Vec3d par5Vec3, Vec3d par6Vec3)
	{
		return null;
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state)
	{
		return EnumPushReaction.DESTROY;
	}

	@Override
	public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return true;
	}

	/**
	 * Check if a conveyor requires this proxy block.
	 */
	public static boolean needsProxy(IConveyorTile conveyor) {
		if (conveyor.getConveyorSubtype() == null) return false;
		return conveyor.getConveyorSubtype().getConveyorDirection() == ConveyorDirection.DOWN;
	}

	@Override
	public TileEntity createBasicTE(World worldIn, BlockTypes_ConveyorProxy meta)
	{
		return new TileEntityConveyorProxy();
	}

	public static class TileEntityConveyorProxy extends TileEntityIEBase implements ICapabilityProvider, INeighbourChangeTile
	{

		public TileEntityConveyorProxy()
		{
		}

		@Override
		public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
		{

		}

		@Override
		public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
		{

		}

		/**
		 * Get the conveyor that we are proxying, returning null if invalid.
		 */
		@Nullable
		public IConveyorTile getConveyor() {
			TileEntity tile = world.getTileEntity(pos.down());
			if(tile instanceof IConveyorTile && needsProxy((IConveyorTile) tile))
				return ((IConveyorTile) tile);
			return null;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return true;
			return super.hasCapability(capability, facing);
		}

		IItemHandler insertionHandler = new ConveyorProxyInventoryHandler(this);

		@Override
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return (T)insertionHandler;
			return super.getCapability(capability, facing);
		}

		@Override
		public void onNeighborBlockChange(BlockPos otherPos)
		{
			IConveyorTile conveyor = getConveyor();
			if (conveyor == null || !needsProxy(conveyor)) {
				world.setBlockToAir(pos);
			}
		}

		public static class ConveyorProxyInventoryHandler implements IItemHandlerModifiable
		{
			TileEntityConveyorProxy proxy;

			public ConveyorProxyInventoryHandler(TileEntityConveyorProxy proxy)
			{
				this.proxy = proxy;
			}

			@Override
			public int getSlots()
			{
				return 1;
			}

			@Override
			public ItemStack getStackInSlot(int slot)
			{
				return ItemStack.EMPTY;
			}

			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
			{
				IConveyorTile convTile = proxy.getConveyor();
				if (convTile == null)
					return stack;
				IConveyorBelt conveyor = convTile.getConveyorSubtype();
				if(!simulate)
				{
					EntityItem entity = new EntityItem(proxy.getWorld(), proxy.getPos().getX()+.5, proxy.getPos().getY()-.1875, proxy.getPos().getZ()+.5, stack.copy());
					entity.motionX = 0;
					entity.motionY = 0;
					entity.motionZ = 0;
					proxy.getWorld().spawnEntity(entity);
					if (conveyor != null)
					{
						conveyor.onItemDeployed((TileEntity)convTile, entity, convTile.getFacing());
					}
				}
				return ItemStack.EMPTY;
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate)
			{
				return ItemStack.EMPTY;
			}

			@Override
			public int getSlotLimit(int slot)
			{
				return 64;
			}

			@Override
			public void setStackInSlot(int slot, ItemStack stack)
			{
			}
		}
	}


}
