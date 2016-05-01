package blusunrize.immersiveengineering.common.blocks.metal;

public class TileEntityTransformerHV extends TileEntityTransformer
{
	@Override
	protected boolean canTakeHV()
	{
		return true;
	}
	@Override
	protected boolean canTakeLV()
	{
		return false;
	}
}