package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.IShaderEquipableItem;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.util.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ItemChemthrower extends ItemUpgradeableTool implements IAdvancedFluidItem, IShaderEquipableItem, ITool
{
	public ItemChemthrower()
	{
		super("chemthrower", 1, "CHEMTHROWER");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		ItemStack shader = getShaderItem(stack);
		if(shader!=null)
			list.add(TextFormatting.DARK_GRAY+shader.getDisplayName());

		FluidStack fs = getFluid(stack);
		if(fs!=null)
		{
			TextFormatting rarity = fs.getFluid().getRarity()==EnumRarity.COMMON? TextFormatting.GRAY:fs.getFluid().getRarity().rarityColor;
			list.add(rarity+fs.getLocalizedName()+ TextFormatting.GRAY+": "+fs.amount+"/"+getCapacity(stack,2000)+"mB");
		}
		else
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.empty"));
	}
	@Override
	public boolean isFull3D()
	{
		return true;
	}
	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.NONE;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
	{
		if(player.isSneaking())
		{
			if(!world.isRemote)
				ItemNBTHelper.setBoolean(stack, "ignite", !ItemNBTHelper.getBoolean(stack, "ignite"));
		}
		else
			player.setActiveHand(hand);
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count)
	{
		FluidStack fs = this.getFluid(stack);
		if(fs!=null && fs.getFluid()!=null)
		{
			int duration = getMaxItemUseDuration(stack)-count;
			int consumed = Config.getInt("chemthrower_consumption");
			if(consumed*duration<=fs.amount)
			{
				Vec3d v = player.getLookVec();
				int split = 8;
				boolean isGas = fs.getFluid().isGaseous()||ChemthrowerHandler.isGas(fs.getFluid());

				float scatter = isGas?.15f:.05f;
				float range = isGas?.5f:1f;
				if(getUpgrades(stack).getBoolean("focus"))
				{
					range += .25f;
					scatter -= .025f;
				}

				boolean ignite = ChemthrowerHandler.isFlammable(fs.getFluid())&&ItemNBTHelper.getBoolean(stack, "ignite");
				for(int i=0; i<split; i++)
				{	
					Vec3d vecDir = v.addVector(player.getRNG().nextGaussian()*scatter,player.getRNG().nextGaussian()*scatter,player.getRNG().nextGaussian()*scatter);
					EntityChemthrowerShot chem = new EntityChemthrowerShot(player.worldObj, player, vecDir.xCoord*0.25,vecDir.yCoord*0.25,vecDir.zCoord*0.25, fs.getFluid());
					chem.motionX = vecDir.xCoord*range;
					chem.motionY = vecDir.yCoord*range;
					chem.motionZ = vecDir.zCoord*range;
					if(ignite)
						chem.setFire(10);
					if(!player.worldObj.isRemote)
						player.worldObj.spawnEntityInWorld(chem);
				}
				if(count%4==0)
				{
					if(ignite)
						player.playSound(IESounds.sprayFire, .5f, 1.5f);
					else
						player.playSound(IESounds.spray, .5f, .75f);
				}
			}
			else
				player.stopActiveHand();
		}
		else
			player.stopActiveHand();
	}
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft)
	{
		FluidStack fs = this.getFluid(stack);
		if(fs!=null)
		{
			int duration = getMaxItemUseDuration(stack)-timeLeft;
			fs.amount -=  Config.getInt("chemthrower_consumption")*duration;
			if(fs.amount <= 0)
				ItemNBTHelper.remove(stack, "Fluid");
			else
				ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack)
	{
		FluidStack fs = getFluid(stack);
		if(fs!=null && fs.amount > getCapacity(stack,2000))
		{
			fs.amount = getCapacity(stack,2000);
			ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		return new IEItemFluidHandler(stack, 2000);
	}
	@Override
	public int getCapacity(ItemStack stack, int baseCapacity)
	{
		return baseCapacity+getUpgrades(stack).getInteger("capacity");
	}

	@Override
	public void setShaderItem(ItemStack stack, ItemStack shader)
	{
		ItemStack[] contained = this.getContainedItems(stack);
		contained[3] =  shader;
		this.setContainedItems(stack, contained);
	}
	@Override
	public ItemStack getShaderItem(ItemStack stack)
	{
		ItemStack[] contained = this.getContainedItems(stack);
		return contained[3];
	}
	@Override
	public String getShaderType()
	{
		return "chemthrower";
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		return new Slot[]
				{
				new IESlot.Upgrades(container, invItem,0, 80,32, "CHEMTHROWER", stack, true),
				new IESlot.Upgrades(container, invItem,1,100,32, "CHEMTHROWER", stack, true),
				new IESlot.Upgrades(container, invItem,2,120,32, "CHEMTHROWER", stack, true),
				new IESlot.Shader(container, invItem,3,150,32, stack)
				};
	}
	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 4;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}