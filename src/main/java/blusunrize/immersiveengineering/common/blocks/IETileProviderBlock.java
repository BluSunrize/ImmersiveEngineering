/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile.PlacementLimitation;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.BiFunction;

public abstract class IETileProviderBlock extends IEBaseBlock implements IColouredBlock
{
	private boolean hasColours = false;

	public IETileProviderBlock(String name, Block.Properties blockProps, BiFunction<Block, Item.Properties, Item> itemBlock,
							   Property... stateProps)
	{
		super(name, blockProps, itemBlock, stateProps);
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
		if(ret.hasProperty(IEProperties.FACING_ALL))
			ret = ret.with(IEProperties.FACING_ALL, getDefaultFacing());
		else if(ret.hasProperty(IEProperties.FACING_HORIZONTAL))
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
		}
		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity tile, ItemStack stack)
	{
		if(tile instanceof IAdditionalDrops)
		{
			//TODO remove or turn into loot entries?
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
		if(tile instanceof IReadOnPlacement)
			((IReadOnPlacement)tile).readOnPlacement(placer, stack);
		if(tile instanceof IHasDummyBlocks)
			((IHasDummyBlocks)tile).placeDummies(context, state);
		if(tile instanceof IPlacementInteraction)
			((IPlacementInteraction)tile).onTilePlaced(world, pos, state, side, hitX, hitY, hitZ, placer, stack);
	}

	@Override
	public ActionResultType hammerUseSide(Direction side, PlayerEntity player, Hand hand, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		TileEntity tile = w.getTileEntity(pos);
		if(tile instanceof IHammerInteraction)
		{
			boolean b = ((IHammerInteraction)tile).hammerUseSide(side, player, hand, hit.getHitVec());
			if(b)
				return ActionResultType.SUCCESS;
			else
				return ActionResultType.FAIL;
		}
		return super.hammerUseSide(side, player, hand, w, pos, hit);
	}

	@Override
	public ActionResultType screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		TileEntity tile = w.getTileEntity(pos);
		if(tile instanceof IScrewdriverInteraction)
		{
			ActionResultType teResult = ((IScrewdriverInteraction)tile).screwdriverUseSide(side, player, hand, hit.getHitVec());
			if(teResult!=ActionResultType.PASS)
				return teResult;
		}
		return super.screwdriverUseSide(side, player, hand, w, pos, hit);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
		ActionResultType superResult = super.onBlockActivated(state, world, pos, player, hand, hit);
		if(superResult.isSuccessOrConsume())
			return superResult;
		final Direction side = hit.getFace();
		final float hitX = (float)hit.getHitVec().x-pos.getX();
		final float hitY = (float)hit.getHitVec().y-pos.getY();
		final float hitZ = (float)hit.getHitVec().z-pos.getZ();
		ItemStack heldItem = player.getHeldItem(hand);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IDirectionalTile&&Utils.isHammer(heldItem)&&((IDirectionalTile)tile).canHammerRotate(
				side,
				hit.getHitVec().subtract(Vector3d.copy(pos)),
				player)&&!world.isRemote)
		{
			Direction f = ((IDirectionalTile)tile).getFacing();
			Direction oldF = f;
			PlacementLimitation limit = ((IDirectionalTile)tile).getFacingLimitation();
			switch(limit)
			{
				case SIDE_CLICKED:
					f = DirectionUtils.VALUES[Math.floorMod(f.ordinal()+(player.isSneaking()?-1: 1), DirectionUtils.VALUES.length)];
					break;
				case PISTON_LIKE:
					f = player.isSneaking()!=(side.getAxisDirection()==AxisDirection.NEGATIVE)?DirectionUtils.rotateAround(f, side.getAxis()).getOpposite(): DirectionUtils.rotateAround(f, side.getAxis());
					break;
				case HORIZONTAL:
				case HORIZONTAL_PREFER_SIDE:
				case HORIZONTAL_QUADRANT:
				case HORIZONTAL_AXIS:
					f = player.isSneaking()!=side.equals(Direction.DOWN)?f.rotateYCCW(): f.rotateY();
					break;
			}
			((IDirectionalTile)tile).setFacing(f);
			((IDirectionalTile)tile).afterRotation(oldF, f);
			tile.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
			world.addBlockEvent(tile.getPos(), tile.getBlockState().getBlock(), 255, 0);
			return ActionResultType.SUCCESS;
		}
		if(tile instanceof IPlayerInteraction)
		{
			boolean b = ((IPlayerInteraction)tile).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
			if(b)
				return ActionResultType.SUCCESS;
		}
		if(tile instanceof IInteractionObjectIE&&hand==Hand.MAIN_HAND&&!player.isSneaking())
		{
			IInteractionObjectIE interaction = (IInteractionObjectIE)tile;
			interaction = interaction.getGuiMaster();
			if(interaction!=null&&interaction.canUseGui(player)&&!world.isRemote)
				NetworkHooks.openGui((ServerPlayerEntity)player, interaction, ((TileEntity)interaction).getPos());
			return ActionResultType.SUCCESS;
		}
		return superResult;
	}

	@Nullable
	private Property<Direction> findFacingProperty(BlockState state)
	{
		if(state.hasProperty(IEProperties.FACING_ALL))
			return IEProperties.FACING_ALL;
		else if(state.hasProperty(IEProperties.FACING_HORIZONTAL))
			return IEProperties.FACING_HORIZONTAL;
		else
			return null;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		Property<Direction> facingProp = findFacingProperty(state);
		if(facingProp!=null&&canRotate())
		{
			Direction currentDirection = state.get(facingProp);
			Direction newDirection = rot.rotate(currentDirection);
			return state.with(facingProp, newDirection);
		}
		return super.rotate(state, rot);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		if(state.hasProperty(IEProperties.MIRRORED)&&canRotate()&&mirrorIn==Mirror.LEFT_RIGHT)
			return state.with(IEProperties.MIRRORED, !state.get(IEProperties.MIRRORED));
		else
		{
			Property<Direction> facingProp = findFacingProperty(state);
			if(facingProp!=null&&canRotate())
			{
				Direction currentDirection = state.get(facingProp);
				Direction newDirection = mirrorIn.mirror(currentDirection);
				return state.with(facingProp, newDirection);
			}
		}
		return super.mirror(state, mirrorIn);
	}

	protected boolean canRotate()
	{
		//Basic heuristic: Multiblocks should not be rotated depending on state
		return !getStateContainer().getProperties().contains(IEProperties.MULTIBLOCKSLAVE);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		if(!world.isRemote)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof IEBaseTileEntity)
				((IEBaseTileEntity)tile).onNeighborBlockChange(fromPos);
		}
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
		if(state.getBlock()==this)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof ISelectionBounds)
				return ((ISelectionBounds)te).getSelectionShape(context);
		}
		return super.getShape(state, world, pos, context);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context)
	{
		if(state.getBlock()==this)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof ICollisionBounds)
				return ((ICollisionBounds)te).getCollisionShape(context);
		}
		return super.getCollisionShape(state, world, pos, context);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader world, BlockPos pos)
	{
		if(world.getBlockState(pos).getBlock()==this)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof ISelectionBounds)
				return ((ISelectionBounds)te).getSelectionShape(null);
		}
		return super.getRaytraceShape(state, world, pos);
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
			return ((IEBlockInterfaces.IRedstoneOutput)te).getWeakRSOutput(side);
		return 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getStrongPower(BlockState blockState, IBlockReader world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput)te).getStrongRSOutput(side);
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
			return ((IEBlockInterfaces.IRedstoneOutput)te).canConnectRedstone(side);
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

	public static boolean areAllReplaceable(BlockPos start, BlockPos end, BlockItemUseContext context)
	{
		World w = context.getWorld();
		return BlockPos.getAllInBox(start, end).allMatch(
				pos -> {
					BlockItemUseContext subContext = BlockItemUseContext.func_221536_a(context, pos, context.getFace());
					return w.getBlockState(pos).isReplaceable(subContext);
				});
	}
}
