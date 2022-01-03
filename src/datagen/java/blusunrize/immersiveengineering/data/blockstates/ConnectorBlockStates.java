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
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class ConnectorBlockStates extends ExtendedBlockstateProvider
{
	public ConnectorBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper)
	{
		super(gen, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels()
	{
		createConnector(
				MetalDevices.FLOODLIGHT,
				ieObjBuilder("block/metal_device/floodlight.obj.ie").callback(FloodlightCallbacks.INSTANCE).end()
		);
		createConnector(
				Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), obj(
						"block/connector/connector_lv", rl("block/connector/connector_lv.obj"),
						ImmutableMap.of("texture", modLoc("block/connector/connector_lv"))
				)
		);
		createConnector(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), obj(
				"block/connector/relay_lv", rl("block/connector/connector_lv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/relay_lv"))
		));

		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), obj(
				"block/connector/connector_mv", rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/connector_mv"))
		));
		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), obj(
				"block/connector/relay_mv", rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/relay_mv"))
		));

		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), obj("block/connector/connector_hv.obj"));
		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), obj("block/connector/relay_hv.obj"));

		createConnector(
				Connectors.CONNECTOR_STRUCTURAL,
				ieObjBuilder("block/connector/connector_structural.obj.ie").callback(StructuralConnectorCallbacks.INSTANCE).end()
		);
		createConnector(
				Connectors.CONNECTOR_REDSTONE,
				ieObjBuilder("block/connector/connector_redstone.obj.ie").callback(RSConnectorCallbacks.INSTANCE).end()
		);
		createConnector(
				Connectors.CONNECTOR_PROBE,
				ieObjBuilder("block/connector/connector_probe.obj.ie").callback(ProbeConnectorCallbacks.INSTANCE).end()
		);
		createConnector(Connectors.CONNECTOR_BUNDLED, obj("block/connector/connector_bundled.obj"));
		ModelFile feedthroughModelFile = models().getBuilder("block/connector/feedthrough")
				.customLoader(SpecialModelBuilder.forLoader(FeedthroughLoader.LOCATION))
				.end();
		createConnector(Connectors.FEEDTHROUGH, feedthroughModelFile);
		buildConnector(MetalDevices.ELECTRIC_LANTERN)
				.binaryModel(IEProperties.ACTIVE, obj(
						"block/metal_device/e_lantern_off", rl("block/metal_device/e_lantern.obj"),
						ImmutableMap.of("texture", modLoc("block/metal_device/electric_lantern"))
				), obj(
						"block/metal_device/e_lantern_on", rl("block/metal_device/e_lantern.obj"),
						ImmutableMap.of("texture", modLoc("block/metal_device/electric_lantern_on"))
				))
				.autoRotationData()
				.build();

		createConnector(
				Connectors.REDSTONE_BREAKER,
				ieObjBuilder("block/connector/redstone_breaker.obj.ie").callback(BreakerSwitchCallbacks.INSTANCE).end()
		);
		buildConnector(Connectors.BREAKER_SWITCH)
				.binaryModel(
						IEProperties.ACTIVE,
						ieObjBuilder("block/connector/breaker_switch_off.obj.ie").callback(BreakerSwitchCallbacks.INSTANCE).end(),
						ieObjBuilder("block/connector/breaker_switch_on.obj.ie").callback(BreakerSwitchCallbacks.INSTANCE).end())
				.rotationProperty(IEProperties.FACING_ALL)
				.build();
		transformerModel("block/connector/transformer_mv", Connectors.TRANSFORMER);
		transformerModel("block/connector/transformer_hv", Connectors.TRANSFORMER_HV);
		createConnector(Connectors.POST_TRANSFORMER, obj("block/connector/transformer_post.obj"));

		ModelFile ctModel = split(obj("block/connector/e_meter.obj"), ImmutableList.of(BlockPos.ZERO, new BlockPos(0, -1, 0)));
		createConnector(Connectors.CURRENT_TRANSFORMER, ctModel);
		createConnector(MetalDevices.RAZOR_WIRE, ieObjBuilder("block/razor_wire.obj.ie").callback(RazorWireCallbacks.INSTANCE).end());
		createConnector(Cloth.BALLOON, ieObjBuilder("block/balloon.obj.ie").callback(BalloonCallbacks.INSTANCE).end());
	}

	private void transformerModel(String baseName, Supplier<? extends Block> transformer)
	{
		buildConnector(transformer).binaryModel(
				IEProperties.MIRRORED,
				split(obj(baseName+"_left.obj"), COLUMN_THREE),
				split(obj(baseName+"_right.obj"), COLUMN_THREE)
		)
				.addAdditional(IEProperties.MULTIBLOCKSLAVE)
				.rotationProperty(IEProperties.FACING_HORIZONTAL)
				.build();
	}

	private void createConnector(Supplier<? extends Block> b, ModelFile model)
	{
		buildConnector(b)
				.fixedModel(model)
				.autoRotationData()
				.build();
	}

	private ConnectorBlockBuilder buildConnector(Supplier<? extends Block> b)
	{
		return ConnectorBlockBuilder.builder(getVariantBuilder(b.get()));
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Connector models/block states";
	}
}
