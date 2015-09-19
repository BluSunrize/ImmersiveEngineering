package blusunrize.immersiveengineering.common.blocks.wooden;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.BlockRenderWoodenDevices;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface = "blusunrize.aquatweaks.api.IAquaConnectable", modid = "AquaTweaks")
public class BlockWoodenDevices extends BlockIEBase implements blusunrize.aquatweaks.api.IAquaConnectable
{
	IIcon[] iconBarrel = new IIcon[3];

	public BlockWoodenDevices()
	{
		super("woodenDevice", Material.wood, 1, ItemBlockWoodenDevices.class, "post","watermill","windmill","windmillAdvanced","crate","modificationWorkbench","barrel");
		this.setHardness(2.0F);
		this.setResistance(5.0F);
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
		icons[0][0] = iconRegister.registerIcon("immersiveengineering:woodenPost");
		icons[1][0] = iconRegister.registerIcon("immersiveengineering:treatedWood");
		icons[2][0] = iconRegister.registerIcon("immersiveengineering:treatedWood");
		icons[3][0] = iconRegister.registerIcon("immersiveengineering:treatedWood");
		icons[4][0] = iconRegister.registerIcon("immersiveengineering:woodenCrate");
		icons[5][0] = iconRegister.registerIcon("immersiveengineering:workbench");
		icons[6][0] = iconRegister.registerIcon("immersiveengineering:woodBarrel");

		iconBarrel[0] = iconRegister.registerIcon("immersiveengineering:woodBarrel_top_none");
		iconBarrel[1] = iconRegister.registerIcon("immersiveengineering:woodBarrel_top_in");
		iconBarrel[2] = iconRegister.registerIcon("immersiveengineering:woodBarrel_top_out");
	}
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if(meta==6&&side<2)
			return iconBarrel[0];
		return super.getIcon(side, meta);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityWoodenBarrel && side<2)
			return iconBarrel[((TileEntityWoodenBarrel)world.getTileEntity(x, y, z)).sideConfig[side]+1];
		return super.getIcon(world, x, y, z, side);
	}

	@Override
	public int getRenderType()
	{
		return BlockRenderWoodenDevices.renderID;
	}
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityWoodenPost)
		{
			byte type = ((TileEntityWoodenPost)world.getTileEntity(x, y, z)).type;
			switch(type)
			{
			case 0:
				this.setBlockBounds(.25f,0,.25f,  .75f,1f,.75f);
				break;
			case 4:
			case 5:
			case 6:
			case 7:
				float fd = .4375f;
				float fu = 1f;
				if(canArmConnectToBlock(world, x,y-1,z, true))
				{
					fd = 0;
					if(!canArmConnectToBlock(world, x,y+1,z, false))
						fu = .5625f;
				}
				this.setBlockBounds(type==7?0:.3125f,fd,type==5?0:.3125f,  type==6?1:.6875f,fu,type==4?1:.6875f);
				break;
			default:
				this.setBlockBounds(isPost(world,x-1,y,z,6)?0:.3125f,0,isPost(world,x,y,z-1,4)?0:.3125f,  isPost(world,x+1,y,z,7)?1:.6875f,1f,isPost(world,x,y,z+1,5)?1:.6875f);
				break;
			}
		}
		else
			this.setBlockBounds(0,0,0,1,1,1);
	}
	boolean isPost(IBlockAccess world, int x, int y, int z, int type)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityWoodenPost)
			return ((TileEntityWoodenPost)world.getTileEntity(x, y, z)).type == type;
		return world.getBlock(x,y,z)==this && world.getBlockMetadata(x, y, z)==0;
	}
	boolean canArmConnectToBlock(IBlockAccess world, int x, int y, int z, boolean down)
	{
		if(world.isAirBlock(x,y,z))
			return false;
		world.getBlock(x,y,z).setBlockBoundsBasedOnState(world, x, y, z);
		return down?world.getBlock(x,y,z).getBlockBoundsMaxY()>=1: world.getBlock(x,y,z).getBlockBoundsMinY()<=0;
	}

	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity)
	{
		return (world.getTileEntity(x, y, z) instanceof TileEntityWoodenPost);
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityWoodenPost && Utils.isHammer(player.getCurrentEquippedItem()))
		{
			byte type = ((TileEntityWoodenPost)world.getTileEntity(x, y, z)).type;
			if(type==3)
			{
				ForgeDirection fd = ForgeDirection.getOrientation(side);
				ForgeDirection rot0 = fd.getRotation(ForgeDirection.UP);
				ForgeDirection rot1 = rot0.getOpposite();
				if(!world.isAirBlock(x+fd.offsetX,y+fd.offsetY,z+fd.offsetZ))
					return false;
				if(world.getTileEntity(x+rot0.offsetX,y+rot0.offsetY,z+rot0.offsetZ) instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)world.getTileEntity(x+rot0.offsetX,y+rot0.offsetY,z+rot0.offsetZ)).type-2==rot0.ordinal())
					return false;
				if(world.getTileEntity(x+rot1.offsetX,y+rot1.offsetY,z+rot1.offsetZ) instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)world.getTileEntity(x+rot1.offsetX,y+rot1.offsetY,z+rot1.offsetZ)).type-2==rot1.ordinal())
					return false;
				world.setBlock(x+fd.offsetX, y, z+fd.offsetZ, this, 0, 0x3);
				if(world.getTileEntity(x+fd.offsetX, y, z+fd.offsetZ) instanceof TileEntityWoodenPost)
					((TileEntityWoodenPost)world.getTileEntity(x+fd.offsetX, y, z+fd.offsetZ)).type=(byte)(2+side);
				world.markBlockForUpdate(x, y-3, z);
			}
			else if(type==4||type==5||type==6||type==7)
			{
				world.setBlockToAir(x, y, z);
				ForgeDirection fd = ForgeDirection.getOrientation(type-2);
				world.markBlockForUpdate(x-fd.offsetX, y-3, z-fd.offsetZ);
			}

			return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityWindmillAdvanced && Utils.getDye(player.getCurrentEquippedItem())>=0 && ((TileEntityWindmillAdvanced)world.getTileEntity(x, y, z)).facing==side)
		{
			int f = ((TileEntityWindmillAdvanced)world.getTileEntity(x, y, z)).facing;
			float w = f==2?1-hitX: f==3?hitX: f==4?hitZ: 1-hitZ;
			double r = Math.sqrt( (w-.5)*(w-.5) + (hitY-.5)*(hitY-.5) );
			double ax = Math.toDegrees( Math.acos( (w-.5)/r ));
			double ay = Math.toDegrees( Math.asin( (hitY-.5)/r ));
			double a = (ay<0?360-ax:ax)+22.25; 
			int sel = ((4-(int)(a/45f)+6)%8);

			if(((TileEntityWindmillAdvanced)world.getTileEntity(x, y, z)).dye[sel] == Utils.getDye(player.getCurrentEquippedItem()))
				return false;
			((TileEntityWindmillAdvanced)world.getTileEntity(x, y, z)).dye[sel] = (byte) Utils.getDye(player.getCurrentEquippedItem());
			if(!player.capabilities.isCreativeMode)
				player.getCurrentEquippedItem().stackSize--;
			return true;
		}
		if(!player.isSneaking() && world.getTileEntity(x, y, z) instanceof TileEntityWoodenCrate)
		{
			if(!world.isRemote)
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_WoodenCrate, world, x,y,z);
			return true;
		}
		if(!player.isSneaking() && world.getTileEntity(x, y, z) instanceof TileEntityModWorkbench)
		{
			TileEntityModWorkbench tile = (TileEntityModWorkbench)world.getTileEntity(x, y, z);
			if(tile.dummy)
			{
				int f = tile.facing;
				int off = tile.dummyOffset;
				int xx = x-(f<4?off:0);
				int zz = z-(f>3?off:0);
				if(world.getTileEntity(xx, y, zz) instanceof TileEntityModWorkbench)
					tile = (TileEntityModWorkbench)world.getTileEntity(xx, y, zz);
			}
			if(!world.isRemote)
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Workbench, world, tile.xCoord,tile.yCoord,tile.zCoord);
			return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityWoodenBarrel)
		{
			if(!world.isRemote)
			{
				TileEntityWoodenBarrel barrel = (TileEntityWoodenBarrel)world.getTileEntity(x, y, z);
				if(Utils.isHammer(player.getCurrentEquippedItem()) && side<2)
				{
					if(player.isSneaking())
						side = ForgeDirection.OPPOSITES[side];
					barrel.toggleSide(side);
				}
				else if(!player.isSneaking())
				{
					FluidStack f = Utils.getFluidFromItemStack(player.getCurrentEquippedItem());
					if(f!=null)
						if(f.getFluid().getTemperature(f)<TileEntityWoodenBarrel.IGNITION_TEMPERATURE)
						{
							if(Utils.fillFluidHandlerWithPlayerItem(world, barrel, player))
							{
								world.markBlockForUpdate(x, y, z);
								return true;
							}
						}
						else
							player.addChatComponentMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"tooHot"));
					if(Utils.fillPlayerItemFromFluidHandler(world, barrel, player, barrel.tank.getFluid()))
					{
						world.markBlockForUpdate(x, y, z);
						return true;
					}
					if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
					{
						world.markBlockForUpdate(x, y, z);
						return true;
					}
					
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityWatermill)
		{
			int f = ((TileEntityWatermill)world.getTileEntity(x, y, z)).facing;
			int[] off = ((TileEntityWatermill)world.getTileEntity(x, y, z)).offset;
			int xx = x - ((f==2||f==3)?off[0]:0);
			int yy = y - off[1];
			int zz = z - ((f==2||f==3)?0:off[0]);
			if(world.getTileEntity(xx,yy,zz) instanceof TileEntityWatermill)
			{
				((TileEntityWatermill)world.getTileEntity(xx,yy,zz)).resetRotationVec();
			}
		}
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)
	{
		if(!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityWoodenCrate)
		{
			ItemStack stack = new ItemStack(this,1,meta);
			NBTTagCompound tag = new NBTTagCompound();
			((TileEntityWoodenCrate)world.getTileEntity(x, y, z)).writeInv(tag, true);
			if(!tag.hasNoTags())
				stack.setTagCompound(tag);
			world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,stack));
		}
		if(!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityWoodenBarrel)
		{
			ItemStack stack = new ItemStack(this,1,meta);
			NBTTagCompound tag = new NBTTagCompound();
			((TileEntityWoodenBarrel)world.getTileEntity(x, y, z)).writeTank(tag, true);
			if(!tag.hasNoTags())
				stack.setTagCompound(tag);
			world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,stack));
		}
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
	{
		if(!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityWoodenCrate)
		{
			ItemStack stack = new ItemStack(this,1,world.getBlockMetadata(x, y, z));
			NBTTagCompound tag = new NBTTagCompound();
			((TileEntityWoodenCrate)world.getTileEntity(x, y, z)).writeInv(tag, true);
			if(!tag.hasNoTags())
				stack.setTagCompound(tag);
			world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,stack));
		}
		if(!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityWoodenBarrel)
		{
			ItemStack stack = new ItemStack(this,1,world.getBlockMetadata(x, y, z));
			NBTTagCompound tag = new NBTTagCompound();
			((TileEntityWoodenBarrel)world.getTileEntity(x, y, z)).writeTank(tag, true);
			if(!tag.hasNoTags())
				stack.setTagCompound(tag);
			world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,stack));
		}
		super.onBlockExploded(world, x, y, z, explosion);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		if(metadata==0 || metadata==4 || metadata==6)
			return ret;

		int count = quantityDropped(metadata, fortune, world.rand);
		for(int i = 0; i < count; i++)
		{
			Item item = getItemDropped(metadata, world.rand, fortune);
			if (item != null)
			{
				ret.add(new ItemStack(item, 1, damageDropped(metadata)));
			}
		}
		return ret;
	}
	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityWoodenPost)
		{
			int yy=y;
			byte type = ((TileEntityWoodenPost)world.getTileEntity(x, y, z)).type;
			switch(type)
			{
			case 4:
			case 5:
			case 6:
			case 7:
				return;
			default:
				yy-= ((TileEntityWoodenPost)world.getTileEntity(x, y, z)).type;
				break;
			}

			for(int i=0;i<=3;i++)
			{
				world.setBlockToAir(x,yy+i,z);
				if(i==3)
					for(ForgeDirection fd : new ForgeDirection[]{ForgeDirection.NORTH,ForgeDirection.SOUTH,ForgeDirection.EAST,ForgeDirection.WEST})
						if(world.getTileEntity(x+fd.offsetX,yy+i,z+fd.offsetZ) instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)world.getTileEntity(x+fd.offsetX,yy+i,z+fd.offsetZ)).type==(2+fd.ordinal()))
							world.setBlockToAir(x+fd.offsetX,yy+i,z+fd.offsetZ);
			}
			if(type==0 && !world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops") && !world.restoringBlockSnapshots)
				world.spawnEntityInWorld(new EntityItem(world, x+.5,y+.5,z+.5, new ItemStack(this,1,0)));
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityWatermill)
		{
			int[] off = ((TileEntityWatermill)world.getTileEntity(x, y, z)).offset;
			int f = ((TileEntityWatermill)world.getTileEntity(x, y, z)).facing;
			int xx = x - ((f==2||f==3)?off[0]:0);
			int yy = y - off[1];
			int zz = z - ((f==2||f==3)?0:off[0]);

			if(!(off[0]==0&&off[1]==0) && world.isAirBlock(xx, yy, zz))
				return;
			world.setBlockToAir(xx, yy, zz);
			for(int hh=-2;hh<=2;hh++)
			{
				int r=hh<-1||hh>1?1:2;
				for(int ww=-r;ww<=r;ww++)
					world.setBlockToAir(xx+((f==2||f==3)?ww:0), yy+hh, zz+((f==2||f==3)?0:ww));
			}
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityModWorkbench)
		{
			TileEntityModWorkbench tile = (TileEntityModWorkbench)world.getTileEntity(x, y, z);
			int f = tile.facing;
			int off = tile.dummyOffset;
			if(tile.dummy)
				off *= -1;
			int xx = x+(f<4?off:0);
			int zz = z+(f>3?off:0);

			if(world.getTileEntity(xx, y, zz) instanceof TileEntityModWorkbench)
				world.setBlockToAir(xx, y, zz);
			if(!world.isRemote && !tile.dummy)
				for(int i=0; i<tile.getSizeInventory(); i++)
				{
					ItemStack stack = tile.getStackInSlot(i);
					if(stack!=null)
					{
						float fx = world.rand.nextFloat() * 0.8F + 0.1F;
						float fz = world.rand.nextFloat() * 0.8F + 0.1F;

						EntityItem entityitem = new EntityItem(world, x+fx, y+.5, z+fz, stack);
						entityitem.motionX = world.rand.nextGaussian()*.05;
						entityitem.motionY = world.rand.nextGaussian()*.05+.2;
						entityitem.motionZ = world.rand.nextGaussian()*.05;
						if(stack.hasTagCompound())
							entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
						world.spawnEntityInWorld(entityitem);
					}
				}
		}
		super.breakBlock(world, x, y, z, par5, par6);
	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
		case 0:
			return new TileEntityWoodenPost();
		case 1:
			return new TileEntityWatermill();
		case 2:
			return new TileEntityWindmill();
		case 3:
			return new TileEntityWindmillAdvanced();
		case 4:
			return new TileEntityWoodenCrate();
		case 5:
			return new TileEntityModWorkbench();
		case 6:
			return new TileEntityWoodenBarrel();
		}
		return null;
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