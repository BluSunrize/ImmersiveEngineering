/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fluids;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 22.02.2017
 */
public class IEFluid extends FlowingFluid
{
	public static final Collection<IEFluid> IE_FLUIDS = new ArrayList<>();
	protected final String fluidName;
	protected final ResourceLocation stillTex;
	protected final ResourceLocation flowingTex;
	protected IEFluid flowing;
	protected IEFluid source;
	@Nullable
	protected final Consumer<FluidAttributes.Builder> buildAttributes;
	public IEFluidBlock block;
	protected Item bucket;

	public IEFluid(String fluidName, ResourceLocation stillTex, ResourceLocation flowingTex)
	{
		this(fluidName, stillTex, flowingTex, null);
	}

	public IEFluid(String fluidName, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes)
	{
		this(fluidName, stillTex, flowingTex, buildAttributes, true);
	}

	public IEFluid(String fluidName, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes, boolean isSource)
	{
		this.fluidName = fluidName;
		this.stillTex = stillTex;
		this.flowingTex = flowingTex;
		this.buildAttributes = buildAttributes;
		IEContent.registeredIEFluids.add(this);
		if(!isSource)
		{
			flowing = this;
			setRegistryName(ImmersiveEngineering.MODID, fluidName+"_flowing");
		}
		else
		{
			source = this;
			this.block = new IEFluidBlock(this);
			this.block.setRegistryName(ImmersiveEngineering.MODID, fluidName+"_fluid_block");
			IEContent.registeredIEBlocks.add(this.block);
			this.bucket = new BucketItem(() -> this.source, new Item.Properties()
					.maxStackSize(1)
					.group(ImmersiveEngineering.itemGroup))
			{
				@Override
				public ItemStack getContainerItem(ItemStack itemStack)
				{
					return new ItemStack(Items.BUCKET);
				}

				@Override
				public boolean hasContainerItem(ItemStack stack)
				{
					return true;
				}

				@Override
				public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
				{
					return new FluidBucketWrapper(stack);
				}
			};
			this.bucket.setRegistryName(ImmersiveEngineering.MODID, fluidName+"_bucket");
			IEContent.registeredIEItems.add(this.bucket);
			flowing = createFlowingVariant();
			setRegistryName(ImmersiveEngineering.MODID, fluidName);
			IE_FLUIDS.add(this);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void addTooltipInfo(FluidStack fluidStack, @Nullable PlayerEntity player, List<ITextComponent> tooltip)
	{
	}

	@Nonnull
	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Nonnull
	@Override
	public Item getFilledBucket()
	{
		return bucket;
	}

	@Override
	protected boolean canDisplace(IFluidState p_215665_1_, IBlockReader p_215665_2_, BlockPos p_215665_3_, Fluid fluidIn, Direction direction)
	{
		return direction==Direction.DOWN&&!isEquivalentTo(fluidIn);
	}

	@Override
	public boolean isEquivalentTo(Fluid fluidIn)
	{
		return fluidIn==source||fluidIn==flowing;
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
	protected BlockState getBlockState(IFluidState state)
	{
		return block.getDefaultState().with(FlowingFluidBlock.LEVEL, getLevelFromState(state));
	}

	@Override
	public boolean isSource(IFluidState state)
	{
		return state.getFluid()==source;
	}

	@Override
	public int getLevel(IFluidState p_207192_1_)
	{
		if(isSource(p_207192_1_))
			return 8;
		else
			return p_207192_1_.get(LEVEL_1_8);
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
		return flowing;
	}

	@Nonnull
	@Override
	public Fluid getStillFluid()
	{
		return source;
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

	protected IEFluid createFlowingVariant()
	{
		IEFluid ret = new IEFluid(fluidName, stillTex, flowingTex, buildAttributes, false)
		{
			@Override
			protected void fillStateContainer(Builder<Fluid, IFluidState> builder)
			{
				super.fillStateContainer(builder);
				builder.add(LEVEL_1_8);
			}
		};
		ret.source = this;
		ret.bucket = bucket;
		ret.block = block;
		ret.setDefaultState(ret.getStateContainer().getBaseState().with(LEVEL_1_8, 7));
		return ret;
	}

	public static Consumer<FluidAttributes.Builder> createBuilder(int density, int viscosity)
	{
		return builder -> {
			builder.viscosity(viscosity)
					.density(density);
		};
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

}
