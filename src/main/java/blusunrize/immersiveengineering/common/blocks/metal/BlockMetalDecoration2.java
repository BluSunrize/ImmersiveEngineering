package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMetalDecoration2 extends BlockIEBase
{
	public static final int META_sheetMetalAlu=0;
	public static final int META_sheetMetalSteel=1;

	public BlockMetalDecoration2()
	{
		super("metalDecoration2", Material.iron,1, ItemBlockMetalDecorations.class, 
				"sheetMetalAlu","sheetMetalSteel");
		setHardness(3.0F);
		setResistance(15.0F);
		this.setMetaLightOpacity(META_sheetMetalAlu, 255);
		this.setMetaLightOpacity(META_sheetMetalSteel, 255);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return true;
	}
	@Override
	public boolean renderAsNormalBlock()
	{
		return true;
	}

	@Override
	public int damageDropped(int meta)
	{
		return super.damageDropped(meta);
	}
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		icons[META_sheetMetalAlu][0] = iconRegister.registerIcon("immersiveengineering:metalDeco2_sheetMetalAlu");
		icons[META_sheetMetalSteel][0] = iconRegister.registerIcon("immersiveengineering:metalDeco2_sheetMetalSteel");
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		this.setBlockBounds(0,0,0,1,1,1);
	}
	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return null;
	}
}