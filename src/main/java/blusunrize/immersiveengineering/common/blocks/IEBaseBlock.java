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
import blusunrize.immersiveengineering.common.items.WirecutterItem;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IEBaseBlock extends Block
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

	public IEBaseBlock(String name, Block.Properties blockProps, @Nullable Class<? extends BlockItemIE> itemBlock, IProperty... additionalProperties)
	{
		super(setTempProperties(blockProps, additionalProperties));
		this.name = name;

		this.additionalProperties = Arrays.copyOf(tempProperties, tempProperties.length);
		this.setDefaultState(getInitDefaultState());
		ResourceLocation registryName = createRegistryName();
		setRegistryName(registryName);
		//TODO this.adjustSound();

		IEContent.registeredIEBlocks.add(this);
		if(itemBlock!=null)
		{
			try
			{
				IEContent.registeredIEItems.add(itemBlock.getConstructor(Block.class)
						.newInstance(this));
			} catch(Exception e)
			{
				//TODO e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		lightOpacity = 255;
	}

	//TODO do we still need this hackyness?
	protected static Block.Properties setTempProperties(Properties blockProps, Object[] additionalProperties)
	{
		ArrayList<IProperty> propList = new ArrayList<IProperty>();
		for(Object o : additionalProperties)
		{
			if(o instanceof IProperty)
				propList.add((IProperty)o);
			if(o instanceof IProperty[])
				propList.addAll(Arrays.asList(((IProperty[])o)));
		}
		tempProperties = propList.toArray(new IProperty[0]);
		return blockProps;
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
		return renderLayers.get(0);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return lightOpacity;
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

	//TODO what is this?
	@Override
	public float func_220080_a(BlockState p_220080_1_, IBlockReader p_220080_2_, BlockPos p_220080_3_)
	{
		return notNormalBlock?1: super.func_220080_a(p_220080_1_, p_220080_2_, p_220080_3_);
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
		//TODO ext states?
		builder.add(tempProperties);
	}

	protected BlockState getInitDefaultState()
	{
		return this.stateContainer.getBaseState();
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
			return hammerUseSide(hit.getFace(), player, world, pos, hit);
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}

	public boolean hammerUseSide(Direction side, PlayerEntity player, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		return false;
	}

	public abstract static class IELadderBlock extends IEBaseBlock
	{
		public IELadderBlock(String name, Block.Properties material,
							 Class<? extends BlockItemIE> itemBlock, IProperty... additionalProperties)
		{
			super(name, material, itemBlock, additionalProperties);
		}

		@Override
		public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
		{
			return true;
		}
	}
}
