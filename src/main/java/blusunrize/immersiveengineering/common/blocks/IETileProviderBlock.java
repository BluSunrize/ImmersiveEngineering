/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.wires.GlobalWireNetwork.getNetwork;

@Mod.EventBusSubscriber
public abstract class IETileProviderBlock extends IEBaseBlock implements IColouredBlock
{
	private boolean hasColours = false;

	public IETileProviderBlock(String name, Block.Properties blockProps, @Nullable Class<? extends BlockItemIE> itemBlock,
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
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	protected BlockState getInitDefaultState()
	{
		BlockState ret = super.getInitDefaultState();
		if(ret.getProperties().contains(IEProperties.FACING_ALL))
			ret = ret.with(IEProperties.FACING_ALL, getDefaultFacing());
		else if(ret.getProperties().contains(IEProperties.FACING_HORIZONTAL))
			ret = ret.with(IEProperties.FACING_HORIZONTAL, getDefaultFacing());
		return ret;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(state.getBlock()!=newState.getBlock())
		{
			if(tile instanceof IEBaseTileEntity)
				((IEBaseTileEntity)tile).setOverrideState(state);
			if(tile instanceof IHasDummyBlocks)
				((IHasDummyBlocks)tile).breakDummies(pos, state);
			Consumer<Connection> dropHandler;
			if(world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
				dropHandler = (c) -> {
					if(!c.isInternal())
					{
						BlockPos end = c.getOtherEnd(c.getEndFor(pos)).getPosition();
						double dx = pos.getX()+.5+Math.signum(end.getX()-pos.getX());
						double dy = pos.getY()+.5+Math.signum(end.getY()-pos.getY());
						double dz = pos.getZ()+.5+Math.signum(end.getZ()-pos.getZ());
						world.addEntity(new ItemEntity(world, dx, dy, dz, c.type.getWireCoil(c)));
					}
				};
			else
				dropHandler = c -> {
				};
			if(tile!=null&&(!(tile instanceof ITileDrop)||!((ITileDrop)tile).preventInventoryDrop())&&!(tile instanceof MultiblockPartTileEntity))
			{
				if(tile instanceof IIEInventory&&((IIEInventory)tile).getDroppedItems()!=null)
					InventoryHelper.dropItems(world, pos, ((IIEInventory)tile).getDroppedItems());
				else
				{
					LazyOptional<IItemHandler> itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
					itemHandler.ifPresent((h) ->
					{
						if(h instanceof IEInventoryHandler)
						{
							NonNullList<ItemStack> drops = NonNullList.create();
							for(int i = 0; i < h.getSlots(); i++)
								if(!h.getStackInSlot(i).isEmpty())
								{
									drops.add(h.getStackInSlot(i));
									((IEInventoryHandler)h).setStackInSlot(i, ItemStack.EMPTY);
								}
							InventoryHelper.dropItems(world, pos, drops);
						}
					});
				}
			}
			if(tile instanceof IImmersiveConnectable&&!world.isRemote)
				for(ConnectionPoint cp : ((IImmersiveConnectable)tile).getConnectionPoints())
					getNetwork(world).removeAllConnectionsAt(cp, dropHandler);
		}
		tempTile.put(new DimensionBlockPos(pos, world.getDimension().getType()), tile);
		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity tile, ItemStack stack)
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
	public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IEntityProof)
			return ((IEntityProof)tile).canEntityDestroy(entity);
		return super.canEntityDestroy(state, world, pos, entity);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof ITileDrop&&target instanceof BlockRayTraceResult)
		{
			ItemStack s = ((ITileDrop)tile).getPickBlock(player, world.getBlockState(pos), target);
			if(!s.isEmpty())
				return s;
		}
		Item item = this.asItem();
		return item==Items.AIR?ItemStack.EMPTY: new ItemStack(item, 1);
	}


	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
	{
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity!=null&&tileentity.receiveClientEvent(eventID, eventParam);
	}

	protected Direction getDefaultFacing()
	{
		return Direction.NORTH;
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

	@Override
	public void onIEBlockPlacedBy(BlockItemUseContext context, BlockState state)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		TileEntity tile = world.getTileEntity(pos);
		PlayerEntity placer = context.getPlayer();
		Direction side = context.getFace();
		float hitX = (float)context.getHitVec().x-pos.getX();
		float hitY = (float)context.getHitVec().y-pos.getY();
		float hitZ = (float)context.getHitVec().z-pos.getZ();
		ItemStack stack = context.getItem();

		if(tile instanceof IDirectionalTile)
		{
			Direction f = ((IDirectionalTile)tile).getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
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
			((IHasDummyBlocks)tile).placeDummies(context, state);
		}
		if(tile instanceof IPlacementInteraction)
		{
			((IPlacementInteraction)tile).onTilePlaced(world, pos, state, side, hitX, hitY, hitZ, placer, stack);
		}
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		TileEntity tile = w.getTileEntity(pos);
		if(tile instanceof IHammerInteraction&&!w.isRemote)
		{
			boolean b = ((IHammerInteraction)tile).hammerUseSide(side, player, hit.getHitVec());
			if(b)
				return true;
		}
		return super.hammerUseSide(side, player, w, pos, hit);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
		final Direction side = hit.getFace();
		final float hitX = (float)hit.getHitVec().x;
		final float hitY = (float)hit.getHitVec().y;
		final float hitZ = (float)hit.getHitVec().z;
		ItemStack heldItem = player.getHeldItem(hand);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IDirectionalTile&&Utils.isHammer(heldItem)&&((IDirectionalTile)tile).canHammerRotate(side, hitX, hitY, hitZ, player)&&!world.isRemote)
		{
			Direction f = ((IDirectionalTile)tile).getFacing();
			Direction oldF = f;
			PlacementLimitation limit = ((IDirectionalTile)tile).getFacingLimitation();
			switch(limit)
			{
				case SIDE_CLICKED:
					f = Direction.VALUES[(f.ordinal()+1)%Direction.VALUES.length];
					break;
				case PISTON_LIKE:
					f = player.isSneaking()?f.rotateAround(side.getAxis()).getOpposite(): f.rotateAround(side.getAxis());
					break;
				case HORIZONTAL:
				case HORIZONTAL_PREFER_SIDE:
				case HORIZONTAL_QUADRANT:
				case HORIZONTAL_AXIS:
					f = player.isSneaking()?f.rotateYCCW(): f.rotateY();
					break;
			}
			((IDirectionalTile)tile).setFacing(f);
			((IDirectionalTile)tile).afterRotation(oldF, f);
			tile.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
			world.addBlockEvent(tile.getPos(), tile.getBlockState().getBlock(), 255, 0);
			return true;
		}
		if(tile instanceof IPlayerInteraction)
		{
			boolean b = ((IPlayerInteraction)tile).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
			if(b)
				return b;
		}
		if(tile instanceof IInteractionObjectIE&&hand==Hand.MAIN_HAND&&!player.isSneaking())
		{
			IInteractionObjectIE interaction = (IInteractionObjectIE)tile;
			interaction = interaction.getGuiMaster();
			if(interaction!=null&&interaction.canUseGui(player)&&!world.isRemote)
				NetworkHooks.openGui((ServerPlayerEntity)player, interaction, ((TileEntity)interaction).getPos());
			return true;
		}
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		if(!world.isRemote)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof INeighbourChangeTile&&!tile.getWorld().isRemote)
				((INeighbourChangeTile)tile).onNeighborBlockChange(fromPos);
		}
	}

	@Override
	public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof ILightValue)
			return ((ILightValue)te).getLightValue();
		return 0;
	}

	public IETileProviderBlock setHasColours()
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
	public int getRenderColour(BlockState state, @Nullable IBlockReader worldIn, @Nullable BlockPos pos, int tintIndex)
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
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context)
	{
		//TODO caching?
		if(world.getBlockState(pos).getBlock()==this)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IAdvancedCollisionBounds)
			{
				List<AxisAlignedBB> bounds = ((IAdvancedCollisionBounds)te).getAdvancedColisionBounds();
				if(bounds!=null&&!bounds.isEmpty())
				{
					VoxelShape ret = VoxelShapes.empty();
					for(AxisAlignedBB aabb : bounds)
						if(aabb!=null)
							ret = VoxelShapes.combineAndSimplify(ret, VoxelShapes.create(aabb), IBooleanFunction.OR);
					return ret;
				}
			}
			if(te instanceof IBlockBounds)
			{
				float[] bounds = ((IBlockBounds)te).getBlockBounds();
				if(bounds!=null)
				{
					AxisAlignedBB aabb = new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
					return VoxelShapes.create(aabb);
				}
			}
		}
		return super.getShape(state, world, pos, context);
	}

	@Nullable
	@Override
	public RayTraceResult getRayTraceResult(BlockState state, World world, BlockPos pos, Vec3d start, Vec3d end, RayTraceResult original)
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
					BlockRayTraceResult mop = VoxelShapes.create(aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ()))
																							 .rayTrace(start, end, pos);
					if(mop!=null)
					{
						//double dist = mop.hitVec.squareDistanceTo(start);
						double dist = mop.getHitVec().squareDistanceTo(start);
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
	@SuppressWarnings("deprecation")
	public boolean hasComparatorInputOverride(BlockState state)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IComparatorOverride)
			return ((IEBlockInterfaces.IComparatorOverride)te).getComparatorInputOverride();
		return 0;
	}


	@Override
	@SuppressWarnings("deprecation")
	public int getWeakPower(BlockState blockState, IBlockReader world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput)te).getWeakRSOutput(blockState, side);
		return 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getStrongPower(BlockState blockState, IBlockReader world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput)te).getStrongRSOutput(blockState, side);
		return 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canProvidePower(BlockState state)
	{
		return true;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput)te).canConnectRedstone(state, side);
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBaseTileEntity)
			((IEBaseTileEntity)te).onEntityCollision(world, entity);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		List<ItemStack> ret = super.getDrops(state, builder);
		LootContext ctx = builder.build(LootParameterSets.BLOCK);
		if(ctx.has(LootParameters.BLOCK_ENTITY))
		{
			TileEntity te = ctx.get(LootParameters.BLOCK_ENTITY);
			if(te instanceof ITileDrop)
			{
				ret.addAll(((ITileDrop)te).getTileDrops(builder));
			}
		}
		return ret;
	}
}