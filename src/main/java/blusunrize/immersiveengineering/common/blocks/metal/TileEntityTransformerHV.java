package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;

public class TileEntityTransformerHV extends TileEntityTransformer
{
	@Override
	protected boolean canTakeHV()
	{
		return true;
	}
	
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		boolean b = con.cableType==limitType;
		boolean c = con.cableType==WireType.STEEL;
		double conRadius = con.cableType.getRenderDiameter()/2;
		if(facing==2)
			return Vec3.createVectorHelper(b?.8125:.1875, (c?1.75:1.5)-conRadius, .5);
		if(facing==3)
			return Vec3.createVectorHelper(b?.1875:.8125, (c?1.75:1.5)-conRadius, .5);
		if(facing==4)
			return Vec3.createVectorHelper(.5, (c?1.75:1.5)-conRadius, b?.1875:.8125);
		if(facing==5)
			return Vec3.createVectorHelper(.5, (c?1.75:1.5)-conRadius, b?.8125:.1875);
		return Vec3.createVectorHelper(.5,.5,.5);
	}
}