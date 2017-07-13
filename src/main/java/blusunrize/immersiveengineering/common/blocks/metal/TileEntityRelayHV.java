package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;

public class TileEntityRelayHV extends TileEntityConnectorHV implements IOBJModelCallback<IBlockState>
{
	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link)
	{
		EnumFacing side = facing.getOpposite();
		return new Vec3d(.5+side.getFrontOffsetX()*.4375, .5+side.getFrontOffsetY()*.4375, .5+side.getFrontOffsetZ()*.4375);
	}
	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return new Vec3d(.5+side.getFrontOffsetX()*(.375-conRadius), .5+side.getFrontOffsetY()*(.375-conRadius), .5+side.getFrontOffsetZ()*(.375-conRadius));
	}

	@Override
	protected boolean isRelay()
	{
		return true;
	}

	@Override
	public TextureAtlasSprite getTextureReplacement(IBlockState object, String material)
	{
		return null;
	}
	@Override
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		return MinecraftForgeClient.getRenderLayer()== BlockRenderLayer.TRANSLUCENT;
	}
}