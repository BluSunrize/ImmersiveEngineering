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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDevices;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "blusunrize.aquatweaks.api.IAquaConnectable", modid = "AquaTweaks")
public class BlockMetalDevices extends BlockIEBase implements blusunrize.aquatweaks.api.IAquaConnectable
{
	public IIcon[][] icon_capacitorTop = new IIcon[3][3];
	public IIcon[][] icon_capacitorBot = new IIcon[3][3];
	public IIcon[][] icon_capacitorSide = new IIcon[3][3];
	public IIcon[] icons_sorter = new IIcon[6];

	public static int META_connectorLV=0;
	public static int META_capacitorLV=1;
	public static int META_connectorMV=2;
	public static int META_capacitorMV=3;
	public static int META_transformer=4;
	public static int META_relayHV=5;
	public static int META_connectorHV=6;
	public static int META_capacitorHV=7;
	public static int META_transformerHV=8;
	public static int META_dynamo=9;
	public static int META_thermoelectricGen=10;
	public static int META_conveyorBelt=11;
	public static int META_furnaceHeater=12;
	public static int META_sorter=13;
	public static int META_sampleDrill=14;

	public BlockMetalDevices()
	{
		super("metalDevice", Material.iron, 4, ItemBlockMetalDevices.class,
				"connectorLV","capacitorLV",
				"connectorMV","capacitorMV","transformer",
				"relayHV","connectorHV","capacitorHV","transformerHV",
				"dynamo","thermoelectricGen",
				"conveyorBelt","furnaceHeater","sorter",
				"sampleDrill");
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
		if(world.getTileEntity(x, y, z) instanceof TileEntityCapacitorLV)
		{
			ItemStack stack = new ItemStack(this,1,world.getBlockMetadata(x, y, z));
			if(((TileEntityCapacitorLV)world.getTileEntity(x,y,z)).energyStorage.getEnergyStored()>0)
				ItemNBTHelper.setInt(stack, "energyStorage", ((TileEntityCapacitorLV)world.getTileEntity(x,y,z)).energyStorage.getEnergyStored());
			int[] sides = ((TileEntityCapacitorLV)world.getTileEntity(x,y,z)).sideConfig;
			ItemNBTHelper.setIntArray(stack, "sideConfig", sides);
			return stack;
		}
		return super.getPickBlock(target, world, x, y, z, player);
	}
	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)
	{
		if(!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityCapacitorLV && player!=null && !player.capabilities.isCreativeMode)
		{
			ItemStack stack = new ItemStack(this,1,meta);
			if(((TileEntityCapacitorLV)world.getTileEntity(x,y,z)).energyStorage.getEnergyStored()>0)
				ItemNBTHelper.setInt(stack, "energyStorage", ((TileEntityCapacitorLV)world.getTileEntity(x,y,z)).energyStorage.getEnergyStored());
			int[] sides = ((TileEntityCapacitorLV)world.getTileEntity(x,y,z)).sideConfig;
			//			if(sides[0]!=-1 || sides[1]!=0||sides[2]!=0||sides[3]!=0||sides[4]!=0||sides[5]!=0)
			ItemNBTHelper.setIntArray(stack, "sideConfig", sides);
			world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,stack));
		}
	}
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		if(metadata==META_capacitorLV||metadata==META_capacitorMV||metadata==META_capacitorHV)
			return new ArrayList();
		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
		return ret;
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityCapacitorLV)
		{
			TileEntityCapacitorLV capacitor = (TileEntityCapacitorLV)world.getTileEntity(x, y, z);
			return (int)(15*(capacitor.getEnergyStored(ForgeDirection.getOrientation(side))/(float)capacitor.getMaxEnergyStored(ForgeDirection.getOrientation(side))));
		}
		return 0;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for(int i=0; i<subNames.length; i++)
		{
			list.add(new ItemStack(item, 1, i));
			if(i==META_capacitorLV||i==META_capacitorMV||i==META_capacitorHV)
			{
				ItemStack stack = new ItemStack(item, 1, i);
				ItemNBTHelper.setInt(stack, "energyStorage", i==META_capacitorLV?Config.getInt("capacitorLV_storage"): i==META_capacitorMV?Config.getInt("capacitorMV_storage"): Config.getInt("capacitorHV_storage"));
				list.add(stack);
			}
		}
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		//1 capacitorLV
		icons[1][0] = iconRegister.registerIcon("immersiveengineering:metal_capacitorLV_bottom_none");
		icons[1][1] = iconRegister.registerIcon("immersiveengineering:metal_capacitorLV_top_none");
		icons[1][2] = iconRegister.registerIcon("immersiveengineering:metal_capacitorLV_side_none");
		icons[1][3] = iconRegister.registerIcon("immersiveengineering:metal_capacitorLV_side_none");
		for(int i=0;i<3;i++)
		{
			String s = i==0?"none":i==1?"in":"out";
			icon_capacitorBot[0][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorLV_bottom_"+s);
			icon_capacitorTop[0][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorLV_top_"+s);
			icon_capacitorSide[0][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorLV_side_"+s);
		}
		//3 capacitorMV
		icons[3][0] = iconRegister.registerIcon("immersiveengineering:metal_capacitorMV_bottom_none");
		icons[3][1] = iconRegister.registerIcon("immersiveengineering:metal_capacitorMV_top_none");
		icons[3][2] = iconRegister.registerIcon("immersiveengineering:metal_capacitorMV_side_none");
		icons[3][3] = iconRegister.registerIcon("immersiveengineering:metal_capacitorMV_side_none");
		for(int i=0;i<3;i++)
		{
			String s = i==0?"none":i==1?"in":"out";
			icon_capacitorBot[1][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorMV_bottom_"+s);
			icon_capacitorTop[1][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorMV_top_"+s);
			icon_capacitorSide[1][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorMV_side_"+s);
		}
		//4 transformer
		icons[META_transformer][0] = iconRegister.registerIcon("immersiveengineering:metal_transformerHV");
		icons[META_transformer][1] = iconRegister.registerIcon("immersiveengineering:metal_transformerPost");
		icons[META_transformer][2] = iconRegister.registerIcon("immersiveengineering:metal_transformerHV");
		icons[META_transformer][3] = iconRegister.registerIcon("immersiveengineering:metal_transformerHV");
		//7 capacitorHV
		icons[7][0] = iconRegister.registerIcon("immersiveengineering:metal_capacitorHV_bottom_none");
		icons[7][1] = iconRegister.registerIcon("immersiveengineering:metal_capacitorHV_top_none");
		icons[7][2] = iconRegister.registerIcon("immersiveengineering:metal_capacitorHV_side_none");
		icons[7][3] = iconRegister.registerIcon("immersiveengineering:metal_capacitorHV_side_none");
		for(int i=0;i<3;i++)
		{
			String s = i==0?"none":i==1?"in":"out";
			icon_capacitorBot[2][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorHV_bottom_"+s);
			icon_capacitorTop[2][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorHV_top_"+s);
			icon_capacitorSide[2][i]= iconRegister.registerIcon("immersiveengineering:metal_capacitorHV_side_"+s);
		}
		//9 dynamo
		icons[9][0] = iconRegister.registerIcon("immersiveengineering:metal_dynamo_bottom");
		icons[9][1] = iconRegister.registerIcon("immersiveengineering:metal_dynamo_top");
		icons[9][2] = iconRegister.registerIcon("immersiveengineering:metal_dynamo_front");
		icons[9][3] = iconRegister.registerIcon("immersiveengineering:metal_dynamo_side");
		//10 thermoelectricGen
		icons[10][0] = iconRegister.registerIcon("immersiveengineering:metal_thermogen_bottom");
		icons[10][1] = iconRegister.registerIcon("immersiveengineering:metal_thermogen_top");
		icons[10][2] = iconRegister.registerIcon("immersiveengineering:metal_thermogen_side");
		icons[10][3] = iconRegister.registerIcon("immersiveengineering:metal_thermogen_side");
		//11 conveyorBelt
		icons[11][0] = iconRegister.registerIcon("immersiveengineering:metal_conveyor_top");
		icons[11][1] = iconRegister.registerIcon("immersiveengineering:metal_conveyor_top");
		icons[11][2] = iconRegister.registerIcon("immersiveengineering:metal_dynamo_bottom");
		icons[11][3] = iconRegister.registerIcon("immersiveengineering:metal_dynamo_bottom");
		//12 furnaceHeater
		icons[12][0] = iconRegister.registerIcon("immersiveengineering:metal_furnaceHeater_socket");
		icons[12][1] = iconRegister.registerIcon("immersiveengineering:metal_furnaceHeater_inactive");
		icons[12][2] = iconRegister.registerIcon("immersiveengineering:metal_furnaceHeater_active");
		icons[12][3] = iconRegister.registerIcon("immersiveengineering:metal_furnaceHeater_active");
		//13 sorter
		for(int i=0; i<6; i++)
			icons_sorter[i] = iconRegister.registerIcon("immersiveengineering:metal_sorter_"+i);

		//0 connectorLV
		//2 connectorMV
		//4 transformer
		//5 relayHV
		//6 connectorHV
		//8 transformerHV
		for(int i=0;i<4;i++)
		{
			icons[META_connectorLV][i] = iconRegister.registerIcon("immersiveengineering:metal_connectorLV");
			icons[META_connectorMV][i] = iconRegister.registerIcon("immersiveengineering:metal_connectorMV");
			icons[META_relayHV][i] = iconRegister.registerIcon("immersiveengineering:metal_relayHV");
			icons[META_connectorHV][i] = iconRegister.registerIcon("immersiveengineering:metal_connectorHV");
			icons[META_transformerHV][i] = iconRegister.registerIcon("immersiveengineering:metal_transformerHV");
			icons[META_sampleDrill][i] = iconRegister.registerIcon("immersiveengineering:metal_coreDrill");
		}
	}
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityCapacitorLV)
		{
			TileEntityCapacitorLV cap = (TileEntityCapacitorLV)world.getTileEntity(x, y, z);
			int t = cap instanceof TileEntityCapacitorHV?2: cap instanceof TileEntityCapacitorMV?1: 0;
			if(side==0)
				return icon_capacitorBot[t][cap.sideConfig[side]+1];
			else if(side==1)
				return icon_capacitorTop[t][cap.sideConfig[side]+1];
			else
				return icon_capacitorSide[t][cap.sideConfig[side]+1];
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityDynamo)
		{
			if(((TileEntityDynamo)world.getTileEntity(x,y,z)).facing == ForgeDirection.OPPOSITES[side])
				return IEContent.blockStorage.getIcon(0,7);
			if(((TileEntityDynamo)world.getTileEntity(x,y,z)).facing>3 && side>1)
				return icons[META_dynamo][side<4?3:2];
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityConveyorBelt && (((TileEntityConveyorBelt)world.getTileEntity(x,y,z)).facing==side || ((TileEntityConveyorBelt)world.getTileEntity(x,y,z)).facing==ForgeDirection.OPPOSITES[side]))
			return icons[META_conveyorBelt][1];
		if(world.getTileEntity(x, y, z) instanceof TileEntityFurnaceHeater)
		{
			if( ((TileEntityFurnaceHeater)world.getTileEntity(x, y, z)).sockets[side]==1)
				return icons[META_furnaceHeater][0];
			else
				return icons[META_furnaceHeater][ ((TileEntityFurnaceHeater)world.getTileEntity(x, y, z)).showActiveTexture()?2:1 ];
		}
		if(world.getBlockMetadata(x, y, z) == META_sorter)
			return icons_sorter[side];

		return super.getIcon(world, x, y, z, side);
	}
	@Override
	public IIcon getIcon(int side, int meta)
	{
		if(meta == META_sorter)
			return icons_sorter[side];
		return super.getIcon(side, meta);
	}
	@Override
	public int getRenderType()
	{
		return BlockRenderMetalDevices.renderID;
	}
	@Override
	public int getRenderBlockPass()
	{
		return 0;
	}


	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityCapacitorLV && Utils.isHammer(player.getCurrentEquippedItem()))
		{
			if(player.isSneaking())
				side = ForgeDirection.OPPOSITES[side];
			if(!world.isRemote)
			{
				((TileEntityCapacitorLV)world.getTileEntity(x, y, z)).toggleSide(side);
				world.getTileEntity(x, y, z).markDirty();
				world.func_147451_t(x, y, z);
			}
			return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityDynamo && Utils.isHammer(player.getCurrentEquippedItem()))
		{
			int f = ((TileEntityDynamo)world.getTileEntity(x, y, z)).facing;
			f = ForgeDirection.ROTATION_MATRIX[player.isSneaking()?1:0][f];
			((TileEntityDynamo)world.getTileEntity(x, y, z)).facing = f;
			world.getTileEntity(x, y, z).markDirty();
			world.func_147451_t(x, y, z);
			world.markBlockForUpdate(x, y, z);
			world.playSoundEffect(x+.5,y+.5,z+.5, "random.door_open", .5f,2f);
			return !world.isRemote;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityConveyorBelt && Utils.isHammer(player.getCurrentEquippedItem()))
		{
			TileEntityConveyorBelt tile = (TileEntityConveyorBelt)world.getTileEntity(x, y, z);
			if(player.isSneaking())
			{
				if(tile.transportUp)
				{
					tile.transportUp=false;
					tile.transportDown=true;
				}
				else if(tile.transportDown)
				{
					tile.transportDown=false;
				}
				else
					tile.transportUp=true;
			}
			else
				tile.facing = ForgeDirection.ROTATION_MATRIX[1][tile.facing];
			world.markBlockForUpdate(x, y, z);
			return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityFurnaceHeater && Utils.isHammer(player.getCurrentEquippedItem()))
		{
			if(player.isSneaking())
				side = ForgeDirection.OPPOSITES[side];
			if(!world.isRemote)
			{
				((TileEntityFurnaceHeater)world.getTileEntity(x, y, z)).toggleSide(side);
				world.getTileEntity(x, y, z).markDirty();
				world.func_147451_t(x, y, z);
			}
			return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityConveyorSorter)
		{
			if(!player.isSneaking())
			{
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Sorter, world, x, y, z);
				return true;
			}
			else if(Utils.isHammer(player.getCurrentEquippedItem()))
			{
				((TileEntityConveyorSorter)world.getTileEntity(x, y, z)).toggleSide(side);
				world.getTileEntity(x, y, z).markDirty();
				world.func_147451_t(x, y, z);
				return true;
			}
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntitySampleDrill)
		{
			int off = ((TileEntitySampleDrill)world.getTileEntity(x, y, z)).pos;
			if(!world.isRemote && world.getTileEntity(x, y-off, z) instanceof TileEntitySampleDrill)
			{
				TileEntitySampleDrill drill = (TileEntitySampleDrill)world.getTileEntity(x, y-off, z);
				int process = drill.process;
				int chunkX = (x>>4);
				int chunkZ = (z>>4);
				String s0 = (chunkX*16)+", "+(chunkZ*16);
				String s1 = (chunkX*16+16)+", "+(chunkZ*16+16);
				player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"forChunk", s0,s1).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_GRAY)));
				if(process<Config.getInt("coredrill_time"))
				{
					float f = process/(float)Config.getInt("coredrill_time");
					player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"coreDrill.progress",(int)(f*100)+"%").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
				}
				else
				{
					ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(world, chunkX, chunkZ);
					if(mineral==null)
						player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"coreDrill.result.none").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
					else
					{
						String min = StatCollector.translateToLocal(Lib.DESC_INFO+"mineral."+mineral.name);
						if(ExcavatorHandler.mineralVeinCapacity<0)
							min = StatCollector.translateToLocal(Lib.CHAT_INFO+"coreDrill.infinite")+" "+min;
						player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"coreDrill.result.mineral",min));
						if(ExcavatorHandler.mineralVeinCapacity>0)
						{
							int dep = ExcavatorHandler.mineralDepletion.get(new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ));
							String f = Utils.formatDouble((Config.getInt("excavator_depletion")-dep)/(float)Config.getInt("excavator_depletion")*100,"0.##")+"%";
							player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"coreDrill.result.depl",f));
						}
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
		if(world.getTileEntity(x, y, z) instanceof TileEntityConnectorLV)
		{
			float length = world.getTileEntity(x, y, z) instanceof TileEntityRelayHV?.875f: world.getTileEntity(x, y, z) instanceof TileEntityConnectorHV?.75f: .5f;

			switch(((TileEntityConnectorLV)world.getTileEntity(x, y, z)).facing )
			{
			case 0://UP
				this.setBlockBounds(.3125f,0,.3125f,  .6875f,length,.6875f);
				break;
			case 1://DOWN
				this.setBlockBounds(.3125f,1-length,.3125f,  .6875f,1,.6875f);
				break;
			case 2://SOUTH
				this.setBlockBounds(.3125f,.3125f,0,  .6875f,.6875f,length);
				break;
			case 3://NORTH
				this.setBlockBounds(.3125f,.3125f,1-length,  .6875f,.6875f,1);
				break;
			case 4://EAST
				this.setBlockBounds(0,.3125f,.3125f,  length,.6875f,.6875f);
				break;
			case 5://WEST
				this.setBlockBounds(1-length,.3125f,.3125f,  1,.6875f,.6875f);
				break;
			}
		}
		else if(world.getTileEntity(x, y, z) instanceof TileEntityTransformer)
		{
			TileEntityTransformer tile = (TileEntityTransformer)world.getTileEntity(x, y, z);
			if( !(tile instanceof TileEntityTransformerHV) && tile.postAttached>0)
			{
				switch(tile.postAttached)
				{
				case 2://SOUTH
					this.setBlockBounds(.25f,0,.6875f,  .75f,1,1.3125f);
					break;
				case 3://NORTH
					this.setBlockBounds(.25f,0,-.3125f,  .75f,1,.3125f);
					break;
				case 4://EAST
					this.setBlockBounds(.6875f,0,.25f,  1.3125f,1,.75f);
					break;
				case 5://WEST
					this.setBlockBounds(-.3125f,0,.25f,  .3125f,1,.75f);
				}
			}
			else
				this.setBlockBounds(0,0,0,1,1,1);
		}
		else if(world.getTileEntity(x, y, z) instanceof TileEntityConveyorBelt)
		{
			TileEntityConveyorBelt tile = (TileEntityConveyorBelt) world.getTileEntity(x, y, z);
			this.setBlockBounds(0F, 0F, 0F, 1F, tile.transportDown||tile.transportUp?1.125f:0.125F, 1F);
		}
		else
			this.setBlockBounds(0,0,0,1,1,1);
	}
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if(world.getBlockMetadata(x, y, z) == META_conveyorBelt)
			return AxisAlignedBB.getBoundingBox(x, y, z, x+1, y, z + 1);
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
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if(meta==META_capacitorLV||meta==META_capacitorMV||meta==META_capacitorHV)
			return true;
		if(meta==META_dynamo||meta==META_thermoelectricGen||meta==META_furnaceHeater)
			return true;
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
		case 0://0 connectorLV
			return new TileEntityConnectorLV();
		case 1://1 capacitorLV
			return new TileEntityCapacitorLV();
		case 2://2 connectorMV
			return new TileEntityConnectorMV();
		case 3://3 capacitorMV
			return new TileEntityCapacitorMV();
		case 4://4 transformer
			return new TileEntityTransformer();
		case 5://5 relayHV
			return new TileEntityRelayHV();
		case 6://6 connectorHV
			return new TileEntityConnectorHV();
		case 7://7 capacitorHV
			return new TileEntityCapacitorHV();
		case 8://8 transformerHV
			return new TileEntityTransformerHV();
		case 9://9 dynamo
			return new TileEntityDynamo();
		case 10://10 thermoelectricGen
			return new TileEntityThermoelectricGen();
		case 11://11 conveyorBelt
			return new TileEntityConveyorBelt();
		case 12://12 furnaceHeater
			return new TileEntityFurnaceHeater();
		case 13://13 sorter
			return new TileEntityConveyorSorter();
		case 14://14 sample drill
			return new TileEntitySampleDrill();
		}
		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block nbid)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityConnectorLV)
		{
			TileEntityConnectorLV relay = (TileEntityConnectorLV)world.getTileEntity(x, y, z);
			ForgeDirection fd = ForgeDirection.getOrientation(relay.facing);
			if(world.isAirBlock(x+fd.offsetX, y+fd.offsetY, z+fd.offsetZ))
			{
				dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
				world.setBlockToAir(x, y, z);
			}
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityTransformer)
		{
			TileEntityTransformer transf = (TileEntityTransformer)world.getTileEntity(x, y, z);
			if(transf.postAttached>0 && !(world.getTileEntity(x+(transf.postAttached==4?1: transf.postAttached==5?-1: 0), y, z+(transf.postAttached==2?1: transf.postAttached==3?-1: 0)) instanceof TileEntityWoodenPost ))
			{
				this.dropBlockAsItem(world, x, y, z, new ItemStack(this,1,world.getBlockMetadata(x, y, z)));
				world.setBlockToAir(x, y, z);
			}
			else if(transf.postAttached<=0 && ((transf.dummy && world.isAirBlock(x,y+1,z))|| (!transf.dummy && world.isAirBlock(x,y-1,z))))
				world.setBlockToAir(x, y, z);
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntitySampleDrill)
		{
			TileEntitySampleDrill drill = (TileEntitySampleDrill)world.getTileEntity(x, y, z);
			if((drill.pos==0 && (world.isAirBlock(x,y+1,z)||world.isAirBlock(x,y+2,z)))
					||(drill.pos==1 && (world.isAirBlock(x,y-1,z)||world.isAirBlock(x,y+1,z)))
					||(drill.pos==2 && (world.isAirBlock(x,y-1,z)||world.isAirBlock(x,y-2,z))))
				world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity par5Entity)
	{
		if(par5Entity!=null && world.getTileEntity(x, y, z) instanceof TileEntityConveyorBelt && !(par5Entity instanceof EntityPlayer && ((EntityPlayer)par5Entity).isSneaking()))
		{
			if(world.isBlockIndirectlyGettingPowered(x, y, z))
				return;
			TileEntityConveyorBelt tile = (TileEntityConveyorBelt) world.getTileEntity(x, y, z);
			int f = tile.facing;
			ForgeDirection fd = ForgeDirection.getOrientation(f).getOpposite();
			double vBase = 1.15;
			double vX = 0.1 * vBase*fd.offsetX;
			double vY = par5Entity.motionY;
			double vZ = 0.1 * vBase*fd.offsetZ;

			if (tile.transportUp)
				vY = 0.17D * vBase;
			else if (tile.transportDown)
				vY = -0.07000000000000001D * vBase;

			if (tile.transportUp||tile.transportDown)
				par5Entity.onGround = false;

			//			if(par5Entity instanceof EntityItem)
			if (fd == ForgeDirection.WEST || fd == ForgeDirection.EAST)
			{
				if (par5Entity.posZ > z + 0.65D)
					vZ = -0.1D * vBase;
				else if (par5Entity.posZ < z + 0.35D)
					vZ = 0.1D * vBase;
				//				else
				//				{
				//					vZ = 0;
				//					par5Entity.posZ=z+.5;
				//				}
			}
			else if (fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH)
			{
				if (par5Entity.posX > x + 0.65D)
					vX = -0.1D * vBase;
				else if (par5Entity.posX < x + 0.35D)
					vX = 0.1D * vBase;
				//				else
				//				{
				//					vX = 0;
				//					par5Entity.posX=x+.5;
				//				}
			}

			par5Entity.motionX = vX;
			par5Entity.motionY = vY;
			par5Entity.motionZ = vZ;
			if(par5Entity instanceof EntityItem)
			{
				((EntityItem)par5Entity).age=0;
				boolean contact = f==3?(par5Entity.posZ-z<=.2): f==2?(par5Entity.posZ-z>=.8): f==5?(par5Entity.posX-x<=.2): (par5Entity.posX-x>=.8);

				if(contact && world.getTileEntity(x+fd.offsetX,y+(tile.transportUp?1: tile.transportDown?-1: 0),z+fd.offsetZ) instanceof IInventory)
				{
					IInventory inv = (IInventory)world.getTileEntity(x+fd.offsetX,y+(tile.transportUp?1: tile.transportDown?-1: 0),z+fd.offsetZ);
					if(!(inv instanceof TileEntityConveyorBelt))
					{
						ItemStack stack = ((EntityItem)par5Entity).getEntityItem();
						if(stack!=null)
						{
							ItemStack ret = Utils.insertStackIntoInventory(inv, ((EntityItem)par5Entity).getEntityItem(), fd.getOpposite().ordinal());
							if(ret==null)
								par5Entity.setDead();
							else if(ret.stackSize<stack.stackSize)
								((EntityItem)par5Entity).setEntityItemStack(ret);
						}
					}
				}
			}
		}
	}


	@Optional.Method(modid = "AquaTweaks")
	public boolean shouldRenderFluid(IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==META_connectorLV
				|| meta==META_connectorMV
				|| meta==META_relayHV
				|| meta==META_connectorHV;
	}
	@Optional.Method(modid = "AquaTweaks")
	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==META_connectorLV
				|| meta==META_connectorMV
				|| meta==META_relayHV
				|| meta==META_connectorHV;
	}
}