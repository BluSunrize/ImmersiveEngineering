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
import blusunrize.immersiveengineering.common.util.GenericDeferredWork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
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
	private int burnTime = -1;

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
					.stacksTo(1)
					.tab(ImmersiveEngineering.ITEM_GROUP)
					.craftRemainder(Items.BUCKET))
			{
				@Override
				public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
				{
					return new FluidBucketWrapper(stack);
				}

				@Override
				public int getBurnTime(ItemStack itemStack)
				{
					return burnTime;
				}
			};
			this.bucket.setRegistryName(ImmersiveEngineering.MODID, fluidName+"_bucket");
			IEContent.registeredIEItems.add(this.bucket);
			GenericDeferredWork.registerDispenseBehavior(this.bucket, BUCKET_DISPENSE_BEHAVIOR);
			flowing = createFlowingVariant();
			setRegistryName(ImmersiveEngineering.MODID, fluidName);
			IE_FLUIDS.add(this);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void addTooltipInfo(FluidStack fluidStack, @Nullable Player player, List<Component> tooltip)
	{
	}

	@Nonnull
	@Override
	public Item getBucket()
	{
		return bucket;
	}

	@Override
	protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockReader, BlockPos pos, Fluid fluidIn, Direction direction)
	{
		return direction==Direction.DOWN&&!isSame(fluidIn);
	}

	@Override
	public boolean isSame(Fluid fluidIn)
	{
		return fluidIn==source||fluidIn==flowing;
	}

	//TODO all copied from water. Maybe make configurable?
	@Override
	public int getTickDelay(LevelReader p_205569_1_)
	{
		return 5;
	}

	@Override
	protected float getExplosionResistance()
	{
		return 100;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state)
	{
		return block.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
	}

	@Override
	public boolean isSource(FluidState state)
	{
		return state.getType()==source;
	}

	@Override
	public int getAmount(FluidState state)
	{
		if(isSource(state))
			return 8;
		else
			return state.getValue(LEVEL);
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
	public Fluid getFlowing()
	{
		return flowing;
	}

	@Nonnull
	@Override
	public Fluid getSource()
	{
		return source;
	}

	@Override
	protected boolean canConvertToSource()
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

	public void setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
	}

	protected IEFluid createFlowingVariant()
	{
		IEFluid ret = new IEFluid(fluidName, stillTex, flowingTex, buildAttributes, false)
		{
			@Override
			protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder)
			{
				super.createFluidStateDefinition(builder);
				builder.add(LEVEL);
			}
		};
		ret.source = this;
		ret.bucket = bucket;
		ret.block = block;
		ret.registerDefaultState(ret.getStateDefinition().any().setValue(LEVEL, 7));
		return ret;
	}

	public static Consumer<FluidAttributes.Builder> createBuilder(int density, int viscosity)
	{
		return builder -> {
			builder.viscosity(viscosity)
					.density(density);
		};
	}

	public static final EntityDataSerializer<Optional<FluidStack>> OPTIONAL_FLUID_STACK = new EntityDataSerializer<Optional<FluidStack>>()
	{
		@Override
		public void write(FriendlyByteBuf buf, Optional<FluidStack> value)
		{
			buf.writeBoolean(value.isPresent());
			value.ifPresent(fs -> buf.writeNbt(fs.writeToNBT(new CompoundTag())));
		}

		@Nonnull
		@Override
		public Optional<FluidStack> read(FriendlyByteBuf buf)
		{
			FluidStack fs = !buf.readBoolean()?null: FluidStack.loadFluidStackFromNBT(buf.readNbt());
			return Optional.ofNullable(fs);
		}

		@Override
		public EntityDataAccessor<Optional<FluidStack>> createAccessor(int id)
		{
			return new EntityDataAccessor<>(id, this);
		}

		@Override
		public Optional<FluidStack> copy(Optional<FluidStack> value)
		{
			return value.map(FluidStack::copy);
		}
	};

	public static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior()
	{
		private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

		public ItemStack execute(BlockSource source, ItemStack stack)
		{
			BucketItem bucketitem = (BucketItem)stack.getItem();
			BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
			Level world = source.getLevel();
			if(bucketitem.emptyBucket(null, world, blockpos, null))
			{
				bucketitem.checkExtraContent(world, stack, blockpos);
				return new ItemStack(Items.BUCKET);
			}
			else
				return this.defaultBehavior.dispense(source, stack);
		}
	};
}
