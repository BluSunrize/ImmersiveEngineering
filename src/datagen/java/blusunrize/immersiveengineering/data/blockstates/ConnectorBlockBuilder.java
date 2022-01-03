package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.api.IEProperties;
import com.google.common.base.Preconditions;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static blusunrize.immersiveengineering.data.blockstates.BlockStates.forEachState;

public class ConnectorBlockBuilder
{
	private final Block block;
	private final VariantBlockStateBuilder outputBuilder;
	private Function<PartialBlockstate, ModelFile> toModel;
	private final List<Property<?>> additional = new ArrayList<>();
	private Property<Direction> facingProp;
	private int xForHorizontal;

	private ConnectorBlockBuilder(VariantBlockStateBuilder outputBuilder)
	{
		this.block = outputBuilder.getOwner();
		this.outputBuilder = outputBuilder;
	}

	public static ConnectorBlockBuilder builder(VariantBlockStateBuilder outputBuilder)
	{
		return new ConnectorBlockBuilder(outputBuilder);
	}

	public ConnectorBlockBuilder addAdditional(Property<?> prop)
	{
		Preconditions.checkArgument(block.getStateDefinition().getProperties().contains(prop));
		additional.add(prop);
		return this;
	}

	public ConnectorBlockBuilder modelFunction(Function<PartialBlockstate, ModelFile> toModel)
	{
		Preconditions.checkNotNull(toModel);
		this.toModel = toModel;
		return this;
	}

	public ConnectorBlockBuilder autoRotationData()
	{
		if(block.defaultBlockState().hasProperty(IEProperties.FACING_ALL))
			return rotationProperty(IEProperties.FACING_ALL);
		else if(block.defaultBlockState().hasProperty(IEProperties.FACING_TOP_DOWN))
			return rotationProperty(IEProperties.FACING_TOP_DOWN);
		else if(block.defaultBlockState().hasProperty(IEProperties.FACING_HORIZONTAL))
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
		Preconditions.checkArgument(block.getStateDefinition().getProperties().contains(prop));
		this.facingProp = prop;
		this.xForHorizontal = xForHorizontal;
		return this;
	}

	public void build()
	{
		forEachState(outputBuilder.partialState(), additional, map -> {
			if(facingProp!=null)
			{
				for(Direction d : facingProp.getPossibleValues())
					if(d==Direction.DOWN)
					{
						PartialBlockstate downState = map.with(facingProp, Direction.DOWN);
						ModelFile downModel = toModel.apply(downState);
						outputBuilder.setModels(downState,
								new ConfiguredModel(downModel, xForHorizontal-90, 0, true));
					}
					else if(d==Direction.UP)
					{
						PartialBlockstate upState = map.with(facingProp, Direction.UP);
						ModelFile upModel = toModel.apply(upState);
						outputBuilder.setModels(upState,
								new ConfiguredModel(upModel, xForHorizontal+90, 0, true));
					}
					else
					{
						int rotation = (int)d.toYRot();
						PartialBlockstate dState = map.with(facingProp, d);
						ModelFile connFile = toModel.apply(dState);
						outputBuilder.setModels(dState, new ConfiguredModel(connFile, xForHorizontal, rotation, true));
					}
			}
			else
			{
				ModelFile connFile = toModel.apply(map);
				outputBuilder.setModels(map,
						new ConfiguredModel(connFile, 0, 0, true));
			}
		});
	}
}
