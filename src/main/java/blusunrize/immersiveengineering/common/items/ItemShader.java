package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.*;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.ITextureOverride;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ItemShader extends ItemIEBase implements IShaderItem, ITextureOverride
{
	public ItemShader()
	{
		super("shader", 1);

		addShader("Rosequartz", 0, EnumRarity.COMMON, 0xff412323, 0xffe6b4b4, 0xfff0cdcd,0xffe6b4b4).setInfo(null,null,"rosequartz");
		addShader("Argo", 2, EnumRarity.COMMON, 0xff2d2d2d, 0xffdcdcdc, 0xffdc7823,0xffc8c8c8).setInfo(null,null,"argo");
		addShader("Sunstrike", 5, EnumRarity.RARE, 0xff737373, 0xffcd6900, 0xb9d73a00,0xb9d73a00).setInfo(null,null,"sunstrike");
		addShader("Locus", 2, EnumRarity.COMMON, 0xff0a0a0a, 0xff4a4a4a, 0xff84964c,0xff4a4a4a).setInfo("Mercenaries","Red vs Blue","locus");
		addShader("Felix", 1, EnumRarity.COMMON, 0xff0a0a0a, 0xff4a4a4a, 0xfff08803,0xff4a4a4a).setInfo("Mercenaries","Red vs Blue","felix");
		addShader("Sharkface", 2, EnumRarity.UNCOMMON, 0xff0a0a0a, 0xff4a4a4a, 0xff910008,0xff4a4a4a, "shark",true,0xffffffff).setInfo("Mercenaries","Red vs Blue","sharkface");
		addShader("Dragon's Breath", 1, EnumRarity.UNCOMMON, 0xff191919, 0xff333f2b, 0xff8a8a8a,0xff8a8a8a, "shark",true,0xffffffff).setInfo(null,"Destiny","sharkface");
		addShader("Hawk", 3, EnumRarity.COMMON, 0xff67636b, 0xfff4eeeb, 0xff2d2d2d,0xfff4eeeb).setInfo(null,"Destiny","hawk");
		addShader("Eyas", 3, EnumRarity.COMMON, 0xff52534e, 0xff7a2a08, 0xff2d2d2d,0xfff4eeeb).setInfo(null,"Destiny","eyas");
		addShader("Magnum", 1, EnumRarity.COMMON, 0xff56382c,0xffdcdcdc,0xffa0a0a0,0xffdcdcdc).setInfo(null,null,"magnum");
		addShader("Fox", 2, EnumRarity.UNCOMMON, 0xff2d2d2d, 0xffd47e31, 0xffeaeaea,0xffeaeaea).setInfo(null,null,"fox");
		addShader("Vault-Tec", 0, EnumRarity.COMMON, 0xff56382c,0xff1a4785,0xffc0aa50,0xffaaaaaa).setInfo(null,"Fallout","vaulttec");

		ShaderRegistryEntry entry = addShader("Sponsor", 0, EnumRarity.EPIC, 0xff191919, 0xfff71b24, 0xffffffff,0xffaaaaaa, "sponsor",false,0xffffffff).setInfo(null,"Fallout","sponsor");
		((ShaderCaseMinecart)entry.getCase("immersiveengineering:minecart")).mirrorSideForPass[2]=false;
		entry.getCase("immersiveengineering:revolver").getLayers()[4].setTextureBounds(0,0,.25,.25);
		entry.getCase("immersiveengineering:drill").getLayers()[3].setTextureBounds(10/64d,34/64d, 26/64d,50/64d);
		entry.getCase("immersiveengineering:chemthrower").getLayers()[3].setTextureBounds(6/64d,16/64d, 22/64d,32/64d);

		addShader("Mass Fusion", 3, EnumRarity.RARE, 0xff6e5a37, 0xff394730, 0xff545454, 0xffaaaaaa, "fusion",true,0xffffffff).setInfo(null,"Fallout","massfusion");

		addShader("StormFlower", 1, EnumRarity.COMMON, 0xff273427,0xff286f30,0xff4b9255,0xff286f30).setInfo(null,"RWBY","stormflower");
		addShader("Miló", 2, EnumRarity.UNCOMMON, 0xff3b1b10,0xff670004,0xffce7e10,0xff670004).setInfo(null,"RWBY","milo");
		addShader("Trident", 2, EnumRarity.UNCOMMON, 0xff515151,0xffa8a8a8,0xff29d3ff,0xffafafaf).setInfo(null,"RWBY","trident");
		addShader("Chloris", 4, EnumRarity.RARE, 0xff38322a,0xff38322a,0xff88fabe,0xffc8c8c8).setInfo(null,"RWBY","chloris");
		addShader("Crescent Rose", 2, EnumRarity.COMMON, 0xff141414,0xff910008,0xff080808,0xffa4a4a4).setInfo(null,"RWBY","crescentrose");
		addShader("Qrow", 2, EnumRarity.UNCOMMON, 0xff6d1c11,0xffd8d7d0,0xff313640,0xff730008).setInfo(null,"RWBY","qrow");

		addShader("Vanguard", 3, EnumRarity.UNCOMMON, 0xff373737,0xff131b42,0xffb86c14,0xffdcdcdc).setInfo(null,"Destiny","vanguard");
		addShader("Regal", 4, EnumRarity.UNCOMMON, 0xffd8d4d1,0xff431c1d,0xffd8d4d1,0xffd8d4d1).setInfo(null,"Destiny","regal");
		addShader("Harrowed", 4, EnumRarity.RARE, 0xff161321,0xff431c1d,0xff161321,0xff161321).setInfo(null,"Destiny","harrowed");
		addShader("Taken", 5, EnumRarity.EPIC, 0xff111c26,0xff111c26,0xffbad7dd,0xff111c26, null,false,0xffffffff).setInfo(null,"Destiny","taken");

		addShader("Angel's Thesis", 2, EnumRarity.EPIC, 0xff1e1e1e,0xff754697,0xff77b93d,0xff505050, null,false,0xffffffff).setInfo(null,"Neon Genesis Evangelion","angelsthesis");

		addShader("Warbird", 7, EnumRarity.UNCOMMON, 0xff313640,0xffd8d7d0,0xffebac00,0xffd8d7d0).setInfo(null,null,"warbird");
		addShader("Matrix", 7, EnumRarity.RARE, 0xff053f3c,0xffe1e1ff,0xffd4ffff,0xffffffff, "pipes",true,0xff84ddd8).setInfo(null,null,"matrix");
		addShader("Twili", 5, EnumRarity.EPIC, 0xff555d70,0xff1a1e2b,0xff222739,0xff1db58e, "circuit",false,0xff1db58e).setInfo(null,"The Legend of Zelda: Twilight Princess","twili");
		addShader("Usurper", 3, EnumRarity.EPIC, 0xff3e1e1e,0xff5c6156,0xff111010,0xff737a6c, "circuit",false,0xffca2f38).setInfo(null,"The Legend of Zelda: Twilight Princess","usurper");

		addShader("Glacis", 6, EnumRarity.RARE, 0xff499bc2, 0x3376d0f9, 0x33bdfffd, 0x33bdfffd).setInfo(null,null,"glacis");
		addShader("Phoenix", 5, EnumRarity.RARE, 0xff750000, 0xffd00000, 0xffff7f00, 0xffff7f00).setInfo(null,null,"phoenix");

		addShader("Radiant", 3, EnumRarity.UNCOMMON, 0xffa36eab,0xfff8dbbc,0xffc79ab1,0xffd8d8e3, "pipes",true,0xfff1c91e).setInfo(null,"Kingdom Hearts","radiant");
		addShader("Hollow", 4, EnumRarity.UNCOMMON, 0xff542d1c,0xffeec5e5,0xffcc8980,0xffc4a1aa, "pipes",true,0xffc49838).setInfo(null,"Kingdom Hearts","hollow");
	}

	@Override
	public ShaderCase getShaderCase(ItemStack shader, ItemStack item, String shaderType)
	{
		String name = getShaderName(shader);
		return ShaderRegistry.getShader(name, shaderType);
	}

	public ShaderRegistryEntry addShader(String name, int overlayType, EnumRarity rarity, int colourBackground, int colourPrimary, int colourSecondary, int colourBlade)
	{
		return this.addShader(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, colourBlade, null,true,0xffffffff);
	}

	public ShaderRegistryEntry addShader(String name, int overlayType, EnumRarity rarity, int colourBackground, int colourPrimary, int colourSecondary, int colourBlade, String additionalTexture, boolean loot, int colourOverlay)
	{
		return ShaderRegistry.registerShader(name, Integer.toString(overlayType), rarity, colourPrimary, colourSecondary, colourBackground, colourBlade, additionalTexture, colourOverlay, loot,true);
		//		ShaderCaseRevolver revolver = IEApi.registerShader_Revolver(name, overlayType, colour0, colour1, colour2, colour3, additionalTexture);
		//		revolver.glowLayer = revolver_glow;
		//		IEApi.registerShader_Chemthrower(name, overlayType, colour0, colour1, colour2, true,false, additionalTexture);
		//		IEApi.registerShader_Minecart(name, overlayType, colour1, colour2, additionalTexture);
		//		IEApi.registerShader_Balloon(name, overlayType, colour1, colour2, additionalTexture);
	}

	@Override
	public String getShaderName(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
		{
			String name = ItemNBTHelper.getString(stack, "shader_name");
			if(ShaderRegistry.shaderRegistry.containsKey(ItemNBTHelper.getString(stack, "shader_name")))
				return name;
			else
			{
				Set<String> keys = ShaderRegistry.shaderRegistry.keySet();
				ArrayList<String> corrected = ManualUtils.getPrimitiveSpellingCorrections(name, keys.toArray(new String[keys.size()]), 4);
				if(!corrected.isEmpty())
				{
					IELogger.info("SHADER UPDATE: Fixing "+name+"to "+corrected.get(0));
					IELogger.info("Others: "+corrected);
					ItemNBTHelper.setString(stack, "shader_name", corrected.get(0));
					return corrected.get(0);
				}
			}
		}
		return "";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		list.add(I18n.format("Level: "+this.getRarity(stack).rarityColor+this.getRarity(stack).rarityName));
		if(!GuiScreen.isShiftKeyDown())
			list.add(I18n.format(Lib.DESC_INFO+"shader.applyTo")+" "+ I18n.format(Lib.DESC_INFO+"holdShift"));
		else
		{
			list.add(I18n.format(Lib.DESC_INFO+"shader.applyTo"));
			String name = getShaderName(stack);
			if(name!=null && !name.isEmpty())
			{
				List<ShaderCase> array = ShaderRegistry.shaderRegistry.get(name).getCases();
				for(ShaderCase sCase : array)
					if(!(sCase instanceof ShaderCaseItem))
						list.add(TextFormatting.DARK_GRAY+" "+ I18n.format(Lib.DESC_INFO+"shader."+sCase.getShaderType()));
			}
		}
	}
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String s = getShaderName(stack);
		return super.getItemStackDisplayName(stack)+(s!=null&&!s.isEmpty()?(": "+s):"");
	}
	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		String s = getShaderName(stack);
		return ShaderRegistry.shaderRegistry.containsKey(s)?ShaderRegistry.shaderRegistry.get(s).getRarity():EnumRarity.COMMON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(String key : ShaderRegistry.shaderRegistry.keySet())
		{
			ItemStack s = new ItemStack(item);
			ItemNBTHelper.setString(s, "shader_name", key);
			list.add(s);
		}
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}
	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		String name = getShaderName(stack);
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase("immersiveengineering:item");
			if(sCase!=null)
			{
				ShaderLayer[] layers = sCase.getLayers();
				if(pass<layers.length && layers[pass]!=null)
					return layers[pass].getColour();
				return 0xffffffff;
			}
		}
		return super.getColourForIEItem(stack, pass);
	}

	@Override
	public String getModelCacheKey(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
			return ItemNBTHelper.getString(stack, "shader_name");
		return null;
	}
	@Override
	public List<ResourceLocation> getTextures(ItemStack stack, String key)
	{
		String name = getShaderName(stack);
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase("immersiveengineering:item");
			if(sCase!=null)
			{
				ShaderLayer[] layers = sCase.getLayers();
				ArrayList<ResourceLocation> list = new ArrayList<>(layers.length);
				for(ShaderLayer layer : layers)
					list.add(layer.getTexture());
				return list;
			}
		}
		return Arrays.asList(new ResourceLocation("immersiveengieering:items/shader_0"),new ResourceLocation("immersiveengieering:items/shader_1"),new ResourceLocation("immersiveengieering:items/shader_2"));
	}
}