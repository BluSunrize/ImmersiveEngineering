/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.HammerItem;
import blusunrize.immersiveengineering.common.items.ScrewdriverItem;
import blusunrize.immersiveengineering.common.items.WirecutterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import java.util.function.BiFunction;

public class IEBaseBlock extends Block implements IIEBlock, SimpleWaterloggedBlock
{
	public final String name;
	boolean isHidden;
	boolean hasFlavour;
	//TODO wtf is variable opacity?
	protected int lightOpacity;
	protected PushReaction mobilityFlag = PushReaction.NORMAL;
	protected boolean canHammerHarvest;
	protected final boolean notNormalBlock;

	public IEBaseBlock(String name, Block.Properties blockProps, BiFunction<Block, Item.Properties, Item> createItemBlock)
	{
		super(blockProps.dynamicShape());
		this.notNormalBlock = !defaultBlockState().canOcclude();
		this.name = name;

		this.registerDefaultState(getInitDefaultState());
		ResourceLocation registryName = createRegistryName();
		setRegistryName(registryName);

		IEContent.registeredIEBlocks.add(this);
		Item item = createItemBlock.apply(this, new Item.Properties().tab(ImmersiveEngineering.ITEM_GROUP));
		if(item!=null)
		{
			item.setRegistryName(registryName);
			IEContent.registeredIEItems.add(item);
		}
		lightOpacity = 15;
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
		return name;
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
		if(notNormalBlock)
			return 0;
			//TODO this sometimes locks up when generating IE blocks as part of worldgen
		else if(state.isSolidRender(worldIn, pos))
			return lightOpacity;
		else
			return state.propagatesSkylightDown(worldIn, pos)?0: 1;
	}

	public IEBaseBlock setMobility(PushReaction flag)
	{
		mobilityFlag = flag;
		return this;
	}

	@Override
	@SuppressWarnings("deprecation")
	public PushReaction getPistonPushReaction(BlockState state)
	{
		return mobilityFlag;
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

	@SuppressWarnings("unchecked")
	protected <V extends Comparable<V>> BlockState applyProperty(BlockState in, Property<V> prop, Object val)
	{
		return in.setValue(prop, (V)val);
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

	@OnlyIn(Dist.CLIENT)
	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
	{
		items.add(new ItemStack(this, 1));
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

	public IEBaseBlock setHammerHarvest()
	{
		canHammerHarvest = true;
		return this;
	}

	public boolean allowHammerHarvest(BlockState blockState)
	{
		return canHammerHarvest;
	}

	public boolean allowWirecutterHarvest(BlockState blockState)
	{
		return false;
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool)
	{
		if(allowHammerHarvest(state)&&tool==HammerItem.HAMMER_TOOL)
			return true;
		if(allowWirecutterHarvest(state)&&tool==WirecutterItem.CUTTER_TOOL)
			return true;
		return super.isToolEffective(state, tool);
	}

	public ResourceLocation createRegistryName()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, name);
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
											 BlockHitResult hit)
	{
		ItemStack activeStack = player.getItemInHand(hand);
		if(activeStack.getToolTypes().contains(HammerItem.HAMMER_TOOL))
			return hammerUseSide(hit.getDirection(), player, hand, world, pos, hit);
		if(activeStack.getToolTypes().contains(ScrewdriverItem.SCREWDRIVER_TOOL))
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
			worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
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
	public Fluid takeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state)
	{
		if(state.hasProperty(BlockStateProperties.WATERLOGGED))
			return SimpleWaterloggedBlock.super.takeLiquid(worldIn, pos, state);
		return Fluids.EMPTY;
	}

	/* LADDERS */

	public abstract static class IELadderBlock extends IEBaseBlock
	{
		public IELadderBlock(String name, Block.Properties material,
							 BiFunction<Block, Item.Properties, Item> itemBlock)
		{
			super(name, material, itemBlock);
		}

		@Override
		public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity)
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
