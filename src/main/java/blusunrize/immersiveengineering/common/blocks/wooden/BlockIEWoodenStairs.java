package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.texture.IIconRegister;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import cpw.mods.fml.common.registry.GameRegistry;

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
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon("immersiveengineering:treatedWood");
	}
}