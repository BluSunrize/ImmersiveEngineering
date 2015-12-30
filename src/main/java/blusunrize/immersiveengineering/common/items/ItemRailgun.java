package blusunrize.immersiveengineering.common.items;

import java.util.HashSet;
import java.util.List;

import blusunrize.immersiveengineering.api.shader.IShaderEquipableItem;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.entities.EntityRailgunShot;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemRailgun extends ItemUpgradeableTool implements IShaderEquipableItem, IEnergyContainerItem, IZoomTool
{
	public ItemRailgun()
	{
		super("railgun", 1, "RAILGUN");
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 2+1;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		return new Slot[]
				{
						new IESlot.Upgrades(container, invItem,0, 80,32, "RAILGUN", stack, true),
						new IESlot.Upgrades(container, invItem,1,100,32, "RAILGUN", stack, true),
						new IESlot.Shader(container, invItem,2,130,32, stack)
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
		if(this.getEnergyStored(stack)>this.getMaxEnergyStored(stack))
			ItemNBTHelper.setInt(stack, "energy", this.getMaxEnergyStored(stack));
	}
	@Override
	public void clearUpgrades(ItemStack stack)
	{
		super.clearUpgrades(stack);
		if(this.getEnergyStored(stack)>this.getMaxEnergyStored(stack))
			ItemNBTHelper.setInt(stack, "energy", this.getMaxEnergyStored(stack));
	}


	@Override
	public void setShaderItem(ItemStack stack, ItemStack shader)
	{
		ItemStack[] contained = this.getContainedItems(stack);
		contained[2] = shader;
		this.setContainedItems(stack, contained);
	}
	@Override
	public ItemStack getShaderItem(ItemStack stack)
	{
		ItemStack[] contained = this.getContainedItems(stack);
		return contained[2];
	}
	@Override
	public String getShaderType()
	{
		return "railgun";
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		ItemStack shader = getShaderItem(stack);
		if(shader!=null)
			list.add(EnumChatFormatting.DARK_GRAY+shader.getDisplayName());
		String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
		list.add(StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.energyStored", stored));
	}
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		//		if(stack.getItemDamage()!=1)
		//		{
		//			String tag = getRevolverDisplayTag(stack);
		//			if(!tag.isEmpty())
		//				return this.getUnlocalizedName()+"."+tag;
		//		}
		return super.getUnlocalizedName(stack);
	}
	@Override
	public boolean isFull3D()
	{
		return true;
	}

	//	@Override
	//	public Multimap getAttributeModifiers(ItemStack stack)
	//	{
	//		Multimap multimap = super.getAttributeModifiers(stack);
	//		double melee = getUpgrades(stack).getDouble("melee");
	//		if(melee!=0)
	//			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", melee, 0));
	//		double speed = getUpgrades(stack).getDouble("speed");
	//		if(speed!=0)
	//			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", speed, 1));
	//		return multimap;
	//	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.bow;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		//		if(!world.isRemote && stack.getItemDamage()!=1 && ent!=null && ItemNBTHelper.hasKey(stack, "blocked"))
		//		{
		//			int l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "casings", ent);
		//			if(l==0)
		//				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderFill", "tile.piston.in",.3f,3, 1,6,1);
		//			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderFill", ent);
		//			if(l==0)
		//				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderClose", "fire.ignite",.6f,5, 1,6,1);
		//			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderClose", ent);
		//			if(l==0)
		//				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderSpin", "note.hat",.1f,5, 5,8,1);
		//			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderSpin", ent);
		//			if(l==0)
		//				ItemNBTHelper.remove(stack, "blocked");
		//		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		int energy = Config.getInt("railgun_consumption");
		float energyMod = 1 + this.getUpgrades(stack).getFloat("consumption");
		energy = (int)(energy*energyMod);
		if(this.extractEnergy(stack, energy, true)==energy && findAmmo(player)!=null)
		{
			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
			String sound = "chargeSlow";
			if(getChargeTime(stack)<=20)
				sound = "chargeFast";
			player.playSound("immersiveengineering:"+sound, 1.5f, 1);
		}
		return stack;
	}
	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count)
	{
		int inUse = this.getMaxItemUseDuration(stack)-count;
		if(inUse>getChargeTime(stack) && inUse%20 == player.getRNG().nextInt(20))
			player.playSound("immersiveengineering:spark", .8f+(.2f*player.getRNG().nextFloat()), .5f+(.5f*player.getRNG().nextFloat()));
	}
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft)
	{
		int inUse = this.getMaxItemUseDuration(stack)-timeLeft;
		if(inUse<getChargeTime(stack))
			return;
		int energy = Config.getInt("railgun_consumption");
		float energyMod = 1 + this.getUpgrades(stack).getFloat("consumption");
		energy = (int)(energy*energyMod);
		if(this.extractEnergy(stack, energy, true)==energy)
		{
			int ammoSlot = findAmmoSlot(player);
			if(ammoSlot>=0)
			{
				ItemStack ammo = player.inventory.mainInventory[ammoSlot];
				Vec3 vec = player.getLookVec();
				float speed = 20;
				EntityRailgunShot shot = new EntityRailgunShot(player.worldObj, player, vec.xCoord*speed,vec.yCoord*speed,vec.zCoord*speed, Utils.copyStackWithAmount(ammo, 1));
				player.inventory.decrStackSize(ammoSlot, 1);
				player.playSound("immersiveengineering:railgunFire", 1, .5f+(.5f*player.getRNG().nextFloat()));
				this.extractEnergy(stack, energy, false);
				if(!world.isRemote)
					player.worldObj.spawnEntityInWorld(shot);
			}
		}
	}

	public static ItemStack findAmmo(EntityPlayer player)
	{
		int slot = findAmmoSlot(player);
		return slot>=0?player.inventory.mainInventory[slot]:null;
	}
	public static int findAmmoSlot(EntityPlayer player)
	{
		ItemStack[] inventory = player.inventory.mainInventory;
		for(int i=0; i<inventory.length; i++)
		{
			ItemStack stack = inventory[i];
			if(stack == null)
				continue;
			RailgunHandler.RailgunProjectileProperties prop = RailgunHandler.getProjectileProperties(stack);
			if(prop!=null)
				return i;
		}
		return -1;
	}

	public int getChargeTime(ItemStack railgun)
	{
		return (int)(40/(1+this.getUpgrades(railgun).getFloat("speed")));
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
		//		ItemStack[] contents = this.getContainedItems(stack);
		//		if(contents[18]!=null&&contents[19]!=null)
		//			player.triggerAchievement(IEAchievements.upgradeRevolver);
	}

	@Override
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate)
	{
		int stored = getEnergyStored(container);
		int accepted = Math.min(maxReceive, getMaxEnergyStored(container)-stored);
		if(!simulate)
		{
			stored += accepted;
			ItemNBTHelper.setInt(container, "energy", stored);
		}
		return accepted;
	}
	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate)
	{
		int stored = getEnergyStored(container);
		int extracted = Math.min(maxExtract, stored);
		if(!simulate)
		{
			stored -= extracted;
			ItemNBTHelper.setInt(container, "energy", stored);
		}
		return extracted;
	}

	@Override
	public int getEnergyStored(ItemStack container)
	{
		return ItemNBTHelper.getInt(container, "energy");
	}
	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return 8000+this.getUpgrades(container).getInteger("capacity");
	}


	public String[] compileRender(ItemStack stack)
	{
		HashSet<String> render = new HashSet<String>();
		render.add("frame");
		render.add("barrel");
		render.add("grip");
		render.add("capacitors");
		render.add("sled");
		render.add("wires");
		NBTTagCompound upgrades = this.getUpgrades(stack);
		if(upgrades.getDouble("speed")>0)
			render.add("upgrade_speed");
		if(upgrades.getBoolean("scope"))
			render.add("upgrade_scope");
		return render.toArray(new String[render.size()]);
	}

	@Override
	public boolean canZoom(ItemStack stack, EntityPlayer player)
	{
		return this.getUpgrades(stack).getBoolean("scope");
	}
	float[] zoomSteps = new float[]{.1f,.15625f,.2f,.25f, .3125f, .4f, .5f,.625f};
	@Override
	public float[] getZoomSteps(ItemStack stack, EntityPlayer player)
	{
		return zoomSteps;
	}
}