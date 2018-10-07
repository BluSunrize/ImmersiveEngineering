/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;

public class TileEntityRelayHV extends TileEntityConnectorHV implements IOBJModelCallback<IBlockState>
{
	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return new Vec3d(.5+side.getXOffset()*(.375-conRadius), .5+side.getYOffset()*(.375-conRadius), .5+side.getZOffset()*(.375-conRadius));
	}

	@Override
	protected boolean isRelay()
	{
		return true;
	}

	@Override
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		return MinecraftForgeClient.getRenderLayer()==BlockRenderLayer.TRANSLUCENT;
	}
}