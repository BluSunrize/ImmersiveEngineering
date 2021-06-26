/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.fluids.IEFluid.FluidConstructor;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.util.GenericDeferredWork;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes.Builder;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.common.fluids.IEFluid.BUCKET_DISPENSE_BEHAVIOR;
import static blusunrize.immersiveengineering.common.fluids.IEFluid.createBuilder;

public class IEFluids
{
	public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, Lib.MODID);
	public static final List<FluidEntry> ALL_ENTRIES = new ArrayList<>();
	public static final Set<BlockEntry<?>> ALL_FLUID_BLOCKS = new HashSet<>();

	public static final FluidEntry fluidCreosote = new FluidEntry(
			"creosote", 800, rl("block/fluid/creosote_still"), rl("block/fluid/creosote_flow")
	);
	public static final FluidEntry fluidPlantoil = new FluidEntry(
			"plantoil", rl("block/fluid/plantoil_still"), rl("block/fluid/plantoil_flow")
	);
	public static final FluidEntry fluidEthanol = new FluidEntry(
			"ethanol", rl("block/fluid/ethanol_still"), rl("block/fluid/ethanol_flow")
	);
	public static final FluidEntry fluidBiodiesel = new FluidEntry(
			"biodiesel", rl("block/fluid/biodiesel_still"), rl("block/fluid/biodiesel_flow")
	);
	public static final FluidEntry fluidConcrete = new FluidEntry(
			"concrete", rl("block/fluid/concrete_still"), rl("block/fluid/concrete_flow"),
			ConcreteFluid::new, ConcreteFluid.Flowing::new, createBuilder(2400, 4000),
			ImmutableList.of(IEProperties.INT_16)
	);
	public static final FluidEntry fluidHerbicide = new FluidEntry(
			"herbicide", rl("block/fluid/herbicide_still"), rl("block/fluid/herbicide_flow")
	);
	public static final RegistryObject<PotionFluid> fluidPotion = REGISTER.register("potion", PotionFluid::new);

	public static class FluidEntry
	{
		private final RegistryObject<IEFluid> flowing;
		private final RegistryObject<IEFluid> still;
		private final BlockEntry<IEFluidBlock> block;
		private final RegistryObject<BucketItem> bucket;
		private final List<Property<?>> properties;

		private FluidEntry(String name, ResourceLocation stillTex, ResourceLocation flowingTex)
		{
			this(name, 0, stillTex, flowingTex);
		}

		private FluidEntry(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex)
		{
			this(name, burnTime, stillTex, flowingTex, null);
		}

		private FluidEntry(
				String name, int burnTime,
				ResourceLocation stillTex, ResourceLocation flowingTex,
				@Nullable Consumer<Builder> buildAttributes
		)
		{
			this(
					name, burnTime, stillTex, flowingTex, IEFluid::new, IEFluid.Flowing::new, buildAttributes,
					ImmutableList.of()
			);
		}

		private FluidEntry(
				String name, ResourceLocation stillTex, ResourceLocation flowingTex,
				IEFluid.FluidConstructor makeStill, IEFluid.FluidConstructor makeFlowing,
				@Nullable Consumer<Builder> buildAttributes, ImmutableList<Property<?>> properties
		)
		{
			this(name, 0, stillTex, flowingTex, makeStill, makeFlowing, buildAttributes, properties);
		}

		private FluidEntry(
				String name, int burnTime,
				ResourceLocation stillTex, ResourceLocation flowingTex,
				FluidConstructor makeStill, FluidConstructor makeFlowing,
				@Nullable Consumer<Builder> buildAttributes, List<Property<?>> properties)
		{
			this.properties = properties;
			Mutable<FluidEntry> thisMutable = new MutableObject<>();
			this.still = REGISTER.register(name, () -> IEFluid.makeFluid(
					makeStill, thisMutable.getValue(), stillTex, flowingTex, buildAttributes
			));
			this.flowing = REGISTER.register(name+"_flowing", () -> IEFluid.makeFluid(
					makeFlowing, thisMutable.getValue(), stillTex, flowingTex, buildAttributes
			));
			this.block = new IEBlocks.BlockEntry<>(name+"_fluid_block", () -> Properties.create(Material.WATER), p -> new IEFluidBlock(thisMutable.getValue(), p));
			this.bucket = IEItems.REGISTER.register(name+"_bucket", () -> makeBucket(still, burnTime));
			thisMutable.setValue(this);
			ALL_FLUID_BLOCKS.add(block);
			ALL_ENTRIES.add(this);
		}

		public IEFluid getFlowing()
		{
			return flowing.get();
		}

		public IEFluid getStill()
		{
			return still.get();
		}

		public IEFluidBlock getBlock()
		{
			return block.get();
		}

		public BucketItem getBucket()
		{
			return bucket.get();
		}

		public List<Property<?>> getProperties()
		{
			return properties;
		}

		private static BucketItem makeBucket(RegistryObject<IEFluid> still, int burnTime)
		{
			BucketItem result = new BucketItem(
					still, new Item.Properties()
					.maxStackSize(1)
					.group(ImmersiveEngineering.ITEM_GROUP)
					.containerItem(Items.BUCKET))
			{
				@Override
				public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
				{
					return new FluidBucketWrapper(stack);
				}

				@Override
				public int getBurnTime(ItemStack itemStack)
				{
					return burnTime;
				}
			};
			GenericDeferredWork.registerDispenseBehavior(result, BUCKET_DISPENSE_BEHAVIOR);
			return result;
		}

		public RegistryObject<IEFluid> getStillGetter()
		{
			return still;
		}
	}
}
