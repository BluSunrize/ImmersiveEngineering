/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.common.entities.IEMinecartEntity;
import blusunrize.immersiveengineering.common.entities.IEMinecartEntity.MinecartConstructor;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class IEMinecartItem extends IEBaseItem
{
	private final MinecartConstructor constructor;
	private final boolean fitsIntoContainers;

	public IEMinecartItem(MinecartConstructor constructor, boolean fitsIntoContainers)
	{
		super(new Properties().stacksTo(1));
		this.constructor = constructor;
		this.fitsIntoContainers = fitsIntoContainers;
	}

	public final IEMinecartEntity<?> createCart(Level world, double x, double y, double z, ItemStack stack)
	{
		return constructor.make(world, x, y, z);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = world.getBlockState(blockpos);
		if(!blockstate.is(BlockTags.RAILS))
			return InteractionResult.FAIL;
		else
		{
			ItemStack itemstack = context.getItemInHand();
			if(!world.isClientSide)
			{
				RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock?((BaseRailBlock)blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null): RailShape.NORTH_SOUTH;
				double d0 = 0.0D;
				if(railshape.isAscending())
					d0 = 0.5D;

				IEMinecartEntity minecartEntity = this.createCart(world, (double)blockpos.getX()+0.5D, (double)blockpos.getY()+0.0625D+d0, (double)blockpos.getZ()+0.5D, itemstack);
				if(itemstack.hasCustomHoverName())
					minecartEntity.setCustomName(itemstack.getHoverName());
				minecartEntity.readTileFromItem(context.getPlayer(), itemstack);
				world.addFreshEntity(minecartEntity);
			}

			itemstack.shrink(1);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag advanced)
	{
		super.appendHoverText(stack, ctx, tooltip, advanced);
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				tooltip.add(TextUtils.applyFormat(
						Component.translatable(Lib.DESC_INFO+"fluidStored", fs.getDisplayName(), fs.getAmount()),
						ChatFormatting.GRAY
				));
		}
	}

	@Override
	public boolean canFitInsideContainerItems()
	{
		return fitsIntoContainers;
	}

	public static final DispenseItemBehavior MINECART_DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior()
	{
		private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();

		/**
		 * Dispense the specified stack, play the dispense sound and spawn particles.
		 */
		public ItemStack execute(BlockSource source, ItemStack stack)
		{
			Direction direction = source.state().getValue(DispenserBlock.FACING);
			Level world = source.level();
			double d0 = source.pos().getX()+(double)direction.getStepX()*1.125D;
			double d1 = Math.floor(source.pos().getY())+(double)direction.getStepY();
			double d2 = source.pos().getZ()+(double)direction.getStepZ()*1.125D;
			BlockPos blockpos = source.pos().relative(direction);
			BlockState blockstate = world.getBlockState(blockpos);
			RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock?((BaseRailBlock)blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null): RailShape.NORTH_SOUTH;
			double d3;
			if(blockstate.is(BlockTags.RAILS))
			{
				if(railshape.isAscending())
					d3 = 0.6D;
				else
					d3 = 0.1D;
			}
			else
			{
				if(!blockstate.isAir()||!world.getBlockState(blockpos.below()).is(BlockTags.RAILS))
					return this.behaviourDefaultDispenseItem.dispense(source, stack);

				BlockState blockstate1 = world.getBlockState(blockpos.below());
				RailShape railshape1 = blockstate1.getBlock() instanceof BaseRailBlock?((BaseRailBlock)blockstate1.getBlock()).getRailDirection(blockstate1, world, blockpos.below(), null): RailShape.NORTH_SOUTH;
				if(direction!=Direction.DOWN&&railshape1.isAscending())
					d3 = -0.4D;
				else
					d3 = -0.9D;
			}

			IEMinecartEntity minecartEntity = ((IEMinecartItem)stack.getItem()).createCart(world, d0, d1+d3, d2, stack);
			if(stack.hasCustomHoverName())
				minecartEntity.setCustomName(stack.getHoverName());
			minecartEntity.readTileFromItem(null, stack);

			world.addFreshEntity(minecartEntity);
			stack.shrink(1);
			return stack;
		}

		@Override
		protected void playSound(BlockSource source)
		{
			source.level().levelEvent(1000, source.pos(), 0);
		}
	};
}