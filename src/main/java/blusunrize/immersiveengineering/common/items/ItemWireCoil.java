package blusunrize.immersiveengineering.common.items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.IWireCoil;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;


public class ItemWireCoil extends ItemIEBase implements IWireCoil
{
	public ItemWireCoil()
	{
		super("wirecoil", 64, "copper","electrum","hv","rope","structural");
	}

	@Override
	public WireType getWireType(ItemStack stack)
	{
		switch(stack.getItemDamage())
		{
		case 0:
		default:
			return WireType.COPPER;
		case 1:
			return WireType.ELECTRUM;
		case 2:
			return WireType.STEEL;
		case 3:
			return WireType.STRUCTURE_ROPE;
		case 4:
			return WireType.STRUCTURE_STEEL;
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(stack.getItemDamage()>2)
		{
			list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"coil.construction0"));
			list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		if(stack.getTagCompound()!=null && stack.getTagCompound().hasKey("linkingPos"))
		{
			int[] link = stack.getTagCompound().getIntArray("linkingPos");
			if(link!=null&&link.length>3)
				list.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"attachedToDim", link[1],link[2],link[3],link[0]));
		}
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof IImmersiveConnectable && ((IImmersiveConnectable)tileEntity).canConnect())
			{
				TargetingInfo target = new TargetingInfo(side, hitX,hitY,hitZ);
				WireType wire = getWireType(stack);
				BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(wire, target);
				tileEntity = world.getTileEntity(masterPos);
				if( !(tileEntity instanceof IImmersiveConnectable) || !((IImmersiveConnectable)tileEntity).canConnect())
					return false;
				
				if( !((IImmersiveConnectable)tileEntity).canConnectCable(wire, target))
				{
					player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_WARN+"wrongCable"));
					return false;
				}
	
				if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
				{
					ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.getDimensionId(),masterPos.getX(),masterPos.getY(),masterPos.getZ()});
					target.writeToNBT(stack.getTagCompound());
				}
				else
				{
					WireType type = getWireType(stack);
					int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos"); 
					BlockPos linkPos = new BlockPos(array[1],array[2],array[3]);
					TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
					int distanceSq = (int) Math.ceil( linkPos.distanceSq(masterPos) );
					if(array[0]!=world.provider.getDimensionId())
						player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_WARN+"wrongDimension"));
					else if(linkPos.equals(masterPos))
						player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_WARN+"sameConnection"));
					else if( distanceSq > (type.getMaxLength()*type.getMaxLength()))
						player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_WARN+"tooFar"));
					else if(!(tileEntityLinkingPos instanceof IImmersiveConnectable))
						player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_WARN+"invalidPoint"));
					else
					{
						IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
						IImmersiveConnectable nodeLink = (IImmersiveConnectable)tileEntityLinkingPos;
						boolean connectionExists = false;
						Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, Utils.toCC(nodeHere));
						if(outputs!=null)
							for(Connection con : outputs)
							{
								if(con.end.equals(Utils.toCC(nodeLink)))
									connectionExists = true;
							}
						if(connectionExists)
							player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_WARN+"connectionExists"));
						else
						{
							Vec3 rtOff0 = nodeHere.getRaytraceOffset(nodeLink).addVector(masterPos.getX(), masterPos.getY(), masterPos.getZ());
							Vec3 rtOff1 = nodeLink.getRaytraceOffset(nodeHere).addVector(linkPos.getX(), linkPos.getY(), linkPos.getZ());
							Set<BlockPos> ignore = new HashSet<>();
							ignore.addAll(nodeHere.getIgnored(nodeLink));
							ignore.addAll(nodeLink.getIgnored(nodeHere));
							boolean canSee = Utils.rayTraceForFirst(rtOff0, rtOff1, world, ignore)==null;
							if(canSee)
							{
								TargetingInfo targetLink = TargetingInfo.readFromNBT(stack.getTagCompound());
								ImmersiveNetHandler.INSTANCE.addConnection(world, Utils.toCC(nodeHere), Utils.toCC(nodeLink), (int)Math.sqrt(distanceSq), type);
								
								nodeHere.connectCable(type, target);
								nodeLink.connectCable(type, targetLink);
								IESaveData.setDirty(world.provider.getDimensionId());
								player.triggerAchievement(IEAchievements.connectWire);
								
								if(!player.capabilities.isCreativeMode)
									stack.stackSize--;
								((TileEntity)nodeHere).markDirty();
								world.addBlockEvent(masterPos, ((TileEntity) nodeHere).getBlockType(), -1, 0);
								world.markBlockForUpdate(masterPos);
								((TileEntity)nodeLink).markDirty();
								world.addBlockEvent(linkPos, ((TileEntity) nodeLink).getBlockType(), -1, 0);
								world.markBlockForUpdate(linkPos);
							}
							else
								player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_WARN+"cantSee"));
						}
					}
					ItemNBTHelper.remove(stack, "linkingPos");
					ItemNBTHelper.remove(stack, "side");
					ItemNBTHelper.remove(stack, "hitX");
					ItemNBTHelper.remove(stack, "hitY");
					ItemNBTHelper.remove(stack, "hitZ");
				}
				return true;
			}
		}
		return false;
	}
}