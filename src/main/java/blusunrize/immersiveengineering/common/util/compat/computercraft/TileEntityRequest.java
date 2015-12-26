package blusunrize.immersiveengineering.common.util.compat.computercraft;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityRequest
{
	public World w;
	public int x, y, z;
	public TileEntity te;
	public boolean checked = false;
	public TileEntityRequest(World world, int _x, int _y, int _z)
	{
		w = world;
		x = _x;
		y = _y;
		z = _z;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TileEntityRequest))
			return false;
		TileEntityRequest o = (TileEntityRequest) obj;
		return o.w.provider.dimensionId==w.provider.dimensionId&&x==o.x&&y==o.y&&z==o.z;
	}
	@Override
	public String toString() {
		return w.provider.dimensionId+"|"+x+"|"+y+"|"+z;
	}
	@Override
	public int hashCode() {
		return x + z << 8 + y << 16+w.provider.dimensionId << 24;
	}
}
