package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockIEWoodenStairs extends BlockStairs
{
	public BlockIEWoodenStairs()
	{
		super(IEContent.blockWoodenDecoration,0);
		this.setBlockName(ImmersiveEngineering.MODID+".woodenStairs");
		GameRegistry.registerBlock(this, "woodenStairs");
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBounds(0,0,0, 1,1,1);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon("immersiveengineering:treatedWood");
	}
}