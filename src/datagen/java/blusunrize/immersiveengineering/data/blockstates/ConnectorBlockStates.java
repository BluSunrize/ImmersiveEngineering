package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader;
import blusunrize.immersiveengineering.common.register.IEBlocks.Cloth;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.data.models.SpecialModelBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.RenderType;
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
				ieObj("block/metal_device/floodlight.obj.ie"),
				RenderType.translucent(), RenderType.solid()
		);
		createConnector(
				Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), obj(
						"block/connector/connector_lv", rl("block/connector/connector_lv.obj"),
						ImmutableMap.of("texture", modLoc("block/connector/connector_lv"))
				), RenderType.solid()
		);
		createConnector(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), obj(
				"block/connector/relay_lv", rl("block/connector/connector_lv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/relay_lv"))
		), RenderType.solid());

		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), obj(
				"block/connector/connector_mv", rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/connector_mv"))
		), RenderType.solid());
		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), obj(
				"block/connector/relay_mv", rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/relay_mv"))
		), RenderType.solid());

		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), obj("block/connector/connector_hv.obj"),
				RenderType.solid());
		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), obj("block/connector/relay_hv.obj"),
				RenderType.translucent());

		createConnector(Connectors.CONNECTOR_STRUCTURAL, ieObj("block/connector/connector_structural.obj.ie"),
				RenderType.solid());
		createConnector(Connectors.CONNECTOR_REDSTONE, ieObj("block/connector/connector_redstone.obj.ie"),
				RenderType.solid());
		createConnector(Connectors.CONNECTOR_PROBE, ieObj("block/connector/connector_probe.obj.ie"),
				RenderType.cutout(), RenderType.translucent());
		createConnector(Connectors.CONNECTOR_BUNDLED, obj("block/connector/connector_bundled.obj"),
				RenderType.cutout());
		ModelFile feedthroughModelFile = models().getBuilder("block/connector/feedthrough")
				.customLoader(SpecialModelBuilder.forLoader(FeedthroughLoader.LOCATION))
				.end();
		createConnector(Connectors.FEEDTHROUGH, feedthroughModelFile, RenderType.chunkBufferLayers().toArray(new RenderType[0]));
		buildConnector(MetalDevices.ELECTRIC_LANTERN)
				.binaryModel(IEProperties.ACTIVE, obj(
						"block/metal_device/e_lantern_off", rl("block/metal_device/e_lantern.obj"),
						ImmutableMap.of("texture", modLoc("block/metal_device/electric_lantern"))
				), obj(
						"block/metal_device/e_lantern_on", rl("block/metal_device/e_lantern.obj"),
						ImmutableMap.of("texture", modLoc("block/metal_device/electric_lantern_on"))
				))
				.autoRotationData()
				.layers(RenderType.solid())
				.build();

		createConnector(
				Connectors.REDSTONE_BREAKER, ieObj("block/connector/redstone_breaker.obj.ie"), RenderType.solid()
		);
		buildConnector(Connectors.BREAKER_SWITCH)
				.binaryModel(
						IEProperties.ACTIVE,
						ieObj("block/connector/breaker_switch_off.obj.ie"),
						ieObj("block/connector/breaker_switch_on.obj.ie"))
				.rotationProperty(IEProperties.FACING_ALL)
				.layers(RenderType.solid())
				.build();
		transformerModel("block/connector/transformer_mv", Connectors.TRANSFORMER);
		transformerModel("block/connector/transformer_hv", Connectors.TRANSFORMER_HV);
		createConnector(Connectors.postTransformer, obj("block/connector/transformer_post.obj"),
				RenderType.solid());

		ModelFile ctModel = split(obj("block/connector/e_meter.obj"), ImmutableList.of(BlockPos.ZERO, new BlockPos(0, -1, 0)));
		createConnector(Connectors.CURRENT_TRANSFORMER, ctModel, RenderType.solid());
		createConnector(MetalDevices.RAZOR_WIRE, ieObj("block/razor_wire.obj.ie"), RenderType.cutout());
		createConnector(Cloth.BALLOON, ieObj("block/balloon.obj.ie"), RenderType.translucent());
	}

	private void transformerModel(String baseName, Supplier<? extends Block> transformer)
	{
		buildConnector(transformer).binaryModel(
				IEProperties.MIRRORED,
				split(obj(baseName+"_left.obj"), COLUMN_THREE),
				split(obj(baseName+"_right.obj"), COLUMN_THREE)
		)
				.addAdditional(IEProperties.MULTIBLOCKSLAVE)
				.layers(RenderType.solid())
				.rotationProperty(IEProperties.FACING_HORIZONTAL)
				.build();
	}

	private void createConnector(Supplier<? extends Block> b, ModelFile model, RenderType... layers)
	{
		buildConnector(b)
				.fixedModel(model)
				.layers(layers)
				.autoRotationData()
				.build();
	}

	private ConnectorBlockBuilder buildConnector(Supplier<? extends Block> b)
	{
		return ConnectorBlockBuilder.builder(models(), getVariantBuilder(b.get()), this::addParticleTextureFrom);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Connector models/block states";
	}
}
