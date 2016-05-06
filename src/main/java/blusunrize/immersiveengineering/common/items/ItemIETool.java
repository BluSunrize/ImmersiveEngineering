package blusunrize.immersiveengineering.common.items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemIETool extends ItemIEBase implements ITool
{
	static int hammerMaxDamage;
	public ItemIETool()
	{
		super("tool", 1, "hammer","wirecutter","voltmeter","manual");
		hammerMaxDamage = Config.getInt("hammerDurabiliy");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(ItemNBTHelper.hasKey(stack, "linkingPos"))
		{
			int[] link = ItemNBTHelper.getIntArray(stack, "linkingPos");
			if(link!=null&&link.length>3)
				list.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"attachedToDim", link[1],link[2],link[3],link[0]));
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return stack.getItemDamage()==0;
	}
	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(stack.getItemDamage()==0)
		{
			int nbtDamage = ItemNBTHelper.getInt(stack, "hammerDmg")+1;
			if(nbtDamage<hammerMaxDamage)
			{
				ItemStack container = stack.copy();
				ItemNBTHelper.setInt(container, "hammerDmg", nbtDamage);
				return container;
			}
		}
		return null;
	}
	//	@Override
	//	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	//	{
	//		return stack.getItemDamage()!=0;
	//	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			//			int chunkX = (x>>4);
			//			int chunkZ = (z>>4);
			//			MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(world, chunkX, chunkZ);
			//			player.addChatMessage(new ChatComponentText("info for chunk; mineral: "+(info.mineral!=null?EnumChatFormatting.GREEN+info.mineral.name+EnumChatFormatting.RESET:"none")+"  override: "+(info.mineralOverride!=null?EnumChatFormatting.GREEN+info.mineralOverride.name+EnumChatFormatting.RESET:"none")+"  depletion: "+info.depletion));
			if(stack.getItemDamage()==0)
			{
				String[] interdictedMultiblocks = null;
				if(ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
				{
					NBTTagList list = stack.getTagCompound().getTagList("multiblockInterdiction", 8);
					interdictedMultiblocks = new String[list.tagCount()];
					for(int i=0; i<interdictedMultiblocks.length; i++)
						interdictedMultiblocks[i] = list.getStringTagAt(i);
				}
				for(IMultiblock mb : MultiblockHandler.getMultiblocks())
					if(mb.isBlockTrigger(world.getBlockState(pos)))
					{
						if(interdictedMultiblocks!=null)
							for(String s : interdictedMultiblocks)
								if(mb.getUniqueName().equalsIgnoreCase(s))
									return false;
						if(mb.createStructure(world, pos, side, player))
							return true;
					}
				if(RotationUtil.rotateBlock(world, pos, side, player))
					return true;
				return false;
			}
			else if(stack.getItemDamage()==1 && tileEntity instanceof IImmersiveConnectable)
			{
				IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
				ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(nodeHere),world, new TargetingInfo(side,hitX,hitY,hitZ));
				IESaveData.setDirty(world.provider.getDimensionId());
				return true;
			}
			else if(stack.getItemDamage()==2)
			{
				if(!player.isSneaking() && (tileEntity instanceof IFluxReceiver || tileEntity instanceof IFluxProvider))
				{
					int max = 0;
					int stored = 0;
					if(tileEntity instanceof IFluxReceiver)
					{
						max = ((IFluxReceiver)tileEntity).getMaxEnergyStored(side);
						stored = ((IFluxReceiver)tileEntity).getEnergyStored(side);
					}
					else
					{
						max = ((IFluxProvider)tileEntity).getMaxEnergyStored(side);
						stored = ((IFluxProvider)tileEntity).getEnergyStored(side);
					}
					if(max>0)
						ChatUtils.sendServerNoSpamMessages(player, new ChatComponentTranslation(Lib.CHAT_INFO+"energyStorage", stored,max));
					return true;
				}
				if(player.isSneaking() && tileEntity instanceof IImmersiveConnectable)
				{
					if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
						ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.getDimensionId(),pos.getX(),pos.getY(),pos.getZ()});
					else
					{
						int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos");
						BlockPos linkPos = new BlockPos(array[1],array[2],array[3]);
						TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
						if(array[0]==world.provider.getDimensionId())
						{
							IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
							IImmersiveConnectable nodeLink = (IImmersiveConnectable)tileEntityLinkingPos;
							if(nodeLink!=null)
							{
								Set<AbstractConnection> connections = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(nodeLink), world, true);
								for(AbstractConnection con : connections)
									if(Utils.toCC(nodeHere).equals(con.end))
										player.addChatComponentMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"averageLoss",Utils.formatDouble(con.getAverageLossRate()*100, "###.000")));
							}
						}
						ItemNBTHelper.remove(stack, "linkingPos");
					}
					return true;
				}
			}
			//						x += 6;
			//						y += 1;
			//			
			//						world.createExplosion(player, x+.5, y+.5, z+.5, 1.5f, true);
			//			
			//						float vex = 16;
			//			
			//						if(world instanceof WorldServer)
			//							for(int i=0; i<vex; i++)
			//							{
			//								float angle = i*(360/vex);
			//								float h = 0;
			//								for(int j=0; j<16; j++)
			//								{
			//									float r = 1f-Math.min(j,5)*.0625f;
			//									double xx = r*Math.cos(angle);
			//									double zz = r*Math.sin(angle);
			//									((WorldServer)world).func_147487_a("explode", x+xx, y+h,z+zz, 0, 0,0,0, 1);
			//									((WorldServer)world).func_147487_a("largesmoke", x+xx,y+h,z+zz, 0, 0,.0,0, 1);
			//									((WorldServer)world).func_147487_a("largesmoke", x+xx,y+h,z+zz, 0, 0,.0,0, 1);
			//									//					world.spawnParticle("explode", x+xx, y+h,z+zz, 0,0,0);
			//									//					world.spawnParticle("largesmoke", x+xx,y+h,z+zz, 0,.0,0);
			//									//					world.spawnParticle("largesmoke", x+xx,y+h,z+zz, 0,.0,0);
			//									if(i%2==0)
			//										//						world.spawnParticle("angryVillager", x+xx, y+h,z+zz, 0,0,0);
			//										((WorldServer)world).func_147487_a("angryVillager", x+xx, y+h,z+zz, 0, 0,0,0, 1);
			//									h += .1875f;
			//								}
			//								for(int j=0; j<16; j++)
			//								{
			//									float r = (float)(Math.cos(112.5f-j*(45/16f)));
			//									double xx = r*Math.cos(angle);
			//									double zz = r*Math.sin(angle);
			//									//					world.spawnParticle("explode", x+xx, y+h, z+zz, 0,.0,0);
			//									//					world.spawnParticle("largesmoke", x+xx, y+h, z+zz, xx*.025,.0,zz*.025);
			//									//					world.spawnParticle("largesmoke", x+xx, y+h, z+zz, xx*.05,.0,zz*.05);
			//									//					world.spawnParticle("largesmoke", x+xx, y+h, z+zz, xx*.1,.0,zz*.1);
			//									((WorldServer)world).func_147487_a("explode", x+xx, y+h, z+zz, 0, 0,.0,0, 1);
			//									((WorldServer)world).func_147487_a("largesmoke", x+xx, y+h, z+zz, 0, xx*.025,.0,zz*.025, 1);
			//									((WorldServer)world).func_147487_a("largesmoke", x+xx, y+h, z+zz, 0, xx*.05,.0,zz*.05, 1);
			//									((WorldServer)world).func_147487_a("largesmoke", x+xx, y+h, z+zz, 0, xx*.1,.0,zz*.1, 1);
			//									h += .0625f;
			//								}
			//							}
		}
		else
		{

		}
		return false;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity)
	{
		if(!player.worldObj.isRemote && stack.getItemDamage()==0 && RotationUtil.rotateEntity(entity, player))
			return true;
		return false;
	}

	@Override
	public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player)
	{
		if(player.getCurrentEquippedItem()!=null && this.equals(player.getCurrentEquippedItem().getItem()))
			return player.getCurrentEquippedItem().getItemDamage()==0;
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(stack.getItemDamage()==3)
		{
			player.triggerAchievement(IEAchievements.openManual);
			if(world.isRemote)
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Manual, world, (int)player.posX,(int)player.posY,(int)player.posZ);
		}
		return stack;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass)
	{
		if(getToolClasses(stack).contains(toolClass))
			return 2;
		else
			return -1;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return stack.getItemDamage()==0&&ItemNBTHelper.getInt(stack, "hammerDmg")>0;
	}
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "hammerDmg") / (double)hammerMaxDamage;
	}
	@Override
	public boolean isDamaged(ItemStack stack)
	{
		return false;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		int meta = stack.getItemDamage();
		return meta==0?ImmutableSet.of(Lib.TOOL_HAMMER): meta==1?ImmutableSet.of(Lib.TOOL_WIRECUTTER): new HashSet<String>();
	}

	@Override
	public float getDigSpeed(ItemStack stack, IBlockState state)
	{
		for(String type : this.getToolClasses(stack))
			if(state.getBlock().isToolEffective(type, state))
				return 6;
		return super.getDigSpeed(stack, state);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return item.getItemDamage()!=3;
	}

	//	@Override
	//	@Optional.Method(modid = "CoFHAPI|item")
	//	public boolean isUsable(ItemStack stack, EntityLivingBase living, int x, int y, int z)
	//	{
	//		return stack!=null&&stack.getItemDamage()==0;
	//	}
	//
	//	@Override
	//	@Optional.Method(modid = "CoFHAPI|item")
	//	public void toolUsed(ItemStack stack, EntityLivingBase living, int x, int y, int z)
	//	{
	//	}
}