/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.IEMinecartEntity;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public abstract class IEMinecartItem extends IEBaseItem
{
	public IEMinecartItem(String name)
	{
		super("minecart_"+name, new Properties().maxStackSize(1));
		DispenserBlock.registerDispenseBehavior(this, MINECART_DISPENSER_BEHAVIOR);
	}

	public abstract IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack);

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos blockpos = context.getPos();
		BlockState blockstate = world.getBlockState(blockpos);
		if(!blockstate.isIn(BlockTags.RAILS))
			return ActionResultType.FAIL;
		else
		{
			ItemStack itemstack = context.getItem();
			if(!world.isRemote)
			{
				RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock?((AbstractRailBlock)blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null): RailShape.NORTH_SOUTH;
				double d0 = 0.0D;
				if(railshape.isAscending())
					d0 = 0.5D;

				IEMinecartEntity minecartEntity = this.createCart(world, (double)blockpos.getX()+0.5D, (double)blockpos.getY()+0.0625D+d0, (double)blockpos.getZ()+0.5D, itemstack);
				if(itemstack.hasDisplayName())
					minecartEntity.setCustomName(itemstack.getDisplayName());
				minecartEntity.readTileFromItem(context.getPlayer(), itemstack);
				world.addEntity(minecartEntity);
			}

			itemstack.shrink(1);
			return ActionResultType.SUCCESS;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced)
	{
		super.addInformation(stack, world, tooltip, advanced);
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				tooltip.add(ClientUtils.applyFormat(
						new TranslationTextComponent(Lib.DESC_INFO+"fluidStored", fs.getDisplayName(), fs.getAmount()),
						TextFormatting.GRAY
				));
		}
	}


	private static final IDispenseItemBehavior MINECART_DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior()
	{
		private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();

		/**
		 * Dispense the specified stack, play the dispense sound and spawn particles.
		 */
		public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
		{
			Direction direction = source.getBlockState().get(DispenserBlock.FACING);
			World world = source.getWorld();
			double d0 = source.getX()+(double)direction.getXOffset()*1.125D;
			double d1 = Math.floor(source.getY())+(double)direction.getYOffset();
			double d2 = source.getZ()+(double)direction.getZOffset()*1.125D;
			BlockPos blockpos = source.getBlockPos().offset(direction);
			BlockState blockstate = world.getBlockState(blockpos);
			RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock?((AbstractRailBlock)blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null): RailShape.NORTH_SOUTH;
			double d3;
			if(blockstate.isIn(BlockTags.RAILS))
			{
				if(railshape.isAscending())
					d3 = 0.6D;
				else
					d3 = 0.1D;
			}
			else
			{
				if(!blockstate.isAir(world, blockpos)||!world.getBlockState(blockpos.down()).isIn(BlockTags.RAILS))
					return this.behaviourDefaultDispenseItem.dispense(source, stack);

				BlockState blockstate1 = world.getBlockState(blockpos.down());
				RailShape railshape1 = blockstate1.getBlock() instanceof AbstractRailBlock?((AbstractRailBlock)blockstate1.getBlock()).getRailDirection(blockstate1, world, blockpos.down(), null): RailShape.NORTH_SOUTH;
				if(direction!=Direction.DOWN&&railshape1.isAscending())
					d3 = -0.4D;
				else
					d3 = -0.9D;
			}

			IEMinecartEntity minecartEntity = ((IEMinecartItem)stack.getItem()).createCart(world, d0, d1+d3, d2, stack);
			if(stack.hasDisplayName())
				minecartEntity.setCustomName(stack.getDisplayName());
			minecartEntity.readTileFromItem(null, stack);

			world.addEntity(minecartEntity);
			stack.shrink(1);
			return stack;
		}

		@Override
		protected void playDispenseSound(IBlockSource source)
		{
			source.getWorld().playEvent(1000, source.getBlockPos(), 0);
		}
	};
}