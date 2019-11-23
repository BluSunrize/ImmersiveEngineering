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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
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
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author BluSunrize - 22.02.2017
 */
public class IEFluid extends FlowingFluid
{
	protected final String fluidName;
	protected final ResourceLocation stillTex;
	protected final ResourceLocation flowingTex;
	protected Fluid flowing;
	protected Fluid source;
	@Nullable
	private final Consumer<FluidAttributes.Builder> buildAttributes;
	@Nullable
	protected final Supplier<Block> block;

	public IEFluid(String fluidName, ResourceLocation stillTex, ResourceLocation flowingTex)
	{
		this(fluidName, stillTex, flowingTex, null);
	}

	public IEFluid(String fluidName, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes)
	{
		this(fluidName, stillTex, flowingTex, buildAttributes, null);
	}

	public IEFluid(String fluidName, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes, @Nullable Supplier<Block> block)
	{
		this(fluidName, stillTex, flowingTex, buildAttributes, block, true);
	}

	public IEFluid(String fluidName, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes, @Nullable Supplier<Block> block, boolean isSource)
	{
		this.fluidName = fluidName;
		this.stillTex = stillTex;
		this.flowingTex = flowingTex;
		this.buildAttributes = buildAttributes;
		this.block = block;
		IEContent.registeredIEFluids.add(this);
		if(!isSource)
		{
			flowing = this;
			setRegistryName(ImmersiveEngineering.MODID, fluidName+"_flowing");
		}
		else
		{
			source = this;
			source = createFlowingVariant();
			setRegistryName(ImmersiveEngineering.MODID, fluidName);
		}
	}

	@Override
	protected void fillStateContainer(Builder<Fluid, IFluidState> builder)
	{
		super.fillStateContainer(builder);
		if(flowing==this)
		{
			builder.add(LEVEL_1_8);
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
		//TODO universal buckets?
		return Items.AIR;
	}

	@Override
	protected boolean canDisplace(IFluidState p_215665_1_, IBlockReader p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_)
	{
		//TODO based on the water version. What is this method even?
		return p_215665_5_==Direction.DOWN&&p_215665_4_!=source&&p_215665_4_!=flowing;
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
		if(block!=null)
			return block.get().getDefaultState().with(FlowingFluidBlock.LEVEL, getLevelFromState(state));
		else
			return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isSource(IFluidState state)
	{
		return state.getFluid()==source;
	}

	@Override
	public int getLevel(IFluidState p_207192_1_)
	{
		return (Integer)p_207192_1_.get(LEVEL_1_8);

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


	protected Fluid createFlowingVariant()
	{
		IEFluid ret = new IEFluid(fluidName, stillTex, flowingTex, buildAttributes, block, false);
		ret.flowing = this;
		return ret;
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
