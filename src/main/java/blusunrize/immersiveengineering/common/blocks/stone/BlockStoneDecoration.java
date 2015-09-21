package blusunrize.immersiveengineering.common.blocks.stone;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;

public class BlockStoneDecoration extends BlockIEBase
{
	public BlockStoneDecoration()
	{
		super("stoneDecoration", Material.rock,1, ItemBlockIEBase.class, 
				"hempcrete","cokeBrick","blastBrick","coalCoke","concrete","concreteTile");
		this.setHardness(2.0F);
		this.setResistance(20.0F);
	}

	@Override
	public int damageDropped(int meta)
	{
		return super.damageDropped(meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		for(int i=0; i<icons.length; i++)
			icons[i][0] = iconRegister.registerIcon("immersiveengineering:"+name+"_"+subNames[i]);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity ent)
	{
//		if(world.getBlockMetadata(x, y, z)==5)
//		{
//			float f5 = 0.15F;
//			if (ent.motionX < (double)(-f5))
//				ent.motionX = (double)(-f5);
//			if (ent.motionX > (double)f5)
//				ent.motionX = (double)f5;
//			if (ent.motionZ < (double)(-f5))
//				ent.motionZ = (double)(-f5);
//			if (ent.motionZ > (double)f5)
//				ent.motionZ = (double)f5;
//
//			ent.fallDistance = 0.0F;
//			if (ent.motionY < -0.15D)
//				ent.motionY = -0.15D;
//
//			if(ent.motionY<0 && ent instanceof EntityPlayer && ent.isSneaking())
//			{
//				ent.motionY=.05;
//				return;
//			}
//			if(ent.isCollidedHorizontally)
//				ent.motionY=.2;
//		}
	}

	@Override
    public boolean hasTileEntity(int meta)
    {
        return false;
    }
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return null;
	}

	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return false;
	}
}