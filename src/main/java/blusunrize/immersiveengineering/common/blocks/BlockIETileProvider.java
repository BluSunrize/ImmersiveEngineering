/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.energy.wires.GlobalWireNetwork.getNetwork;

@Mod.EventBusSubscriber
public abstract class BlockIETileProvider extends BlockIEBase implements IColouredBlock
{
	private boolean hasColours = false;

	public BlockIETileProvider(String name, Block.Properties blockProps, Class<? extends ItemBlockIEBase> itemBlock,
							   IProperty... stateProps)
	{
		super(name, blockProps, itemBlock, stateProps);
	}

	private static final Map<DimensionBlockPos, TileEntity> tempTile = new HashMap<>();

	@SubscribeEvent
	public static void onTick(TickEvent.ServerTickEvent ev)
	{
		if(ev.phase==TickEvent.Phase.END)
			tempTile.clear();
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(IBlockState state, IBlockReader world)
	{
		TileEntity basic = createBasicTE(state);
		Collection<IProperty<?>> keys = state.getProperties();
		if(basic instanceof IDirectionalTile)
		{
			EnumFacing newFacing = null;
			if(keys.contains(IEProperties.FACING_HORIZONTAL))
				newFacing = state.get(IEProperties.FACING_HORIZONTAL);
			else if(keys.contains(IEProperties.FACING_ALL))
				newFacing = state.get(IEProperties.FACING_ALL);
			int type = ((IDirectionalTile)basic).getFacingLimitation();
			if(newFacing!=null)
			{
				switch(type)
				{
					case 2:
					case 4:
					case 5:
					case 6:
						if(newFacing.getAxis()==Axis.Y)
							newFacing = null;
						break;
					case 3:
						if(newFacing.getAxis()!=Axis.Y)
							newFacing = null;
						break;
				}
				if(newFacing!=null)
					((IDirectionalTile)basic).setFacing(newFacing);
			}
		}
		if(basic instanceof IAttachedIntegerProperies)
		{
			IAttachedIntegerProperies tileIntProps = (IAttachedIntegerProperies)basic;
			String[] names = ((IAttachedIntegerProperies)basic).getIntPropertyNames();
			for(String propertyName : names)
			{
				IntegerProperty property = tileIntProps.getIntProperty(propertyName);
				if(keys.contains(property))
					tileIntProps.setValue(propertyName, state.get(property));
			}
		}

		return basic;
	}

	@Override
	protected IBlockState getInitDefaultState()
	{
		IBlockState ret = super.getInitDefaultState();
		if(ret.getProperties().contains(IEProperties.FACING_ALL))
			ret = ret.with(IEProperties.FACING_ALL, getDefaultFacing());
		else if(ret.getProperties().contains(IEProperties.FACING_HORIZONTAL))
			ret = ret.with(IEProperties.FACING_HORIZONTAL, getDefaultFacing());
		return ret;
	}

	@Nullable
	public abstract TileEntity createBasicTE(IBlockState state);

	@Override
	public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune)
	{
		TileEntity tile = world.getTileEntity(pos);
		DimensionBlockPos dpos = new DimensionBlockPos(pos, world.getDimension().getType());
		if(tile==null&&tempTile.containsKey(dpos))
			tile = tempTile.get(dpos);
		if(tile!=null&&(!(tile instanceof ITileDrop)||!((ITileDrop)tile).preventInventoryDrop()))
		{
			if(tile instanceof IIEInventory&&((IIEInventory)tile).getDroppedItems()!=null)
			{
				for(ItemStack s : ((IIEInventory)tile).getDroppedItems())
					if(!s.isEmpty())
						drops.add(s);
			}
			else
			{
				LazyOptional<IItemHandler> itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				itemHandler.ifPresent((h) ->
				{
					if(h instanceof IEInventoryHandler)
						for(int i = 0; i < h.getSlots(); i++)
							if(!h.getStackInSlot(i).isEmpty())
							{
								drops.add(h.getStackInSlot(i));
								((IEInventoryHandler)h).setStackInSlot(i, ItemStack.EMPTY);
							}
				});
			}
		}
		if(tile instanceof ITileDrop)
		{
			NonNullList<ItemStack> s = ((ITileDrop)tile).getTileDrops(harvesters.get(), state);
			drops.addAll(s);
		}
		else
			super.getDrops(state, drops, world, pos, fortune);

		tempTile.remove(dpos);
	}

