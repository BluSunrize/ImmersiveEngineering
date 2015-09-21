package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.InventoryStorageItem;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class ItemDrill extends ItemUpgradeableTool implements IFluidContainerItem
{
	public static Material[] validMaterials = {Material.anvil,Material.clay,Material.glass,Material.ground,Material.ice,Material.iron,Material.packedIce,Material.piston,Material.rock,Material.sand, Material.snow};

	public ItemDrill()
	{
		super("drill", 1, IUpgrade.UpgradeType.DRILL, "diesel");
	}
	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 4;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, InventoryStorageItem invItem)
	{
		return new Slot[]
				{
				new IESlot.DrillHead(container, invItem,0, 98,22),
				new IESlot.Upgrades(container, invItem,1,  78,42, IUpgrade.UpgradeType.DRILL, stack, true),
				new IESlot.Upgrades(container, invItem,2,  98,52, IUpgrade.UpgradeType.DRILL, stack, true),
				new IESlot.Upgrades(container, invItem,3, 118,42, IUpgrade.UpgradeType.DRILL, stack, true)
				};
	}
	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}
	@Override
	public void recalculateUpgrades(ItemStack stack)
	{
		super.recalculateUpgrades(stack);
		FluidStack fs = this.getFluid(stack);
		if(fs!=null && fs.amount>this.getCapacity(stack))
		{
			fs.amount = this.getCapacity(stack);
			ItemNBTHelper.setFluidStack(stack, "fuel",fs);
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		FluidStack fs = getFluid(stack);
		if(fs!=null)
			list.add(StatCollector.translateToLocal("desc.ImmersiveEngineering.flavour.drill.fuel")+" "+fs.amount+"/"+getCapacity(stack)+"mB");
		else
			list.add(StatCollector.translateToLocal("desc.ImmersiveEngineering.flavour.drill.empty"));
		if(getHead(stack)==null)
			list.add(StatCollector.translateToLocal("desc.ImmersiveEngineering.flavour.drill.noHead"));
		else
		{
			int maxDmg = getMaxHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			String status = ""+(quote<.1?EnumChatFormatting.RED: quote<.3?EnumChatFormatting.GOLD: quote<.6?EnumChatFormatting.YELLOW: EnumChatFormatting.GREEN);
			list.add(StatCollector.translateToLocal("desc.ImmersiveEngineering.flavour.drill.headDamage")+" "+status+dmg+"/"+maxDmg);
		}
	}

	/*RENDER STUFF*/
	@Override
	public boolean isFull3D()
	{
		return true;
	}
	public static HashMap<String, Integer> animationTimer = new HashMap<String, Integer>();
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
	{
		if(canDrillBeUsed(stack, entityLiving))
		{
			if(!animationTimer.containsKey(entityLiving.getCommandSenderName()))
				animationTimer.put(entityLiving.getCommandSenderName(), 40);
			else if(animationTimer.get(entityLiving.getCommandSenderName())<20)
				animationTimer.put(entityLiving.getCommandSenderName(), 20);
			return true;
		}
		return false;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double)getHeadDamage(stack) / (double)getMaxHeadDamage(stack);
	}
	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return getHeadDamage(stack)>0;
	}
	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.bow;
	}

	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player)
	{
		if(stack==null || player==null)
			return;
		player.triggerAchievement(IEAchievements.makeDrill);
	}
	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
		ItemStack[] contents = this.getContainedItems(stack);
		if(contents[0]!=null&&contents[1]!=null&&contents[2]!=null&&contents[3]!=null)
			player.triggerAchievement(IEAchievements.upgradeDrill);
	}
	
	/*INVENTORY STUFF*/
	public ItemStack getHead(ItemStack drill)
	{
		ItemStack head = this.getContainedItems(drill)[0];
		return head!=null&&head.getItem() instanceof IDrillHead?head: null;
	}
	public void setHead(ItemStack drill, ItemStack head)
	{
		ItemStack[] inv = this.getContainedItems(drill);
		inv[0] = head;
		this.setContainedItems(drill, inv);
	}

	/*TOOL STUFF*/

	public boolean canDrillBeUsed(ItemStack drill, EntityLivingBase player)
	{
		if(drill.getItemDamage()==0 && player.isInsideOfMaterial(Material.water) && !getUpgrades(drill).getBoolean("waterproof"))
			return false;
		if(drill.getItemDamage()==0 && getFluid(drill)==null)
			return false;
		return true;
	}

	public int getMaxHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return head!=null?((IDrillHead)head.getItem()).getMaximumHeadDamage(head): 0;
	}
	public int getHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return head!=null?((IDrillHead)head.getItem()).getHeadDamage(head): 0;
	}
	public boolean isDrillBroken(ItemStack stack)
	{
		return getHeadDamage(stack)>=getMaxHeadDamage(stack) || this.getFluid(stack)==null || this.getFluid(stack).amount<1;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(player.isSneaking())
			player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Workbench, world, (int)player.posX,(int)player.posY,(int)player.posZ);
		return stack;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase player)
	{
		return true;
	}
	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase living)
	{
		if((double)block.getBlockHardness(world, x, y, z) != 0.0D)
		{
			int dmg = ForgeHooks.isToolEffective(stack, block, world.getBlockMetadata(x, y, z))?1:3;
			ItemStack head = getHead(stack);
			if(head!=null)
			{
				if(living instanceof EntityPlayer)
				{
					if(((EntityPlayer)living).capabilities.isCreativeMode)
						return true;
					((IDrillHead)head.getItem()).afterBlockbreak(stack, head, (EntityPlayer)living);
				}
				((IDrillHead)head.getItem()).damageHead(head, dmg);
				this.setHead(stack, head);
				this.drain(stack, 1, true);
			}
		}

		return true;
	}
	@Override
	public int getItemEnchantability()
	{
		return 0;
	}
	@Override
	public Multimap getAttributeModifiers(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		Multimap multimap = super.getAttributeModifiers(stack);
		if(head!=null)
			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Tool modifier", ((IDrillHead)head.getItem()).getAttackDamage(head)+getUpgrades(stack).getInteger("damage"), 0));
		return multimap;
	}
	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass)
	{
		ItemStack head = getHead(stack);
		if(head!=null)
			return ((IDrillHead)head.getItem()).getMiningLevel(head)+ItemNBTHelper.getInt(stack, "harvestLevel");
		return 0;
	}
	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		if(getHead(stack)!=null && !isDrillBroken(stack))
			return ImmutableSet.of("pickaxe");
		return super.getToolClasses(stack);
	}

	public boolean isEffective(Material mat)
	{
		for(Material m : validMaterials)
			if(m == mat)
				return true;
		return false;
	}
	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack)
	{
		return isEffective(block.getMaterial()) && !isDrillBroken(stack);
	}
	@Override
	public float func_150893_a(ItemStack stack, Block block)
	{
		ItemStack head = getHead(stack);
		return isEffective(block.getMaterial())&&head!=null&&!isDrillBroken(stack) ? ((IDrillHead)head.getItem()).getMiningSpeed(head)+ItemNBTHelper.getInt(stack, "speed") : 1.0F;
	}
	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		ItemStack head = getHead(stack);
		if(ForgeHooks.isToolEffective(stack, block, meta) && head!=null && !isDrillBroken(stack))
			return ((IDrillHead)head.getItem()).getMiningSpeed(head)+ItemNBTHelper.getInt(stack, "speed");
		return super.getDigSpeed(stack, block, meta);
	}
	public boolean canBreakExtraBlock(World world, Block block, int x, int y, int z, int meta, EntityPlayer player, ItemStack drill, ItemStack head, boolean inWorld)
	{
		if(block.canHarvestBlock(player, meta) && isEffective(block.getMaterial()) && !isDrillBroken(drill))
		{
			if(inWorld)
				return !((IDrillHead)head.getItem()).beforeBlockbreak(drill, head, player);
			else
				return true;
		}
		return false;
	}
	@Override
	public boolean onBlockStartBreak(ItemStack stack, int ix, int iy, int iz, EntityPlayer player)
	{
		World world = player.worldObj;
		if(player.isSneaking() || world.isRemote || !(player instanceof EntityPlayerMP))
			return false;
		MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);
		ItemStack head = getHead(stack);
		if(mop==null || head==null)
			return false;
		int side = mop.sideHit;
		int diameter = ((IDrillHead)head.getItem()).getMiningSize(head)+getUpgrades(stack).getInteger("size");
		int depth = ((IDrillHead)head.getItem()).getMiningDepth(head)+getUpgrades(stack).getInteger("depth");

		int startX=ix;
		int startY=iy;
		int startZ=iz;
		if(diameter%2==0)//even numbers
		{
			float hx = (float)mop.hitVec.xCoord-ix;
			float hy = (float)mop.hitVec.yCoord-iy;
			float hz = (float)mop.hitVec.zCoord-iz;
			if((side<2&&hx<.5)||(side<4&&hx<.5))
				startX-= diameter/2;
			if(side>1&&hy<.5)
				startY-= diameter/2;
			if((side<2&&hz<.5)||(side>3&&hz<.5))
				startZ-= diameter/2;
		}
		else//odd numbers
		{
			startX-=(side==4||side==5?0: diameter/2);
			startY-=(side==0||side==1?0: diameter/2);
			startZ-=(side==2||side==3?0: diameter/2);
		}

		for(int dd=0; dd<depth; dd++)
			for(int dw=0; dw<diameter; dw++)
				for(int dh=0; dh<diameter; dh++)
				{
					int x = startX+ (side==4||side==5?dd: dw);
					int y = startY+ (side==0||side==1?dd: dh);
					int z = startZ+ (side==0||side==1?dh: side==4||side==5?dw: dd);
					if(x==ix&&y==iy&&z==iz)
						continue;
					if(!world.blockExists(x, y, z))
						continue;
					Block block = world.getBlock(x, y, z);
					int meta = world.getBlockMetadata(x, y, z);

					if(block!=null && !block.isAir(world, x, y, z) && block.getPlayerRelativeBlockHardness(player, world, x, y, z) != 0)
					{
						if(!this.canBreakExtraBlock(world, block, x, y, z, meta, player, stack, head, true))
							continue;
						BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP)player).theItemInWorldManager.getGameType(), (EntityPlayerMP) player, x,y,z);
						if(event.isCanceled())
							continue;


						if(player.capabilities.isCreativeMode)
						{
							block.onBlockHarvested(world, x, y, z, meta, player);
							if (block.removedByPlayer(world, player, x, y, z, false))
								block.onBlockDestroyedByPlayer(world, x, y, z, meta);
						} 
						else
						{
							block.onBlockHarvested(world, x,y,z, meta, player);
							if(block.removedByPlayer(world, player, x,y,z, true))
							{
								block.onBlockDestroyedByPlayer( world, x,y,z, meta);
								block.harvestBlock(world, player, x,y,z, meta);
								block.dropXpOnBlockBreak(world, x,y,z, event.getExpToDrop());
							}
							player.getCurrentEquippedItem().func_150999_a(world, block, x, y, z, player);
						}
						world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
						((EntityPlayerMP)player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
					}
				}
		return false;
	}

	/*FLUID STUFF*/
	@Override
	public FluidStack getFluid(ItemStack container)
	{
		return ItemNBTHelper.getFluidStack(container, "fuel");
	}
	@Override
	public int getCapacity(ItemStack container)
	{
		return 2000+getUpgrades(container).getInteger("capacity");
	}
	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill)
	{
		if(resource!=null && IEContent.fluidBiodiesel.equals(resource.getFluid()))
		{
			FluidStack fs = getFluid(container);
			int space = fs==null?getCapacity(container): getCapacity(container)-fs.amount;
			int accepted = Math.min(space, resource.amount);
			if(fs==null)
				fs = new FluidStack(IEContent.fluidBiodiesel, accepted);
			else
				fs.amount += accepted;
			if(doFill)
				ItemNBTHelper.setFluidStack(container, "fuel", fs);
			return accepted;
		}
		return 0;
	}
	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
	{
		FluidStack fs = getFluid(container);
		if(fs == null)
			return null;
		int drained = Math.min(maxDrain, fs.amount);
		FluidStack stack = new FluidStack(fs, drained);
		if(doDrain)
		{
			fs.amount -= drained;
			if(fs.amount <= 0)
				ItemNBTHelper.remove(container, "fuel");
			else
				ItemNBTHelper.setFluidStack(container, "fuel", fs);
		}
		return stack;
	}
	/*===================================== FLUIDS END =================================*/
}