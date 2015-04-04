package blusunrize.immersiveengineering.common.blocks.wooden;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;

public class BlockIEWoodenStairs extends BlockStairs
{
	public BlockIEWoodenStairs()
	{
		super(IEContent.blockWoodenDecoration,0);
		this.setBlockName(ImmersiveEngineering.MODID+".woodenStairs");
		GameRegistry.registerBlock(this, ImmersiveEngineering.MODID+".woodenStairs");
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon("immersiveengineering:treatedWood");
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return AxisAlignedBB.getBoundingBox(x,y,z, x+1,y+1,z+1);
	}
}