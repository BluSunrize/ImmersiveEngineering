/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader;
import blusunrize.immersiveengineering.client.models.obj.callback.block.*;
import blusunrize.immersiveengineering.common.register.IEBlocks.Cloth;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.data.models.SpecialModelBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static net.minecraft.client.renderer.RenderType.*;

public class ConnectorBlockStates extends ExtendedBlockstateProvider
{
	public ConnectorBlockStates(PackOutput output, ExistingFileHelper exFileHelper)
	{
		super(output, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels()
	{
		floodlightModel();
		createAllRotatedBlock(
				Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), obj(
						"block/connector/connector_lv", rl("block/connector/connector_lv.obj"),
						ImmutableMap.of("texture", modLoc("block/connector/connector_lv")),
						models()
				)
		);
		createAllRotatedBlock(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), obj(
				"block/connector/relay_lv", rl("block/connector/connector_lv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/relay_lv")),
				models()
		));

		createAllRotatedBlock(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), obj(
				"block/connector/connector_mv", rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/connector_mv")),
				models()
		));
		createAllRotatedBlock(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), obj(
				"block/connector/relay_mv", rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/relay_mv")),
				models()
		));

		createAllRotatedBlock(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), obj("block/connector/connector_hv.obj"));
		createAllRotatedBlock(
				Connectors.getEnergyConnector(WireType.HV_CATEGORY, true),
				obj("block/connector/relay_hv.obj", translucent())
		);

		createAllRotatedBlock(
				Connectors.CONNECTOR_STRUCTURAL,
				ieObjBuilder("block/connector/connector_structural.obj.ie").callback(StructuralConnectorCallbacks.INSTANCE).end()
		);
		createAllRotatedBlock(
				Connectors.CONNECTOR_REDSTONE,
				ieObjBuilder("block/connector/connector_redstone.obj.ie").callback(RSConnectorCallbacks.INSTANCE).end()
		);
		createAllRotatedBlock(
				Connectors.CONNECTOR_PROBE,
				ieObjBuilder("block/connector/connector_probe.obj.ie")
						.callback(ProbeConnectorCallbacks.INSTANCE)
						.layer(cutout(), translucent())
						.end()
		);
		createAllRotatedBlock(Connectors.CONNECTOR_BUNDLED, obj("block/connector/connector_bundled.obj", cutout()));
		ModelFile feedthroughModelFile = models().getBuilder("block/connector/feedthrough")
				.customLoader(SpecialModelBuilder.forLoader(FeedthroughLoader.LOCATION))
				.end();
		createAllRotatedBlock(Connectors.FEEDTHROUGH, feedthroughModelFile);
		lanternModel();

		createAllRotatedBlock(
				Connectors.REDSTONE_BREAKER,
				ieObjBuilder("block/connector/redstone_breaker.obj.ie").callback(BreakerSwitchCallbacks.INSTANCE).end()
		);
		breakerModel();
		transformerModel("block/connector/transformer_mv", Connectors.TRANSFORMER);
		transformerModel("block/connector/transformer_hv", Connectors.TRANSFORMER_HV);
		createHorizontalRotatedBlock(Connectors.POST_TRANSFORMER, obj("block/connector/transformer_post.obj"), 0);

		ModelFile ctModel = split(innerObj("block/connector/e_meter.obj"), ImmutableList.of(BlockPos.ZERO, new BlockPos(0, -1, 0)));
		createHorizontalRotatedBlock(Connectors.CURRENT_TRANSFORMER, ctModel, 0);
		createHorizontalRotatedBlock(
				MetalDevices.RAZOR_WIRE,
				ieObjBuilder("block/razor_wire.obj.ie")
						.callback(RazorWireCallbacks.INSTANCE)
						.layer(cutout())
						.end(),
				0
		);
		simpleBlock(
				Cloth.BALLOON.get(),
				ieObjBuilder("block/balloon.obj.ie").callback(BalloonCallbacks.INSTANCE).layer(translucent()).end()
		);
	}

	private void floodlightModel()
	{
		ResourceLocation modelLoc = modLoc("block/metal_device/floodlight.obj.ie");
		BlockModelBuilder offModel = ieObjBuilder("block/metal_device/floodlight_off", modelLoc)
				.callback(FloodlightCallbacks.INSTANCE)
				.layer(solid(), translucent())
				.end()
				.texture("texture", modLoc("block/metal_device/floodlight"));
		BlockModelBuilder onModel = ieObjBuilder("block/metal_device/floodlight_on", modelLoc)
				.callback(FloodlightCallbacks.INSTANCE)
				.layer(solid(), translucent())
				.end()
				.texture("texture", modLoc("block/metal_device/floodlight_on"));
		createAllRotatedBlock(
				MetalDevices.FLOODLIGHT,
				state -> state.getSetStates().get(IEProperties.ACTIVE)==Boolean.TRUE?onModel: offModel,
				List.of(IEProperties.ACTIVE)
		);
	}

	private void lanternModel()
	{
		BlockModelBuilder offModel = obj(
				"block/metal_device/e_lantern_off", rl("block/metal_device/e_lantern.obj"),
				ImmutableMap.of("texture", modLoc("block/metal_device/electric_lantern")),
				models()
		);
		BlockModelBuilder onModel = obj(
				"block/metal_device/e_lantern_on", rl("block/metal_device/e_lantern.obj"),
				ImmutableMap.of("texture", modLoc("block/metal_device/electric_lantern_on")),
				models()
		);
		createRotatedBlock(
				MetalDevices.ELECTRIC_LANTERN,
				state -> state.getSetStates().get(IEProperties.ACTIVE)==Boolean.TRUE?onModel: offModel,
				IEProperties.FACING_TOP_DOWN,
				List.of(IEProperties.ACTIVE),
				90, 180
		);
	}

	private void breakerModel()
	{
		BlockModelBuilder onModel = ieObjBuilder("block/connector/breaker_switch_on.obj.ie").callback(BreakerSwitchCallbacks.INSTANCE).end();
		BlockModelBuilder offModel = ieObjBuilder("block/connector/breaker_switch_off.obj.ie").callback(BreakerSwitchCallbacks.INSTANCE).end();
		createAllRotatedBlock(
				Connectors.BREAKER_SWITCH,
				state -> state.getSetStates().get(IEProperties.ACTIVE)==Boolean.TRUE?onModel: offModel,
				List.of(IEProperties.ACTIVE)
		);
	}

	private void transformerModel(String baseName, Supplier<? extends Block> transformer)
	{
		ModelFile leftModel = split(innerObj(baseName+"_left.obj"), COLUMN_THREE);
		ModelFile rightModel = split(mirror(innerObj(baseName+"_left.obj"), innerModels), COLUMN_THREE);
		createRotatedBlock(
				transformer,
				state -> state.getSetStates().get(IEProperties.MIRRORED)==Boolean.TRUE?rightModel: leftModel,
				IEProperties.FACING_HORIZONTAL,
				List.of(IEProperties.MIRRORED),
				0, 0
		);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Connector models/block states";
	}
}
