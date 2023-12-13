/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.fluids.ConcreteFluid;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import blusunrize.immersiveengineering.common.fluids.IEFluidBlock;
import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.common.fluids.IEFluid.createBuilder;

public class IEFluids
{
	public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(BuiltInRegistries.FLUID, Lib.MODID);
	public static final DeferredRegister<FluidType> TYPE_REGISTER = DeferredRegister.create(
			NeoForgeRegistries.Keys.FLUID_TYPES, Lib.MODID
	);
	public static final List<FluidEntry> ALL_ENTRIES = new ArrayList<>();
	public static final Set<BlockEntry<? extends LiquidBlock>> ALL_FLUID_BLOCKS = new HashSet<>();

	public static final FluidEntry CREOSOTE = FluidEntry.make(
			"creosote", 800, rl("block/fluid/creosote_still"), rl("block/fluid/creosote_flow")
	);
	public static final FluidEntry PLANTOIL = FluidEntry.make(
			"plantoil", rl("block/fluid/plantoil_still"), rl("block/fluid/plantoil_flow")
	);
	public static final FluidEntry ETHANOL = FluidEntry.make(
			"ethanol", rl("block/fluid/ethanol_still"), rl("block/fluid/ethanol_flow")
	);
	public static final FluidEntry BIODIESEL = FluidEntry.make(
			"biodiesel", rl("block/fluid/biodiesel_still"), rl("block/fluid/biodiesel_flow")
	);
	public static final FluidEntry CONCRETE = FluidEntry.make(
			"concrete", rl("block/fluid/concrete_still"), rl("block/fluid/concrete_flow"),
			ConcreteFluid::new, ConcreteFluid.Flowing::new, createBuilder(2400, 4000),
			ImmutableList.of(IEProperties.INT_32)
	);
	public static final FluidEntry HERBICIDE = FluidEntry.make(
			"herbicide", rl("block/fluid/herbicide_still"), rl("block/fluid/herbicide_flow")
	);
	public static final FluidEntry REDSTONE_ACID = FluidEntry.make(
			"redstone_acid", rl("block/fluid/redstone_acid_still"), rl("block/fluid/redstone_acid_flow")
	);
	public static final Holder<FluidType> POTION_TYPE = TYPE_REGISTER.register("potion", PotionFluid.PotionFluidType::new);
	public static final DeferredHolder<Fluid, PotionFluid> POTION = REGISTER.register("potion", PotionFluid::new);
	public static final FluidEntry ACETALDEHYDE = FluidEntry.make(
			"acetaldehyde", rl("block/fluid/acetaldehyde_still"), rl("block/fluid/acetaldehyde_flow"),
			createBuilder(788, 210)
	);
	public static final FluidEntry PHENOLIC_RESIN = FluidEntry.make(
			"phenolic_resin", rl("block/fluid/resin_still"), rl("block/fluid/resin_flow"),
			createBuilder(1100, 2800)
	);

	public static void registerBucketCapabilities(RegisterCapabilitiesEvent event)
	{
		for(FluidEntry entry : ALL_ENTRIES)
			event.registerItem(
					FluidHandler.ITEM,
					(stack, $) -> new FluidBucketWrapper(stack),
					entry.bucket.get()
			);
	}

	public record FluidEntry(
			DeferredHolder<Fluid, IEFluid> flowing,
			DeferredHolder<Fluid, IEFluid> still,
			BlockEntry<IEFluidBlock> block,
			DeferredHolder<Item, BucketItem> bucket,
			Holder<FluidType> type,
			List<Property<?>> properties
	)
	{
		private static FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex)
		{
			return make(name, 0, stillTex, flowingTex);
		}

		private static FluidEntry make(
				String name, ResourceLocation stillTex, ResourceLocation flowingTex, Consumer<FluidType.Properties> buildAttributes
		)
		{
			return make(name, 0, stillTex, flowingTex, buildAttributes);
		}

