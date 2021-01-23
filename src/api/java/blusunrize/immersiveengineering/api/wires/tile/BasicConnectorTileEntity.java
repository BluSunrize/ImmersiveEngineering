package blusunrize.immersiveengineering.api.wires.tile;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;

public abstract class BasicConnectorTileEntity extends TileEntity implements IImmersiveConnectable
{
	public BasicConnectorTileEntity(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Nonnull
	@Override
	public IModelData getModelData()
	{
		return CombinedModelData.combine(
				super.getModelData(),
				new SinglePropertyModelData<>(ConnectorTileCalls.getModelData(world, this), Model.CONNECTIONS)
		);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		ConnectorTileCalls.onLoad(this, world);
	}

	@Override
	public void onChunkUnloaded()
	{
		super.onChunkUnloaded();
		ConnectorTileCalls.onChunkUnloaded(GlobalWireNetwork.getNetwork(world), this);
	}

	@Override
	public void remove()
	{
		super.remove();
		ConnectorTileCalls.remove(this, world);
	}
}
