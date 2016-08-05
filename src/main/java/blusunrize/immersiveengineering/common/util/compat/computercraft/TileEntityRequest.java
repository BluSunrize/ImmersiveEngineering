package blusunrize.immersiveengineering.common.util.compat.computercraft;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class TileEntityRequest
{
	public World w;
	public BlockPos pos;
	public TileEntity te;
	public boolean checked = false;
	public TileEntityRequest(World world, BlockPos pos)
	{
		w = world;
		this.pos = pos;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TileEntityRequest))
			return false;
		TileEntityRequest o = (TileEntityRequest) obj;
		return o.w.provider.getDimensionId()==w.provider.getDimensionId()&&pos.equals(o.pos);
	}
	@Override
	public String toString() {
		return w.provider.getDimensionId()+"|"+pos;
	}
	@Override
	public int hashCode() {
		return 31*pos.hashCode()+w.hashCode();
	}
}
