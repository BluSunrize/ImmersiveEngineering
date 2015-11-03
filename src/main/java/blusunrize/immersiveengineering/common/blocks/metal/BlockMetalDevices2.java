package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.AdvancedAABB;
import blusunrize.immersiveengineering.api.fluid.PipeConnection;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICustomBoundingboxes;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface = "blusunrize.aquatweaks.api.IAquaConnectable", modid = "AquaTweaks")
public class BlockMetalDevices2 extends BlockIEBase implements ICustomBoundingboxes, blusunrize.aquatweaks.api.IAquaConnectable
{
	public static final int META_breakerSwitch=0;
	public static final int META_skycrateDispenser=1;
	public static final int META_energyMeter=2;
	public static final int META_electricLantern=3;
	public static final int META_floodlight=4;
	public static final int META_fluidPipe=5;
	public static final int META_fluidPump=6;
	public static final int META_barrel=7;
	IIcon[] iconPump = new IIcon[7];
	IIcon iconFloodlightGlass;
	IIcon[] iconBarrel = new IIcon[4];

	public BlockMetalDevices2()
	{
		super("metalDevice2", Material.iron, 1, ItemBlockMetalDevices2.class,
				"breakerSwitch","skycrateDispenser","energyMeter","electricLantern","floodlight","fluidPipe", "fluidPump", "barrel");
		setHardness(3.0F);
		setResistance(15.0F);
	}


