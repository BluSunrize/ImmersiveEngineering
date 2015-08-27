package blusunrize.immersiveengineering.common.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemIEBase extends Item
{
	public String itemName;
	public String[] subNames;
	public IIcon[] icons;
	public ItemIEBase(String name, int stackSize, String... subNames)
	{
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setHasSubtypes(subNames!=null&&subNames.length>0);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.setMaxStackSize(stackSize);
		this.itemName = name;
		this.subNames = subNames!=null&&subNames.length<1?null:subNames;
		this.icons = new IIcon[this.subNames!=null?this.subNames.length:1];

		GameRegistry.registerItem(this, name);
	}

	
	public String[] getSubNames()
	{
		return subNames;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		if(getSubNames()!=null)
			for(int i=0;i<icons.length;i++)
				this.icons[i] = ir.registerIcon("immersiveengineering:"+itemName+"_"+getSubNames()[i]);
		else
			this.icons[0] = ir.registerIcon("immersiveengineering:"+itemName);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta)
	{
		if(getSubNames()!=null)
			if(meta>=0 && meta<icons.length)
				return this.icons[meta];
		return icons[0];
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		if(getSubNames()!=null)
			for(int i=0;i<getSubNames().length;i++)
				list.add(new ItemStack(this,1,i));
		else
			list.add(new ItemStack(this));

	}
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if(getSubNames()!=null)
		{
			String subName = stack.getItemDamage()<getSubNames().length?getSubNames()[stack.getItemDamage()]:"";
			return this.getUnlocalizedName()+"."+subName;
		}
		return this.getUnlocalizedName();
	}
}
