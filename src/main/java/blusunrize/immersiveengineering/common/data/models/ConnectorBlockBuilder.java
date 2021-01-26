package blusunrize.immersiveengineering.common.data.models;

import blusunrize.immersiveengineering.api.IEProperties;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static blusunrize.immersiveengineering.common.data.BlockStates.forEachState;

public class ConnectorBlockBuilder
{
	private final BlockModelProvider models;
	private final Block block;
	private Function<PartialBlockstate, ModelFile> toModel;
	private final List<Property<?>> additional = new ArrayList<>();
	private RenderType[] layers;
	private Property<Direction> facingProp;
	private int xForHorizontal;

	private ConnectorBlockBuilder(BlockModelProvider models, Block block)
	{
		this.models = models;
		this.block = block;
	}

	public static ConnectorBlockBuilder builder(BlockModelProvider models, Block block)
	{
		return new ConnectorBlockBuilder(models, block);
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
			return rotationData(IEProperties.FACING_ALL, 90);
		else if(block.getDefaultState().hasProperty(IEProperties.FACING_TOP_DOWN))
			return rotationData(IEProperties.FACING_TOP_DOWN, 90);
		else if(block.getDefaultState().hasProperty(IEProperties.FACING_HORIZONTAL))
			return rotationData(IEProperties.FACING_HORIZONTAL, 0);
		else
			return this;
	}

	public ConnectorBlockBuilder rotationData(Property<Direction> prop, int xForHorizontal)
	{
		Preconditions.checkArgument(block.getStateContainer().getProperties().contains(prop));
		this.facingProp = prop;
		this.xForHorizontal = xForHorizontal;
		return this;
	}

	public void writeTo(VariantBlockStateBuilder builder)
	{
		forEachState(builder.partialState(), additional, map -> {
			if(facingProp!=null)
			{
				for(Direction d : facingProp.getAllowedValues())
					if(d==Direction.DOWN)
					{
						PartialBlockstate downState = map.with(facingProp, Direction.DOWN);
						ModelFile downModel = toModel.apply(downState);
						builder.setModels(downState,
								new ConfiguredModel(downModel, xForHorizontal-90, 0, true));
					}
					else if(d==Direction.UP)
					{
						PartialBlockstate upState = map.with(facingProp, Direction.UP);
						ModelFile upModel = toModel.apply(upState);
						builder.setModels(upState,
								new ConfiguredModel(upModel, xForHorizontal+90, 0, true));
					}
					else
					{
						int rotation = (int)d.getHorizontalAngle();
						PartialBlockstate dState = map.with(facingProp, d);
						ModelFile connFile = toModel.apply(dState);
						builder.setModels(dState, new ConfiguredModel(connFile, xForHorizontal, rotation, true));
					}
			}
			else
			{
				ModelFile connFile = toModel.apply(map);
				builder.setModels(map,
						new ConfiguredModel(connFile, 0, 0, true));
			}
		});
	}
}