	@Override
	public void onReplaced(IBlockState state, World world, BlockPos pos, IBlockState newState, boolean isMoving)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IHasDummyBlocks)
			((IHasDummyBlocks)tile).breakDummies(pos, state);
		Consumer<Connection> dropHandler;
		if(world.getGameRules().getBoolean("doTileDrops"))
			dropHandler = (c) -> {
				if(!c.isInternal())
				{
					BlockPos end = c.getOtherEnd(c.getEndFor(pos)).getPosition();
					double dx = pos.getX()+.5+Math.signum(end.getX()-pos.getX());
					double dy = pos.getY()+.5+Math.signum(end.getY()-pos.getY());
					double dz = pos.getZ()+.5+Math.signum(end.getZ()-pos.getZ());
					world.spawnEntity(new EntityItem(world, dx, dy, dz, c.type.getWireCoil(c)));
				}
			};
		else
			dropHandler = c -> {
			};
		if(tile instanceof IImmersiveConnectable&&!world.isRemote)
			for(ConnectionPoint cp : ((IImmersiveConnectable)tile).getConnectionPoints())
				getNetwork(world).removeAllConnectionsAt(cp, dropHandler);
		tempTile.put(new DimensionBlockPos(pos, world.getDimension().getType()), tile);
		super.onReplaced(state, world, pos, newState, isMoving);
		world.removeTileEntity(pos);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack stack)
	{
		if(tile instanceof IAdditionalDrops)
		{
			Collection<ItemStack> stacks = ((IAdditionalDrops)tile).getExtraDrops(player, state);
			if(stacks!=null&&!stacks.isEmpty())
				for(ItemStack s : stacks)
					if(!s.isEmpty())
						spawnAsEntity(world, pos, s);
		}
		super.harvestBlock(world, player, pos, state, tile, stack);
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockReader world, BlockPos pos, Entity entity)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IEntityProof)
			return ((IEntityProof)tile).canEntityDestroy(entity);
		return super.canEntityDestroy(state, world, pos, entity);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof ITileDrop)
		{
			ItemStack s = ((ITileDrop)tile).getPickBlock(player, world.getBlockState(pos), target);
			if(!s.isEmpty())
				return s;
		}
		Item item = Item.getItemFromBlock(this);
		return item==Items.AIR?ItemStack.EMPTY: new ItemStack(item, 1);
	}


	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
	{
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity!=null&&tileentity.receiveClientEvent(eventID, eventParam);
	}

	protected EnumFacing getDefaultFacing()
	{
		return EnumFacing.NORTH;
	}

	/*TODO why isn't there an axis/EnumFacing parameter any more
	@Override
	public IBlockState rotate(IBlockState state, IWorld world, BlockPos pos, Rotation direction)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IDirectionalTile)
		{
			if(!((IDirectionalTile)tile).canRotate(axis))
				return false;
			IBlockState state = world.getBlockState(pos);
			if(state.getProperties().contains(IEProperties.FACING_ALL)||state.getProperties().contains(IEProperties.FACING_HORIZONTAL))
			{
				DirectionProperty prop = state.getProperties().contains(IEProperties.FACING_HORIZONTAL)?IEProperties.FACING_HORIZONTAL: IEProperties.FACING_ALL;
				EnumFacing f = ((IDirectionalTile)tile).getFacing();
				int limit = ((IDirectionalTile)tile).getFacingLimitation();

				if(limit==0)
					f = EnumFacing.VALUES[(f.ordinal()+1)%EnumFacing.VALUES.length];
				else if(limit==1)
					f = axis.getAxisDirection()==AxisDirection.POSITIVE?f.rotateAround(axis.getAxis()).getOpposite(): f.rotateAround(axis.getAxis());
				else if(limit==2||limit==5)
					f = axis.getAxisDirection()==AxisDirection.POSITIVE?f.rotateY(): f.rotateYCCW();
				if(f!=((IDirectionalTile)tile).getFacing())
				{
					EnumFacing old = ((IDirectionalTile)tile).getFacing();
					((IDirectionalTile)tile).setFacing(f);
					((IDirectionalTile)tile).afterRotation(old, f);
					state = applyProperty(state, prop, ((IDirectionalTile)tile).getFacing());
					world.setBlockState(pos, state.cycleProperty(prop));
				}
			}
		}
		return false;
	}*/

	/*TODO when extended states are a thing again...
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockReader world, BlockPos pos)
	{
		state = super.getExtendedState(state, world, pos);
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState extended = (IExtendedBlockState)state;
			TileEntity te = world.getTileEntity(pos);
			if(te!=null)
			{
				if(te instanceof IConfigurableSides)
					for(int i = 0; i < 6; i++)
						if(extended.getUnlistedNames().contains(IEProperties.SIDECONFIG[i]))
							extended = extended.with(IEProperties.SIDECONFIG[i], ((IConfigurableSides)te).getSideConfig(i));
				if(te instanceof IAdvancedHasObjProperty)
					extended = extended.with(Properties.AnimationProperty, ((IAdvancedHasObjProperty)te).getOBJState());
				else if(te instanceof IHasObjProperty)
					extended = extended.with(Properties.AnimationProperty, new OBJState(((IHasObjProperty)te).compileDisplayList(), true));
				if(te instanceof IDynamicTexture)
					extended = extended.with(IEProperties.OBJ_TEXTURE_REMAP, ((IDynamicTexture)te).getTextureReplacements());
				if(te instanceof IOBJModelCallback)
					extended = extended.with(IOBJModelCallback.PROPERTY, (IOBJModelCallback)te);
				if(te.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
					extended = extended.with(CapabilityShader.BLOCKSTATE_PROPERTY, te.getCapability(CapabilityShader.SHADER_CAPABILITY, null));
				if(te instanceof IPropertyPassthrough&&((IExtendedBlockState)state).getUnlistedNames().contains(IEProperties.TILEENTITY_PASSTHROUGH))
					extended = extended.with(IEProperties.TILEENTITY_PASSTHROUGH, te);
				if(te instanceof TileEntityImmersiveConnectable&&((IExtendedBlockState)state).getUnlistedNames().contains(IEProperties.CONNECTIONS))
					extended = extended.with(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
			}
			state = extended;
		}

		return state;
	}
	 */

	@Override
	public void onIEBlockPlacedBy(BlockItemUseContext context, IBlockState state)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		TileEntity tile = world.getTileEntity(pos);
		EntityPlayer placer = context.getPlayer();
		EnumFacing side = context.getFace();
		float hitX = context.getHitX();
		float hitY = context.getHitY();
		float hitZ = context.getHitZ();
		ItemStack stack = context.getItem();

		if(tile instanceof IDirectionalTile)
		{
			EnumFacing f = ((IDirectionalTile)tile).getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
			((IDirectionalTile)tile).setFacing(f);
			if(tile instanceof IAdvancedDirectionalTile)
				((IAdvancedDirectionalTile)tile).onDirectionalPlacement(side, hitX, hitY, hitZ, placer);
		}
		if(tile instanceof ITileDrop)
		{
			((ITileDrop)tile).readOnPlacement(placer, stack);
		}
		if(tile instanceof IHasDummyBlocks)
		{
			((IHasDummyBlocks)tile).placeDummies(pos, state, side, hitX, hitY, hitZ);
		}
		if(tile instanceof IPlacementInteraction)
		{
			((IPlacementInteraction)tile).onTilePlaced(world, pos, state, side, hitX, hitY, hitZ, placer, stack);
		}
	}

	@Override
	public boolean onBlockActivated(IBlockState state, World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack heldItem = player.getHeldItem(hand);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IConfigurableSides&&Utils.isHammer(heldItem)&&!world.isRemote)
		{
			EnumFacing activeSide = player.isSneaking()?side.getOpposite(): side;
			if(((IConfigurableSides)tile).toggleSide(activeSide, player))
				return true;
		}
		if(tile instanceof IDirectionalTile&&Utils.isHammer(heldItem)&&((IDirectionalTile)tile).canHammerRotate(side, hitX, hitY, hitZ, player)&&!world.isRemote)
		{
			EnumFacing f = ((IDirectionalTile)tile).getFacing();
			EnumFacing oldF = f;
			int limit = ((IDirectionalTile)tile).getFacingLimitation();

			if(limit==0)
				f = EnumFacing.VALUES[(f.ordinal()+1)%EnumFacing.VALUES.length];
			else if(limit==1)
				f = player.isSneaking()?f.rotateAround(side.getAxis()).getOpposite(): f.rotateAround(side.getAxis());
			else if(limit==2||limit==5)
				f = player.isSneaking()?f.rotateYCCW(): f.rotateY();
			((IDirectionalTile)tile).setFacing(f);
			((IDirectionalTile)tile).afterRotation(oldF, f);
			tile.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
			world.addBlockEvent(tile.getPos(), tile.getBlockState().getBlock(), 255, 0);
			return true;
		}
		if(tile instanceof IHammerInteraction&&Utils.isHammer(heldItem)&&!world.isRemote)
		{
			boolean b = ((IHammerInteraction)tile).hammerUseSide(side, player, hitX, hitY, hitZ);
			if(b)
				return b;
		}
		if(tile instanceof IPlayerInteraction)
		{
			boolean b = ((IPlayerInteraction)tile).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
			if(b)
				return b;
		}
		if(tile instanceof IGuiTile&&hand==EnumHand.MAIN_HAND&&!player.isSneaking())
		{
			TileEntity master = ((IGuiTile)tile).getGuiMaster();
			if(!world.isRemote&&master!=null&&((IGuiTile)master).canOpenGui(player))
				CommonProxy.openGuiForTile(player, (TileEntity & IGuiTile)master);
			return true;
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
	{
		if(!world.isRemote)
		{
			//Necessary to prevent ghostloading, see conversation in #immersive-engineering on Discord on 12/13 Mar 2019
			Chunk posChunk = world.getChunk(pos);
			//TODO figure out why this became a "future task"...
			ApiUtils.addFutureServerTask(world, () ->
			{
				if(world.isBlockLoaded(pos))//TODO where did this go? &&!posChunk.unloadQueued)
				{
					TileEntity tile = world.getTileEntity(pos);
					if(tile instanceof INeighbourChangeTile&&!tile.getWorld().isRemote)
						((INeighbourChangeTile)tile).onNeighborBlockChange(fromPos);
				}
			});
		}
	}

	@Override
	public int getLightValue(IBlockState state, IWorldReader world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof ILightValue)
			return ((ILightValue)te).getLightValue();
		return 0;
	}

	public BlockIETileProvider setHasColours()
	{
		this.hasColours = true;
		return this;
	}

	@Override
	public boolean hasCustomBlockColours()
	{
		return hasColours;
	}

	@Override
	public int getRenderColour(IBlockState state, @Nullable IBlockReader worldIn, @Nullable BlockPos pos, int tintIndex)
	{
		if(worldIn!=null&&pos!=null)
		{
			TileEntity tile = worldIn.getTileEntity(pos);
			if(tile instanceof IColouredTile)
				return ((IColouredTile)tile).getRenderColour(tintIndex);
		}
		return 0xffffff;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader world, IBlockState state, BlockPos pos, EnumFacing side)
	{
		if(!notNormalBlock)
			return BlockFaceShape.SOLID;
		else if(side!=null)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IFaceShape)
				return ((IFaceShape)te).getFaceShape(side);
			else
			{
				//TODO nicer way?
				AxisAlignedBB bb = getShape(state, world, pos).getBoundingBox();
				double wMin = side.getAxis()==Axis.X?bb.minZ: bb.minX;
				double wMax = side.getAxis()==Axis.X?bb.maxZ: bb.maxX;
				double hMin = side.getAxis()==Axis.Y?bb.minZ: bb.minY;
				double hMax = side.getAxis()==Axis.Y?bb.maxZ: bb.maxY;
				if(wMin==0&&hMin==0&&wMax==1&&hMax==1)
					return BlockFaceShape.SOLID;
				else if(hMin==0&&hMax==1&&wMin==(1-wMax))
				{
					if(wMin > .375)
						return BlockFaceShape.MIDDLE_POLE_THIN;
					else if(wMin > .3125)
						return BlockFaceShape.MIDDLE_POLE;
					else
						return BlockFaceShape.MIDDLE_POLE_THICK;
				}
				else if(hMin==wMin&&hMax==wMax)
				{
					if(wMin > .375)
						return BlockFaceShape.CENTER_SMALL;
					else if(wMin > .3125)
						return BlockFaceShape.CENTER;
					else
						return BlockFaceShape.CENTER_BIG;
				}
				return BlockFaceShape.UNDEFINED;
			}
		}
		return super.getBlockFaceShape(world, state, pos, side);
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos)
	{
		//TODO caching?
		if(world.getBlockState(pos).getBlock()==this)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IAdvancedCollisionBounds)
			{
				List<AxisAlignedBB> bounds = ((IAdvancedCollisionBounds)te).getAdvancedColisionBounds();
				if(!bounds.isEmpty())
				{
					VoxelShape ret = VoxelShapes.empty();
					for(AxisAlignedBB aabb : bounds)
						if(aabb!=null)
							ret = VoxelShapes.combineAndSimplify(ret, VoxelShapes.create(aabb), IBooleanFunction.OR);
					return ret;
				}
			}
			else if(te instanceof IBlockBounds)
			{
				float[] bounds = ((IBlockBounds)te).getBlockBounds();
				AxisAlignedBB aabb = new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
				return VoxelShapes.create(aabb);
			}
		}
		return super.getShape(state, world, pos);
	}

	@Nullable
	@Override
	public RayTraceResult getRayTraceResult(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end, RayTraceResult original)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IAdvancedSelectionBounds)
		{
			List<AxisAlignedBB> list = ((IAdvancedSelectionBounds)te).getAdvancedSelectionBounds();
			if(list!=null&&!list.isEmpty())
			{
				RayTraceResult min = null;
				double minDist = Double.POSITIVE_INFINITY;
				for(AxisAlignedBB aabb : list)
				{
					RayTraceResult mop = aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ())
							.calculateIntercept(start, end, pos);
					if(mop!=null)
					{
						double dist = mop.hitVec.squareDistanceTo(start);
						if(dist < minDist)
						{
							min = mop;
							minDist = dist;
						}
					}
				}
				return min;
			}
		}
		return original;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state)
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IComparatorOverride)
			return ((IEBlockInterfaces.IComparatorOverride)te).getComparatorInputOverride();
		return 0;
	}


	@Override
	public int getWeakPower(IBlockState blockState, IBlockReader world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput)te).getWeakRSOutput(blockState, side);
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockReader world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput)te).getStrongRSOutput(blockState, side);
		return 0;
	}

	@Override
	public boolean canProvidePower(IBlockState state)
	{
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockReader world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput)te).canConnectRedstone(state, side);
		return false;
	}

	@Override
	public void onEntityCollision(IBlockState state, World world, BlockPos pos, Entity entity)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityIEBase)
			((TileEntityIEBase)te).onEntityCollision(world, entity);
	}
}