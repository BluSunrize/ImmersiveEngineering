/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IETags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ScheduledTick;

import javax.annotation.Nullable;

public class IEBaseBlock extends Block implements IIEBlock, SimpleWaterloggedBlock
{
	boolean isHidden;
	boolean hasFlavour;
	//TODO wtf is variable opacity?
	protected int lightOpacity;
	protected final boolean notNormalBlock;
	private final boolean fitsIntoContainer;

	public IEBaseBlock(Block.Properties blockProps)
	{
		this(blockProps, true);
	}

	public IEBaseBlock(Block.Properties blockProps, boolean fitsIntoContainer)
	{
		super(blockProps);
		this.fitsIntoContainer = fitsIntoContainer;
		this.notNormalBlock = !defaultBlockState().canOcclude();

		this.registerDefaultState(getInitDefaultState());
		lightOpacity = -1;
	}

	public IEBaseBlock setHidden(boolean shouldHide)
	{
		isHidden = shouldHide;
		return this;
	}

	public boolean isHidden()
	{
		return isHidden;
	}

	public IEBaseBlock setHasFlavour(boolean shouldHave)
	{
		hasFlavour = shouldHave;
		return this;
	}

	@Override
	public String getNameForFlavour()
	{
		return BuiltInRegistries.BLOCK.getKey(this).getPath();
	}

	@Override
	public boolean hasFlavour()
	{
		return hasFlavour;
	}

	public IEBaseBlock setLightOpacity(int opacity)
	{
		lightOpacity = opacity;
		return this;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos)
	{
		if(this.lightOpacity!=-1)
			return this.lightOpacity;
		else if(notNormalBlock)
			return 0;
		else
			return super.getLightBlock(state, worldIn, pos);
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
	{
		return notNormalBlock?1: super.getShadeBrightness(state, world, pos);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
	{
		return notNormalBlock||super.propagatesSkylightDown(state, reader, pos);
	}

	protected BlockState getInitDefaultState()
	{
		BlockState state = this.stateDefinition.any();
		if(state.hasProperty(BlockStateProperties.WATERLOGGED))
			state = state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE);
		return state;
	}

	public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state)
	{
	}

	public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context)
	{
		return true;
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(worldIn, pos, state, placer, stack);
	}

	public void fillCreativeTab(Output out)
	{
		out.accept(this);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int eventID, int eventParam)
	{
		if(worldIn.isClientSide&&eventID==255)
		{
			worldIn.sendBlockUpdated(pos, state, state, 3);
			return true;
		}
		return super.triggerEvent(state, worldIn, pos, eventID, eventParam);
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
								 BlockHitResult hit)
	{
		ItemStack activeStack = player.getItemInHand(hand);
		if(activeStack.is(IETags.hammers))
			return hammerUseSide(hit.getDirection(), player, hand, world, pos, hit);
		if(activeStack.is(IETags.screwdrivers))
			return screwdriverUseSide(hit.getDirection(), player, hand, world, pos, hit);
		return super.use(state, world, pos, player, hand, hit);
	}

	public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit)
	{
		return InteractionResult.PASS;
	}

	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit)
	{
		return InteractionResult.PASS;
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type)
	{
		return false;
	}

	/* WATER LOGGING */

	public static BlockState applyLocationalWaterlogging(BlockState state, Level world, BlockPos pos)
	{
		if(state.hasProperty(BlockStateProperties.WATERLOGGED))
			return state.setValue(BlockStateProperties.WATERLOGGED, world.getFluidState(pos).getType()==Fluids.WATER);
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState state = this.defaultBlockState();
		state = applyLocationalWaterlogging(state, context.getLevel(), context.getClickedPos());
		return state;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if(stateIn.hasProperty(BlockStateProperties.WATERLOGGED)&&stateIn.getValue(BlockStateProperties.WATERLOGGED))
			worldIn.getFluidTicks().schedule(new ScheduledTick<>(
					Fluids.WATER, currentPos, Fluids.WATER.getTickDelay(worldIn), 0
			));
		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public FluidState getFluidState(BlockState state)
	{
		if(state.hasProperty(BlockStateProperties.WATERLOGGED)&&state.getValue(BlockStateProperties.WATERLOGGED))
			return Fluids.WATER.getSource(false);
		return super.getFluidState(state);
	}

	@Override
	public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn)
	{
		return state.hasProperty(BlockStateProperties.WATERLOGGED)&&SimpleWaterloggedBlock.super.canPlaceLiquid(worldIn, pos, state, fluidIn);
	}

	@Override
	public boolean placeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn)
	{
		return state.hasProperty(BlockStateProperties.WATERLOGGED)&&SimpleWaterloggedBlock.super.placeLiquid(worldIn, pos, state, fluidStateIn);
	}

	@Override
	public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state)
	{
		if(state.hasProperty(BlockStateProperties.WATERLOGGED))
			return SimpleWaterloggedBlock.super.pickupBlock(level, pos, state);
		else
			return ItemStack.EMPTY;
	}

	public boolean fitsIntoContainer()
	{
		return fitsIntoContainer;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		Property<Direction> facingProp = findFacingProperty(state);
		if(facingProp!=null&&canRotate())
		{
			Direction currentDirection = state.getValue(facingProp);
			Direction newDirection = rot.rotate(currentDirection);
			return state.setValue(facingProp, newDirection);
		}
		return super.rotate(state, rot);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		if(state.hasProperty(IEProperties.MIRRORED)&&canRotate()&&mirrorIn==Mirror.LEFT_RIGHT)
			return state.setValue(IEProperties.MIRRORED, !state.getValue(IEProperties.MIRRORED));
		else
		{
			Property<Direction> facingProp = findFacingProperty(state);
			if(facingProp!=null&&canRotate())
			{
				Direction currentDirection = state.getValue(facingProp);
				Direction newDirection = mirrorIn.mirror(currentDirection);
				return state.setValue(facingProp, newDirection);
			}
		}
		return super.mirror(state, mirrorIn);
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

	protected boolean canRotate()
	{
		//Basic heuristic: Multiblocks should not be rotated depending on state
		return !getStateDefinition().getProperties().contains(IEProperties.MULTIBLOCKSLAVE);
	}

	/* LADDERS */

	public abstract static class IELadderBlock extends IEBaseBlock
	{
		public IELadderBlock(Block.Properties material)
		{
			super(material);
		}

		@Override
		public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, @Nullable LivingEntity entity)
		{
			return true;
		}

		@Override
		public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn)
		{
			super.entityInside(state, worldIn, pos, entityIn);
			if(entityIn instanceof LivingEntity&&isLadder(state, worldIn, pos, (LivingEntity)entityIn))
				applyLadderLogic(entityIn);
		}

		public static void applyLadderLogic(Entity entityIn)
		{
			if(entityIn instanceof LivingEntity&&!((LivingEntity)entityIn).onClimbable())
			{
				Vec3 motion = entityIn.getDeltaMovement();
				float maxMotion = 0.15F;
				motion = new Vec3(
						Mth.clamp(motion.x, -maxMotion, maxMotion),
						Math.max(motion.y, -maxMotion),
						Mth.clamp(motion.z, -maxMotion, maxMotion)
				);

				entityIn.fallDistance = 0.0F;

				if(motion.y < 0&&entityIn instanceof Player&&entityIn.isShiftKeyDown())
					motion = new Vec3(motion.x, 0, motion.z);
				else if(entityIn.horizontalCollision)
					motion = new Vec3(motion.x, 0.2, motion.z);
				entityIn.setDeltaMovement(motion);
			}
		}
	}
}
