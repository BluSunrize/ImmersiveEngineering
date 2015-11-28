package blusunrize.immersiveengineering.common.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import blusunrize.immersiveengineering.api.shader.IShaderEquipableItem;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;

public class ItemChemthrower extends ItemUpgradeableTool implements IShaderEquipableItem, IFluidContainerItem
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
			list.add(EnumChatFormatting.DARK_GRAY+shader.getDisplayName());

		FluidStack fs = getFluid(stack);
		if(fs!=null)
		{
			EnumChatFormatting rarity = fs.getFluid().getRarity()==EnumRarity.common?EnumChatFormatting.GRAY:fs.getFluid().getRarity().rarityColor;
			list.add(rarity+fs.getLocalizedName()+EnumChatFormatting.GRAY+": "+fs.amount+"/"+getCapacity(stack)+"mB");
		}
		else
			list.add(StatCollector.translateToLocal("desc.ImmersiveEngineering.flavour.drill.empty"));
	}
	@Override
	public boolean isFull3D()
	{
		return true;
	}
	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.bow;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(player.isSneaking())
		{
			if(!world.isRemote)
				ItemNBTHelper.setBoolean(stack, "ignite", !ItemNBTHelper.getBoolean(stack, "ignite"));
		}
		else
			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count)
	{
		FluidStack fs = this.getFluid(stack);
		if(fs!=null && fs.getFluid()!=null)
		{
			int duration = getMaxItemUseDuration(stack)-count;
			int consumed = Config.getInt("chemthrower_consumption");
			if(consumed*duration<=fs.amount)
			{
				Vec3 v = player.getLookVec();
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
					Vec3 vecDir = v.addVector(player.getRNG().nextGaussian()*scatter,player.getRNG().nextGaussian()*scatter,player.getRNG().nextGaussian()*scatter);
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
						player.playSound("immersiveengineering:spray_fire", .5f, 1.5f);
					else
						player.playSound("immersiveengineering:spray", .5f, .75f);
				}
			}
			else
				player.stopUsingItem();
		}
		else
			player.stopUsingItem();
	}
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft)
	{
		FluidStack fs = this.getFluid(stack);
		if(fs!=null)
		{
			int duration = getMaxItemUseDuration(stack)-timeLeft;
			int consumed = Config.getInt("chemthrower_consumption");
			fs.amount -= consumed*duration;
			if(fs.amount <= 0)
				ItemNBTHelper.remove(stack, "fluid");
			else
				ItemNBTHelper.setFluidStack(stack, "fluid", fs);
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void clearUpgrades(ItemStack stack)
	{
		super.clearUpgrades(stack);
		FluidStack fs = getFluid(stack);
		if(fs!=null && fs.amount > getCapacity(stack))
		{
			fs.amount = getCapacity(stack);
			ItemNBTHelper.setFluidStack(stack, "fluid", fs);
		}
	}

	@Override
	public FluidStack getFluid(ItemStack container)
	{
		return ItemNBTHelper.getFluidStack(container, "fluid");
	}
	@Override
	public int getCapacity(ItemStack container)
	{
		return 2000+getUpgrades(container).getInteger("capacity");
	}
	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill)
	{
		if(resource!=null)
		{
			FluidStack fs = getFluid(container);
			if(fs==null || fs.amount<0 || fs.isFluidEqual(resource))
			{
				int space = fs==null?getCapacity(container): getCapacity(container)-fs.amount;
				int accepted = Math.min(space, resource.amount);
				if(fs==null)
					fs = new FluidStack(resource, accepted);
				else
					fs.amount += accepted;
				if(doFill)
					ItemNBTHelper.setFluidStack(container, "fluid", fs);
				return accepted;
			}
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
				ItemNBTHelper.remove(container, "fluid");
			else
				ItemNBTHelper.setFluidStack(container, "fluid", fs);
		}
		return stack;
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

}
