package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.data.models.ConnectorBuilder;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.data.blockstates.BlockStates.forEachState;

public class ConnectorBlockBuilder
{
	private final BlockModelProvider models;
	private final Block block;
	private final VariantBlockStateBuilder outputBuilder;
	private final BiConsumer<BlockModelBuilder, ModelFile> copyParticles;
	private Function<PartialBlockstate, ModelFile> toModel;
	private final List<Property<?>> additional = new ArrayList<>();
	private RenderType[] layers;
	private Property<Direction> facingProp;
	private int xForHorizontal;

	private ConnectorBlockBuilder(
			BlockModelProvider models, VariantBlockStateBuilder outputBuilder, BiConsumer<BlockModelBuilder, ModelFile> copyParticles
	)
	{
		this.models = models;
		this.block = outputBuilder.getOwner();
		this.outputBuilder = outputBuilder;
		this.copyParticles = copyParticles;
	}

	public static ConnectorBlockBuilder builder(
			BlockModelProvider models, VariantBlockStateBuilder outputBuilder, BiConsumer<BlockModelBuilder, ModelFile> copyParticles
	)
	{
		return new ConnectorBlockBuilder(models, outputBuilder, copyParticles);
	}

	public ConnectorBlockBuilder addAdditional(Property<?> prop)
	{
		Preconditions.checkArgument(block.getStateContainer().getProperties().contains(prop));
		additional.add(prop);
		return this;
	}

	public ConnectorBlockBuilder modelFunction(Function<PartialBlockstate, ModelFile> toModel)
	{
		Preconditions.checkNotNull(toModel);
		this.toModel = toModel;
		return this;
	}

	public ConnectorBlockBuilder layers(RenderType... layers)
	{
		Preconditions.checkNotNull(layers);
		this.layers = layers;
		return this;
	}

	public ConnectorBlockBuilder autoRotationData()
	{
		if(block.getDefaultState().hasProperty(IEProperties.FACING_ALL))
			return rotationProperty(IEProperties.FACING_ALL);
		else if(block.getDefaultState().hasProperty(IEProperties.FACING_TOP_DOWN))
			return rotationProperty(IEProperties.FACING_TOP_DOWN);
		else if(block.getDefaultState().hasProperty(IEProperties.FACING_HORIZONTAL))
			return rotationProperty(IEProperties.FACING_HORIZONTAL);
		else
			return this;
	}

	public ConnectorBlockBuilder fixedModel(ModelFile model)
	{
		Preconditions.checkNotNull(model);
		return modelFunction($ -> model);
	}

	public ConnectorBlockBuilder binaryModel(Property<Boolean> prop, ModelFile falseModel, ModelFile trueModel)
	{
		this.additional.add(prop);
		Preconditions.checkNotNull(trueModel);
		Preconditions.checkNotNull(falseModel);
		return modelFunction(state -> {
			if(state.getSetStates().get(prop)==Boolean.FALSE)
				return falseModel;
			else
				return trueModel;
		});
	}

	public ConnectorBlockBuilder rotationProperty(Property<Direction> property)
	{
		int horizontal;
		if(property==IEProperties.FACING_ALL||property==IEProperties.FACING_TOP_DOWN)
			horizontal = 90;
		else if(property==IEProperties.FACING_HORIZONTAL)
			horizontal = 0;
		else
			throw new RuntimeException("Unexpected property: "+property);
		return rotationData(property, horizontal);
	}

	public ConnectorBlockBuilder rotationData(Property<Direction> prop, int xForHorizontal)
	{
		Preconditions.checkArgument(block.getStateContainer().getProperties().contains(prop));
		this.facingProp = prop;
		this.xForHorizontal = xForHorizontal;
		return this;
	}

	private ModelFile forConnectorModel(ModelFile model)
	{
		BlockModelBuilder ret = models.getBuilder(model.getLocation().getPath()+"_connector")
				.customLoader(ConnectorBuilder::begin)
				.baseModel(model)
				.layers(Arrays.stream(layers).map(BlockStates::getName).collect(Collectors.toList()))
				.end();
		copyParticles.accept(ret, model);
		return ret;
	}

	public void build()
	{
		forEachState(outputBuilder.partialState(), additional, map -> {
			if(facingProp!=null)
			{
				for(Direction d : facingProp.getAllowedValues())
					if(d==Direction.DOWN)
					{
						PartialBlockstate downState = map.with(facingProp, Direction.DOWN);
						ModelFile downModel = modelForState(downState);
						outputBuilder.setModels(downState,
								new ConfiguredModel(downModel, xForHorizontal-90, 0, true));
					}
					else if(d==Direction.UP)
					{
						PartialBlockstate upState = map.with(facingProp, Direction.UP);
						ModelFile upModel = modelForState(upState);
						outputBuilder.setModels(upState,
								new ConfiguredModel(upModel, xForHorizontal+90, 0, true));
					}
					else
					{
						int rotation = (int)d.getHorizontalAngle();
						PartialBlockstate dState = map.with(facingProp, d);
						ModelFile connFile = modelForState(dState);
						outputBuilder.setModels(dState, new ConfiguredModel(connFile, xForHorizontal, rotation, true));
					}
			}
			else
			{
				ModelFile connFile = modelForState(map);
				outputBuilder.setModels(map,
						new ConfiguredModel(connFile, 0, 0, true));
			}
		});
	}

	private ModelFile modelForState(PartialBlockstate state)
	{
		return forConnectorModel(toModel.apply(state));
	}
}
