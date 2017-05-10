package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ContainerModWorkbench extends ContainerIEBase<TileEntityModWorkbench>
{
	public InventoryStorageItem toolInv;
	public InventoryShader shaderInv;
	public InventoryPlayer inventoryPlayer;

	public ContainerModWorkbench(InventoryPlayer inventoryPlayer, TileEntityModWorkbench tile)
	{
		super(inventoryPlayer, tile);

		this.inventoryPlayer = inventoryPlayer;
		rebindSlots();
	}

	private void bindPlayerInv(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 87+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 145));
	}

	public void rebindSlots()
	{
		this.inventorySlots.clear();
		this.addSlotToContainer(new IESlot.UpgradeableItem(this, this.inv, 0, 24, 22, 1));
		slotCount=1;

		ItemStack tool = this.getSlot(0).getStack();
		if(!tool.isEmpty())
		{
			if(tool.getItem() instanceof IUpgradeableTool)
			{
				if(tool.getItem() instanceof ItemEngineersBlueprint)
					((ItemEngineersBlueprint)tool.getItem()).updateOutputs(tool);

				this.toolInv = new InventoryStorageItem(this, tool);
				Slot[] slots = ((IUpgradeableTool)tool.getItem()).getWorkbenchSlots(this, tool, toolInv);
				if(slots != null)
					for(Slot s : slots)
					{
						this.addSlotToContainer(s);
						slotCount++;
					}

				NonNullList<ItemStack> cont = ((IUpgradeableTool)tool.getItem()).getContainedItems(tool);
				this.toolInv.stackList = cont;
			}
			if(tool.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
			{
				ShaderWrapper wrapper = tool.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
				if(wrapper!=null)
				{
					this.shaderInv = new InventoryShader(this, wrapper);
					this.addSlotToContainer(new IESlot.Shader(this, shaderInv, 0, 130, 32, tool));
					slotCount++;
					this.shaderInv.shader = wrapper.getShaderItem();
				}
			}
		}
		bindPlayerInv(inventoryPlayer);
		ImmersiveEngineering.proxy.reInitGui();
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = inventorySlots.get(slot);

		if (slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if (slot < slotCount)
			{
				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount + 36), true))
					return ItemStack.EMPTY;
			}
			else if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.getItem() instanceof IUpgradeableTool && ((IUpgradeableTool)stackInSlot.getItem()).canModify(stackInSlot))
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(stackInSlot.getItem() instanceof IConfigurableTool && ((IConfigurableTool)stackInSlot.getItem()).canConfigure(stackInSlot))
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(slotCount>1)
				{
					boolean b = true;
					for(int i=1; i<slotCount; i++)
					{
						Slot s = inventorySlots.get(i);
						if(s!=null && s.isItemValid(stackInSlot))
							if(this.mergeItemStack(stackInSlot, i, i+1, true))
							{
								b = false;
								break;
							}
							else
								continue;
					}
					if(b)
						return ItemStack.EMPTY;
				}
			}

			if (stackInSlot.getCount() == 0)
				slotObject.putStack(ItemStack.EMPTY);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.getCount() == stack.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, stack);
		}
		return stack;
	}
	@Override
	public void onCraftMatrixChanged(IInventory p_75130_1_)
	{
		super.onCraftMatrixChanged(p_75130_1_);
		tile.markContainingBlockForUpdate(null);
	}
}