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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map.Entry;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, value = {Dist.CLIENT}, bus = Bus.MOD)
public class BlockRenderLayers
{
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent ev)
	{
		ItemBlockRenderTypes.setRenderLayer(StoneDecoration.insulatingGlass, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(StoneDecoration.concreteSprayed, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(
				Connectors.getEnergyConnector(WireType.HV_CATEGORY, true),
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()
		);
		ItemBlockRenderTypes.setRenderLayer(
				MetalDevices.floodlight,
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()
		);

		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			ItemBlockRenderTypes.setRenderLayer(MetalDecoration.steelScaffoldingStair.get(type), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(MetalDecoration.aluScaffoldingStair.get(type), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(MetalDecoration.aluScaffolding.get(type), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(MetalDecoration.steelScaffolding.get(type), RenderType.cutout());
		}
		ItemBlockRenderTypes.setRenderLayer(WoodenDecoration.treatedScaffolding, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(
				WoodenDevices.logicUnit,
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()
		);
		ItemBlockRenderTypes.setRenderLayer(
				Connectors.ENERGY_CONNECTORS.get(ImmutablePair.of(WireType.HV_CATEGORY, true)),
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()
		);
		ItemBlockRenderTypes.setRenderLayer(
				Connectors.connectorBundled,
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()||rt==RenderType.cutout()
		);
		ItemBlockRenderTypes.setRenderLayer(
				Connectors.connectorProbe,
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()||rt==RenderType.cutout()
		);
		ItemBlockRenderTypes.setRenderLayer(
				Connectors.feedthrough,
				rt -> true
		);
		ItemBlockRenderTypes.setRenderLayer(MetalDevices.razorWire, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(MetalDevices.fluidPlacer, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(MetalDevices.furnaceHeater, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(MetalDevices.fluidPipe, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(MetalDevices.cloche, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(MetalDevices.sampleDrill, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(
				MetalDevices.cloche,
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()
		);
		ItemBlockRenderTypes.setRenderLayer(MetalDecoration.slopeAlu, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(MetalDecoration.slopeSteel, RenderType.cutout());
		for(Block b : MetalDevices.CONVEYORS.values())
			ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(
				MetalDevices.chargingStation,
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()
		);
		ItemBlockRenderTypes.setRenderLayer(Multiblocks.tank, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(Multiblocks.dieselGenerator, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(
				Multiblocks.bottlingMachine,
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()
		);

		for(Entry<Block, SlabBlock> slab : IEBlocks.toSlab.entrySet())
			ItemBlockRenderTypes.setRenderLayer(
					slab.getValue(),
					rt -> ItemBlockRenderTypes.canRenderInLayer(slab.getKey().defaultBlockState(), rt)
			);
		ItemBlockRenderTypes.setRenderLayer(
				Cloth.balloon,
				rt -> rt==RenderType.solid()||rt==RenderType.translucent()
		);
		for(CoverType cover : CoverType.values())
			ItemBlockRenderTypes.setRenderLayer(
					MetalDecoration.metalLadder.get(cover),
					RenderType.cutout()
			);

		ItemBlockRenderTypes.setRenderLayer(Misc.hempPlant, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(Misc.pottedHemp, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(IEContent.fluidPotion, RenderType.translucent());
		for(IEFluid fluid : IEFluid.IE_FLUIDS)
			if(fluid!=IEContent.fluidConcrete)
			{
				ItemBlockRenderTypes.setRenderLayer(fluid, RenderType.translucent());
				ItemBlockRenderTypes.setRenderLayer(fluid.getFlowing(), RenderType.translucent());
			}
	}
}