	@Override
	public boolean allowHammerHarvest(int meta)
	{
		return true;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		return super.getPickBlock(target, world, x, y, z, player);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 3));
		list.add(new ItemStack(item, 1, 4));
		list.add(new ItemStack(item, 1, 5));
		list.add(new ItemStack(item, 1, 6));
		list.add(new ItemStack(item, 1, 7));
		//		for(int i=0; i<subNames.length; i++)
		//		{
		//			list.add(new ItemStack(item, 1, i));
		//		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		for(int i=0; i<this.subNames.length; i++)
			if(i!=META_fluidPump)
				this.icons[i][0] = iconRegister.registerIcon("immersiveEngineering:metal2_"+this.subNames[i]);

		iconFloodlightGlass = iconRegister.registerIcon("immersiveEngineering:metal2_floodlightGlass");
		iconPump[0] = iconRegister.registerIcon("immersiveEngineering:metal2_pump_side_none");
		iconPump[1] = iconRegister.registerIcon("immersiveEngineering:metal2_pump_side_in");
		iconPump[2] = iconRegister.registerIcon("immersiveEngineering:metal2_pump_side_out");
		iconPump[3] = iconRegister.registerIcon("immersiveEngineering:metal2_pump_bottom_none");
		iconPump[4] = iconRegister.registerIcon("immersiveEngineering:metal2_pump_bottom_in");
		iconPump[5] = iconRegister.registerIcon("immersiveEngineering:metal2_pump_bottom_out");
		iconPump[6] = iconRegister.registerIcon("immersiveEngineering:metal2_pump_top");

		iconBarrel[0] = iconRegister.registerIcon("immersiveengineering:metal2_barrel_top_none");
		iconBarrel[1] = iconRegister.registerIcon("immersiveengineering:metal2_barrel_top_in");
		iconBarrel[2] = iconRegister.registerIcon("immersiveengineering:metal2_barrel_top_out");
	}
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityMetalBarrel && side<2)
			return iconBarrel[((TileEntityMetalBarrel)te).sideConfig[side]+1];
		return super.getIcon(world, x, y, z, side);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if(meta==META_fluidPump)
			return iconPump[Math.min(side+1,6)];
		if(meta==META_floodlight && side==1)
			return iconFloodlightGlass;
		if(meta==META_barrel && side<2)
			return iconBarrel[0];
		return super.getIcon(side, meta);
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
		return BlockRenderMetalDevices2.renderID;
	}
	@Override
	public boolean canRenderInPass(int pass)
	{
		BlockRenderMetalDevices2.renderPass=pass;
		return true;
	}
	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(!player.isSneaking() && te instanceof TileEntityBreakerSwitch)
		{
			if(!world.isRemote)
			{
				if(Utils.isHammer(player.getCurrentEquippedItem()))
				{
					((TileEntityBreakerSwitch)te).inverted = !((TileEntityBreakerSwitch)te).inverted;
					player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_INFO+ (((TileEntityBreakerSwitch)te).inverted?"invertedOn":"invertedOff")));
					te.markDirty();
				}
				else
				{
					((TileEntityBreakerSwitch)te).toggle();
					te.markDirty();
				}
				world.notifyBlocksOfNeighborChange(x, y, z, this);
				for(ForgeDirection fd:  ForgeDirection.VALID_DIRECTIONS)
					world.notifyBlocksOfNeighborChange(x+fd.offsetX, y+fd.offsetY, z+fd.offsetZ, this);

				world.func_147451_t(x, y, z);
			}
			return true;
		}
		else if(te instanceof TileEntityEnergyMeter)
		{
			if(!world.isRemote)
				player.addChatComponentMessage(new ChatComponentText("Energy transferred through: "+((TileEntityEnergyMeter)te).energyPassed));
		}
		else if(te instanceof TileEntityFluidPipe_old)
		{
			if(!world.isRemote) {
				TileEntityFluidPipe_old fluidPipe = ((TileEntityFluidPipe_old) te);
				if(Utils.isHammer(player.getCurrentEquippedItem()))
				{
					//					if (++fluidPipe.mode > 2) {
					//						fluidPipe.mode = 0;
					//					}
					fluidPipe.markDirty();
					world.markBlockForUpdate(x, y, z);
					return true;
				}
				else
					player.addChatMessage(new ChatComponentText("Server Connections: " + fluidPipe.connections.size()));
			}
		}
		else if(Utils.isHammer(player.getCurrentEquippedItem()) && te instanceof TileEntityFloodlight)
		{
			if(side==((TileEntityFloodlight)te).side || ForgeDirection.OPPOSITES[side]==((TileEntityFloodlight)te).side)
			{
				((TileEntityFloodlight)te).rotY+=player.isSneaking()?-11.25:11.25;
				((TileEntityFloodlight)te).rotY%=360;
			}
			else
			{
				float newX = (((TileEntityFloodlight)te).rotX+(player.isSneaking()?-11.25f:11.25f))%360;
				if(newX>=-11.25 && newX<=191.25)
					((TileEntityFloodlight)te).rotX=newX;
			}
			((TileEntityFloodlight)te).updateFakeLights(true,((TileEntityFloodlight)te).active);
			te.markDirty();
			world.markBlockForUpdate(x, y, z);
		}
		else if(Utils.isHammer(player.getCurrentEquippedItem()) && te instanceof TileEntityFluidPump)
		{
			TileEntityFluidPump pump = (TileEntityFluidPump) te;
			if (!pump.dummy)
			{
				if (!world.isRemote)
				{
					if(player.isSneaking())
						side = ForgeDirection.OPPOSITES[side];
					pump.toggleSide(side);
					world.markBlockForUpdate(x, y, z);
				}
				return true;
			}
		}
		else if(te instanceof TileEntityFluidPipe)
		{
			if(Utils.isHammer(player.getCurrentEquippedItem()))
			{
				if(!world.isRemote)
				{
					TileEntityFluidPipe tile = ((TileEntityFluidPipe)te);
					ForgeDirection fd =ForgeDirection.UNKNOWN;

					ArrayList<AxisAlignedBB> boxes = addCustomSelectionBoxesToList(world, x, y, z);
					for(AxisAlignedBB box : boxes)
					{
						if(box.expand(.002,.002,.002).isVecInside(Vec3.createVectorHelper(hitX, hitY, hitZ)))
							if(box instanceof AdvancedAABB)
								fd = ((AdvancedAABB)box).fd;
					}
					if(fd!=null && fd!=ForgeDirection.UNKNOWN)
					{
						tile.toggleSide(fd.ordinal());
						world.markBlockForUpdate(x, y, z);
						TileEntityFluidPipe.indirectConnections.clear();
						return true;
					}
				}
			}
			else
			{
				TileEntityFluidPipe tile = ((TileEntityFluidPipe)te);
				for(ItemStack valid : TileEntityFluidPipe.validScaffoldCoverings)
					if(OreDictionary.itemMatches(valid, player.getCurrentEquippedItem(), true))
					{
						if(!OreDictionary.itemMatches(tile.scaffoldCovering, player.getCurrentEquippedItem(), true))
						{
							if(!world.isRemote && tile.scaffoldCovering!=null)
							{
								EntityItem entityitem = player.dropPlayerItemWithRandomChoice(tile.scaffoldCovering.copy(), false);
								entityitem.delayBeforeCanPickup = 0;
							}
							tile.scaffoldCovering = Utils.copyStackWithAmount(player.getCurrentEquippedItem(), 1);
							if(!player.capabilities.isCreativeMode)
								player.inventory.decrStackSize(player.inventory.currentItem, 1);
							world.markBlockForUpdate(x, y, z);
							return true;
						}
					}
			}
		}
		else if(te instanceof TileEntityWoodenBarrel)
		{
			if(!world.isRemote)
			{
				TileEntityWoodenBarrel barrel = (TileEntityWoodenBarrel)te;
				if(Utils.isHammer(player.getCurrentEquippedItem()) && side<2)
				{
					if(player.isSneaking())
						side = ForgeDirection.OPPOSITES[side];
					barrel.toggleSide(side);
				}
				else if(!player.isSneaking())
				{
					FluidStack f = Utils.getFluidFromItemStack(player.getCurrentEquippedItem());
					if(f!=null && Utils.fillFluidHandlerWithPlayerItem(world, barrel, player))
					{
						world.markBlockForUpdate(x, y, z);
						return true;
					}
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
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityBreakerSwitch)
		{
			int f = ((TileEntityBreakerSwitch)te).facing;
			int side = ((TileEntityBreakerSwitch)te).sideAttached;
			if(side==0)
				this.setBlockBounds(f<4?.25f:f==5?.75f:0, .1875f, f>3?.25f:f==3?.75f:0, f<4?.75f:f==4?.25f:1,.8125f,f>3?.75f:f==2?.25f:1);
			else if(side==1)
				this.setBlockBounds(f>=4?.1875f:.25f,0,f<=3?.1875f:.25f, f>=4?.8125f:.75f,.25f,f<=3?.8125f:.75f);
			else
				this.setBlockBounds(f>=4?.1875f:.25f,.75f,f<=3?.1875f:.25f, f>=4?.8125f:.75f,1,f<=3?.8125f:.75f);
		}
		else if (te instanceof TileEntityFloodlight)
		{
			TileEntityFloodlight light = ((TileEntityFloodlight)te);

			this.setBlockBounds(light.side/2==2?0:.0625f,light.side/2==0?0:.0625f,light.side/2==1?0:.0625f, light.side/2==2?1:.9375f,light.side/2==0?1:.9375f,light.side/2==1?1:.9375f);
		}
		else if (te instanceof TileEntityFluidPipe_old)
		{
			TileEntityFluidPipe_old pipe = ((TileEntityFluidPipe_old) te);
			if(pipe.connections.size()==0)
				this.setBlockBounds(.125f,0,.125f, .875f,1,.875f);
			else
			{
				byte connections = 0;
				for(PipeConnection connection : pipe.connections)
					if(connection.direction != ForgeDirection.UNKNOWN)
						connections = (byte) (connections | 1 << connection.direction.ordinal());
				if(pipe.connections.size()==1)
				{
					byte tempCon = connections;
					for(int i = 0; i < 6; i++)
						if((tempCon & (1 << i)) >> i == 1)
							connections = (byte) (connections | 1 << ForgeDirection.OPPOSITES[i]);
				}

				if(connections == 48) // 110000
					this.setBlockBounds(0,.125f,.125f, 1,.875f,.875f);
				else if(connections == 12) // 001100
					this.setBlockBounds(.125f,.125f,0, .875f,.875f,1);
				else if(connections == 3) // 000011
					this.setBlockBounds(.125f,0,.125f, .875f,1,.875f);
				else
					this.setBlockBounds(0,0,0, 1,1,1);
			}
		}
		else if (te instanceof TileEntityFluidPipe)
			this.setBlockBounds(.25f,.25f,.25f,.75f,.75f,.75f);
		else if (te instanceof TileEntityFluidPump)
		{
			if (((TileEntityFluidPump) te).dummy)
				this.setBlockBounds(0.1875f, 0, 0.1875f, 0.8125f, 1, 0.8125f);
			else
				this.setBlockBounds(0, 0, 0, 1, 1, 1);
		}
		else if(world.getBlockMetadata(x, y, z)==META_electricLantern)
			this.setBlockBounds(.1875f,0,.1875f, .8125f,1,.8125f);
		else
			this.setBlockBounds(0,0,0,1,1,1);
	}


	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity ent)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityFluidPipe)
		{
			TileEntityFluidPipe tile = (TileEntityFluidPipe)te;
			if(tile.scaffoldCovering!=null)
			{
				this.setBlockBounds(.0625f,0,.0625f, .9375f,1,.9375f);
				addCollisionBox(world, x, y, z, aabb, list, ent);
			}
			else
			{

				byte connections = tile.getConnectionByte();
				if(/*connections==16||connections==32||*/connections==48)
				{
					this.setBlockBounds(0,.25f,.25f, 1,.75f,.75f);
					addCollisionBox(world, x, y, z, aabb, list, ent);
					if((connections&16) == 0)
					{
						this.setBlockBounds(0,.125f,.125f, .125f,.875f,.875f);
						addCollisionBox(world, x, y, z, aabb, list, ent);
					}
					if((connections&32) == 0)
					{
						this.setBlockBounds(.875f,.125f,.125f, 1,.875f,.875f);
						addCollisionBox(world, x, y, z, aabb, list, ent);
					}
				}
				else if(/*connections==4||connections==8||*/connections==12)
				{
					this.setBlockBounds(.25f,.25f,0, .75f,.75f,1);
					addCollisionBox(world, x, y, z, aabb, list, ent);
					if((connections&4) == 0)
					{
						this.setBlockBounds(.125f,.125f,0, .875f,.875f,.125f);
						addCollisionBox(world, x, y, z, aabb, list, ent);
					}
					if((connections&8) == 0)
					{
						this.setBlockBounds(.125f,.125f,.875f, .875f,.875f,1);
						addCollisionBox(world, x, y, z, aabb, list, ent);
					}
				}
				else if(/*connections==1||connections==2||*/connections==3)
				{
					this.setBlockBounds(.25f,0,.25f, .75f,1,.75f);
					addCollisionBox(world, x, y, z, aabb, list, ent);
					if((connections&1) == 0)
					{
						this.setBlockBounds(.125f,0,.125f, .875f,.125f,.875f);
						addCollisionBox(world, x, y, z, aabb, list, ent);
					}
					if((connections&2) == 0)
					{
						this.setBlockBounds(.125f,.875f,.125f, .875f,1,.875f);
						addCollisionBox(world, x, y, z, aabb, list, ent);
					}
				}
				else
				{
					this.setBlockBounds(.25f,.25f,.25f, .75f,.75f,.75f);
					addCollisionBox(world, x, y, z, aabb, list, ent);
					for(int i=0; i<6; i++)
					{
						if((connections & 0x1)==1)
						{
							this.setBlockBounds(i==4?0:i==5?.875f:.125f, i==0?0:i==1?.875f:.125f, i==2?0:i==3?.875f:.125f,  i==4?.125f:i==5?1:.875f, i==0?.125f:i==1?1:.875f, i==2?.125f:i==3?1:.875f);
							addCollisionBox(world, x, y, z, aabb, list, ent);
						}
						connections >>= 1;
					}
				}
			}
		}
		else
			super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
	}

	@Override
	public ArrayList<AxisAlignedBB> addCustomSelectionBoxesToList(World world, int x, int y, int z)
	{
		ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityFluidPipe)
		{
			TileEntityFluidPipe tile = (TileEntityFluidPipe)te;
			byte connections = tile.getAvailableConnectionByte();
			byte availableConnections = tile.getConnectionByte();
			double[] baseAABB = tile.scaffoldCovering!=null? new double[]{.002,.998, .002,.998, .002,.998}: new double[]{.25,.75, .25,.75, .25,.75};
			for(int i=0; i<6; i++)
			{
				double depth = tile.getConnectionStyle(i)==0?.25:.125;
				double size = tile.getConnectionStyle(i)==0?.25:.125;
				if(tile.scaffoldCovering!=null)
					size = 0;
				if((connections & 0x1)==1)
					list.add(new AdvancedAABB(i==4?0:i==5?1-depth:size, i==0?0:i==1?1-depth:size, i==2?0:i==3?1-depth:size,  i==4?depth:i==5?1:1-size, i==0?depth:i==1?1:1-size, i==2?depth:i==3?1:1-size, ForgeDirection.getOrientation(i)));
				if((availableConnections & 0x1)==1)
					baseAABB[i] += i%2==1?.125: -.125;
				baseAABB[i] = Math.min(Math.max(baseAABB[i], 0), 1);
				availableConnections = (byte)(availableConnections>>1);
				connections = (byte)(connections>>1);
			}
			list.add(new AdvancedAABB(baseAABB[4],baseAABB[0],baseAABB[2], baseAABB[5],baseAABB[1],baseAABB[3], ForgeDirection.UNKNOWN));
		}
		return list;
	}
	@Override
	public boolean addSpecifiedSubBox(World world, int x, int y, int z, EntityPlayer player, AxisAlignedBB box, Vec3 hitVec, ArrayList<AxisAlignedBB> list)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityFluidPipe && box instanceof AdvancedAABB && ((AdvancedAABB)box).fd!=null)
		{
			ForgeDirection fd = ((AdvancedAABB)box).fd;
			//			AxisAlignedBB newBox = box.expand(fd.offsetX!=0?0:.03125, fd.offsetY!=0?0:.03125, fd.offsetZ!=0?0:.03125);
			//			if(newBox.isVecInside(hitVec.addVector(-x,-y,-z)))
			//				return newBox;
			if(box.expand(.002,.002,.002).isVecInside(hitVec.addVector(-x,-y,-z)))
			{
				list.add(fd==ForgeDirection.UNKNOWN?box: box.expand(fd.offsetX!=0?0:.03125, fd.offsetY!=0?0:.03125, fd.offsetZ!=0?0:.03125));
				return fd==ForgeDirection.UNKNOWN&&list.isEmpty();	
			}
		}
		return false;
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
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity ent)
	{
		if(world.getBlockMetadata(x, y, z)==META_fluidPipe)
		{
			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof TileEntityFluidPipe && ((TileEntityFluidPipe)te).scaffoldCovering!=null)
			{
				float f5 = 0.15F;
				if (ent.motionX < (double)(-f5))
					ent.motionX = (double)(-f5);
				if (ent.motionX > (double)f5)
					ent.motionX = (double)f5;
				if (ent.motionZ < (double)(-f5))
					ent.motionZ = (double)(-f5);
				if (ent.motionZ > (double)f5)
					ent.motionZ = (double)f5;

				ent.fallDistance = 0.0F;
				if (ent.motionY < -0.15D)
					ent.motionY = -0.15D;

				if(ent.motionY<0 && ent instanceof EntityPlayer && ent.isSneaking())
				{
					ent.motionY=.05;
					return;
				}
				if(ent.isCollidedHorizontally)
					ent.motionY=.2;
			}
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if(meta==META_electricLantern)
			return side.ordinal()<2;
		else if(meta==META_floodlight)
			return side == ForgeDirection.DOWN;
		else if(meta==META_fluidPipe)
		{
			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof TileEntityFluidPipe && ((TileEntityFluidPipe)te).scaffoldCovering!=null)
				return true;
		}
		else if(meta==META_fluidPump)
			return true;
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
		case META_breakerSwitch:
			return new TileEntityBreakerSwitch();
		case META_skycrateDispenser:
			return new TileEntitySkycrateDispenser();
		case META_energyMeter:
			return new TileEntityEnergyMeter();
		case META_electricLantern:
			return new TileEntityElectricLantern();
		case META_floodlight:
			return new TileEntityFloodlight();
		case META_fluidPipe:
			return new TileEntityFluidPipe();
		case META_fluidPump:
			return new TileEntityFluidPump();
		case META_barrel:
			return new TileEntityMetalBarrel();
		}
		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityFloodlight)
		{
			((TileEntityFloodlight)te).updateFakeLights(true,false);
		}
		if(te instanceof TileEntityFluidPump)
		{
			if(((TileEntityFluidPump) te).dummy)
				world.setBlockToAir(x, y - 1, z);
			else
				world.setBlockToAir(x, y + 1, z);
		}
		if(te instanceof TileEntityFluidPipe && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
		{
			if(((TileEntityFluidPipe)te).scaffoldCovering!=null)
			{
				EntityItem entityitem = new EntityItem(world, x+.5,y+.5,z+.5, ((TileEntityFluidPipe)te).scaffoldCovering.copy());
				entityitem.delayBeforeCanPickup = 10;
				world.spawnEntityInWorld(entityitem);
			}
		}
		super.breakBlock(world, x, y, z, par5, par6);
	}
	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(!world.isRemote)
		{
			if(te instanceof TileEntityWoodenBarrel)
			{
				ItemStack stack = new ItemStack(this, 1, meta);
				NBTTagCompound tag = new NBTTagCompound();
				((TileEntityWoodenBarrel) te).writeTank(tag, true);
				if(!tag.hasNoTags())
					stack.setTagCompound(tag);
				world.spawnEntityInWorld(new EntityItem(world, x+.5, y+.5, z+.5, stack));
			}
		}
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
	{
		if(!world.isRemote)
		{
			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof TileEntityWoodenBarrel)
			{
				ItemStack stack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
				NBTTagCompound tag = new NBTTagCompound();
				((TileEntityWoodenBarrel) te).writeTank(tag, true);
				if(!tag.hasNoTags())
					stack.setTagCompound(tag);
				world.spawnEntityInWorld(new EntityItem(world, x+.5, y+.5, z+.5, stack));
			}
		}
		super.onBlockExploded(world, x, y, z, explosion);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		if(metadata==META_barrel)
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
	public void onBlockAdded(World world, int x, int y, int z)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityFluidPipe)
			TileEntityFluidPipe.indirectConnections.clear();
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof TileEntityFluidPipe)
		{
			TileEntity other = world.getTileEntity(tileX, tileY, tileZ);
			if(other instanceof IFluidHandler)
			{
				TileEntityFluidPipe.indirectConnections.clear();
				ForgeDirection fd = tileY<y?ForgeDirection.DOWN: tileY>y?ForgeDirection.UP: tileZ<z?ForgeDirection.NORTH: tileZ>z?ForgeDirection.SOUTH: tileX<x?ForgeDirection.WEST: ForgeDirection.EAST;
				((TileEntityFluidPipe) tile).sideConfig[fd.ordinal()]=0;
			}
		}
	}

	//	@Override
	//	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ)
	//	{
	//		TileEntity tileEntity = world.getTileEntity(x, y, z);
	//		if (tileEntity instanceof TileEntityFluidPipe_old && !(world.getTileEntity(tileX, tileY, tileZ) instanceof TileEntityFluidPipe_old))
	//		{
	//			TileEntityFluidPipe_old fluidPipe = (TileEntityFluidPipe_old) tileEntity;
	//			boolean connected = false;
	//			for (PipeConnection connection : fluidPipe.connections)
	//			{
	//				connected = connection.to.equals(new ChunkCoordinates(tileX, tileY, tileZ));
	//				if(connected)
	//					break;
	//			}
	//			TileEntity neighbouringTileEntity = world.getTileEntity(tileX, tileY, tileZ);
	//
	//			if (PipeConnection.isTank(neighbouringTileEntity, PipeConnection.toDirection(Utils.toCC(neighbouringTileEntity), Utils.toCC(fluidPipe)).getOpposite()))
	//			{
	//				if (!connected)
	//				{
	//					fluidPipe.connections.add(new PipeConnection(Utils.toCC(fluidPipe), Utils.toCC(neighbouringTileEntity), PipeConnection.Type.TANK));
	//					fluidPipe.markDirty();
	//					if (world instanceof World)
	//						((World) world).markBlockForUpdate(x, y, z);
	//				}
	//			}
	//			else if (connected)
	//			{
	//				for (PipeConnection connection : fluidPipe.connections)
	//					if (connection.to.equals(new ChunkCoordinates(tileX, tileY, tileZ)))
	//					{
	//						fluidPipe.connections.remove(connection);
	//						break;
	//					}
	//				fluidPipe.markDirty();
	//				if (world instanceof World)
	//					((World) world).markBlockForUpdate(x, y, z);
	//			}
	//		}
	//	}
	//
	//	@Override
	//	public void onBlockAdded(World world, int x, int y, int z)
	//	{
	//		TileEntity tileEntity = world.getTileEntity(x, y, z);
	//		if (tileEntity instanceof TileEntityFluidPipe_old)
	//		{
	//			TileEntityFluidPipe_old fluidPipe = (TileEntityFluidPipe_old) tileEntity;
	//			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
	//			{
	//				TileEntity neighbouringTileEntity = world.getTileEntity(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
	//
	//				if (neighbouringTileEntity instanceof TileEntityFluidPipe_old)
	//				{
	//					((TileEntityFluidPipe_old) neighbouringTileEntity).connections.add(new PipeConnection(Utils.toCC(neighbouringTileEntity), Utils.toCC(fluidPipe), direction.getOpposite(), PipeConnection.Type.PIPE));
	//					fluidPipe.connections.add(new PipeConnection(Utils.toCC(fluidPipe), Utils.toCC(neighbouringTileEntity), direction, PipeConnection.Type.PIPE));
	//					fluidPipe.markDirty();
	//					neighbouringTileEntity.markDirty();
	//					world.markBlockForUpdate(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
	//				}
	//				else if(neighbouringTileEntity instanceof IFluidPipe)
	//				{
	//					((IFluidPipe) neighbouringTileEntity).addConnection(new PipeConnection(Utils.toCC(neighbouringTileEntity), Utils.toCC(fluidPipe), direction.getOpposite(), PipeConnection.Type.PIPE));
	//					fluidPipe.connections.add(new PipeConnection(Utils.toCC(fluidPipe), Utils.toCC(neighbouringTileEntity), direction, PipeConnection.Type.PIPE));
	//					fluidPipe.markDirty();
	//					neighbouringTileEntity.markDirty();
	//					world.markBlockForUpdate(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
	//				}
	//				else
	//				{
	//					boolean isTank = neighbouringTileEntity instanceof IFluidHandler;
	//					if(isTank)
	//					{
	//						IFluidHandler fluidHandler = (IFluidHandler) neighbouringTileEntity;
	//						FluidTankInfo[] tankInfo = fluidHandler.getTankInfo(direction.getOpposite());
	//						isTank = tankInfo!=null&&tankInfo.length > 0;
	//					}
	//					if(isTank)
	//					{
	//						fluidPipe.connections.add(new PipeConnection(Utils.toCC(fluidPipe), Utils.toCC(neighbouringTileEntity), direction, PipeConnection.Type.TANK));
	//						fluidPipe.markDirty();
	//					}
	//				}
	//			}
	//			world.markBlockForUpdate(x, y, z);
	//			//			world.markBlockRangeForRenderUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
	//		}
	//	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityBreakerSwitch)
		{
			TileEntityBreakerSwitch breaker = (TileEntityBreakerSwitch)te;
			boolean power = (breaker.active&&!breaker.inverted) || (!breaker.active&&breaker.inverted);
			return power?15:0;
		}
		return 0;
	}
	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityBreakerSwitch)
		{
			TileEntityBreakerSwitch breaker = (TileEntityBreakerSwitch)te;
			int powerSide = breaker.sideAttached>0?breaker.sideAttached-1:breaker.facing;
			boolean power = (breaker.active&&!breaker.inverted) || (!breaker.active&&breaker.inverted);
			return power&&ForgeDirection.OPPOSITES[side]==powerSide?15:0;
		}
		return 0;
	}
	@Override
	public boolean canProvidePower()
	{
		return true;
	}
	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		if(world.getBlockMetadata(x, y, z)==META_breakerSwitch)
			return super.canConnectRedstone(world, x, y, z, side);
		return false;
	}


	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityElectricLantern)
			return ((TileEntityElectricLantern)te).active?15:0;
		return 0;
	}

	@Optional.Method(modid = "AquaTweaks")
	public boolean shouldRenderFluid(IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==META_breakerSwitch||meta==META_electricLantern;
	}
	@Optional.Method(modid = "AquaTweaks")
	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==META_breakerSwitch||meta==META_electricLantern;
	}
}