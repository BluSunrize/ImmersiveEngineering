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
}
