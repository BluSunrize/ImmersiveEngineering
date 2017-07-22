package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.IInternalStorageItem;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;

public class ContainerRevolver extends ContainerInternalStorageItem
{
	public static int[][][] slotPositions = {
			{
					{ 48, 11},
					{ 56, 30}
			},
			{
					{ 48, 11},
					{ 68,  3},
					{ 78, 22},
					{ 88,  3},
					{ 98, 22},
					{108,  3},
					{118, 22},
					{ 56, 30}
			},
			{
					{ 48,  3},
					{ 67,  3},
					{ 86,  3},
					{105,  3},
					{124, 11},
					{132, 30},
					{124, 49},
					{105, 57},
					{ 86, 49},
					{ 86, 30},
					{ 67, 30},
					{ 48, 30},
			}
	};

	private EntityEquipmentSlot otherHand;
	public ItemStack otherRevolver;
	private IInventory otherInventory;
	private int offset = 0;

	public ContainerRevolver(InventoryPlayer iinventory, World world, EntityEquipmentSlot slot, ItemStack revolver)
	{
		super(iinventory, world, slot, revolver);

		if(this.otherInventory!=null)
		{
			((InventoryStorageItem)this.otherInventory).stackList = ((IInternalStorageItem)this.otherRevolver.getItem()).getContainedItems(this.otherRevolver);
			this.onCraftMatrixChanged(this.otherInventory);
		}
	}

	@Override
	int addSlots(InventoryPlayer iinventory)
	{
		if(this.equipmentSlot==EntityEquipmentSlot.MAINHAND||this.equipmentSlot==EntityEquipmentSlot.OFFHAND)
		{
			int bullets0 = ((ItemRevolver)(heldItem).getItem()).getBulletSlotAmount(heldItem);

			this.otherHand = this.equipmentSlot==EntityEquipmentSlot.MAINHAND?EntityEquipmentSlot.OFFHAND:EntityEquipmentSlot.MAINHAND;
			this.otherRevolver = iinventory.player.getItemStackFromSlot(this.otherHand);
			if(!otherRevolver.isEmpty() && otherRevolver.getItem() instanceof ItemRevolver)
			{
				this.otherInventory = new InventoryStorageItem(this, otherRevolver);
				int bullets1 = ((ItemRevolver)(otherRevolver).getItem()).getBulletSlotAmount(otherRevolver);
				this.offset = ((bullets0>=18?150:bullets0>8?136:74)+(bullets1>=18?150:bullets1>8?136:74)+4-176)/2;
			}
			else
			{
				this.otherRevolver = null;
				this.otherHand = null;
				this.offset = ((bullets0>=18?150:bullets0>8?136:74)-176)/2;
			}
		}

		int total = 0;
		int off = (offset<0?-offset:0);
		for(int hand=0; hand<(this.otherHand!=null?2:1); hand++)
		{
			int i = 0;
			ItemStack held = this.otherHand==null?heldItem: (hand==0)==(player.getPrimaryHand()==EnumHandSide.RIGHT)?otherRevolver:heldItem;
			IInventory inv = this.otherHand==null?input: (hand==0)==(player.getPrimaryHand()==EnumHandSide.RIGHT)?otherInventory:input;
			int revolverSlots = ((ItemRevolver)(held).getItem()).getBulletSlotAmount(held);

			this.addSlotToContainer(new IESlot.Bullet(this, inv, i++, off+29, 3, 1));
			int slots = revolverSlots >= 18?2: revolverSlots > 8?1: 0;
			for(int[] slot : slotPositions[slots])
				this.addSlotToContainer(new IESlot.Bullet(this, inv, i++, off+slot[0], slot[1], 1));
			this.addSlotToContainer(new IESlot.Bullet(this, inv, i++,off+48, 49, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, inv, i++,off+29, 57, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, inv, i++,off+10, 49, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, inv, i++,off+2, 30, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, inv, i++,off+10, 11, 1));
			off += (revolverSlots>=18?150: revolverSlots>8?136: 74)+4;
			total += i;
		}

		this.bindPlayerInventory(iinventory);
		return total;
	}
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		int off = (offset>0?offset:0);
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, off+8+j*18, 85+i*18));

		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(inventoryPlayer, i, off+8+i*18, 143));
	}


	@Override
	protected boolean allowShiftclicking()
	{
		return false;
	}
}