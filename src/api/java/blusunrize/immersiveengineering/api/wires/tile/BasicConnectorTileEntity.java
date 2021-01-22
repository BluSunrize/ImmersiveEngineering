package blusunrize.immersiveengineering.api.wires.tile;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.api.wires.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class BasicConnectorTileEntity extends TileEntity implements IImmersiveConnectable
{
	public BasicConnectorTileEntity(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target)
	{
		return pos;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		return true;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset)
	{
		return new ConnectionPoint(pos, 0);
	}

	@Override
	public void removeCable(@Nullable Connection connection, ConnectionPoint attachedPoint)
	{
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return new Vector3d(0.5, 0.5, 0.5);
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, 0));
	}

	@Override
	public BlockPos getPosition()
	{
		return pos;
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
		ConnectorTileCalls.onLoad(GlobalWireNetwork.getNetwork(world), this, world);
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
		ConnectorTileCalls.remove(GlobalWireNetwork.getNetwork(world), this);
	}
}
