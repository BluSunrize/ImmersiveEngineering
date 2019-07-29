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
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class IEBaseBlock extends Block
{
	protected static IProperty[] tempProperties;

	public final String name;
	public final IProperty[] additionalProperties;
	boolean isHidden;
	boolean hasFlavour;
	protected Set<BlockRenderLayer> renderLayers = Sets.newHashSet(BlockRenderLayer.SOLID);
	//TODO wtf is variable opacity?
	protected int lightOpacity;
	protected PushReaction mobilityFlag = PushReaction.NORMAL;
	protected boolean canHammerHarvest;
	protected boolean notNormalBlock;
	private boolean opaqueCube = false;

	public IEBaseBlock(String name, Block.Properties blockProps, @Nullable Class<? extends ItemBlockIEBase> itemBlock, IProperty... additionalProperties)
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
				Item.Properties itemProps = new Item.Properties().group(ImmersiveEngineering.itemGroup);
				IEContent.registeredIEItems.add(itemBlock.getConstructor(Block.class, Item.Properties.class)
						.newInstance(this, itemProps));
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
		this.renderLayers = Sets.newHashSet(layer);
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

	//TODO review: Review correction applied: getLightValue(...) -> getOpacity(...)
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

	//TODO review: this is now determined by shape data.
	//	@Override
	//	public boolean isFullCube(BlockState state)
	//	{
	//		return notNormalBlock;
	//	}


/*TODO does this still exist?	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return notNormalBlock;
	}*/

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
		return !notNormalBlock; //TODO review: Review acceptance applied.
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

	public boolean isOpaqueCube()
	{
		return opaqueCube;
	}

	public IEBaseBlock setOpaque(boolean isOpaque)
	{
		opaqueCube = isOpaque;
		return this;
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool)
	{
		if(allowHammerHarvest(state)&&tool==IEContent.toolHammer)
			return true;
		if(allowWirecutterHarvest(state)&&IEContent.toolWireCutter)
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
		if(activeStack.getToolTypes().contains(IEContent.toolHammer))
			return hammerUseSide(hit.getFace(), player, world, pos, hit);
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}

	public boolean hammerUseSide(Direction side, PlayerEntity player, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		return false; //TODO review: returning "event not handled" for now.
	}

	public abstract static class IELadderBlock extends IEBaseBlock
	{
		public IELadderBlock(String name, Block.Properties material,
							 Class<? extends ItemBlockIEBase> itemBlock, IProperty... additionalProperties)
		{
			super(name, material, itemBlock, additionalProperties);
		}

		//TODO review: Review changes applied. Notes: Suggestion to keep this class for "climbable" blocks
		//			   like scaffolding, but change actual metal/tr.wood ladders to `mc::LadderBlock`s for
		//			   compatibility reasons.
		@Override
		public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
		{
			return true;
		}
		/*
		//TODO: Method marked for delete by main authors.
		//  review: actually not sure if this override is still needed
		// 	  		It appears to handle the moment when players jump
		//			on a ladder, preventing fall damage, and limit
		//			the motion accordingly. This should be already
		//			the vanilla default by now.
		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
		{
			super.onEntityCollision(state, worldIn, pos, entityIn);
			if(entityIn instanceof LivingEntity&&!((LivingEntity)entityIn).isOnLadder()&&isLadder(state, worldIn, pos, (LivingEntity)entityIn))
			{
				float f5 = 0.15F;
				final Vec3d motion = entityIn.getMotion();
				double vx = MathHelper.clamp(motion.x, -f5, f5);
				double vy = MathHelper.clamp(motion.y, -f5, f5);
				double vz = MathHelper.clamp(motion.z, -f5, f5);
				entityIn.fallDistance = 0.0F;
				vy = Math.max(vy, -0.15D);
				if(vy < 0&&entityIn instanceof PlayerEntity&&entityIn.isSneaking())
					vy = 0;
				else if(entityIn.collidedHorizontally)
					vy = .2;
				entityIn.setMotion(new Vec3d(vx,vy,vz));
			}
		}
		*/
	}
}