		private static FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex)
		{
			return make(name, burnTime, stillTex, flowingTex, null);
		}

		private static FluidEntry make(
				String name, int burnTime,
				ResourceLocation stillTex, ResourceLocation flowingTex,
				@Nullable Consumer<FluidType.Properties> buildAttributes
		)
		{
			return make(
					name, burnTime, stillTex, flowingTex, IEFluid::new, IEFluid.Flowing::new, buildAttributes,
					ImmutableList.of()
			);
		}

		private static FluidEntry make(
				String name, ResourceLocation stillTex, ResourceLocation flowingTex,
				Function<FluidEntry, ? extends IEFluid> makeStill, Function<FluidEntry, ? extends IEFluid> makeFlowing,
				@Nullable Consumer<FluidType.Properties> buildAttributes, ImmutableList<Property<?>> properties
		)
		{
			return make(name, 0, stillTex, flowingTex, makeStill, makeFlowing, buildAttributes, properties);
		}

		private static FluidEntry make(
				String name, int burnTime,
				ResourceLocation stillTex, ResourceLocation flowingTex,
				Function<FluidEntry, ? extends IEFluid> makeStill, Function<FluidEntry, ? extends IEFluid> makeFlowing,
				@Nullable Consumer<FluidType.Properties> buildAttributes, List<Property<?>> properties)
		{
			FluidType.Properties builder = FluidType.Properties.create()
					.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
					.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
			if(buildAttributes!=null)
				buildAttributes.accept(builder);
			Holder<FluidType> type = TYPE_REGISTER.register(
					name, () -> makeTypeWithTextures(builder, stillTex, flowingTex)
			);
			Mutable<FluidEntry> thisMutable = new MutableObject<>();
			DeferredHolder<Fluid, IEFluid> still = REGISTER.register(name, () -> IEFluid.makeFluid(
					makeStill, thisMutable.getValue()
			));
			DeferredHolder<Fluid, IEFluid> flowing = REGISTER.register(name+"_flowing", () -> IEFluid.makeFluid(
					makeFlowing, thisMutable.getValue()
			));
			BlockEntry<IEFluidBlock> block = new IEBlocks.BlockEntry<>(
					name+"_fluid_block",
					() -> Properties.ofFullCopy(Blocks.WATER),
					p -> new IEFluidBlock(thisMutable.getValue(), p)
			);
			DeferredHolder<Item, BucketItem> bucket = IEItems.REGISTER.register(name+"_bucket", () -> makeBucket(still, burnTime));
			FluidEntry entry = new FluidEntry(flowing, still, block, bucket, type, properties);
			thisMutable.setValue(entry);
			ALL_FLUID_BLOCKS.add(block);
			ALL_ENTRIES.add(entry);
			return entry;
		}

		private static FluidType makeTypeWithTextures(
				FluidType.Properties builder, ResourceLocation stillTex, ResourceLocation flowingTex
		)
		{
			return new FluidType(builder)
			{
				@Override
				public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
				{
					consumer.accept(new IClientFluidTypeExtensions()
					{
						@Override
						public ResourceLocation getStillTexture()
						{
							return stillTex;
						}

						@Override
						public ResourceLocation getFlowingTexture()
						{
							return flowingTex;
						}
					});
				}
			};
		}

		public IEFluid getFlowing()
		{
			return flowing.value();
		}

		public IEFluid getStill()
		{
			return still.value();
		}

		public IEFluidBlock getBlock()
		{
			return block.get();
		}

		public BucketItem getBucket()
		{
			return bucket.value();
		}

		private static BucketItem makeBucket(Supplier<IEFluid> still, int burnTime)
		{
			return new BucketItem(
					still, new Item.Properties()
					.stacksTo(1)
					.craftRemainder(Items.BUCKET))
			{
				@Override
				public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
				{
					return burnTime;
				}
			};
		}

		public DeferredHolder<Fluid, IEFluid> getStillGetter()
		{
			return still;
		}
	}
}
