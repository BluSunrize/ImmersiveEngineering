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
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEFluids;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
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
		setRenderLayer(StoneDecoration.INSULATING_GLASS, RenderType.translucent());
		setRenderLayer(StoneDecoration.CONCRETE_SPRAYED, RenderType.cutout());
		setRenderLayer(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), RenderType.solid(), RenderType.translucent());
		setRenderLayer(MetalDevices.FLOODLIGHT, RenderType.solid(), RenderType.translucent());

		for(MetalScaffoldingType type : MetalScaffoldingType.values())
			for(BlockEntry<?> block : ImmutableList.of(MetalDecoration.ALU_SCAFFOLDING.get(type), MetalDecoration.STEEL_SCAFFOLDING.get(type)))
				setRenderLayer(block, RenderType.cutout());
		setRenderLayer(WoodenDecoration.TREATED_SCAFFOLDING, RenderType.cutout());
		setRenderLayer(WoodenDevices.LOGIC_UNIT, RenderType.solid(), RenderType.translucent());
		setRenderLayer(Connectors.ENERGY_CONNECTORS.get(ImmutablePair.of(WireType.HV_CATEGORY, true)), RenderType.solid(), RenderType.translucent());
		setRenderLayer(Connectors.CONNECTOR_BUNDLED, RenderType.solid(), RenderType.translucent(), RenderType.cutout());
		setRenderLayer(Connectors.CONNECTOR_PROBE, RenderType.solid(), RenderType.translucent(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(Connectors.FEEDTHROUGH.get(), rt -> true);
		setRenderLayer(MetalDevices.RAZOR_WIRE, RenderType.cutout());
		setRenderLayer(MetalDevices.FLUID_PLACER, RenderType.cutout());
		setRenderLayer(MetalDevices.FURNACE_HEATER, RenderType.cutout());
		setRenderLayer(MetalDevices.FLUID_PIPE, RenderType.cutout());
		setRenderLayer(MetalDevices.SAMPLE_DRILL, RenderType.cutout());
		setRenderLayer(MetalDevices.CLOCHE, RenderType.solid(), RenderType.translucent());
		setRenderLayer(MetalDecoration.ALU_SLOPE, RenderType.cutout());
		setRenderLayer(MetalDecoration.STEEL_SLOPE, RenderType.cutout());
		for(BlockEntry<ConveyorBlock> b : MetalDevices.CONVEYORS.values())
			setRenderLayer(b, RenderType.cutout());
		setRenderLayer(MetalDevices.CHARGING_STATION, RenderType.solid(), RenderType.translucent());
		setRenderLayer(Multiblocks.TANK, RenderType.cutoutMipped());
		setRenderLayer(Multiblocks.DIESEL_GENERATOR, RenderType.cutoutMipped());
		setRenderLayer(Multiblocks.BOTTLING_MACHINE, RenderType.solid(), RenderType.translucent());

		for(Map<ResourceLocation, ? extends BlockEntry<? extends Block>> map : ImmutableList.of(IEBlocks.TO_SLAB, IEBlocks.TO_STAIRS))
			for(Entry<ResourceLocation, ? extends BlockEntry<? extends Block>> slab : map.entrySet())
			{
				Supplier<Block> baseBlock = Suppliers.memoize(() -> ForgeRegistries.BLOCKS.getValue(slab.getKey()));
				ItemBlockRenderTypes.setRenderLayer(
						slab.getValue().get(),
						rt -> ItemBlockRenderTypes.canRenderInLayer(baseBlock.get().defaultBlockState(), rt)
				);
			}
		setRenderLayer(Cloth.BALLOON, RenderType.solid(), RenderType.translucent());
		for(CoverType cover : CoverType.values())
			setRenderLayer(MetalDecoration.METAL_LADDER.get(cover), RenderType.cutout());

		setRenderLayer(Misc.HEMP_PLANT, RenderType.cutout());
		setRenderLayer(Misc.POTTED_HEMP, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(IEFluids.POTION.get(), RenderType.translucent());
		for(RegistryObject<Fluid> f : IEFluids.REGISTER.getEntries())
			if(f.get()!=IEFluids.CONCRETE.getFlowing()&&f.get()!=IEFluids.CONCRETE.getStill())
				ItemBlockRenderTypes.setRenderLayer(f.get(), RenderType.translucent());
	}

	private static void setRenderLayer(Supplier<? extends Block> supplier, RenderType type)
	{
		ItemBlockRenderTypes.setRenderLayer(supplier.get(), type);
	}

	private static void setRenderLayer(Supplier<? extends Block> supplier, RenderType... types)
	{
		ItemBlockRenderTypes.setRenderLayer(supplier.get(), t -> {
			for(RenderType allowed : types)
				if(t==allowed)
					return true;
			return false;
		});
	}
}
