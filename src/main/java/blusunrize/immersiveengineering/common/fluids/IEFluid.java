/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.common.fluids.IEFluids.FluidEntry;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 22.02.2017
 */
public class IEFluid extends FlowingFluid
{
	private static FluidEntry entryStatic;
	protected final FluidEntry entry;
	protected final ResourceLocation stillTex;
	protected final ResourceLocation flowingTex;
	@Nullable
	protected final Consumer<FluidAttributes.Builder> buildAttributes;

	public static IEFluid makeFluid(
			FluidConstructor make,
			IEFluids.FluidEntry entry, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes
	)
	{
		entryStatic = entry;
		IEFluid result = make.create(entry, stillTex, flowingTex, buildAttributes);
		entryStatic = null;
		return result;
	}

	public IEFluid(IEFluids.FluidEntry entry, ResourceLocation stillTex, ResourceLocation flowingTex)
	{
		this(entry, stillTex, flowingTex, null);
	}

	public IEFluid(IEFluids.FluidEntry entry, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes)
	{
		this.entry = entry;
		this.stillTex = stillTex;
		this.flowingTex = flowingTex;
		this.buildAttributes = buildAttributes;
	}

	@OnlyIn(Dist.CLIENT)
	public void addTooltipInfo(FluidStack fluidStack, @Nullable PlayerEntity player, List<ITextComponent> tooltip)
	{
	}

	@Nonnull
	@Override
	public Item getFilledBucket()
	{
		return entry.getBucket();
	}

	@Override
	protected boolean canDisplace(FluidState fluidState, IBlockReader blockReader, BlockPos pos, Fluid fluidIn, Direction direction)
	{
		return direction==Direction.DOWN&&!isEquivalentTo(fluidIn);
	}

	@Override
	public boolean isEquivalentTo(Fluid fluidIn)
	{
		return fluidIn==entry.getStill()||fluidIn==entry.getStill();
	}

	//TODO all copied from water. Maybe make configurable?
	@Override
	public int getTickRate(IWorldReader p_205569_1_)
	{
		return 5;
	}

	@Override
	protected float getExplosionResistance()
	{
		return 100;
	}

	@Override
	protected void fillStateContainer(Builder<Fluid, FluidState> builder)
	{
		super.fillStateContainer(builder);
		for(Property<?> p : (entry==null?entryStatic: entry).getProperties())
			builder.add(p);
	}

	@Override
	protected BlockState getBlockState(FluidState state)
	{
		BlockState result = entry.getBlock().getDefaultState().with(FlowingFluidBlock.LEVEL, getLevelFromState(state));
		for(Property<?> prop : entry.getProperties())
			result = IEFluidBlock.withCopiedValue(prop, result, state);
		return result;
	}

	@Override
	public boolean isSource(FluidState state)
	{
		return state.getFluid()==entry.getStill();
	}

	@Override
	public int getLevel(FluidState state)
	{
		if(isSource(state))
			return 8;
		else
			return state.get(LEVEL_1_8);
	}

	@Nonnull
	@Override
	protected FluidAttributes createAttributes()
	{
		FluidAttributes.Builder builder = FluidAttributes.builder(stillTex, flowingTex);
		if(buildAttributes!=null)
			buildAttributes.accept(builder);
		return builder.build(this);
	}

	@Nonnull
	@Override
	public Fluid getFlowingFluid()
	{
		return entry.getFlowing();
	}

	@Nonnull
	@Override
	public Fluid getStillFluid()
	{
		return entry.getStill();
	}

	@Override
	protected boolean canSourcesMultiply()
	{
		return false;
	}

	@Override
	protected void beforeReplacingBlock(IWorld iWorld, BlockPos blockPos, BlockState blockState)
	{

	}

	@Override
	protected int getSlopeFindDistance(IWorldReader iWorldReader)
	{
		return 4;
	}

	@Override
	protected int getLevelDecreasePerBlock(IWorldReader iWorldReader)
	{
		return 1;
	}

	public static Consumer<FluidAttributes.Builder> createBuilder(int density, int viscosity)
	{
		return builder -> builder.viscosity(viscosity).density(density);
	}

	public static class Flowing extends IEFluid
	{
		public Flowing(
				IEFluids.FluidEntry entry,
				ResourceLocation stillTex, ResourceLocation flowingTex,
				@Nullable Consumer<FluidAttributes.Builder> buildAttributes
		)
		{
			super(entry, stillTex, flowingTex, buildAttributes);
		}

		@Override
		protected void fillStateContainer(Builder<Fluid, FluidState> builder)
		{
			super.fillStateContainer(builder);
			builder.add(LEVEL_1_8);
		}
	}

	public interface FluidConstructor
	{
		IEFluid create(
				IEFluids.FluidEntry entry,
				ResourceLocation stillTex, ResourceLocation flowingTex,
				@Nullable Consumer<FluidAttributes.Builder> buildAttributes
		);
	}

	public static final IDataSerializer<Optional<FluidStack>> OPTIONAL_FLUID_STACK = new IDataSerializer<Optional<FluidStack>>()
	{
		@Override
		public void write(PacketBuffer buf, Optional<FluidStack> value)
		{
			buf.writeBoolean(value.isPresent());
			value.ifPresent(fs -> buf.writeCompoundTag(fs.writeToNBT(new CompoundNBT())));
		}

		@Nonnull
		@Override
		public Optional<FluidStack> read(PacketBuffer buf)
		{
			FluidStack fs = !buf.readBoolean()?null: FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
			return Optional.ofNullable(fs);
		}

		@Override
		public DataParameter<Optional<FluidStack>> createKey(int id)
		{
			return new DataParameter<>(id, this);
		}

		@Override
		public Optional<FluidStack> copyValue(Optional<FluidStack> value)
		{
			return value.map(FluidStack::copy);
		}
	};

	public static final IDispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior()
	{
		private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

		public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
		{
			BucketItem bucketitem = (BucketItem)stack.getItem();
			BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
			World world = source.getWorld();
			if(bucketitem.tryPlaceContainedLiquid(null, world, blockpos, null))
			{
				bucketitem.onLiquidPlaced(world, stack, blockpos);
				return new ItemStack(Items.BUCKET);
			}
			else
				return this.defaultBehavior.dispense(source, stack);
		}
	};
}
