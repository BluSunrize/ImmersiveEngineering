/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.fluids.IEFluids;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, value = {Dist.CLIENT}, bus = Bus.MOD)
public class BlockRenderLayers
{
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent ev)
	{
		setRenderLayer(StoneDecoration.insulatingGlass, RenderType.getTranslucent());
		setRenderLayer(StoneDecoration.concreteSprayed, RenderType.getCutout());
		setRenderLayer(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), RenderType.getSolid(), RenderType.getTranslucent());
		setRenderLayer(MetalDevices.floodlight, RenderType.getSolid(), RenderType.getTranslucent());

		for(MetalScaffoldingType type : MetalScaffoldingType.values())
			for (BlockEntry<?> block : ImmutableList.of(MetalDecoration.aluScaffolding.get(type), MetalDecoration.steelScaffolding.get(type)))
				setRenderLayer(block, RenderType.getCutout());
		setRenderLayer(WoodenDecoration.treatedScaffolding, RenderType.getCutout());
		setRenderLayer(WoodenDevices.logicUnit, RenderType.getSolid(), RenderType.getTranslucent());
		setRenderLayer(Connectors.ENERGY_CONNECTORS.get(ImmutablePair.of(WireType.HV_CATEGORY, true)), RenderType.getSolid(), RenderType.getTranslucent());
		setRenderLayer(Connectors.connectorBundled, RenderType.getSolid(), RenderType.getTranslucent(), RenderType.getCutout());
		setRenderLayer(Connectors.connectorProbe, RenderType.getSolid(), RenderType.getTranslucent(), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(Connectors.feedthrough.get(), rt -> true);
		setRenderLayer(MetalDevices.razorWire, RenderType.getCutout());
		setRenderLayer(MetalDevices.fluidPlacer, RenderType.getCutout());
		setRenderLayer(MetalDevices.furnaceHeater, RenderType.getCutout());
		setRenderLayer(MetalDevices.fluidPipe, RenderType.getCutout());
		setRenderLayer(MetalDevices.cloche, RenderType.getCutout());
		setRenderLayer(MetalDevices.sampleDrill, RenderType.getCutout());
		setRenderLayer(MetalDevices.cloche, RenderType.getSolid(), RenderType.getTranslucent());
		setRenderLayer(MetalDecoration.slopeAlu, RenderType.getCutout());
		setRenderLayer(MetalDecoration.slopeSteel, RenderType.getCutout());
		for(BlockEntry<ConveyorBlock> b : MetalDevices.CONVEYORS.values())
			setRenderLayer(b, RenderType.getCutout());
		setRenderLayer(MetalDevices.chargingStation, RenderType.getSolid(), RenderType.getTranslucent());
		setRenderLayer(Multiblocks.tank, RenderType.getCutoutMipped());
		setRenderLayer(Multiblocks.bottlingMachine, RenderType.getSolid(), RenderType.getTranslucent());

		for(Map<ResourceLocation, ? extends BlockEntry<? extends Block>> map : ImmutableList.of(IEBlocks.toSlab, IEBlocks.toStairs))
			for(Entry<ResourceLocation, ? extends BlockEntry<? extends Block>> slab : map.entrySet())
			{
				Supplier<Block> baseBlock = Suppliers.memoize(() -> ForgeRegistries.BLOCKS.getValue(slab.getKey()));
				RenderTypeLookup.setRenderLayer(
						slab.getValue().get(),
						rt -> RenderTypeLookup.canRenderInLayer(baseBlock.get().getDefaultState(), rt)
				);
			}
		setRenderLayer(Cloth.balloon, RenderType.getSolid(), RenderType.getTranslucent());
		for(CoverType cover : CoverType.values())
			setRenderLayer(MetalDecoration.metalLadder.get(cover), RenderType.getCutout());

		setRenderLayer(Misc.hempPlant, RenderType.getCutout());
		setRenderLayer(Misc.pottedHemp, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IEFluids.fluidPotion.get(), RenderType.getTranslucent());
		for(RegistryObject<Fluid> f : IEFluids.REGISTER.getEntries())
			if(f.get()!=IEFluids.fluidConcrete.getFlowing()&&f.get()!=IEFluids.fluidConcrete.getStill())
				RenderTypeLookup.setRenderLayer(f.get(), RenderType.getTranslucent());
	}

	private static void setRenderLayer(Supplier<? extends Block> supplier, RenderType type)
	{
		RenderTypeLookup.setRenderLayer(supplier.get(), type);
	}

	private static void setRenderLayer(Supplier<? extends Block> supplier, RenderType... types)
	{
		RenderTypeLookup.setRenderLayer(supplier.get(), t -> {
			for(RenderType allowed : types)
				if(t==allowed)
					return true;
			return false;
		});
	}
}
