package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.tool.IShaderItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemShader extends ItemIEBase implements IShaderItem
{
	public ItemShader()
	{
		super("shader", 1);
		addShader("Rosequartz", 0, new int[]{65,35,35,255}, new int[]{230,180,180,255}, new int[]{240,205,205,255}, null);
		addShader("Argo", 2, new int[]{45,45,45,255}, new int[]{220,220,220,255}, new int[]{220,120,35,255}, null);
		addShader("Sunstrike", 1, new int[]{115,115,115,255}, new int[]{205,105,0,255}, new int[]{215,58,0,185}, null);
		addShader("Locus", 2, new int[]{10,10,10,255}, new int[]{74,74,74,255}, new int[]{132,150,76,255}, null);
		addShader("Felix", 1, new int[]{10,10,10,255}, new int[]{74,74,74,255}, new int[]{240,136,3,255}, null);
		addShader("Dragon's Breath", 1, new int[]{25,25,25,255}, new int[]{51,63,43,255}, new int[]{138,138,138,255}, "immersiveengineering:shaders/revolver_shark");
	}

	public void addShader(String name, int overlayType, int[] colour0, int[] colour1, int[] colour2, String additionalTexture)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("shader_name", name);
		tag.setInteger("shader_overlay", overlayType);
		tag.setIntArray("shader_colour0", colour0);
		tag.setIntArray("shader_colour1", colour1);
		tag.setIntArray("shader_colour2", colour2);
		if(additionalTexture!=null && !additionalTexture.isEmpty())
			tag.setString("shader_extraTexture", additionalTexture);
		IEApi.shaderList.add(tag);
	}

	@Override
	public boolean canEquipOnItem(ItemStack shader, ItemStack item)
	{
		return true;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		//		String[] sub = getSubNames();
		//		if(stack.getItemDamage()<sub.length)
		//		{
		//		}
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
			list.add(ItemNBTHelper.getString(stack, "shader_name"));
	}
	@Override
	public WeightedRandomChestContent getChestGenBase(ChestGenHooks chest, Random random, WeightedRandomChestContent original)
	{
		int i = random.nextInt(IEApi.shaderList.size());
		original.theItemId.setTagCompound(IEApi.shaderList.get(i));
		return original;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(NBTTagCompound tag : IEApi.shaderList)
		{
			ItemStack s = new ItemStack(item);
			s.setTagCompound(tag);
			list.add(s);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.icons = new IIcon[3];
		for(int i=0; i<3; i++)
			this.icons[i] = ir.registerIcon("immersiveengineering:shader_"+i);
	}
	@Override
	public IIcon getIconFromDamageForRenderPass(int meta, int pass)
	{
		return icons[pass];
	}
	@Override
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}
	@Override
	public int getRenderPasses(int metadata)
	{
		return 3;
	}
	@Override
	public int getColorFromItemStack(ItemStack stack, int pass)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_colour"+pass))
		{
			int[] col = ItemNBTHelper.getIntArray(stack, "shader_colour"+pass);
			if(col!=null&&col.length>3)
				return (col[3]<<24)+(col[0]<<16)+(col[1]<<8)+col[2];
		}
		return super.getColorFromItemStack(stack, pass);
	}


	@Override
	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
	{
		if(item.getItem() instanceof ItemRevolver)
		{
			int i = ItemNBTHelper.hasKey(shader, "shader_extraTexture")?1:0;
			if(modelPart.equals("cosmetic_compensator") || modelPart.equals("player_bayonet")||modelPart.equals("dev_bayonet"))
				return 1+i;
			if(modelPart.equals("bayonet_attachment") || modelPart.equals("player_mag")||modelPart.equals("dev_mag"))
				return 2+i;
			return 3+i;
		}
		return 3;
	}

	@Override
	public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if(item.getItem() instanceof ItemRevolver)
		{
			int maxPass = getPasses(shader, item, modelPart);
			if(pass==maxPass-1 && ItemNBTHelper.hasKey(shader, "shader_extraTexture"))
				return i_revolverAdditional.get(ItemNBTHelper.getString(shader, "shader_extraTexture"));

			int iOverlay = ItemNBTHelper.getInt(shader, "shader_overlay");
			switch(modelPart)
			{
			case "revolver_frame":
				return pass==0?i_revolverGrip: pass==1?i_revolverBase: i_revolverOverlay[iOverlay];
			case "barrel":
			case "dev_scope":
			case "player_mag":
			case "dev_mag":
			case "player_electro_0":
			case "player_electro_1":
				return pass==0?i_revolverBase: pass==1?i_revolverOverlay[iOverlay]: i_revolverUncoloured;
			case "cosmetic_compensator":
				return i_revolverOverlay[iOverlay];
			case "bayonet_attachment":
				if(pass==0)
					return i_revolverGrip;
				return i_revolverOverlay[iOverlay];
			case "player_bayonet":
			case "dev_bayonet":
				return i_revolverBase;
			}
		}
		return i_revolverBase;
	}

	@Override
	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if(item.getItem() instanceof ItemRevolver)
		{
			if(pass==2&&(modelPart.equals("barrel") || modelPart.equals("dev_scope")||modelPart.equals("player_electro_0")||modelPart.equals("player_electro_1")))
				return new int[]{255,255,255,255};

			int i=0; //0 == grip, 1==main, 2==detail
			switch(modelPart)
			{
			case "revolver_frame":
				i=pass;
				break;
			case "barrel":
			case "dev_scope":
			case "player_mag":
			case "dev_mag":
			case "player_electro_0":
			case "player_electro_1":
				i=pass+1;
				break;
			case "cosmetic_compensator":
				i=2;
				break;
			case "bayonet_attachment":
				if(pass==1)
					i=2;
				break;
			case "player_bayonet":
			case "dev_bayonet":
				i=1;
				break;
			}
			if(i==0)
				return ItemNBTHelper.getIntArray(shader, "shader_colour0");
			if(i==1)
				return ItemNBTHelper.getIntArray(shader, "shader_colour1");
			if(i==2)
				return ItemNBTHelper.getIntArray(shader, "shader_colour2");
		}
		return new int[]{255,255,255,255};
	}

	public IIcon i_revolverBase;
	public IIcon[] i_revolverOverlay = new IIcon[3];
	public IIcon i_revolverGrip;
	public IIcon i_revolverUncoloured;
	public HashMap<String,IIcon> i_revolverAdditional = new HashMap<String,IIcon>();
	public void stichTextures(IIconRegister ir, int sheetID)
	{
		if(sheetID==IEApi.revolverTextureSheetID)
		{
			i_revolverBase = ir.registerIcon("immersiveengineering:shaders/revolver_0");
			for(int i=0; i<i_revolverOverlay.length; i++)
				i_revolverOverlay[i] = ir.registerIcon("immersiveengineering:shaders/revolver_1_"+i);
			i_revolverGrip = ir.registerIcon("immersiveengineering:shaders/revolver_grip");
			i_revolverUncoloured = ir.registerIcon("immersiveengineering:shaders/revolver_noColour");
			for(NBTTagCompound tag : IEApi.shaderList)
				if(tag.hasKey("shader_extraTexture"))
				{
					String s = tag.getString("shader_extraTexture");
					String sub = s.substring(s.lastIndexOf("/")+1);
					if(sub.startsWith("revolver"))
						i_revolverAdditional.put(s, ir.registerIcon(s));
				}
		}
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre)
	{
		if(item.getItem() instanceof ItemRevolver && modelPart.equals("cosmetic_compensator"))
		{
			if(pre)
				GL11.glDisable(GL11.GL_CULL_FACE);
			else
				GL11.glEnable(GL11.GL_CULL_FACE);
		}
	}
}
