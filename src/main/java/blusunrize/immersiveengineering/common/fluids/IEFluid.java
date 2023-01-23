/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEFluids.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author BluSunrize - 22.02.2017
 */
public class IEFluid extends FlowingFluid
{
	private static FluidEntry entryStatic;
	protected final FluidEntry entry;

	public static IEFluid makeFluid(Function<FluidEntry, ? extends IEFluid> make, IEFluids.FluidEntry entry)
	{
		entryStatic = entry;
		IEFluid result = make.apply(entry);
		entryStatic = null;
		return result;
	}

	public IEFluid(IEFluids.FluidEntry entry)
	{
		this.entry = entry;
	}

	@Nonnull
	@Override
	public Item getBucket()
	{
		return entry.getBucket();
	}

	@Override
	protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockReader, BlockPos pos, Fluid fluidIn, Direction direction)
	{
		return direction==Direction.DOWN&&!isSame(fluidIn);
	}

	@Override
	public boolean isSame(@Nonnull Fluid fluidIn)
	{
		return fluidIn==entry.getStill()||fluidIn==entry.getFlowing();
	}

	@Override
	public int getTickDelay(LevelReader p_205569_1_)
	{
		// viscosity delta to water (1000)
		int dW = this.getFlowing().getFluidType().getViscosity()-Fluids.WATER.getFluidType().getViscosity();
		// dW for water & lava is 5000, difference in tick delay is 25 -> 0.005 as a modifier
		double v = Math.round(5 + dW*0.005);
		return Math.max(2, (int)v);
	}

	@Override
	protected float getExplosionResistance()
	{
		return 100;
	}

	@Override
	protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder)
	{
		super.createFluidStateDefinition(builder);
		for(Property<?> p : (entry==null?entryStatic: entry).properties())
			builder.add(p);
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state)
	{
		BlockState result = entry.getBlock().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
		for(Property<?> prop : entry.properties())
			result = IEFluidBlock.withCopiedValue(prop, result, state);
		return result;
	}

	@Override
	public boolean isSource(FluidState state)
	{
		return state.getType()==entry.getStill();
	}

	@Override
	public int getAmount(FluidState state)
	{
		if(isSource(state))
			return 8;
		else
			return state.getValue(LEVEL);
	}

	@Override
	public FluidType getFluidType()
	{
		return entry.type().get();
	}

	@Nonnull
	@Override
	public Fluid getFlowing()
	{
		return entry.getFlowing();
	}

	@Nonnull
	@Override
	public Fluid getSource()
	{
		return entry.getStill();
	}

	@Override
	public boolean canConvertToSource(Level level)
	{
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor iWorld, BlockPos blockPos, BlockState blockState)
	{

	}

	@Override
	protected int getSlopeFindDistance(LevelReader iWorldReader)
	{
		return 4;
	}

	@Override
	protected int getDropOff(LevelReader iWorldReader)
	{
		return 1;
	}

	public static Consumer<FluidType.Properties> createBuilder(int density, int viscosity)
	{
		return builder -> builder.viscosity(viscosity).density(density);
	}

	public static class Flowing extends IEFluid
	{
		public Flowing(IEFluids.FluidEntry entry)
		{
			super(entry);
		}

		@Override
		protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder)
		{
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}
	}

	public static class EntityFluidSerializer implements EntityDataSerializer<FluidStack>
	{
		@Override
		public void write(FriendlyByteBuf buf, @Nonnull FluidStack value)
		{
			buf.writeFluidStack(value);
		}

		@Nonnull
		@Override
		public FluidStack read(FriendlyByteBuf buf)
		{
			return buf.readFluidStack();
		}

		@Nonnull
		@Override
		public FluidStack copy(FluidStack value)
		{
			return value.copy();
		}
	}

	public static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior()
	{
		private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

		public ItemStack execute(BlockSource source, ItemStack stack)
		{
			BucketItem bucketitem = (BucketItem)stack.getItem();
			BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
			Level world = source.getLevel();
			if(bucketitem.emptyContents(null, world, blockpos, null))
			{
				bucketitem.checkExtraContent(null, world, stack, blockpos);
				return new ItemStack(Items.BUCKET);
			}
			else
				return this.defaultBehavior.dispense(source, stack);
		}
	};
}
