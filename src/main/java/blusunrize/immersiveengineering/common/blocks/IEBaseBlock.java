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
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class IEBaseBlock extends Block implements IIEBlock, IWaterLoggable
{
	protected static IProperty[] tempProperties;

	public final String name;
	public final IProperty[] additionalProperties;
	boolean isHidden;
	boolean hasFlavour;
	protected List<BlockRenderLayer> renderLayers = Collections.singletonList(BlockRenderLayer.SOLID);
	//TODO wtf is variable opacity?
	protected int lightOpacity;
	protected PushReaction mobilityFlag = PushReaction.NORMAL;
	protected boolean canHammerHarvest;
	protected boolean notNormalBlock;

	public IEBaseBlock(String name, Block.Properties blockProps, BiFunction<Block, Item.Properties, Item> createItemBlock, IProperty... additionalProperties)
	{
		super(setTempProperties(blockProps, additionalProperties));
		this.name = name;

		this.additionalProperties = Arrays.copyOf(tempProperties, tempProperties.length);
		this.setDefaultState(getInitDefaultState());
		ResourceLocation registryName = createRegistryName();
		setRegistryName(registryName);

		IEContent.registeredIEBlocks.add(this);
		Item item = createItemBlock.apply(this, new Item.Properties().group(ImmersiveEngineering.itemGroup));
		if(item!=null)
		{
			item.setRegistryName(registryName);
			IEContent.registeredIEItems.add(item);
		}
		lightOpacity = 15;
	}

	//TODO do we still need this hackyness?
	protected static Block.Properties setTempProperties(Properties blockProps, Object[] additionalProperties)
	{
		List<IProperty<?>> propList = new ArrayList<>();
		for(Object o : additionalProperties)
		{
			if(o instanceof IProperty)
				propList.add((IProperty<?>)o);
			if(o instanceof IProperty[])
				propList.addAll(Arrays.asList(((IProperty<?>[])o)));
		}
		tempProperties = propList.toArray(new IProperty[0]);
		return blockProps.variableOpacity();
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

	public IEBaseBlock setBlockLayer(BlockRenderLayer... layer)
	{
		Preconditions.checkArgument(layer.length > 0);
		this.renderLayers = Arrays.asList(layer);
		return this;
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer)
	{
		return renderLayers.contains(layer);
	}

	public IEBaseBlock setLightOpacity(int opacity)
	{
		lightOpacity = opacity;
		return this;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		//TODO This is currently mostly a marker for culling, the actual layer is determined by canRenderInLayer
		return notNormalBlock?BlockRenderLayer.CUTOUT: BlockRenderLayer.SOLID;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		if(notNormalBlock)
			return 0;
			//TODO this sometimes locks up when generating IE blocks as part of worldgen
		else if(state.isOpaqueCube(worldIn, pos))
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
	public PushReaction getPushReaction(BlockState state)
	{
		return mobilityFlag;
	}

	public IEBaseBlock setNotNormalBlock()
	{
		notNormalBlock = true;
		return this;
	}


	@Override
	public float getAmbientOcclusionLightValue(BlockState state, IBlockReader world, BlockPos pos)
	{
		return notNormalBlock?1: super.getAmbientOcclusionLightValue(state, world, pos);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_)
	{
		return notNormalBlock||super.propagatesSkylightDown(p_200123_1_, p_200123_2_, p_200123_3_);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return !notNormalBlock;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos)
	{
		return !notNormalBlock;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(tempProperties);
	}

	protected BlockState getInitDefaultState()
	{
		BlockState state = this.stateContainer.getBaseState();
		if(state.has(BlockStateProperties.WATERLOGGED))
			state = state.with(BlockStateProperties.WATERLOGGED, Boolean.FALSE);
		return state;
	}

	@SuppressWarnings("unchecked")
	protected <V extends Comparable<V>> BlockState applyProperty(BlockState in, IProperty<V> prop, Object val)
	{
		return in.with(prop, (V)val);
	}

	public void onIEBlockPlacedBy(BlockItemUseContext context, BlockState state)
	{
	}

	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
	{
		items.add(new ItemStack(this, 1));
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
	{
		if(worldIn.isRemote&&eventID==255)
		{
			worldIn.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		return super.eventReceived(state, worldIn, pos, eventID, eventParam);
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
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
									BlockRayTraceResult hit)
	{
		ItemStack activeStack = player.getHeldItem(hand);
		if(activeStack.getToolTypes().contains(HammerItem.HAMMER_TOOL))
			return hammerUseSide(hit.getFace(), player, hand, world, pos, hit);
		if(activeStack.getToolTypes().contains(ScrewdriverItem.SCREWDRIVER_TOOL))
			return screwdriverUseSide(hit.getFace(), player, hand, world, pos, hit);
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}

	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		return false;
	}

	public boolean screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		return false;
	}

	/* WATER LOGGING */

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		BlockState state = this.getDefaultState();
		if(state.has(BlockStateProperties.WATERLOGGED)
				&&context.getWorld().getFluidState(context.getPos()).getFluid()==Fluids.WATER)
		{
			state = state.with(BlockStateProperties.WATERLOGGED, true);
		}
		return state;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if (stateIn.has(BlockStateProperties.WATERLOGGED) && stateIn.get(BlockStateProperties.WATERLOGGED)) {
			worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public IFluidState getFluidState(BlockState state)
	{
		if(state.has(BlockStateProperties.WATERLOGGED)&&state.get(BlockStateProperties.WATERLOGGED))
			return Fluids.WATER.getStillFluidState(false);
		return super.getFluidState(state);
	}

	@Override
	public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn)
	{
		return state.has(BlockStateProperties.WATERLOGGED)&&IWaterLoggable.super.canContainFluid(worldIn, pos, state, fluidIn);
	}

	@Override
	public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, IFluidState fluidStateIn)
	{
		return state.has(BlockStateProperties.WATERLOGGED)&&IWaterLoggable.super.receiveFluid(worldIn, pos, state, fluidStateIn);
	}

	@Override
	public Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state)
	{
		if(state.has(BlockStateProperties.WATERLOGGED))
			return IWaterLoggable.super.pickupFluid(worldIn, pos, state);
		return Fluids.EMPTY;
	}

	/* LADDERS */

	public abstract static class IELadderBlock extends IEBaseBlock
	{
		public IELadderBlock(String name, Block.Properties material,
							 BiFunction<Block, Item.Properties, Item> itemBlock, IProperty... additionalProperties)
		{
			super(name, material, itemBlock, additionalProperties);
		}

		@Override
		public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
		{
			return true;
		}

		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
		{
			super.onEntityCollision(state, worldIn, pos, entityIn);
			if(entityIn instanceof LivingEntity&&isLadder(state, worldIn, pos, (LivingEntity)entityIn))
				applyLadderLogic(entityIn);
		}

		public static void applyLadderLogic(Entity entityIn)
		{
			if(entityIn instanceof LivingEntity&&!((LivingEntity)entityIn).isOnLadder())
			{
				Vec3d motion = entityIn.getMotion();
				float maxMotion = 0.15F;
				motion = new Vec3d(
						MathHelper.clamp(motion.x, -maxMotion, maxMotion),
						Math.max(motion.y, -maxMotion),
						MathHelper.clamp(motion.z, -maxMotion, maxMotion)
				);

				entityIn.fallDistance = 0.0F;

				if(motion.y < 0&&entityIn instanceof PlayerEntity&&entityIn.isSneaking())
					motion = new Vec3d(motion.x, 0, motion.z);
				else if(entityIn.collidedHorizontally)
					motion = new Vec3d(motion.x, 0.2, motion.z);
				entityIn.setMotion(motion);
			}
		}
	}
}
