package blusunrize.immersiveengineering.common.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import blusunrize.immersiveengineering.api.shader.IShaderEquipableItem;
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
		//		if(!world.isRemote)
		//		{
		//			System.out.println("CLICKIGN SHIT");
		//			Vec3 v = player.getLookVec();
		//			int split = 8;
		//			for(int i=0; i<split; i++)
		//			{	
		//				//			float angle = i * (360f/split);
		//				//			Matrix4 matrix = new Matrix4();
		//				//			matrix.rotate(angle, v.xCoord,v.yCoord,0);
		//				//			Vec3 vecDir = Vec3.createVectorHelper(0, 0, 1);
		//				//			matrix.apply(vecDir);
		//				float scatter = .025f;
		//				Vec3 vecDir = v.addVector(player.getRNG().nextGaussian()*scatter,player.getRNG().nextGaussian()*scatter,player.getRNG().nextGaussian()*scatter);
		//
		//				EntityChemthrowerShot chem = new EntityChemthrowerShot(world, player, vecDir.xCoord*1.5,vecDir.yCoord*1.5,vecDir.zCoord*1.5, FluidRegistry.LAVA);
		////							chem.setPosition(player.posX+vecDir.xCoord, player.posY+vecDir.yCoord, posZ+vecDir.zCoord);
		//				chem.motionX = vecDir.xCoord;
		//				chem.motionY = vecDir.yCoord;
		//				chem.motionZ = vecDir.zCoord;
		//
		//				if(!world.isRemote)
		//				world.spawnEntityInWorld(chem);
		//			}
		//		}

		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count)
	{
		Vec3 v = player.getLookVec();
		int split = 8;
		for(int i=0; i<split; i++)
		{	
			float scatter = .025f;
			Vec3 vecDir = v.addVector(player.getRNG().nextGaussian()*scatter,player.getRNG().nextGaussian()*scatter,player.getRNG().nextGaussian()*scatter);

			EntityChemthrowerShot chem = new EntityChemthrowerShot(player.worldObj, player, vecDir.xCoord*0.25,vecDir.yCoord*0.25,vecDir.zCoord*0.25, FluidRegistry.LAVA);
			chem.motionX = vecDir.xCoord;
			chem.motionY = vecDir.yCoord;
			chem.motionZ = vecDir.zCoord;

			if(!player.worldObj.isRemote)
				player.worldObj.spawnEntityInWorld(chem);
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
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
		//		if(resource!=null && IEContent.fluidBiodiesel.equals(resource.getFluid()))
		//		{
		//			FluidStack fs = getFluid(container);
		//			int space = fs==null?getCapacity(container): getCapacity(container)-fs.amount;
		//			int accepted = Math.min(space, resource.amount);
		//			if(fs==null)
		//				fs = new FluidStack(IEContent.fluidBiodiesel, accepted);
		//			else
		//				fs.amount += accepted;
		//			if(doFill)
		//				ItemNBTHelper.setFluidStack(container, "fuel", fs);
		//			return accepted;
		//		}
		return 0;
	}
	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
	{
		//		FluidStack fs = getFluid(container);
		//		if(fs == null)
		//			return null;
		//		int drained = Math.min(maxDrain, fs.amount);
		//		FluidStack stack = new FluidStack(fs, drained);
		//		if(doDrain)
		//		{
		//			fs.amount -= drained;
		//			if(fs.amount <= 0)
		//				ItemNBTHelper.remove(container, "fuel");
		//			else
		//				ItemNBTHelper.setFluidStack(container, "fuel", fs);
		//		}
		//		return stack;
		return null;
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
