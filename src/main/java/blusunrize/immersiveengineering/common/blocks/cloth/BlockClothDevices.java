package blusunrize.immersiveengineering.common.blocks.cloth;

import java.util.List;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.client.render.BlockRenderClothDevices;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

@Optional.Interface(iface = "blusunrize.aquatweaks.api.IAquaConnectable", modid = "AquaTweaks")
public class BlockClothDevices extends BlockIEBase implements blusunrize.aquatweaks.api.IAquaConnectable
{
	IIcon[] iconBarrel = new IIcon[3];

	public BlockClothDevices()
	{
		super("clothDevice", Material.cloth, 1, ItemBlockClothDevices.class, "balloon");
		this.setHardness(0.8F);
	}
	@Override
	public boolean allowHammerHarvest(int meta)
	{
		return true;
	}
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for(int i=0; i<subNames.length; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		icons[0][0] = iconRegister.registerIcon("immersiveengineering:cloth_balloon");
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	@Override
	public int getRenderType()
	{
		return BlockRenderClothDevices.renderID;
	}
	@Override
	public boolean canRenderInPass(int pass)
	{
		BlockRenderClothDevices.renderPass=pass;
		return true;
	}
	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		this.setBlockBounds(.125f,0,.125f,.875f,.9375f,.875f);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		return world.getBlockMetadata(x, y, z)==0?13:0;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof TileEntityBalloon)
		{
			ItemStack equipped = player.getCurrentEquippedItem();
			if(Utils.isHammer(equipped))
			{
				((TileEntityBalloon)tile).style = ((TileEntityBalloon)tile).style==0?1:0;
				world.addBlockEvent(x, y, z, this, 0, 0);
				return true;
			}
			else if(equipped!=null && equipped.getItem() instanceof IShaderItem)
			{
				((TileEntityBalloon)tile).shader = equipped;
				world.addBlockEvent(x, y, z, this, 0, 0);
				return true;
			}
			else
			{
				int target = 0;
				int style = ((TileEntityBalloon)tile).style;
				if(side<2 && style==0)
					target = (hitX<.375||hitX>.625)&&(hitZ<.375||hitZ>.625)?1:0;
				else if(side>=2&&side<4)
				{
					if(style==0)
						target = (hitX<.375||hitX>.625)?1:0;
					else
						target =(hitY>.5625&&hitY<.75)?1:0;
				}
				else if(side>=4)
				{
					if(style==0)
						target = (hitZ<.375||hitZ>.625)?1:0;
					else
						target =(hitY>.5625&&hitY<.75)?1:0;
				}
				int heldDye = Utils.getDye(equipped);
				if(heldDye==-1)
					return false;
				if(target==0)
				{
					if(((TileEntityBalloon)tile).colour0==heldDye)
						return false;
					((TileEntityBalloon)tile).colour0 = (byte)heldDye;
				}
				else
				{
					if(((TileEntityBalloon)tile).colour1==heldDye)
						return false;
					((TileEntityBalloon)tile).colour1 = (byte)heldDye;
				}
				world.addBlockEvent(x, y, z, this, 0, 0);
				return true;
			}
		}
		return false;
	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityBalloon();
	}


	@Optional.Method(modid = "AquaTweaks")
	public boolean shouldRenderFluid(IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==0;
	}
	@Optional.Method(modid = "AquaTweaks")
	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==0;
	}
}