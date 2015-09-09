package blusunrize.immersiveengineering.client.fx;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFXBlockParts extends EntityFXItemParts
{
	public EntityFXBlockParts(World world, ItemStack item, int part, double x,double y,double z, double mx,double my,double mz)
	{
		super(world, item, part, x,y,z, mx,my,mz);
	}

	@Override
	public ResourceLocation getParticleTexture()
	{
		return TextureMap.locationBlocksTexture;
	}
	@Override
	public String getParticleName()
	{
		return "blockParts";
	}
}